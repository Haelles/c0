package c0java.symbol;


import c0java.instruction.Instruction;

public class Symbol {
    private boolean isConst;
    private SymbolType symbolType; // 是哪种变量，参数/全局/局部
    private ValueType valueType; // 是哪种数据类型的变量
    private String name; // 变量名字
    private boolean isDeclared;
    private int pos; // 记录是第几个——第几个全局变量，第几个函数，第几个局部变量，第几个参数


    public Symbol(SymbolType symbolType, String name){
        this.symbolType = symbolType;
        this.name = name;
    }

    public Symbol(boolean isConst, SymbolType symbolType, ValueType valueType, String name){
        this.isConst = isConst;
        this.symbolType = symbolType;
        this.valueType = valueType;
        this.name = name;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public void setDeclared(boolean isDeclared){
        this.isDeclared = isDeclared;
    }

    public boolean isConstOrNot(){
        return isConst;
    }

    public boolean isDeclaredOrNot(){
        return isDeclared;
    }

    public SymbolType getSymbolType(){
        return symbolType;
    }

    public ValueType getValueType(){
        return valueType;
    }

    public void addInstruction(Instruction instruction){

    }
}
