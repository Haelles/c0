package c0java;

import java.io.*;
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
        FileInputStream input2;
        try {
            input = new FileInputStream(args[1]);
            input2 = new FileInputStream(args[1]);
            byte[] bbuf = new byte[1024];
            //用于保存实际读取的字节数
            int hasRead = 0;
            //使用循环来重复读取数据
            while( (hasRead = input2.read(bbuf)) > 0){
                //将字节数组转换为字符串输出
                System.out.print(new String(bbuf,0,hasRead));
            }
            //关闭文件输出流，放在finally块里更安全
            input2.close();
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find input file.");
            e.printStackTrace();
            System.exit(-1);
            return;
        } catch (IOException e) {
            e.printStackTrace();
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
