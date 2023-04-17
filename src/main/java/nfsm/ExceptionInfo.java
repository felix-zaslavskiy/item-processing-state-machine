package nfsm;

public class ExceptionInfo {
    private boolean hadException = false;
    Exception e;
    public ExceptionInfo(Exception e) {
        hadException=true;
        this.e = e;
    }

    public ExceptionInfo() {
    }

    boolean hadException() { return hadException; }
}
