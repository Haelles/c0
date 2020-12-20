package c0java.analyser;

import c0java.error.AnalyzeError;
import c0java.error.CompileError;
import c0java.error.ErrorCode;
import c0java.error.ExpectedTokenError;
import c0java.error.TokenizeError;
import c0java.instruction.Instruction;
import c0java.instruction.Operation;
import c0java.symbol.Symbol;
import c0java.symbol.SymbolTable;
import c0java.symbol.SymbolType;
import c0java.symbol.func.Function;
import c0java.tokenizer.Token;
import c0java.tokenizer.TokenType;
import c0java.tokenizer.Tokenizer;
import c0java.util.Pos;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    // 符号表的栈
    ArrayList<SymbolTable> symbolTableStack;

//    /** 下一个变量的栈偏移 */
//    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
        this.symbolTableStack  = new ArrayList<>();
    }

    private Token peek() {
        if (peekedToken == null) {
            peekedToken = tokenizer.peekNextToken(); // 读头不需要移位
        }
        return peekedToken;
    }

    private Token next() {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.getNextToken(); // 读头需要进行移位
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     */
    private boolean check(TokenType tt) {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     */
    private Token nextIf(TokenType tt) {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

//    /**
//     * 获取下一个变量的栈偏移
//     *
//     * @return
//     */
//    private int getNextVariableOffset() {
//        return this.nextOffset++;
//    }

    /**
     * 添加一个符号
     *
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, Pos curPos) throws AnalyzeError {
        if (this.symbolTable.get(name) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            this.symbolTable.put(name, new SymbolEntry(isConstant, isInitialized, getNextVariableOffset()));
        }
    }

    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void initializeSymbol(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量在栈上的偏移
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    private int getOffset(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.getStackOffset();
        }
    }

    /**
     * 获取变量是否是常量
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    public List<Instruction> analyse() throws CompileError{
        // 首先对_start进行初始化，并生成tokens
        initStart();
        // 开始分析程序
        analyseProgram();
        return instructions;
    }

    private void initStart() throws TokenizeError {
        Function _start = new Function(SymbolType.FUNC, "_start");
        SymbolTable funcTable = new SymbolTable();
        SymbolTable globalTable = new SymbolTable();
        funcTable.addSymbol(_start);
        symbolTableStack.add(globalTable); // 固定在栈的第0位
        symbolTableStack.add(funcTable); // 固定在栈的第1号位
        tokenizer.generateTokens();
    }

    /**
     * 获得父级符号表
     */
    private SymbolTable getParentTable(){
        int size = symbolTableStack.size();
        return symbolTableStack.get(size - 1);
    }

    // 程序
    // program -> item*
    //item -> function | decl_stmt
    private void analyseProgram() throws CompileError {
        Token peek;
        while (tokenizer.hasNext()){
            peek = tokenizer.peekNextToken();
            TokenType tokenType = peek.getTokenType();
            if(tokenType == TokenType.FN_KW)
                analyseFunc();
            else if(tokenType == TokenType.LET_KW || t == TokenType.CONST_KW)
                analyseDeclareStmt();
            else if(tokenType == TokenType.EOF){
                break;
            } else{
                List<TokenType> expectedTokenType = new LinkedList<>();
                expectedTokenType.add(TokenType.FN_KW);
                expectedTokenType.add(TokenType.LET_KW);
                expectedTokenType.add(TokenType.CONST_KW);
                throw new ExpectedTokenError(expectedTokenType ,peek);
            }
        }
        // 准备输出

    }

    /**
     * 分析函数
     * @throws CompileError 不是符合文法的token则抛出异常
     */
    private void analyseFunc() throws CompileError {
        // # 函数
        // function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
        tokenizer.getNextToken(); // 移到fn下一位

        // 分析函数名
        Token ident = expect(TokenType.IDENT);
        Function function = new Function(SymbolType.FUNC, ident.getValueString());
        // 分析参数
        ArrayList<Symbol> params = analyseParam(function);

        Token returnValue = expect(TokenType.TY);
        // 需要根据有无返回值设置地址参数，但注意返回值不占params数组，只占地址
        switch (returnValue.getValueString()){
            case "void":
                function.addParams(false, params);
                break;
            case "int":
            case "double":
                function.addParams(true, params);
                break;
        }

        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);

        analyseBlockStmt();

        // 开始添加指令

    }

    /**
     * 分析函数的参数，返回一个参数列表
     * @param function 拥有这些参数的函数
     */
    private ArrayList<Symbol> analyseParam(Function function) throws CompileError {
        // function_param_list -> function_param (',' function_param)*
        // function_param -> 'const'? IDENT ':' ty
        ArrayList<Symbol> params = new ArrayList<>();
        while(true){
            if(nextIf(TokenType.CONST_KW) != null){
                Token ident = expect(TokenType.IDENT);
                // TODO:检查参数

            }
            else break;
        }
        return params;
    }

    private void analyseBlockStmt(){

    }

    private void analyseConstantDeclaration() throws CompileError {
        // 示例函数，示例如何解析常量声明
        // 常量声明 -> 常量声明语句*

        // 如果下一个 token 是 const 就继续
        while (nextIf(TokenType.Const) != null) {
            // 常量声明语句 -> 'const' 变量名 '=' 常表达式 ';'

            // 变量名
            var nameToken = expect(TokenType.Ident);

            // 加入符号表
            String name = (String) nameToken.getValue();
            addSymbol(name, true, true, nameToken.getStartPos());

            // 等于号
            expect(TokenType.Equal);

            // 常表达式
            var value = analyseConstantExpression();

            // 分号
            expect(TokenType.Semicolon);

            // 这里把常量值直接放进栈里，位置和符号表记录的一样。
            // 更高级的程序还可以把常量的值记录下来，遇到相应的变量直接替换成这个常数值，
            // 我们这里就先不这么干了。
            instructions.add(new Instruction(Operation.LIT, value));
        }
    }

    private void analyseVariableDeclaration() throws CompileError {
        // 变量声明 -> 变量声明语句*

        // 如果下一个 token 是 var 就继续
        while (nextIf(TokenType.Var) != null) {
            // 变量声明语句 -> 'var' 变量名 ('=' 表达式)? ';'

            // 变量名
            var nameToken = expect(TokenType.Ident);
            // 变量初始化了吗
            boolean initialized = false;

            // 下个 token 是等于号吗？如果是的话分析初始化
           if(nextIf(TokenType.Equal) != null){
               // 分析初始化的表达式
               analyseExpression();
               initialized = true;
           }
            // 分号
            expect(TokenType.Semicolon);

            // 加入符号表，请填写名字和当前位置（报错用）
            String name = (String) nameToken.getValue();
            addSymbol(name, initialized, false, /* 当前位置 */ nameToken.getStartPos());

            // 如果没有初始化的话在栈里推入一个初始值
            if (!initialized) {
                instructions.add(new Instruction(Operation.LIT, 0));
            }
        }
    }

    private void analyseStatementSequence() throws CompileError {
        // 语句序列 -> 语句*
        // 语句 -> 赋值语句 | 输出语句 | 空语句

        while (true) {
            // 如果下一个 token 是……
            var peeked = peek();
            if (peeked.getTokenType() == TokenType.Ident) {
                // 调用相应的分析函数
                // 如果遇到其他非终结符的 FIRST 集呢？
                analyseAssignmentStatement();
            }
            else if(peeked.getTokenType() == TokenType.Print){
                analyseOutputStatement();
            }
            else if(peeked.getTokenType() == TokenType.Semicolon){
                next();
            }
            else {
                // 都不是，摸了
                break;
            }
        }
    }

    private int analyseConstantExpression() throws CompileError {
        // 常表达式 -> 符号? 无符号整数
        boolean negative = false;
        if (nextIf(TokenType.Plus) != null) {
            negative = false;
        } else if (nextIf(TokenType.Minus) != null) {
            negative = true;
        }

        var token = expect(TokenType.Uint);

        int value = (int) token.getValue();
        if (negative) {
            value = -value;
        }

        return value;
    }

    private void analyseExpression() throws CompileError {
        // 表达式 -> 项 (加法型运算符 项)*
        // 项
        analyseItem();

        while (true) {
            // 预读可能是运算符的 token
            var op = peek();
            if (op.getTokenType() != TokenType.Plus && op.getTokenType() != TokenType.Minus) {
                break;
            }

            // 运算符
            next();

            // 项
            analyseItem();

            // 生成代码
            if (op.getTokenType() == TokenType.Plus) {
                instructions.add(new Instruction(Operation.ADD));
            } else if (op.getTokenType() == TokenType.Minus) {
                instructions.add(new Instruction(Operation.SUB));
            }
        }
    }

    private void analyseAssignmentStatement() throws CompileError {
        // 赋值语句 -> 标识符 '=' 表达式 ';'

        // 分析这个语句
        var nameToken = expect(TokenType.Ident);
        expect(TokenType.Equal);
        // 标识符是什么？
        String name = (String) nameToken.getValue();
        var symbol = symbolTable.get(name);
        if (symbol == null) {
            // 没有这个标识符
            throw new AnalyzeError(ErrorCode.NotDeclared, /* 当前位置 */ nameToken.getStartPos());
        } else if (symbol.isConstant) {
            // 标识符是常量
            throw new AnalyzeError(ErrorCode.AssignToConstant, /* 当前位置 */ nameToken.getStartPos());
        }
        // 设置符号已初始化
        initializeSymbol(name, nameToken.getStartPos());
        analyseExpression();
        expect(TokenType.Semicolon);
        // 把结果保存
        var offset = getOffset(name, nameToken.getStartPos());
        instructions.add(new Instruction(Operation.STO, offset));
    }

    private void analyseOutputStatement() throws CompileError {
        // 输出语句 -> 'print' '(' 表达式 ')' ';'

        expect(TokenType.Print);
        expect(TokenType.LParen);

        analyseExpression();

        expect(TokenType.RParen);
        expect(TokenType.Semicolon);

        instructions.add(new Instruction(Operation.WRT));
    }

    private void analyseItem() throws CompileError {
        // 项 -> 因子 (乘法运算符 因子)*

        // 因子
        analyseFactor();
        while (true) {
            // 预读可能是运算符的 token
            var op = peek();
            if (op.getTokenType() != TokenType.Mult && op.getTokenType() != TokenType.Div) {
                break;
            }
            // 运算符
            next();
            // 因子
            analyseFactor();
            // 生成代码
            if (op.getTokenType() == TokenType.Mult) {
                instructions.add(new Instruction(Operation.MUL));
            } else if (op.getTokenType() == TokenType.Div) {
                instructions.add(new Instruction(Operation.DIV));
            }
        }
    }

    private void analyseFactor() throws CompileError {
        // 因子 -> 符号? (标识符 | 无符号整数 | '(' 表达式 ')')

        boolean negate;
        if (nextIf(TokenType.Minus) != null) {
            negate = true;
            // 计算结果需要被 0 减
            instructions.add(new Instruction(Operation.LIT, 0));
        } else {
            nextIf(TokenType.Plus);
            negate = false;
        }

        if (check(TokenType.Ident)) {
            // 是标识符
            var nameToken = expect(TokenType.Ident);
            // 加载标识符的值
            String name = /* 快填 */ (String) nameToken.getValue();
            var symbol = symbolTable.get(name);
            if (symbol == null) {
                // 没有这个标识符
                throw new AnalyzeError(ErrorCode.NotDeclared, /* 当前位置 */ nameToken.getStartPos());
            } else if (!symbol.isInitialized) {
                // 标识符没初始化
                throw new AnalyzeError(ErrorCode.NotInitialized, /* 当前位置 */ nameToken.getStartPos());
            }
            var offset = getOffset(name, nameToken.getStartPos());
            instructions.add(new Instruction(Operation.LOD, offset));
        } else if (check(TokenType.Uint)) {
            // 是整数
            // 加载整数值
            var token = expect(TokenType.Uint);
            int value = (int) token.getValue();
            instructions.add(new Instruction(Operation.LIT, value));
        } else if (check(TokenType.LParen)) {
            // 是表达式
            // 调用相应的处理函数
            expect(TokenType.LParen);
            analyseExpression();
            expect(TokenType.RParen);
        } else {
            // 都不是，摸了
            throw new ExpectedTokenError(List.of(TokenType.Ident, TokenType.Uint, TokenType.LParen), next());
        }

        if (negate) {
            instructions.add(new Instruction(Operation.SUB));
        }
    }
}
