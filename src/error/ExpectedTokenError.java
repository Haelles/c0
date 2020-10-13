package error;

import java.util.ArrayList;
import java.util.List;

import tokenizer.Token;
import tokenizer.TokenType;
import util.Pos;

public class ExpectedTokenError extends CompileError {
    private static final long serialVersionUID = 1L;

    List<TokenType> expecTokenType;
    Token token;

    Pos pos;

    @Override
    public ErrorCode getErr() {
        return ErrorCode.ExpectedToken;
    }

    @Override
    public Pos getPos() {
        return pos;
    }

    /**
     * @param expectedTokenType
     * @param token
     * @param code
     * @param pos
     */
    public ExpectedTokenError(TokenType expectedTokenType, Token token, Pos pos) {
        this.expecTokenType = new ArrayList<>();
        this.expecTokenType.add(expectedTokenType);
        this.token = token;
        this.pos = pos;
    }

    /**
     * @param expectedTokenType
     * @param token
     * @param code
     * @param pos
     */
    public ExpectedTokenError(List<TokenType> expectedTokenType, Token token, Pos pos) {
        this.expecTokenType = expectedTokenType;
        this.token = token;
        this.pos = pos;
    }

}
