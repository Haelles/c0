package c0java.symbol.func;

import c0java.instruction.Instruction;
import c0java.symbol.Symbol;
import c0java.symbol.ValueType;
import c0java.symbol.SymbolType;

import java.util.ArrayList;

public class Function extends Symbol {
    private int paramSlots;  // 参数和返回值加一起一共有几个slot
    private int localSlots; // 局部变量占几个slots
    private int returnSlots; // 返回值占几个slots
    private ArrayList<Instruction> instructions;
    private ArrayList<Symbol> params; // 记录所有的参数和返回值

    public Function(SymbolType symbolType, String name){
        super(symbolType, name);
        init();
    }
    public Function(boolean isConst, SymbolType symbolType, ValueType valueType, String name){
        super(isConst, symbolType, valueType, name);
        init();
    }

    public void init(){
        paramSlots = 0;
        localSlots = 0;
        returnSlots = 0;
    }

    public void setParamSlots(int paramSlots){
        this.paramSlots = paramSlots;
    }

    public void setLocalSlots(int localSlots){
        this.localSlots = localSlots;
    }

    public void setReturnSlots(int returnSlots){
        this.returnSlots = returnSlots;
    }

    // 根据ret决定symbol的地址从0开始还是从1开始；添加symbol到params的同时还需要设置symbol的地址（第几个）
    public void addParams(boolean ret, ArrayList<Symbol> symbols){

    }

    public void addInstruction(Instruction instruction){
        instructions.add(instruction);
    }

}
