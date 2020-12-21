package c0java.error;

import c0java.util.Pos;

public class DuplicateError extends CompileError{
    Pos pos;
    String message;

    public DuplicateError(Pos pos, String message) {
        this.pos = pos;
        this.message = message;
    }
    @Override
    public ErrorCode getErr() {
        return ErrorCode.DuplicateDeclaration;
    }

    @Override
    public Pos getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("DuplicateDeclare Error: ").append(ErrorCode.DuplicateDeclaration).append(", at: ").append(pos).toString();
    }
}
