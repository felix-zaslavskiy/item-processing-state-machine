package com.hexadevlabs.simplefsm;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * The persistence will be stored to an H2 DB using transactions.
 * This is essentially the simplest possible working Split Handler we can make for testing.
 */
public class HandleSplitPersisting implements SplitHandler{

    final Supplier<Connection> connectionSupplier;

    boolean parallel;



    public HandleSplitPersisting(Supplier<Connection> connectionSupplier, boolean parallel) {
        this.connectionSupplier = connectionSupplier;
        this.parallel = parallel;
    }

    @Override
    public void handleSplit(SimpleFSM simpleFSM, ProcessingData data, Collection<String> splitTransitions) {
        String state = simpleFSM.exportState();
        try(Connection conn = connectionSupplier.get()) {
            // Normally persist State machine and data.
            try (Statement st = conn.createStatement()) {
                conn.setAutoCommit(true); // Start transaction on this connection.

                String jsonData = data.toJson();
                st.execute("INSERT INTO store (state, data ) values ('" + state + "', '" + jsonData + "')");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if(parallel) {
            ExecutorService executor = Executors.newFixedThreadPool(2); // adjust the thread pool size as needed

            // Trigger processing of all the split states.
            for (String splitState : splitTransitions) {
                executor.submit(() -> {
                    // Load from DB.
                    ProcessingData d = new ProcessingData();
                    d.mergeFrom(data);
                    SimpleFSM sm = simpleFSM.buildEmptyCopy();
                    sm.importState(state);
                    sm.continueOnSplitState(splitState, d);

                    // Only if state machine is finished do we want to save it to DB
                    // Will be read by Test case to verify.
                    if(sm.isFinished()) {
                        try (Connection conn = connectionSupplier.get()) {
                            // Normally persist State machine and data.
                            try (Statement st = conn.createStatement()) {
                                conn.setAutoCommit(true); // Start transaction on this connection.

                                String jsonData = d.toJson();
                                st.executeUpdate("UPDATE store SET data = '" + jsonData + "' , state = '" + sm.exportState() + "'");

                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            executor.shutdown();
        } else {
            for (String splitState : splitTransitions) {
                    simpleFSM.continueOnSplitState(splitState, data);
            }
        }
    }


    @Override
    public boolean getAndUpdateStateAndData(SimpleFSM simpleFSM, ProcessingData currentData, String splitSourceState, String completedSplitState) {

        try(Connection conn = connectionSupplier.get()) {
            conn.setAutoCommit(false); // Start transaction on this connection.

            try (Statement st = conn.createStatement()) {

                // Mysql Default is Repeatable Read isolation level. The "For Update" will lock the
                // select read here until another transaction has committed.
                // The lock is not indefinite and can result in ERROR 1205 (HY000): Lock wait timeout exceeded; try restarting transaction
                // Change isolation level: st.execute("SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE");

                // H2 default isolation level is Read committed but can be changed like this:
                // H2 st.execute("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL SERIALIZABLE");


                String state;
                String otherData;
                try (ResultSet rs = st.executeQuery("SELECT state, data FROM store FOR UPDATE WAIT 1000")) {
                    rs.next();
                    state = rs.getString("state");
                    otherData = rs.getString("data");
                }

                SimpleFSM sm = simpleFSM.buildEmptyCopy();
                sm.importState(state);

                // merge the traces.
                sm.mergeTraceFrom(simpleFSM);

                // Update the work state of State machine
                sm.recordCompletionSplitState(completedSplitState);
                int totalSplitStatesCompleted = sm.getCompletionSplitStates().size();

                State source = sm.getState(splitSourceState);
                int totalSplitTransitionsExpected = source.getSplitTransitions().size();

                boolean completedOtherWork = totalSplitStatesCompleted == totalSplitTransitionsExpected;

                // Write the updated State machine to Persistence
                String newState = sm.exportState();

                ProcessingData otherSavedProcessingData = ProcessingData.fromJson(otherData);
                // Merge the other Data to currentData.
                currentData.mergeFrom(otherSavedProcessingData);
                // Merge the state machine from other state, so it can continue with full state.
                simpleFSM.mergeTraceFrom(sm);

                st.executeUpdate("UPDATE store SET state = '" + newState + "', data ='" + currentData.toJson() + "'");

                conn.commit();

                return completedOtherWork;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }




}
