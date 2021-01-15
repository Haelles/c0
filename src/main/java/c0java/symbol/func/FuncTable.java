package c0java.symbol.func;

import c0java.symbol.SymbolTable;

public class FuncTable extends SymbolTable {
    private int nextFid = 1;
    public int getNextFid(){
        return nextFid++;
    }
}
