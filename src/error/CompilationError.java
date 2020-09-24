package error;

import javafx.util.Pair;

public final class CompilationError {
    private ErrorCode err;
    private Pair<Integer, Integer> pos;

    public CompilationError(ErrorCode err, Pair<Integer, Integer> pos) {
        this.err = err;
        this.pos = pos;
    }

    public CompilationError(ErrorCode err, Integer line, Integer column){
        this.err = err;
        this.pos = new Pair<>(line, column);
    }

    public ErrorCode getErr() {
        return err;
    }

    public void setErr(ErrorCode err) {
        this.err = err;
    }

    public Pair<Integer, Integer> getPos() {
        return pos;
    }

    public void setPos(Pair<Integer, Integer> pos) {
        this.pos = pos;
    }

    public static void DieAndPrint(String condition) {
        System.out.println("Exception: " + condition);
        System.out.println("The program should not reach here.");
        System.out.println("Please check your program carefully.");
        System.out.println("If you believe it's not your fault, please report this to TAs.");
        System.exit(0);
    }
}
