package c0java.analyser;

import c0java.symbol.SymbolTable;

import java.util.ArrayList;

public class SymbolTableStack {
    private ArrayList<SymbolTable> stack;
    int top;

    public SymbolTableStack(){
        stack = new ArrayList<>();
        top = 0;
    }

    public SymbolTable get(int i){
        return stack.get(i);
    }

    public void push(SymbolTable symbolTable){
        stack.add(symbolTable);
        top += 1;
    }

    public SymbolTable pop(){
        top -= 1;
        return stack.get(top - 1);
    }

    /**
     * 获得父级符号表
     */
    public SymbolTable getCurrentTable() {
        if (top >= 2)
            return stack.get(top - 1);
        else return stack.get(0); // 全局符号表
    }

    /**
     *
     * @return 当前符号表是否是全局符号表
     */
    public boolean isGlobalTable(){
        return top == 2;
    }
}
