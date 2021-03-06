package c0java.analyser;

import c0java.error.AnalyzeError;
import c0java.error.ErrorCode;
import c0java.symbol.Symbol;
import c0java.symbol.SymbolTable;
import c0java.util.Pos;

import java.util.ArrayList;

public class SymbolTableStack {
    private SymbolTable[] stack;
    int top;

    public SymbolTableStack(){
        stack = new SymbolTable[100];
        top = 0;
    }

    public SymbolTable get(int i){
        return stack[i];
    }

    public void push(SymbolTable symbolTable){
        stack[top++] = symbolTable;
    }

    public SymbolTable pop(){
        return stack[--top];
    }

    /**
     * 获得父级符号表
     */
    public SymbolTable getCurrentTable() {
        return stack[top - 1];
    }

    /**
     *
     * @return 当前符号表是否是全局符号表
     */
    public boolean isGlobalTable(){
        return top == 1;
    }

    public Symbol getSymbolByName(String symbolName, Pos pos) throws AnalyzeError {
        if(symbolName.equals(""))
            throw new AnalyzeError(ErrorCode.VariableNotDecl, pos, "不能查找匿名对象");
        int i = top - 1;
        for(; i >= 0; --i){
            ArrayList<Symbol> symbolTable = stack[i].getSymbolList();
            for(Symbol symbol : symbolTable){
                if (symbol.getName().equals(symbolName))
                    return symbol;
            }
        }
        throw new AnalyzeError(ErrorCode.VariableNotDecl, pos, "查找不到这个变量");
    }
}
