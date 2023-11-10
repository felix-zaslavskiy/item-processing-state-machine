package com.hexadevlabs.simplefsm;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * The persistence will be stored to an H2 DB using transactions.
 * This is essentially the simplest possible working Split Handler we can make for testing.
 */
public class HandleSplitPersisting implements SplitHandler{

    Connection conn;

    static ObjectMapper mapper = new ObjectMapper().setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    public HandleSplitPersisting(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void handleSplit(SimpleFSM simpleFSM, ProcessingData data, Collection<String> splitTransitions) {
        // Normally persist State machine and data.
        try (Statement st = conn.createStatement()) {
            conn.setAutoCommit(true); // Start transaction on this connection.
            String state = simpleFSM.exportState();
            String jsonData = getJsonFromProcessingData(data);
            st.execute("INSERT INTO store (state, data ) values ('" + state + "', '" + jsonData + "')");
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Trigger processing of all the split states.
        for(String splitState: splitTransitions){
            simpleFSM.continueOnSplitState(splitState, data);
        }

    }

    @Override
    public ProcessingData mergeDataAndSave( SimpleFSM simpleFSM, ProcessingData fromCurrentStep, ProcessingData fromSharedData) {
        // Don't do anything for now.
        return fromCurrentStep;
    }

    @Override
    public GetStateResult getStateAndUpdateWorkState(SimpleFSM simpleFSM, String splitSourceState, String completedSplitState) {

        try (Statement st = conn.createStatement()) {

            conn.setAutoCommit(false); // Start transaction on this connection.

            String state;
            String data;
            try(ResultSet rs = st.executeQuery("SELECT state, data FROM store")) {
                rs.next();
                state = rs.getString("state");
                data = rs.getString("data");
            }

            SimpleFSM sm = SplitStatePersistenceTest.buildNew(conn);
            sm.importState(state);

            // Update the work state of State machine
            sm.recordCompletionSplitState(completedSplitState);
            int totalSplitStatesCompleted = sm.getCompletionSplitStates().size();

            // Get the expected # of
            State source = sm.getState(splitSourceState);
            int totalSplitTransitionsExpected = source.getSplitTransitions().size();

            GetStateResult result = new GetStateResult();
            result.completedOtherWork = totalSplitStatesCompleted == totalSplitTransitionsExpected;


            // Write the updated State machine to Persistence
            String newState = sm.exportState();
            st.executeUpdate("UPDATE store SET state = '" + newState + "'");

            conn.commit();

            result.otherSavedProcessingData = getProcessingDataFromJson(data);
            return result;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private ProcessingData getProcessingDataFromJson(String data) {
        try {
            return mapper.readValue(data, ProcessingData.class);
        } catch (JsonProcessingException e) {
            System.out.println("Got json exception" + e.getMessage());
            return new ProcessingData();
        }
    }

    private  String getJsonFromProcessingData(ProcessingData data)  {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            System.out.println("Got json exception" + e.getMessage());
            return "";
        }
    }


}
