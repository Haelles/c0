package c0java.symbol;

import java.util.ArrayList;

public class SymbolTable {
    private ArrayList<Symbol> symbolList;

    public SymbolTable(){
        this.symbolList = new ArrayList<>();
    }

    public ArrayList<Symbol> getSymbolList() {
        return symbolList;
    }

    public void addSymbol(Symbol symbol){
        symbolList.add(symbol);
        symbol.setAddress(symbolList.size() - 1);
    }

    public int getSymbolLength(){
        return symbolList.size();
    }

    public boolean isDeclared(String symbolName){
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
