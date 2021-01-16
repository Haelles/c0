package c0java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import c0java.analyser.Analyser;
import c0java.error.CompileError;
import c0java.instruction.Instruction;
import c0java.tokenizer.*;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class App {
    public static void main(String[] args){
        InputStream input;
        try {
            input = new FileInputStream(args[1]);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find input file.");
            e.printStackTrace();
            System.exit(-1);
            return;
        }

        // 从这里对main进行修改
        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        Tokenizer tokenizer = new Tokenizer(iter);
        // analyze
        var analyzer = new Analyser(tokenizer);
        try {
            analyzer.analyse(args[3]);
        } catch (Exception e) {
            System.out.println(e.toString());
            System.exit(-1);
        }

    }


}
