package com.hexadevlabs.simplefsm;

import java.util.Collection;

public interface SplitHandler {

    public static class GetStateResult {
        public boolean completedOtherWork;
        public ProcessingData otherSavedProcessingData;
    }

    void handleSplit(SimpleFSM simpleFSM, ProcessingData data, Collection<String> splitTransitions);

    /**
     * Should be transactional. As a transaction it should read the
     * work state of Machine from persistence and update it with
     * state of current work completion.
     *
     * @return If the complete work of split is complete.
     * @param simpleFSM
     */
    boolean getAndUpdateStateAndData(SimpleFSM simpleFSM, ProcessingData otherData, String splitSourceState, String completedSplitState);


}
