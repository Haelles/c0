package tokenizer;

import error.TokenizeError;
import error.ErrorCode;
import util.Pos;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class Tokenizer {

    // 如果没有初始化，那么就 readAll
    private Boolean initialized;
    // 指向下一个要读取的字符
    Pos ptr;
    // 以行为基础的缓冲区
    ArrayList<String> linesBuffer;

    static Scanner scanner;

    public Tokenizer() {
        initialized = false;
        ptr = new Pos(0, 0);
        linesBuffer = new ArrayList<>();
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    public Token nextToken() throws TokenizeError {
        if (!initialized) {
            readAll();
        }
        if (isEOF()) {
            throw new TokenizeError(ErrorCode.EOF, 0, 0);
        }

        throw new Error("Not implemented");
    }

    public void checkToken(Token token) throws TokenizeError {
        switch (token.getTokenType()) {
            case Ident: {
                String val = token.getValueString();
                if (Character.isDigit(val.charAt(0))) {
                    throw new TokenizeError(ErrorCode.InvalidIdentifier, token.getStartPos());
                }
                break;
            }
            default:
                break;
        }
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
        // todo:check read \n?
        initialized = true;
    }

    // 一个简单的总结
    // | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 偏移
    // | = | = | = | = | = | = | = | = | = | = |
    // | h | a | 1 | 9 | 2 | 6 | 0 | 8 | 1 | \n |（缓冲区第0行）
    // | 7 | 1 | 1 | 4 | 5 | 1 | 4 | （缓冲区第1行）
    // 这里假设指针指向第一行的 \n，那么有
    // nextPos() = (1, 0)
    // currentPos() = (0, 9)
    // previousPos() = (0, 8)
    // nextChar() = '\n' 并且指针移动到 (1, 0)
    // unreadLast() 指针移动到 (0, 8)
    public Pos nextPos() {
        if (ptr.row >= linesBuffer.size()) {
            throw new Error("advance after EOF");
        }
        if (ptr.col == linesBuffer.get(ptr.row).length() - 1) {
            return new Pos(ptr.col + 1, 0);
        }
        return new Pos(ptr.row, ptr.col + 1);
    }

    public Pos currentPos() {
        return ptr;
    }

    public Pos previousPos() {
        if (ptr.row == 0 && ptr.col == 0) {
            throw new Error("previous position from beginning");
        }
        if (ptr.col == 0) {
            return new Pos(ptr.row - 1, linesBuffer.get(ptr.row - 1).length() - 1);
        }
        return new Pos(ptr.row, ptr.col - 1);
    }

    public Optional<Character> nextChar() {
        if (isEOF()) {
            return Optional.empty();
        }
        Character result = linesBuffer.get(ptr.row).charAt(ptr.col);
        ptr = nextPos();
        return Optional.of(result);
    }

    public Boolean isEOF() {
        return ptr.row >= linesBuffer.size();
    }

    // Note: Is it evil to unread a buffer?
    public void unreadLast() {
        ptr = previousPos();
    }
}
