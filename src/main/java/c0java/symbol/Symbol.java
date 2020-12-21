package c0java.symbol;


import c0java.instruction.Instruction;

public class Symbol {
    private boolean isConst;
    private SymbolType symbolType; // 是哪种变量，参数/全局/局部/函数

    private String name; // 变量名字
    private int length; // 变量长度为几个字节
    private int address; // 记录是第几个——第几个全局变量，第几个函数，第几个局部变量，第几个参数

    public Symbol(String name){
        this.name = name;
    }

    public Symbol(SymbolType symbolType, String name){
        this.symbolType = symbolType;
        this.name = name;
    }

    public Symbol(boolean isConst, SymbolType symbolType, String name){
        this.isConst = isConst;
        this.symbolType = symbolType;
        this.name = name;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public void setLength(int length){
        this.length = length;
    }

    public void setSymbolType(SymbolType symbolType){
        this.symbolType = symbolType;
    }

    public boolean isConstOrNot(){
        return isConst;
    }

    public SymbolType getSymbolType(){
        return symbolType;
    }

    public String getName(){
        return name;
    }

    public int getAddress(){
        return address;
    }


}
