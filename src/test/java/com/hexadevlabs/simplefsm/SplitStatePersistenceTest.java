package com.hexadevlabs.simplefsm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This will test persisting the State to DB while executing in parallel.
 */
public class SplitStatePersistenceTest {


    private SimpleFSM simpleFSM;
    private Connection conn;

    @BeforeEach
    public void setUp() throws ClassNotFoundException, SQLException {

        Class.forName("org.h2.Driver");
        conn = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", "sa", "");
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE store (state TEXT, data TEXT)");
        }
        simpleFSM = buildNew(conn);
    }

    @AfterEach
    public void tearDown() throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE store");
        }
        conn.close();
    }

    protected static SimpleFSM buildNew(Connection conn){
        return new SimpleFSM.Builder()
            .state("START", new NoopStep())
                .auto().goTo("STEP_SPLIT")
            .and()
            .state( "STEP_SPLIT", new StepSplit() )
                .split().goTo("SPLIT1" )
                .split().goTo("SPLIT2" )
            .and()
            .finalState("END", new SplitEnd())
            .state("SPLIT1", new Split1() )
                .join( "END" )
            .and()
            .state("SPLIT2", new Split2() )
                .join( "END" )
            .and()
            .onExceptionGoTo("END")
            .withName("Test FSM")
                .splitHander(new HandleSplitPersisting(conn))
            .withTrace()
            .build();
    }


    @Test
    public void runSimpleSplittingStateMachine(){
        ProcessingData data = new ProcessingData();
        simpleFSM.start("START", data);
        assertTrue(simpleFSM.isFinished());
        assertFalse(simpleFSM.wasTerminated());
        assertNotNull(simpleFSM.getFinalState());
        assertEquals("END", simpleFSM.getFinalState().getName());
        Integer result = (Integer) data.get("value_sum");
        assertEquals(5, result);
        simpleFSM.getTrace().print();
    }

}
