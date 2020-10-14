package miniplc0java;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import miniplc0java.analyser.Analyser;
import miniplc0java.error.CompileError;
import miniplc0java.instruction.Instruction;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;

public class App {
    public static void main(String[] args) throws CompileError {
        if (args.length < 3 || (!args[1].equals("tokenize") && !args[1].equals("analyze"))) {
            System.out.println("Usage: miniplc0java <command> <file>");
            System.out.println();
            System.out.println("Commands:");
            System.out.println("  tokenize        Tokenize the file");
            System.out.println("  analyze         Analyse the file and output opcodes");
            System.exit(1);
            return;
        }

        var filename = args[2];
        var file = new File(filename);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find file.");
            e.printStackTrace();
            System.exit(2);
            return;
        }
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);

        if (args[1].equals("tokenize")) {
            // tokenize
            while (true) {
                var token = tokenizer.nextToken();
                if (token.getTokenType().equals(TokenType.EOF)) {
                    break;
                }
                System.out.println(token.toString());
            }
        } else {
            // analyze
            var analyzer = new Analyser(tokenizer);
            var instructions = analyzer.analyse();
            for (Instruction instruction : instructions) {
                System.out.println(instruction.toString());
            }
        }
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
