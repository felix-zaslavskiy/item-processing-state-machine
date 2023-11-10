package com.hexadevlabs.simplefsm;

import java.util.Collection;

/**
 * Used as a placeholder.
 * The persistence will be stored to a statis member variable.
 * This is essentially the simplest possible working Split Handler we can make for testing.
 */
public class HandleSplitPlaceholder implements SplitHandler{

    static  ProcessingData data;

    @Override
    public void handleSplit(SimpleFSM simpleFSM, ProcessingData data, Collection<String> splitTransitions) {
        // Normally persist State machine and data.

        HandleSplitPlaceholder.data = data;
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

        simpleFSM.recordCompletionSplitState(completedSplitState);
        int totalSplitStatesCompleted = simpleFSM.getCompletionSplitStates().size();

        // Get the expected # of
        State source = simpleFSM.getState(splitSourceState);
        int totalSplitTransitionsExpected = source.getSplitTransitions().size();

        GetStateResult result = new GetStateResult();
        result.completedOtherWork = totalSplitStatesCompleted == totalSplitTransitionsExpected;
        result.otherSavedProcessingData = HandleSplitPlaceholder.data;
        return result;

    }


}
