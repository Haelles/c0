package c0java.tokenizer;

import java.util.ArrayList;

public class TokenTypeStack {
    private TokenType[] tokenTypeStack;
    int top;

    public TokenTypeStack(){
        tokenTypeStack = new TokenType[200];
        top = 0;
    }

    public TokenType[] getTokenTypeStack(){
        return tokenTypeStack;
    }

    public void push(TokenType tokenType){
       tokenTypeStack[top++] = tokenType;
    }

    public TokenType pop(){
        return tokenTypeStack[--top];
    }

    public TokenType getTopElement(){
        return tokenTypeStack[top - 1];
    }

    public TokenType getElement(int i){
        return tokenTypeStack[i];
    }

    public boolean isEmptyStack(){
        return top == 0;
    }

    public int getTop(){
        return top;
    }

}
