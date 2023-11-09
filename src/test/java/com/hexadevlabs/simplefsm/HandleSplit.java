package com.hexadevlabs.simplefsm;

import java.util.Collection;

public class HandleSplit implements SplitHandler{
    @Override
    public void handleSplit(SimpleFSM simpleFSM, ProcessingData data, Collection<String> splitTransitions) {
        // Normally persist State machine and data.

        // Trigger processing of all the split states.
        for(String splitState: splitTransitions){
            simpleFSM.continueOnSplitState(splitState, data);
        }

    }

    @Override
    public void mergeDataAndSave(ProcessingData fromCurrentStep, ProcessingData fromSharedData) {

    }

    @Override
    public GetStateResult getStateAndUpdateWorkState(SimpleFSM simpleFSM) {
        return null;
    }


}
