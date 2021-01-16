package c0java.symbol;

import c0java.tokenizer.TokenType;

import java.util.ArrayList;

public class ValueTypeStack {
    private ValueType[] valueTypeStack;
    int top;

    public ValueTypeStack(){
        valueTypeStack = new ValueType[200];
        top = 0;
    }

    public ValueType[] getvalueTypeStack(){
        return valueTypeStack;
    }

    public void push(ValueType valueType){
        valueTypeStack[top++] = valueType;
    }

    public ValueType pop(){
        return valueTypeStack[--top];
    }

    public ValueType getTopElement(){
        return valueTypeStack[top - 1];
    }

    public ValueType getElement(int i){
        return valueTypeStack[i];
    }

    public boolean isEmptyStack(){
        return top == 0;
    }

    public int getTop(){
        return top;
    }

}

