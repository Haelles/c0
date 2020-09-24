package tokenizer;

import error.CompilationError;
import error.ErrorCode;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class Tokenizer {

    // 如果没有初始化，那么就 readAll
    private Boolean initialized;
    // 指向下一个要读取的字符
    Pair<Integer, Integer> ptr;
    // 以行为基础的缓冲区
    ArrayList<String> linesBuffer;

    static Scanner scanner;

    public Tokenizer() {
        initialized = false;
        ptr = new Pair<>(0, 0);
        linesBuffer = new ArrayList<>();
    }

    public Pair<Optional<Token>, Optional<CompilationError>> NextToken() {
        if (!initialized) {
            readAll();
        }
        if (isEOF()) {
            return new Pair<>(Optional.empty(), Optional.of(new CompilationError(ErrorCode.ErrEOF, 0, 0)));
        }
        Pair<Optional<Token>, Optional<CompilationError>> p = nextToken();
        if (p.getValue().isPresent()) {
            return p;
        }
        Token token = p.getKey().orElse(null);
        if (token == null) {
            return new Pair<>(Optional.empty(), Optional.of(new CompilationError(ErrorCode.ErrStreamError, 0, 0)));
        }
        CompilationError err = checkToken(token).orElse(null);
        if (err != null) {
            return new Pair<>(p.getKey(), Optional.of(err));
        }
        return new Pair<>(p.getKey(), Optional.empty());
    }

    public Pair<ArrayList<Token>, Optional<CompilationError>> AllTokens() {
        ArrayList<Token> result = new ArrayList<>();
        while (true) {
            Pair<Optional<Token>, Optional<CompilationError>> p = NextToken();
            if (p.getValue().isPresent()) {
                if (p.getValue().get().getErr() == ErrorCode.ErrEOF) {
                    return new Pair<>(result, Optional.empty());
                } else {
                    return new Pair<>(new ArrayList<>(), p.getValue());
                }
            }
            if (p.getKey().isPresent()) {
                result.add(p.getKey().get());
            }
        }
    }

    public Pair<Optional<Token>, Optional<CompilationError>> nextToken() {
//todo:
        return new Pair<>(Optional.empty(), Optional.empty());
    }


    public Optional<CompilationError> checkToken(Token token) {
        switch (token.getTokenType()) {
            case IDENTIFIER: {
                String val = token.getValueString();
                if (Character.isDigit(val.charAt(0))) {
                    return Optional.of(new CompilationError(ErrorCode.ErrInvalidIdentifier, token.getStartPos().getKey(),
                            token.getStartPos().getValue()));
                }
                break;
            }
            default:
                break;
        }
        return Optional.empty();
    }

    // 从这里开始其实是一个基于行号的缓冲区的实现
    // 为了简单起见，我们没有单独拿出一个类实现
    // 核心思想和 C 的文件输入输出类似，就是一个 buffer 加一个指针，有三个细节
    // 1.缓冲区包括 \n
    // 2.指针始终指向下一个要读取的 char
    // 3.行号和列号从 0 开始

    // 一次读入全部内容，并且替换所有换行为 \n
    // 这样其实是不合理的，这里只是简单起见这么实现
    public void readAll() {
        if (initialized) {
            return;
        }
        while (scanner.hasNext()) {
            linesBuffer.add(scanner.nextLine() + '\n');
        }
        //todo:check read \n?
        initialized = true;
    }

    // 一个简单的总结
    // | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9  | 偏移
    // | = | = | = | = | = | = | = | = | = | =  |
    // | h | a | 1 | 9 | 2 | 6 | 0 | 8 | 1 | \n |（缓冲区第0行）
    // | 7 | 1 | 1 | 4 | 5 | 1 | 4 |             （缓冲区第1行）
    // 这里假设指针指向第一行的 \n，那么有
    // nextPos() = (1, 0)
    // currentPos() = (0, 9)
    // previousPos() = (0, 8)
    // nextChar() = '\n' 并且指针移动到 (1, 0)
    // unreadLast() 指针移动到 (0, 8)
    public Pair<Integer, Integer> nextPos() {
        if (ptr.getKey() >= linesBuffer.size()) {
            CompilationError.DieAndPrint("advance after EOF");
        }
        if (ptr.getValue() == linesBuffer.get(ptr.getKey()).length() - 1) {
            return new Pair<>(ptr.getKey() + 1, 0);
        }
        return new Pair<>(ptr.getKey(), ptr.getValue() + 1);
    }

    public Pair<Integer, Integer> currentPos() {
        return ptr;
    }

    public Pair<Integer, Integer> previousPos() {
        if (ptr.getKey() == 0 && ptr.getValue() == 0) {
            CompilationError.DieAndPrint("previous position from beginning");
        }
        if (ptr.getValue() == 0) {
            return new Pair<>(ptr.getKey() - 1, linesBuffer.get(ptr.getKey() - 1).length() - 1);
        }
        return new Pair<>(ptr.getKey(), ptr.getValue() - 1);
    }

    public Optional<Character> nextChar() {
        if (isEOF()) {
            return Optional.empty();
        }
        Character result = linesBuffer.get(ptr.getKey()).charAt(ptr.getValue());
        ptr = nextPos();
        return Optional.of(result);
    }

    public Boolean isEOF() {
        return ptr.getKey() >= linesBuffer.size();
    }

    // Note: Is it evil to unread a buffer?
    public void unreadLast() {
        ptr = previousPos();
    }
}
