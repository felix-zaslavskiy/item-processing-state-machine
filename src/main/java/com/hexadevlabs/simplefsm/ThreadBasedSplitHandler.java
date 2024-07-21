package com.hexadevlabs.simplefsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ThreadBasedSplitHandler implements SplitHandler {

    // this holds the shared intermediate state that is accessed by
    // different threads. Access to these must be synchronized.
    String state;
    String otherData;


    /**
     * Will create workers to perform the work on each of the split states.
     *
     * @param simpleFSM The state machine object which needs to handle splits.
     * @param data The processing data before the split states started.
     * @param splitTransitions List of all the transitions that need to be handled.
     */
    @Override
    public void handleSplit(SimpleFSM simpleFSM, ProcessingData data, Collection<String> splitTransitions){
        ExecutorService executor = Executors.newFixedThreadPool(2); // adjust the thread pool size as needed
        String state = simpleFSM.exportState();
        this.state = state;
        this.otherData = new ProcessingData().toJson();

        List<Future<?>> futures = new ArrayList<>();

        // Set initial state

        // Trigger processing of all the split states.
        for (String splitState : splitTransitions) {
            Future<ProcessingData> future = executor.submit(() -> {
                try {
                    // Load from DB.
                    // Make a copy so the nextState var is not shared
                    ProcessingData d = new ProcessingData();
                    d.mergeFrom(data);

                    SimpleFSM sm = simpleFSM.buildEmptyCopy();
                    sm.importState(state);
                    sm.continueOnSplitState(splitState, d);

                    // Only if state machine is finished do we want to save it to DB
                    // Will be read by Test case to verify.
                    if (sm.isConcluded() || sm.isPaused()) {
                        System.out.println("Concluded");

                        // Copy the completed state back into the
                        // original state machine
                        simpleFSM.importState(sm.exportState());

                        return d;
                    } else {
                        return null;
                    }
                }catch(Exception e) {

                    System.err.println("Unexpected exception caught " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }

            });
            futures.add(future);
        }

        executor.shutdown(); // Stop accepting new tasks

        // Wait for all tasks to complete with a timeout
        ProcessingData resultData = null;
        for (Future<?> future : futures) {
            try {
                ProcessingData processingData = (ProcessingData) future.get(5, TimeUnit.MINUTES);  // Adjust timeout as needed
                if(processingData!=null){
                    resultData = processingData;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Handle thread interruption
            } catch (ExecutionException e) {
                System.err.println("Task encountered an exception: " + e.getCause());
            } catch (TimeoutException e) {
                System.err.println("Task timed out and was not completed.");
            }
        }

        for (Future<?> future : futures) {
            if (!future.isDone()) {
                System.out.println("This task is still running or waiting to run." + future);
            }
        }

        // Optionally, enforce shutdown now if not all tasks completed
        if (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            }catch(InterruptedException ignored){
            }
            if(!executor.isTerminated()) {
                System.err.println("Not all tasks finished, forcing shutdown...");
                List<Runnable> remainingTasks = executor.shutdownNow(); // Attempt to stop all actively executing tasks
                System.out.println("Remaining tasks stopped: " + remainingTasks.size());
            }
        }

        // Merge the data on the final data
        assert resultData != null;
        data.mergeFrom(resultData);

    }

    /**
     *
     * @param simpleFSM The state machine object that needs a split state recorded.
     * @param currentData The processing data as it is after the current split state just finished processing.
     * @param splitSourceState Which state the split is coming from. This can be used to locate related split states.
     * @param completedSplitState  The state that has just completed executing and needs its state recorded.
     * @return
     */
    @Override
    public boolean getAndUpdateStateAndData(SimpleFSM simpleFSM, ProcessingData currentData, String splitSourceState, String completedSplitState) {

        synchronized (this) {

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

            // Replace the shared state.
            state = newState;
            otherData =  currentData.toJson();

            return completedOtherWork;

        }

    }
}
