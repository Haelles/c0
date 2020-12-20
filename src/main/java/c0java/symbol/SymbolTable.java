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
    }

    public int getSymbolLength(){
        return symbolList.size();
    }
}
