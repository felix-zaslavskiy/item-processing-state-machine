# Non-Deterministic Finate State Machine (NFSM)

NFSM is a lightweight and customizable finite state machine library for Java, designed to simplify complex workflows and manage states effectively. The primary goal of this library is to provide developers with a simple yet powerful tool to implement and manage state machines within their applications.

## Background
Finite state machines (FSMs) are a powerful technique to manage the flow of control in an application. They allow you to define a set of states and transitions between those states, ensuring that the application behaves in a predictable and maintainable manner. NFSM aims to provide an easy-to-use implementation of FSMs, which makes it ideal for managing complex workflows and handling state transitions in a more organized way.

## Features
- Simple and intuitive API for defining states, transitions, and events.
- Support for synchronous and asynchronous transitions.
- Built-in support for conditional transitions and auto-transitions.
- Configurable exception handling and error states.
- Trace mode for monitoring state transitions and debugging.
- Export and import of the state machine's state for persistence or communication between systems.
- Graphviz integration for visualizing the state machine.

## Getting Started
To use NFSM in your Java project, simply include it as a dependency and start defining states and transitions. Here's a basic example to get you started:
```java
import nfsm.*;

public class MyFSM {

    public static void main(String[] args) {
        // Create a new NFSM.Builder instance
        NFSM.Builder builder = new NFSM.Builder();

        // Define states and transitions
        builder
            .state("state1", new MyProcessingStep1())
                .on("event1").goTo("state2")
            .and()
            .state("state2", new MyProcessingStep2())
            .   on("event2").goTo("state3")
            .and()
            .finalState("state3", new MyProcessingStep3());

        // Build the NFSM instance
        NFSM fsm = builder.build();

        // Initialize the FSM with a starting state and data
        ProcessingData data = new ProcessingData();
        fsm.start("state1", data);

        // Trigger events to drive the FSM
        fsm.triggerEvent("event1", data);
        fsm.triggerEvent("event2", data);

        // Check if the FSM has reached a final state
        if (fsm.isFinished()) {
            System.out.println("FSM has reached a final state: " + fsm.getFinalState().getName());
        }
    }
}
```

In this example, we define a simple FSM with three states and two events. Each state has a processing step associated with it, which can be any class that extends [ProcessingStep](src/main/java/nfsm/ProcessingStep.java). The processing steps are responsible for performing the required actions in each state and setting the next state, if necessary.

The [NFSM.Builder](src/main/java/nfsm/NFSM.java) class provides a fluent API for defining states, transitions, and other FSM properties, making it easy to build complex state machines in a clean, readable way.

## Documentation
For more information on using NFSM, including detailed API documentation, examples, and best practices, please refer to the [official documentation](DOC.MD).

## Contributing
We welcome contributions to NFSM! If you'd like to contribute, please fork the repository, make your changes, and submit a pull request. For more information on contributing, please see our contributing guidelines.

## License
NFSM is released under the **Apache Version 2.0**