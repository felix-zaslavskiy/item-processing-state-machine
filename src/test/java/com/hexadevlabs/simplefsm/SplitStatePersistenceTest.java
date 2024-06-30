package com.hexadevlabs.simplefsm;

import com.hexadevlabs.simplefsm.supporting.HandleSplitPersisting;
import com.hexadevlabs.simplefsm.testSteps.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This will test persisting the State to DB while executing in parallel.
 */
public class SplitStatePersistenceTest {


    private SimpleFSM simpleFSM;


    @BeforeEach
    public void setUp() throws  SQLException {

        Connection conn = makeNewConnection();
        try (Statement st = conn.createStatement()) {

            st.execute("CREATE TABLE store (state TEXT, data TEXT)");
        }
        conn.close();
        simpleFSM = buildNew(this::makeNewConnection);
    }



    private Connection makeNewConnection()  {
        try {
            Class.forName("org.h2.Driver");
            //  Class.forName("com.mysql.cj.jdbc.Driver");
            //  Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "password");
            return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", "sa", "");
        }catch(Exception e){
            System.err.println("Could not open connection to H2");
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        Connection conn = makeNewConnection();
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE store");
        }
        conn.close();
    }

    protected static SimpleFSM buildNew(Supplier<Connection> connectionSupplier){
        HandleSplitPersisting handleSplitPersisting;
        if(connectionSupplier==null){
            handleSplitPersisting = new HandleSplitPersisting(null, false);
        } else {
            handleSplitPersisting = new HandleSplitPersisting(connectionSupplier, true);
        }

        return new SimpleFSM.Builder()
            .state("START", new NoopStep())
                .auto().goTo("STEP_SPLIT")
            .and()
            .state( "STEP_SPLIT", new StepSplit() )
                .split().goTo("SPLIT1" )
                .split().goTo("SPLIT2" )
            .and()
            .state("SPLIT_END", new SplitEnd())
                .auto().goTo("END")
            .and()
            .finalState("END", new NoopStep())
            .state("SPLIT1", new Split1() )
                .join( "SPLIT_END" )
            .and()
            .state("SPLIT2", new Split2() )
                .join( "SPLIT_END" )
            .and()
            .onExceptionGoTo("END")
            .withName("Test FSM")
                .splitHandler(handleSplitPersisting)
            .withTrace()
            .build();
    }

    @Test
    public void runSimpleSplittingStateMachine() throws InterruptedException {
        ProcessingData data = new ProcessingData();
        simpleFSM.start("START", data);
        Thread.sleep(2000);

        String stateAfter;
        String dataAfter;
        try(Connection conn = makeNewConnection()) {

            conn.setAutoCommit(false); // Start transaction on this connection.

            try (Statement st = conn.createStatement()) {
                // Read from DB.

                try (ResultSet rs = st.executeQuery("SELECT state, data FROM store ")) {
                    rs.next();
                    stateAfter = rs.getString("state");
                    dataAfter = rs.getString("data");

                    SimpleFSM resultFSM = simpleFSM.buildEmptyCopy();
                    resultFSM.importState(stateAfter);

                    // Print Trace
                    resultFSM.getTrace().print();
                    System.out.println(resultFSM.exportState());
                    System.out.println(dataAfter);

                    assertTrue(resultFSM.isConcluded());
                    assertFalse(resultFSM.wasTerminated());
                    assertNotNull(resultFSM.getFinalState());
                    assertEquals("END", resultFSM.getFinalState().getName());
                    ProcessingData afterData = ProcessingData.fromJson(dataAfter);
                    Integer result = (Integer) afterData.get("value_sum");
                    assertEquals(5, result);


                }

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    @Test
    public void withException() throws InterruptedException {
        simpleFSM.getState("SPLIT1").setProcessingStep(new Split1WithException());

        ProcessingData data = new ProcessingData();
        simpleFSM.start("START", data);


        Thread.sleep(2000);

        String stateAfter;
        String dataAfter;
        try(Connection conn = makeNewConnection()) {

            conn.setAutoCommit(false); // Start transaction on this connection.

            try (Statement st = conn.createStatement()) {
                // Read from DB.

                try (ResultSet rs = st.executeQuery("SELECT state, data FROM store ")) {
                    rs.next();
                    stateAfter = rs.getString("state");
                    dataAfter = rs.getString("data");

                    SimpleFSM resultFSM = simpleFSM.buildEmptyCopy();
                    resultFSM.importState(stateAfter);

                    // Print Trace
                    resultFSM.getTrace().print();
                    System.out.println(resultFSM.exportState());
                    System.out.println(dataAfter);


                    assertTrue(resultFSM.isConcluded());
                    assertFalse(resultFSM.wasTerminated());
                    assertNotNull(resultFSM.getFinalState());
                    assertEquals("END", resultFSM.getFinalState().getName());
                    ProcessingData afterData = ProcessingData.fromJson(dataAfter);

                    assertTrue(afterData.hadException());

                    // Step 1 had exception so should go to End without executing Split_end state.
                    // This means the values of value1 and value2 could not be added.
                    assertNull(afterData.get("value_sum"));


                }

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void runSimpleSplittingStateMachineLongProcessing() throws InterruptedException {
        simpleFSM.getState("SPLIT1").setProcessingStep(new Step1WithLongPause());

        ProcessingData data = new ProcessingData();
        simpleFSM.start("START", data);
        Thread.sleep(3000);

        String stateAfter;
        String dataAfter;
        try(Connection conn = makeNewConnection()) {

            conn.setAutoCommit(false); // Start transaction on this connection.

            try (Statement st = conn.createStatement()) {
                // Read from DB.

                try (ResultSet rs = st.executeQuery("SELECT state, data FROM store ")) {
                    rs.next();
                    stateAfter = rs.getString("state");
                    dataAfter = rs.getString("data");

                    SimpleFSM resultFSM = simpleFSM.buildEmptyCopy();
                    resultFSM.importState(stateAfter);

                    // Print Trace
                    resultFSM.getTrace().print();
                    System.out.println(resultFSM.exportState());
                    System.out.println(dataAfter);

                    assertTrue(resultFSM.isConcluded());
                    assertFalse(resultFSM.wasTerminated());
                    assertNotNull(resultFSM.getFinalState());
                    assertEquals("END", resultFSM.getFinalState().getName());
                    ProcessingData afterData = ProcessingData.fromJson(dataAfter);
                    Integer result = (Integer) afterData.get("value_sum");
                    assertEquals(5, result);


                }

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


}
