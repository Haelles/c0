package c0java.symbol.variable;


import c0java.error.AnalyzeError;
import c0java.error.ErrorCode;
import c0java.symbol.Symbol;
import c0java.symbol.SymbolType;
import c0java.symbol.ValueType;
import c0java.tokenizer.Token;

public class Variable extends Symbol {
    private ValueType valueType; // 是哪种数据类型的变量
    private boolean isInitialized = false;
    private boolean isConst = false;

    public Variable(String name){
        super(name);
    }

    public void setInitialized(boolean isInitialized){
        this.isInitialized = isInitialized;
    }

    public void setIsConst(boolean isConst){
        this.isConst = isConst;
    }

    public void setValueType(ValueType valueType){
        this.valueType = valueType;
    }

    public void setVariableValueType(Token returnType) throws AnalyzeError {
        String type = returnType.getValueString();
        if (type.equals("void"))
            throw new AnalyzeError(ErrorCode.InvalidVariableType, returnType.getStartPos(), "变量类型不应该是void");
        if (type.equals("int"))
            setValueType(ValueType.INT);
        else if (type.equals("double"))
            setValueType(ValueType.DOUBLE);
        else throw new AnalyzeError(ErrorCode.InvalidVariableType, returnType.getStartPos(), "变量类型只能是int/double");
    }

    public boolean isInitializedOrNot(){
        return isInitialized;
    }

    public ValueType getValueType(){
        return valueType;
    }


}
