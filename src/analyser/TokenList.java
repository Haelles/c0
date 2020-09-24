package analyser;

import instruction.Instruction;
import javafx.util.Pair;
import tokenizer.Token;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

public class TokenList {

    ArrayList<Token> tokens;
    Integer offset;
    ArrayList<Instruction> instructions;
    Pair<Integer, Integer> current_pos;
    // 为了简单处理，我们直接把符号表耦合在语法分析里
    // 变量                   示例
    // uninitializedVars    var a;
    // vars                  var a=1;
    // constants             const a=1;
    LinkedHashMap<String, Integer> uninitializedVars;
    LinkedHashMap<String, Integer> vars;
    LinkedHashMap<String, Integer> constants;

    Integer nextTokenIndex;

    // 返回下一个 token
    Optional<Token> nextToken() {
        return Optional.empty();
    }

    // 回退一个 token
    void unreadToken() {
    }

    void _add(Token token, LinkedHashMap<String, Integer> map) {

    }

    // 添加变量、常量、未初始化的变量
    void addVariable(Token token) {

    }

    void addConstant(Token token) {

    }

    void addUninitializedVariable(Token token) {

    }

    // 是否被声明过
    Boolean isDeclared(String string) {
        return false;
    }

    // 是否是未初始化的变量
    Boolean isUninitializedVariable(String string) {
        return false;
    }

    // 是否是已初始化的变量
    Boolean isInitializedVariable(String string) {
        return false;
    }

    // 是否是常量
    Boolean isConstant(String string) {
        return false;
    }

    // 获得 {变量，常量} 在栈上的偏移
    Integer getIndex(String string) {
        return 0;
    }
}
