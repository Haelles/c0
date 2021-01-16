package c0java;

import c0java.error.OutputError;
import c0java.instruction.Instruction;
import c0java.symbol.Symbol;
import c0java.symbol.SymbolTable;
import c0java.symbol.SymbolType;
import c0java.symbol.ValueType;
import c0java.symbol.func.FuncTable;
import c0java.symbol.func.Function;
import c0java.symbol.variable.Variable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Output {
    public static int magic = 0x72303b3e;
    public static int version = 0x00000001;

    public static void outputBinary(SymbolTable global, HashMap<Integer, String> hashMap, FuncTable functions, String fileName) throws IOException, OutputError {
        DataOutputStream dos=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
        // magic and version
        dos.writeInt(magic);
        dos.writeInt(version);

        // global table
        dos.writeInt(global.getSymbolLength());
        ArrayList<Symbol> variableArrayList = global.getSymbolList();
        char[] temp;
        int addr;
        for(Symbol symbol : variableArrayList){
            Variable variable = (Variable)symbol;
            if(variable.getSymbolType() != SymbolType.GLOBAL)
                throw new OutputError("全局变量表中存在非GLOBAL变量");
            if(variable.isConst())
                dos.writeByte(0x01);
            else dos.writeByte(0x00);
            dos.writeInt(variable.getLength());
            if (variable.getValueType() == ValueType.STRING){
                addr = variable.getAddress();
                temp = hashMap.get(addr).toCharArray();
                for(char c : temp)
                    dos.writeByte(c);
            }
            else{
                long t = 0;
                // 用0填充，在_start函数里面去完成赋值
                dos.writeLong(0x0000000000000000);
            }
        }

        // functions
        dos.writeInt(functions.getSymbolLength());
        ArrayList<Symbol> funcList = functions.getSymbolList();
        Function function;
        int n;
        ArrayList<Instruction> instructions;
        for(Symbol symbol : funcList){
            function = (Function) symbol;
            dos.writeInt(function.getFname());
            dos.writeInt(function.getReturnSlots());
            dos.writeInt(function.getParamSlots());
            dos.writeInt(function.getLocalSlots());
            instructions = function.getInstructions();
            n = instructions.size();
            dos.writeInt(n);
            for(Instruction instruction : instructions){
                dos.writeByte(Instruction.instructionToBinaryCode[instruction.operationToInt()]);
                switch (instruction.getChooseDataType()){
                    case 1:
                        dos.writeInt(instruction.getX());
                        break;
                    case 2:
                        dos.writeLong(instruction.getX1());
                        break;
                    case 3:
                        dos.writeDouble(instruction.getX2());
                        break;
                    default:
                }
            }
        }
        dos.flush();
        dos.close();
    }

    public static void outputFile(SymbolTable global, HashMap<Integer, String> hashMap, FuncTable functions, String fileName) throws IOException, OutputError {
        File file = new File(fileName);
        if(!file.exists()){
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fileWriter);

        bw.write("magic: " + Integer.toHexString(magic) + "\n");
        bw.write("version: " + Integer.toHexString(version) + "\n");

        // global
        bw.write("global size: " + global.getSymbolLength() + "\n");
        ArrayList<Symbol> variableArrayList = global.getSymbolList();

        for(Symbol symbol : variableArrayList){
            Variable variable = (Variable)symbol;
            if(variable.getSymbolType() != SymbolType.GLOBAL)
                throw new OutputError("全局变量表中存在非GLOBAL变量");
            bw.write("is const: " + variable.isConst() + "\n");
            bw.write("size: " + variable.getLength() + "\n");
            if (variable.getValueType() == ValueType.STRING){
                bw.write("string: " + hashMap.get(variable.getAddress()) + "\n");
            }
            else{
                // 用0填充，在_start函数里面去完成赋值
                bw.write("0x0000000000000000\n");
            }
        }

        bw.write("-----区分global和func------\n");

        // functions
        bw.write(functions.getSymbolLength() + "\n");
        ArrayList<Symbol> funcList = functions.getSymbolList();
        Function function;
        ArrayList<Instruction> instructions;
        for(Symbol symbol : funcList){
            function = (Function) symbol;
            bw.write("fid: " + function.getAddress() + "\n");
            bw.write("returnSlots: " + function.getReturnSlots() + "\n");
            bw.write("paramSlots: " + function.getParamSlots() + "\n");
            bw.write("localSlots: " + function.getLocalSlots() + "\n");
            instructions = function.getInstructions();
            bw.write("instruction size: " + instructions.size() + "\n");
            for(Instruction instruction : instructions){
                bw.write(instruction.toString() + " \n");
            }
        }
        bw.flush();
        bw.close();
    }

}
