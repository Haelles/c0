package c0java.symbol;

import java.util.ArrayList;

public class ValueTypeStack {
    private ArrayList<ValueType> valueTypeStack;
    int top;

    public ValueTypeStack(){
        valueTypeStack = new ArrayList<>();
        top = 0;
    }

    public ArrayList<ValueType> getvalueTypeStack(){
        return valueTypeStack;
    }

    public void push(ValueType valueType){
        valueTypeStack.add(valueType);
        top += 1;
    }

    public ValueType pop(){
        top -= 1;
        return valueTypeStack.get(top);
    }

    public boolean isEmptyStack(){
        return top == 0;
    }

    public int getTop(){
        return top;
    }

}

