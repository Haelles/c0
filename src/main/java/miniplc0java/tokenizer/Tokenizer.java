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

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char cur = it.nextChar();
        if (Character.isDigit(cur)) {
            return lexUIntOrDouble();
        } else if (cur == '_' || Character.isLowerCase(cur) || Character.isUpperCase(cur)) {
            return lexIdentOrKeyword();
        } else{
            return lexOperatorOrUnknown();
        }
    }

    private Token lexUIntOrDouble() throws TokenizeError {
        buffer.setLength(0); // 清空缓存
        Pos startPos = it.currentPos();
        buffer.append(cur);
        peek = it.peekChar();
        while(Character.isDigit(peek)){
            buffer.append(peek);
            swapCurPeek();
        }
        if(peek == '.'){ // 读到小数点
            buffer.append(peek);
            swapCurPeek();
            if(!Character.isDigit(peek)){ // 小数点下一位不是数字，非法（至少要有一位数字）
                throw new TokenizeError(ErrorCode.InvalidInput, it.currentPos());
            }
            while(Character.isDigit(peek)){
                buffer.append(peek);
                swapCurPeek();
            }
            if(peek == 'e' || peek == 'E'){
                buffer.append(peek);
                swapCurPeek();
                if(peek == '+' || peek == '-'){
                    buffer.append(peek);
                    swapCurPeek();
                }
                if(!Character.isDigit(peek)) // 至少要有一位数字
                    throw new TokenizeError(ErrorCode.InvalidInput, it.currentPos());
                while (Character.isDigit(peek)){
                    buffer.append(peek);
                    swapCurPeek();
                }
            }
            return new Token(TokenType.DOUBLE_LITERAL, Double.parseDouble(buffer.toString()), startPos, it.currentPos());
        }
        else{ // 无符号整数
            return new Token(TokenType.UINT_LITERAL, Integer.parseInt(buffer.toString()), startPos, it.currentPos());
        }
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串
        Pos startPos = it.currentPos();
        try{
            StringBuilder temp = new StringBuilder();
            temp.append(it.nextChar());
            do{
                char ch = it.peekChar();
                if(Character.isDigit(ch) || Character.isAlphabetic(ch)){
                    temp.append(ch);
                    it.nextChar();
                }
                else break;
            }while (true);
            String str = temp.toString();
            TokenType tokenType = switch (str) {
                case "begin" -> TokenType.Begin;
                case "end" -> TokenType.End;
                case "const" -> TokenType.Const;
                case "var" -> TokenType.Var;
                case "print" -> TokenType.Print;
                default -> TokenType.Ident;
            };
            return new Token(tokenType, str, startPos, it.currentPos());
        } catch (Exception e) {
            throw new TokenizeError(ErrorCode.InvalidInput, startPos);
        }
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.Plus, '+', it.previousPos(), it.currentPos());

            case '-':
                // 填入返回语句
                return new Token(TokenType.Minus, '-', it.previousPos(), it.currentPos());

            case '*':
                // 填入返回语句
                return new Token(TokenType.Mult, '*', it.previousPos(), it.currentPos());

            case '/':
                // 填入返回语句
                return new Token(TokenType.Div, '/', it.previousPos(), it.currentPos());

            // 填入更多状态和返回语句
            case '=':
                return new Token(TokenType.Equal, '=', it.previousPos(), it.currentPos());
            case ';':
                return new Token(TokenType.Semicolon, ';', it.previousPos(), it.currentPos());
            case '(':
                return new Token(TokenType.LParen, '(', it.previousPos(), it.currentPos());
            case ')':
                return new Token(TokenType.RParen, ')', it.previousPos(), it.currentPos());
            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }

    private void swapCurPeek(){
        cur = it.nextChar();
        peek = it.peekChar();
    }
}
