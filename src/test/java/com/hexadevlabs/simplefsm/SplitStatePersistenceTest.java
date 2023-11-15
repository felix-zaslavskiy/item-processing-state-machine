package com.hexadevlabs.simplefsm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This will test persisting the State to DB while executing in parallel.
 */
public class SplitStatePersistenceTest {


    private SimpleFSM simpleFSM;


    @BeforeEach
    public void setUp() throws  SQLException {

        Connection conn = makeNewConnection();
        try (Statement st = conn.createStatement()) {

            st.execute("CREATE TABLE store (state TEXT, data TEXT)");
        }
        conn.close();
        simpleFSM = buildNew(this::makeNewConnection);
    }



    private Connection makeNewConnection()  {
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", "sa", "");
//             Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "password");
            return conn;
        }catch(Exception e){
            System.err.println("Could not open connection to H2");
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        Connection conn = makeNewConnection();
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE store");
        }
        conn.close();
    }

    protected static SimpleFSM buildNew(Supplier<Connection> connectionSupplier){
        HandleSplitPersisting handleSplitPersisting;
        if(connectionSupplier==null){
            handleSplitPersisting = new HandleSplitPersisting(null, false);
        } else {
            handleSplitPersisting = new HandleSplitPersisting(connectionSupplier, true);
        }

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
                .splitHander(handleSplitPersisting)
            .withTrace()
            .build();
    }

    @Test
    public void runSimpleSplittingStateMachine() throws InterruptedException {
        ProcessingData data = new ProcessingData();
        simpleFSM.start("START", data);
        Thread.sleep(1000);

        assertTrue(simpleFSM.isFinished());
        assertFalse(simpleFSM.wasTerminated());
        assertNotNull(simpleFSM.getFinalState());
        assertEquals("END", simpleFSM.getFinalState().getName());
        Integer result = (Integer) data.get("value_sum");
        assertEquals(5, result);

        simpleFSM.getTrace().print();
    }

}
