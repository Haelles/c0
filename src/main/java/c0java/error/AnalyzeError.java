package c0java.error;

import c0java.util.Pos;

public class AnalyzeError extends CompileError {
    private static final long serialVersionUID = 1L;

    ErrorCode code;
    Pos pos;
    String message;

    @Override
    public ErrorCode getErr() {
        return code;
    }

    @Override
    public Pos getPos() {
        return pos;
    }

    /**
     *
     * @param code
     * @param pos
     */
    public AnalyzeError(ErrorCode code, Pos pos, String message) {
        this.code = code;
        this.pos = pos;
        this.message = message;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Analyze Error: ").append(code).append(", message:").append(message).append("," +
                " at: ").append(pos).toString();
    }
}
