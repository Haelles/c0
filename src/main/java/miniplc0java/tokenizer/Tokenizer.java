package miniplc0java.tokenizer;

import miniplc0java.error.TokenizeError;
import miniplc0java.error.ErrorCode;
import miniplc0java.util.Pos;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;

public class Tokenizer {

    private StringIter it;
    private StringBuilder buffer;
    private char peek;
    private char cur;

    public Tokenizer(StringIter it) {
        this.it = it;
        this.buffer = new StringBuilder();
        this.peek = 0;
        this.cur = 0;
    }
    
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        stepCurPeek();
        if(Character.isDigit(cur)) {
            return lexUIntOrDouble();
        }
        else if(cur == '_' || Character.isLowerCase(cur) || Character.isUpperCase(cur)) {
            return lexIdentOrKeyword();
        }
        else if(cur == '\''){
            return lexChar();
        }
        else if(cur == '\"'){
            return lexString();
        }
        else{
            return lexOperatorOrCommentOrUnknown();
        }
    }

    private Token lexString() throws TokenizeError{
        buffer.setLength(0);
        Pos startPos = it.currentPos();
        stepCurPeek(); // 未执行此条语句时候cur == '\"'
        while (cur != 0 && cur != '\"'){
            addCharToBuffer();
            stepCurPeek();
        }
        if(cur != '\"')
            throw new TokenizeError(ErrorCode.InvalidInput, it.currentPos());
        return new Token(TokenType.STRING_LITERAL, buffer.toString(), startPos, it.currentPos());
    }

    private void addCharToBuffer() throws TokenizeError {
        if(cur == '\\'){
            stepCurPeek();
            switch (cur) {
                case '\\' -> buffer.append("\\");
                case '\"' -> buffer.append("\"");
                case '\'' -> buffer.append("'");
                case 'n' -> buffer.append("\n");
                case 'r' -> buffer.append("\r");
                case 't' -> buffer.append("\t");
                default -> throw new TokenizeError(ErrorCode.InvalidInput, it.currentPos());
            }
        }
        else{
            buffer.append(cur);
        }
    }

    private Token lexChar() throws TokenizeError{
        buffer.setLength(0);
        Pos startPos = it.currentPos();
        stepCurPeek(); // 未执行此条语句时候cur == '\''
        addCharToBuffer();
        stepCurPeek(); // 检查下一位是不是'\''
        if (cur != '\''){
            throw new TokenizeError(ErrorCode.InvalidInput, it.currentPos());
        }
        return new Token(TokenType.CHAR_LITERAL, buffer.toString(), startPos, it.currentPos());
    }

    private Token lexUIntOrDouble() throws TokenizeError {
        buffer.setLength(0); // 清空缓存
        Pos startPos = it.currentPos();
        buffer.append(cur);
        while(Character.isDigit(peek)){
            buffer.append(peek);
            stepCurPeek();
        }
        if(peek == '.'){ // 读到小数点
            buffer.append(peek);
            stepCurPeek();
            if(!Character.isDigit(peek)){ // 小数点下一位不是数字，非法（至少要有一位数字）
                throw new TokenizeError(ErrorCode.InvalidInput, it.currentPos());
            }
            while(Character.isDigit(peek)){
                buffer.append(peek);
                stepCurPeek();
            }
            if(peek == 'e' || peek == 'E'){
                buffer.append(peek);
                stepCurPeek();
                if(peek == '+' || peek == '-'){
                    buffer.append(peek);
                    stepCurPeek();
                }
                if(!Character.isDigit(peek)) // 至少要有一位数字
                    throw new TokenizeError(ErrorCode.InvalidInput, it.currentPos());
                while (Character.isDigit(peek)){
                    buffer.append(peek);
                    stepCurPeek();
                }
            }
            return new Token(TokenType.DOUBLE_LITERAL, Double.parseDouble(buffer.toString()), startPos, it.currentPos());
        }
        else{ // 无符号整数
            return new Token(TokenType.UINT_LITERAL, Integer.parseInt(buffer.toString()), startPos, it.currentPos());
        }
    }

    private Token lexIdentOrKeyword(){
        buffer.setLength(0);
        Pos startPos = it.currentPos();
        buffer.append(cur);
        while(peek == '_' || Character.isDigit(peek) || Character.isLowerCase(peek) || Character.isUpperCase(peek)){
            buffer.append(peek);
            stepCurPeek();
        }
        TokenType tokenType = switch (buffer.toString()) {
            case "fn" -> TokenType.FN_KW;
            case "let" -> TokenType.LET_KW;
            case "const" -> TokenType.CONST_KW;
            case "as" -> TokenType.AS_KW;
            case "while" -> TokenType.WHILE_KW;
            case "if" -> TokenType.IF_KW;
            case "else" -> TokenType.ELSE_KW;
            case "return" -> TokenType.RETURN_KW;
            // 扩展c0
            case "break" -> TokenType.BREAK_KW;
            case "continue" -> TokenType.CONTINUE_KW;
            default -> TokenType.IDENT;
        };
        return new Token(tokenType, buffer.toString(), startPos, it.currentPos());
    }

    private Token lexOperatorOrCommentOrUnknown() throws TokenizeError {
        buffer.setLength(0);
        Pos startPos = it.currentPos();
        switch (cur){
            case '+':
                return new Token(TokenType.PLUS, '+', startPos, it.nextPos());

            case '-':
                if(peek == '>')
                    return new Token(TokenType.ARROW, "->", startPos, it.nextPos());
                return new Token(TokenType.MINUS, '-', startPos, it.nextPos());

            case '*':
                return new Token(TokenType.MUL, '*', startPos, it.nextPos());

            case '/':
                if(peek == '/'){
                    return lexComment(startPos);
                }
                return new Token(TokenType.DIV, '/', startPos, it.nextPos());

            case '=':
                if(peek == '=')
                    return new Token(TokenType.EQ, "==", startPos, it.nextPos());
                return new Token(TokenType.ASSIGN, '=', startPos, it.nextPos());

            case '!':
                if(peek != '=')
                    throw new TokenizeError(ErrorCode.InvalidInput, startPos);
                else return new Token(TokenType.ASSIGN, "!=", startPos, it.nextPos());

            case '<':
                if(peek == '=')
                    return new Token(TokenType.LE, "<=", startPos, it.nextPos());
                else return new Token(TokenType.LT, '=', startPos, it.nextPos());

            case '>':
                if(peek == '=')
                    return new Token(TokenType.GE, ">=", startPos, it.nextPos());
                else return new Token(TokenType.GT, '=', startPos, it.nextPos());

            case '(':
                return new Token(TokenType.L_PAREN, '(', startPos, it.nextPos());

            case ')':
                return new Token(TokenType.R_PAREN, ')', startPos, it.nextPos());

            case '{':
                return new Token(TokenType.L_BRACE, '{', startPos, it.nextPos());

            case '}':
                return new Token(TokenType.R_BRACE, '}', startPos, it.nextPos());

            case ',':
                return new Token(TokenType.COMMA, ';', startPos, it.nextPos());

            case ':':
                return new Token(TokenType.COLON, ';', startPos, it.nextPos());

            case ';':
                return new Token(TokenType.SEMICOLON, ';', startPos, it.nextPos());

            default:
                throw new TokenizeError(ErrorCode.InvalidInput, startPos);
        }
    }

    private Token lexComment(Pos startPos){
        stepCurPeek();
        while (peek != '\n' && peek != 0)
            stepCurPeek();
        return new Token(TokenType.COMMENT, "//", startPos, it.currentPos());
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }

    private void stepCurPeek(){
        cur = it.nextChar();
        peek = it.peekChar();
    }
}
