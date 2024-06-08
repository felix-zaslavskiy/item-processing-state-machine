package com.hexadevlabs.simplefsm;

import java.util.Collection;

public interface SplitHandler {

    /*
    class GetStateResult {
        public boolean completedOtherWork;
        public ProcessingData otherSavedProcessingData;
    }
     */

    void handleSplit(SimpleFSM simpleFSM, ProcessingData data, Collection<String> splitTransitions);

    /**
     * Should be transactional. As a transaction it should read the
     * work state of Machine from persistence and update it with
     * state of current work completion.
     *
     * @return If the complete work of split is complete.
     */
    boolean getAndUpdateStateAndData(SimpleFSM simpleFSM, ProcessingData currentData, String splitSourceState, String completedSplitState);


}
