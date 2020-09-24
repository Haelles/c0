package tokenizer;

import error.CompilationError;
import javafx.util.*;

import java.util.Objects;

public class Token {
    private TokenType tokenType;
    private Object value;
    private Pair<Integer, Integer> startPos;
    private Pair<Integer, Integer> endPos;

    public Token(TokenType tokenType, Object value, Integer startLine, Integer startColumn, Integer endLine, Integer endColumn) {
        this.tokenType = tokenType;
        this.value = value;
        this.startPos = new Pair<>(startLine, startColumn);
        this.endPos = new Pair<>(endLine, endColumn);
    }

    public Token(TokenType tokenType, Object value, Pair<Integer, Integer> startPos, Pair<Integer, Integer> endPos) {
        this.tokenType = tokenType;
        this.value = value;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public Token(Token token) {
        this.tokenType = token.tokenType;
        this.value = token.value;
        this.startPos = token.startPos;
        this.endPos = token.endPos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return tokenType == token.tokenType &&
                Objects.equals(value, token.value) &&
                Objects.equals(startPos, token.startPos) &&
                Objects.equals(endPos, token.endPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenType, value, startPos, endPos);
    }

    public String GeValueString() {
        if (value instanceof Integer || value instanceof String || value instanceof Character) {
            return value.toString();
        }
        CompilationError.DieAndPrint("No suitable cast for token value.");
        return "Invalid";
    }


    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Pair<Integer, Integer> getStartPos() {
        return startPos;
    }

    public void setStartPos(Pair<Integer, Integer> startPos) {
        this.startPos = startPos;
    }

    public Pair<Integer, Integer> getEndPos() {
        return endPos;
    }

    public void setEndPos(Pair<Integer, Integer> endPos) {
        this.endPos = endPos;
    }
}
