package c0java.tokenizer;

import java.util.ArrayList;

public class TokenTypeStack {
    private ArrayList<TokenType> tokenTypeStack;
    int top;

    public TokenTypeStack(){
        tokenTypeStack = new ArrayList<>();
        top = 0;
    }

    public ArrayList<TokenType> getTokenTypeStack(){
        return tokenTypeStack;
    }

    public void push(TokenType tokenType){
        tokenTypeStack.add(tokenType);
        top += 1;
    }

    public TokenType pop(){
        top -= 1;
        return tokenTypeStack.get(top);
    }

    public boolean isEmptyStack(){
        return top == 0;
    }

    public int getTop(){
        return top;
    }

}
