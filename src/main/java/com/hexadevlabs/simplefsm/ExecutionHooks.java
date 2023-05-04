package com.hexadevlabs.simplefsm;

public interface ExecutionHooks {
    void before(State state, ProcessingData data) throws Exception;
    void after(State state, ProcessingData data) throws Exception;
}
