package simplefsm;

public class ExceptionInfo {
    private boolean hadException = false;
    public Exception exception;
    public ExceptionInfo(Exception exception) {
        this.hadException=true;
        this.exception = exception;
    }

    public ExceptionInfo() {
    }

    boolean hadException() { return hadException; }
}
