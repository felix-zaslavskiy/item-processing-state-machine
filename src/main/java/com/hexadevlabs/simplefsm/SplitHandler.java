package com.hexadevlabs.simplefsm;

import java.util.Collection;

public interface SplitHandler {

    /**
     * The split handler is responsible on how to split up the work across
     * the split states. The work on each "split" state is performed in parallel.
     * For example one implementation may decide to split the work across multiple
     * threads and in another work may be sent to different servers for processing.
     *
     * @param simpleFSM The state machine object which needs to handle splits.
     * @param data The processing data before the split states started.
     * @param splitTransitions List of all the transitions that need to be handled.
     */
    void handleSplit(SimpleFSM simpleFSM, ProcessingData data, Collection<String> splitTransitions);

    /**
     * Should be transactional. As a transaction it should read the
     * work state of Machine from persistence and update it with
     * state of current work completion.
     *
     * @param simpleFSM The state machine object that needs a split state recorded.
     * @param currentData The processing data as it is after the current split state just finished processing.
     * @param splitSourceState Which state the split is coming from. This can be used to locate related split states.
     * @param completedSplitState  The state that has just completed executing and needs its state recorded.
     * @return If the complete work of the split is complete.
     */
    boolean getAndUpdateStateAndData(SimpleFSM simpleFSM, ProcessingData currentData, String splitSourceState, String completedSplitState);


}
