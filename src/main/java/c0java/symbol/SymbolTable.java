package c0java.symbol;

import c0java.error.AnalyzeError;
import c0java.error.ErrorCode;
import c0java.symbol.func.Function;
import c0java.util.Pos;

import java.util.ArrayList;

public class SymbolTable {
    protected ArrayList<Symbol> symbolList;

    public SymbolTable(){
        this.symbolList = new ArrayList<>();
    }

    public ArrayList<Symbol> getSymbolList() {
        return symbolList;
    }

    public void addSymbol(Symbol symbol, Pos pos) throws AnalyzeError {
        if(isDeclared(symbol.getName())){
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, pos, symbol.getName() + "已经被声明");
        }
        symbolList.add(symbol);
    }

    public Symbol searchSymbol(String name, Pos pos) throws AnalyzeError {
        for (Symbol symbol : symbolList) {
            if (symbol.getName().equals(name))
                return symbol;
        }
        throw new AnalyzeError(ErrorCode.VariableNotDecl, pos, "查找不到这个符号");
    }

    public int getSymbolLength(){
        return symbolList.size();
    }

    public boolean isDeclared(String symbolName){
        if(symbolName.equals(""))
            return false;
        for(Symbol symbol : symbolList){
            if (symbol.getName().equals(symbolName))
                return true;
        }
        return false;
    }

    public Symbol getSymbol(int i){
        return symbolList.get(i);
    }
}
