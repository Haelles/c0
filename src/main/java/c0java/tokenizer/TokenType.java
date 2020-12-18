package c0java.tokenizer;

public enum TokenType {
    // 关键字
    FN_KW,   //  -> 'fn'
    LET_KW, //   -> 'let'
    CONST_KW, //  -> 'const'
    AS_KW, //     -> 'as'
    WHILE_KW, //  -> 'while'
    IF_KW, //     -> 'if'
    ELSE_KW, //   -> 'else'
    RETURN_KW, // -> 'return'

    // 这两个是扩展 c0 的关键字
    BREAK_KW, //  -> 'break'
    CONTINUE_KW, // -> 'continue'

    // 字面量
    UINT_LITERAL, // -> digit+
    STRING_LITERAL, // -> '"' (string_regular_char | escape_sequence)* '"'
    CHAR_LITERAL, // -> '\'' (char_regular_char | escape_sequence) '\''
    // 扩展 c0
    DOUBLE_LITERAL, // -> digit+ '.' digit+ ([eE] [+-]? digit+)?

    // 标识符
    IDENT, // -> [_a-zA-Z] [_a-zA-Z0-9]*

    // 运算符
    PLUS, //      -> '+'
    MINUS, //     -> '-'
    MUL, //       -> '*'
    DIV, //       -> '/'
    ASSIGN, //    -> '='
    EQ, //        -> '=='
    NEQ, //       -> '!='
    LT, //        -> '<'
    GT, //        -> '>'
    LE, //        -> '<='
    GE, //        -> '>='
    L_PAREN, //   -> '('
    R_PAREN, //   -> ')'
    L_BRACE, //   -> '{'
    R_BRACE, //   -> '}'
    ARROW, //     -> '->'
    COMMA, //     -> ','
    COLON, //     -> ':'
    SEMICOLON, // -> ';'

    // 注释，这是扩展c0内容
    COMMENT, // -> '//' regex(.*) '\n'

    // 额外添加，文件尾
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case FN_KW:
                return "FunctionKeyword";
            case LET_KW:
                return "Let";
            case CONST_KW:
                return "Const";
            case AS_KW:
                return "As";
            case WHILE_KW:
                return "While";
            case IF_KW:
                return "If";
            case ELSE_KW:
                return "Else";
            case RETURN_KW:
                return "Return";

            case BREAK_KW:
                return "Break";
            case CONTINUE_KW:
                return "Continue";

            case UINT_LITERAL:
                return "UnsignedInteger";
            case STRING_LITERAL:
                return "String";
            case CHAR_LITERAL:
                return "Char";

            case DOUBLE_LITERAL:
                return "Double";

            case IDENT:
                return "Identifier";

            case PLUS:
                return "PlusSign";
            case MINUS:
                return "MinusSign";
            case MUL:
                return "MultiplicationSign";
            case DIV:
                return "DivideSign";
            case ASSIGN:
                return "Assign";
            case EQ:
                return "Equal";
            case NEQ:
                return "NotEqual";
            case LT:
                return "LessThan";
            case GT:
                return "GreaterThan";
            case LE:
                return "LessOrEqualThan";
            case GE:
                return "GreaterOrEqualThan";
            case L_PAREN:
                return "LeftBracket";
            case R_PAREN:
                return "RightBracket";
            case L_BRACE:
                return "LightBrace";
            case R_BRACE:
                return "RightBrace";
            case ARROW:
                return "RightArrow";
            case COMMA:
                return "Comma";
            case SEMICOLON:
                return "Semicolon";
            case COLON:
                return "Colon";
            case COMMENT:
                return "Comment";
            case EOF:
                return "EOF";
            default:
                return "InvalidToken";
        }
    }
}
