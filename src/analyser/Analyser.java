package analyser;

import error.CompilationError;
import instruction.Instruction;
import javafx.util.Pair;
import tokenizer.Token;
import tokenizer.TokenType;

import java.util.*;

public final class Analyser {

    TokenList tokenList;

    public Pair<List<Instruction>, Optional<CompilationError>> analyse() {
        Optional<CompilationError> err = analyseProgram();
        if(err.isEmpty()){
            return new Pair<>(new ArrayList<>(), err);
        }else {
            return new Pair<>(tokenList.instructions, Optional.empty());
        }
    }

    // <程序> ::= 'begin'<主过程>'end'
    private Optional<CompilationError> analyseProgram() {
        // 示例函数，示例如何调用子程序
        Optional<Token> token = tokenList.nextToken();
        if(token.isEmpty() || token.get().getTokenType() != TokenType.BEGIN){

        }
        // 'begin'
        return Optional.empty();
    }

    private Optional<CompilationError> analyseMain() {
        return Optional.empty();
    }

    private Optional<CompilationError> analyseConstantDeclaration() {
        return Optional.empty();
    }

    private Optional<CompilationError> analyseVariableDeclaration() {
        return Optional.empty();
    }

    private Optional<CompilationError> analyseStatementSequence() {
        return Optional.empty();
    }

    private Optional<CompilationError> analyseConstantExpression() {
        return Optional.empty();
    }

    private Optional<CompilationError> analyseExpression() {
        return Optional.empty();
    }

    private Optional<CompilationError> analyseAssignmentStatement() {
        return Optional.empty();
    }

    private Optional<CompilationError> analyseItem() {
        return Optional.empty();
    }

    private Optional<CompilationError> analyseFactor() {
        return Optional.empty();
    }
}
