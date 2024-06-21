package com.hexadevlabs.simplefsm;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class TraceTest {
    private Trace trace1;
    private Trace trace2;

    @BeforeEach
    void setUp() {
        trace1 = new Trace();
        trace2 = new Trace();
    }

    @Test
    void testMergeWithNonDuplicateLogs() {
        trace1.add("First message");
        trace2.add("Second message");

        trace1.merge(trace2);
        assertEquals(2, trace1.size(), "Trace should have exactly two log entries.");
        assertEquals("First message", trace1.logs.get(0).getMessage(), "First message should be 'First message'.");
        assertEquals("Second message", trace1.logs.get(1).getMessage(), "Second message should be 'Second message'.");
    }

    @Test
    void testMergeWithDuplicateLogs() {
        trace1.add("Duplicate message");
        trace2.add("Duplicate message");

        trace1.merge(trace2);
        assertEquals(1, trace1.size(), "Trace should have exactly one log entry due to duplicate prevention.");
    }

    @Test
    void testMergeWithEmptyTrace() {
        trace1.add("Only message");

        Trace emptyTrace = new Trace();
        trace1.merge(emptyTrace);

        assertEquals(1, trace1.size(), "Trace should still contain one log entry.");
    }

    @Test
    void testMergeIntoEmptyTrace() {
        trace2.add("Only message");
        Trace emptyTrace = new Trace();

        emptyTrace.merge(trace2);
        assertEquals(1, emptyTrace.size(), "Merged trace should now contain one log entry.");
        assertEquals("Only message", emptyTrace.logs.get(0).getMessage(), "Log entry should match the merged message.");
    }

    @Test
    void testMergeNull() {
        trace1.add("Existing message");

        Trace nullTrace = null;
        assertThrows(NullPointerException.class, () -> trace1.merge(nullTrace), "Merging with null should throw NullPointerException.");
    }

    @Test
    void testMergePreservesOrder() {
        trace1.add("First");
        trace1.add("Second");
        trace2.add("Third");
        trace2.add("Fourth");

        trace1.merge(trace2);
        assertEquals(4, trace1.size(), "Trace should have four log entries.");
        assertEquals("First", trace1.logs.get(0).getMessage());
        assertEquals("Second", trace1.logs.get(1).getMessage());
        assertEquals("Third", trace1.logs.get(2).getMessage());
        assertEquals("Fourth", trace1.logs.get(3).getMessage());
    }
}

