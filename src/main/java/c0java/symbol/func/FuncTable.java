package c0java.symbol.func;

import c0java.error.AnalyzeError;
import c0java.error.ErrorCode;
import c0java.symbol.Symbol;
import c0java.symbol.SymbolTable;
import c0java.util.Pos;

public class FuncTable extends SymbolTable {
    private int nextFid = 1;
    public int getNextFid(){
        return nextFid++;
    }

    public Function searchFunc(String funcName, Pos pos) throws AnalyzeError {
        for (Symbol function : symbolList) {
            if (function.getName().equals(funcName))
                return (Function) function;
        }
        throw new AnalyzeError(ErrorCode.VariableNotDecl, pos, "查找不到这个函数");
    }
}
