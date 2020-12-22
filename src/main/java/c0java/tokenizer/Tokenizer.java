package c0java.tokenizer;

import c0java.error.AnalyzeError;
import c0java.error.TokenizeError;
import c0java.error.ErrorCode;
import c0java.util.Pos;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Tokenizer {

    private StringIter it;
    private StringBuilder buffer;
    private char peek;
    private char cur;

    private int tokenLength;
    private int currentPos; // 记录读取到哪个token
    private ArrayList<Token> tokens = new ArrayList<>();

    public Tokenizer(StringIter it) {
        this.it = it;
        this.buffer = new StringBuilder();
        this.peek = 0;
        this.cur = 0;
        this.tokenLength = 0;
        this.currentPos = 0;
    }

    public boolean hasNext(){
        return currentPos < tokenLength;
    }

    public int getTokenLength(){
        return tokenLength;
    }

    public ArrayList<Token> getTokens(){
        return tokens;
    }

    public Token getNextToken(){
        currentPos += 1;
        return tokens.get(currentPos - 1);
    }

    public Token getCurrentToken(){ // 再次获得上一个token，它已经被读完
        return tokens.get(currentPos - 1);
    }

    public Token peekNextToken(){
        return getToken(currentPos);
    }

    public Token getToken(int i){
        return tokens.get(i);
    }

    public Token moveToForward() throws AnalyzeError {
        currentPos -= 1;
        if(currentPos < 0)
            throw new AnalyzeError(ErrorCode.IndexOutOfBound, new Pos(0, 0), "token下标最小值为0");
        return getToken(currentPos);
    }

    public void removeToken(int i){
        tokens.remove(i);
        tokenLength -= 1;
    }

    public ArrayList<Token> generateTokens() throws TokenizeError {
        while (true) {
            var token = nextToken();
            if (token.getTokenType().equals(TokenType.EOF)) {
                break;
            }
            if(token.getTokenType() != TokenType.COMMENT)
                addToken(token);
        }
        return tokens;
    }

    /**
     * 以下函数均被封装，外部不会调用
     */
    private void addToken(Token token){
        tokens.add(token);
        tokenLength += 1;
    }

    private Token nextToken() throws TokenizeError {
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
            // int void double
            case "int", "void", "double" -> TokenType.TY;

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
                if(peek == '>'){
                    stepCurPeek();
                    return new Token(TokenType.ARROW, "->", startPos, it.nextPos());
                }
                return new Token(TokenType.MINUS, '-', startPos, it.nextPos());

            case '*':
                return new Token(TokenType.MUL, '*', startPos, it.nextPos());

            case '/':
                if(peek == '/'){
                    return lexComment(startPos);
                }
                return new Token(TokenType.DIV, '/', startPos, it.nextPos());

            case '=':
                if(peek == '='){
                    stepCurPeek();
                    return new Token(TokenType.EQ, "==", startPos, it.nextPos());
                }
                return new Token(TokenType.ASSIGN, '=', startPos, it.nextPos());

            case '!':
                if(peek != '=')
                    throw new TokenizeError(ErrorCode.InvalidInput, startPos);
                else return new Token(TokenType.NEQ, "!=", startPos, it.nextPos());

            case '<':
                if(peek == '='){
                    stepCurPeek();
                    return new Token(TokenType.LE, "<=", startPos, it.nextPos());
                }
                else return new Token(TokenType.LT, '<', startPos, it.nextPos());

            case '>':
                if(peek == '='){
                    stepCurPeek();
                    return new Token(TokenType.GE, ">=", startPos, it.nextPos());
                }
                else return new Token(TokenType.GT, '>', startPos, it.nextPos());

            case '(':
                return new Token(TokenType.L_PAREN, '(', startPos, it.nextPos());

            case ')':
                return new Token(TokenType.R_PAREN, ')', startPos, it.nextPos());

            case '{':
                return new Token(TokenType.L_BRACE, '{', startPos, it.nextPos());

            case '}':
                return new Token(TokenType.R_BRACE, '}', startPos, it.nextPos());

            case ',':
                return new Token(TokenType.COMMA, ',', startPos, it.nextPos());

            case ':':
                return new Token(TokenType.COLON, ':', startPos, it.nextPos());

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
