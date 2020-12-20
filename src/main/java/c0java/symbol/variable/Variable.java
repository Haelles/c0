package c0java.symbol.variable;


import c0java.symbol.Symbol;
import c0java.symbol.SymbolType;
import c0java.symbol.ValueType;

public class Variable extends Symbol {
    private int intValue;
    private String stringValue;
    private double doubleValue;

    public Variable(boolean isConst, SymbolType symbolType, ValueType valueType, String name){
        super(isConst, symbolType, valueType, name);
    }

    public void setValue(int value){
        intValue = value;
    }

    public void setValue(String value){
        stringValue = value;
    }

    public void setValue(double value){
        doubleValue = value;
    }


}
