package c0java.symbol.func;

import c0java.error.AnalyzeError;
import c0java.error.ErrorCode;
import c0java.instruction.Instruction;
import c0java.symbol.Symbol;
import c0java.symbol.ValueType;
import c0java.symbol.SymbolType;
import c0java.symbol.variable.Variable;
import c0java.tokenizer.Token;

import java.util.ArrayList;

public class Function extends Symbol {
    // 按照playground中fn的顺序
    private int localSlots; // 局部变量占几个slots
    private int paramSlots;  // 参数和返回值加一起一共有几个slot
    private int returnSlots; // 返回值占几个slots
    private int Fname; // 对应全局变量表中的第几个变量
    private ValueType returnValueType;
    private ArrayList<Instruction> instructions;
    private ArrayList<ValueType> paramValueTypeList; // 记录所有的参数和返回值

    public Function(SymbolType symbolType, String name){
        super(symbolType, name);
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

    public void setReturnValueType(ValueType valueType){
        this.returnValueType = valueType;
    }

    public void setLocalSlots(int localSlots){
        this.localSlots = localSlots;
    }

    public void setReturnSlots(int returnSlots){
        this.returnSlots = returnSlots;
    }

    public void setFname(int fname){
        this.Fname = fname;
    }

    // 根据ret决定symbol的地址从0开始还是从1开始；添加symbol到params的同时还需要设置symbol的地址（第几个）
    public void addParams(Token returnValue, ArrayList<Variable> variables) throws AnalyzeError {
        int address = 1;
        returnSlots = 1;
        String type = returnValue.getValueString();
        switch (type) {
            case "void" -> {
                address = 0;
                returnSlots = 0;
                this.returnValueType = ValueType.VOID;
            }
            case "int" -> this.returnValueType = ValueType.INT;
            case "double" -> this.returnValueType = ValueType.DOUBLE;
            default -> throw new AnalyzeError(ErrorCode.ExpectedToken,
                    returnValue.getStartPos(), "函数返回值类型有误");
        }
        for(Variable variable : variables){
            variable.setAddress(address);
            address += 1;
            paramSlots += 1;
            paramValueTypeList.add(variable.getValueType());
        }
    }

    public int addInstruction(Instruction instruction){
        instructions.add(instruction);
        return instructions.size() - 1;
    }

    public void setInstructionValue(int id, int offset){
        Instruction instruction = instructions.get(id);
        instruction.setX(offset);
    }

    public void setBreakPos(int begin, int end){
        int i = begin;
        for(; i < end; ++i){
            Instruction instruction = instructions.get(i);
            // 用0x3f3f3f3f来表示要被填写的
            if (instruction.getRecordBreak() == 0x3f3f3f3f){
                instruction.setX(end - i);
            }
        }
    }

    public ValueType getReturnValueType(){
        return returnValueType;
    }

    public int nextLocal(){
        return localSlots++;
    }

    public ArrayList<ValueType> getParamValueTypeList() {
        return paramValueTypeList;
    }

    public int getFname(){
        return Fname;
    }

    public int getLocalSlots() {
        return localSlots;
    }

    public int getParamSlots() {
        return paramSlots;
    }

    public int getReturnSlots() {
        return returnSlots;
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }
}
