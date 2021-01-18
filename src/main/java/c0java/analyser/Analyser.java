package c0java.analyser;

import c0java.Output;
import c0java.error.*;
import c0java.instruction.Instruction;
import c0java.instruction.Operation;
import c0java.symbol.*;
import c0java.symbol.func.FuncTable;
import c0java.symbol.func.Function;
import c0java.symbol.variable.Variable;
import c0java.tokenizer.Token;
import c0java.tokenizer.TokenType;
import c0java.tokenizer.TokenTypeStack;
import c0java.tokenizer.Tokenizer;
import c0java.util.Pos;

import java.io.IOException;
import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    // 符号表的栈
    private SymbolTableStack symbolTableStack;
    // 单独拿出函数表作为一个属性
    private FuncTable funcTable;
    // 存储函数名和string
    private HashMap<Integer, String> funcNameAndStringMap;

//    /** 下一个变量的栈偏移 */
//    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
        this.symbolTableStack = new SymbolTableStack();
    }

    private Token peek() {
        if (peekedToken == null) {
            peekedToken = tokenizer.peekNextToken(); // 读头不需要移位
        }
        return peekedToken;
    }

    private Token previous() throws AnalyzeError {
        peekedToken = null;
        return tokenizer.moveToForward();
    }

    private Token next() {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            tokenizer.addCurrentPos();
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
    
    public Token currentToken(){
        return tokenizer.getCurrentToken();
    }


    public void analyse(String fileName) throws CompileError, IOException, OutputError {
        // 首先对_start进行初始化，并生成tokens
        initStart();
        // 开始分析程序
        analyseProgram();
        Output.outputBinary(symbolTableStack.get(0), funcNameAndStringMap, funcTable, fileName);
        Output.outputFile(symbolTableStack.get(0), funcNameAndStringMap, funcTable, "result.txt");
    }

    private void initStart() throws TokenizeError, AnalyzeError {
        Function _start = new Function(SymbolType.FUNC, "_start");
        _start.setAddress(0); // 在函数表的位置为0

        Variable fn_name = new Variable(""); // 函数名和字符串均是匿名
        fn_name.setSymbolType(SymbolType.GLOBAL);
        fn_name.setValueType(ValueType.STRING);
        fn_name.setAddress(0); // 在全局符号表的位置为0
        fn_name.setLength(6);

        funcTable = new FuncTable();
        funcTable.addSymbol(_start, new Pos(0, 0));

        SymbolTable globalTable = new SymbolTable();
        globalTable.addSymbol(fn_name, new Pos(0, 0));
        symbolTableStack.push(globalTable); // 全局变量表固定在栈的第0位

        // 生成字符串存储表，存入_start
        funcNameAndStringMap = new HashMap<>();
        funcNameAndStringMap.put(0, "_start");

        // 生成token序列
        tokenizer.generateTokens();
    }

    // 程序
    // program -> item*
    //item -> function | decl_stmt
    private void analyseProgram() throws CompileError {
        Token peek;
        Function _start = (Function) funcTable.getSymbol(0); // 获得_start
        while (tokenizer.hasNext()) {
            peek = tokenizer.peekNextToken();
            TokenType tokenType = peek.getTokenType();
            if (tokenType == TokenType.FN_KW)
                analyseFunc();
            else if (tokenType == TokenType.LET_KW || tokenType == TokenType.CONST_KW)
                analyseDeclStmt(_start);
            else if (tokenType == TokenType.SHARP) {
                break;
            } else {
                List<TokenType> expectedTokenType = new LinkedList<>();
                expectedTokenType.add(TokenType.FN_KW);
                expectedTokenType.add(TokenType.LET_KW);
                expectedTokenType.add(TokenType.CONST_KW);
                throw new ExpectedTokenError(expectedTokenType, peek);
            }
        }
        // 准备输出
        Function main = funcTable.searchFunc("main", currentToken().getStartPos());
        _start.addInstruction(new Instruction(Operation.CALL, main.getAddress()));
    }

    /**
     * 分析函数
     *
     * @throws CompileError 不是符合文法的token则抛出异常
     */
    private void analyseFunc() throws CompileError {
        // # 函数
        // function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
        tokenizer.getNextToken(); // 移到fn下一位

        // 分析函数名，检查是否重复
        Token ident = expect(TokenType.IDENT);
        SymbolTable globalTable = symbolTableStack.get(0);
        String funcName = ident.getValueString();
        if (globalTable.isDeclared(funcName) || funcTable.isDeclared(funcName)) {
            throw new DuplicateError(ident.getStartPos(), "函数名字与已有函数/全局变量重复");
        }
        Function function = new Function(SymbolType.FUNC, ident.getValueString());
        expect(TokenType.L_PAREN);
        // 分析参数
        ArrayList<Variable> params = analyseParam(function);

        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);

        Token returnValue = expect(TokenType.TY);
        // 添加参数和返回值，函数名字长度
        function.addParams(returnValue, params);

        // 添加一张局部变量表，开始对函数体进行分析
        SymbolTable localTable = new SymbolTable();
        // 先加入参数到变量表中
        if(params.size() > 0)
            for(Symbol param : params){
                localTable.addSymbol(param, currentToken().getStartPos());
            }
        function.setAddress(funcTable.getNextFid()); // 第几个函数

        symbolTableStack.push(localTable);
        analyseBlockStmt(function, -1); // 用-1表示不在while中
        function.addInstruction(new Instruction(Operation.RET));
        symbolTableStack.pop();

        // 函数名添加到全局变量表;函数合法，添加到函数表
        Variable fn_name = new Variable(funcName, SymbolType.GLOBAL, ValueType.STRING);
        fn_name.setLength(funcName.length());
        fn_name.setAddress(globalTable.getSymbolLength());
        globalTable.addSymbol(fn_name, currentToken().getStartPos());
        funcNameAndStringMap.put(fn_name.getAddress(), funcName);
        function.setFname(fn_name.getAddress());
        funcTable.addSymbol(function, currentToken().getStartPos());
    }

    /**
     * 分析函数的参数，返回一个参数列表
     *
     * @param function 拥有这些参数的函数
     */
    private ArrayList<Variable> analyseParam(Function function) throws CompileError {
        // function_param_list -> function_param (',' function_param)*
        // function_param -> 'const'? IDENT ':' ty
        ArrayList<Variable> params = new ArrayList<>();
        if (tokenizer.peekNextToken().getTokenType() == TokenType.R_PAREN)
            return params;
        params.add(nextParam());
        while (nextIf(TokenType.COMMA) != null) {
            params.add(nextParam());
        }
        return params;
    }

    private Variable nextParam() throws CompileError {
        Variable param;
        boolean isConst = false;
        if (nextIf(TokenType.CONST_KW) != null)
            isConst = true;

        Token ident = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        Token returnType = expect(TokenType.TY);
        String type = returnType.getValueString();
        param = new Variable(ident.getValueString());
        param.setSymbolType(SymbolType.PARAM);
        if (isConst)
            param.setIsConst(true);
        param.setLength(8);
        param.setVariableValueType(returnType);

        return param;
    }

    private void analyseBlockStmt(Function function, int inWhile) throws CompileError {
        // block_stmt -> '{' stmt* '}'
        expect(TokenType.L_BRACE);
        while (peek().getTokenType() != TokenType.R_BRACE)
            analyseStmt(function, inWhile);
        expect(TokenType.R_BRACE);
    }

    private void analyseStmt(Function function, int inWhile) throws CompileError {
//        stmt ->
//                expr_stmt
//                        | decl_stmt
//                        | if_stmt
//                        | while_stmt
//                        | break_stmt
//                        | continue_stmt
//                        | return_stmt
//                        | block_stmt
//                        | empty_stmt
        Token peek = peek();
        TokenType tokenType = peek.getTokenType();
        if (tokenType == TokenType.CONST_KW || peek.getTokenType() == TokenType.LET_KW)
            analyseDeclStmt(function);
        else if(tokenType == TokenType.IF_KW)
            analyseIfStmt(function, inWhile);
        else if(tokenType == TokenType.WHILE_KW)
            analyseWhileStmt(function, inWhile);
        else if(tokenType == TokenType.BREAK_KW){
            analyseBreakStmt(function, inWhile);
        } else if(tokenType == TokenType.CONTINUE_KW){
            analyseContinueStmt(function, inWhile);
        } else if(tokenType == TokenType.RETURN_KW){
            analyseReturnStmt(function);
        } else if(tokenType == TokenType.L_BRACE){
            SymbolTable localTable = new SymbolTable();
            symbolTableStack.push(localTable);
            analyseBlockStmt(function, inWhile);
            symbolTableStack.pop();
        } else if(tokenType == TokenType.SEMICOLON){
            analyseEmptyStmt(function);
        } else if(inFirstSetOfExprStmt(tokenType))
            analyseExpr(function);
        else throw new AnalyzeError(ErrorCode.ExpectedToken,
                currentToken().getStartPos(), "需要合适的token类型来进入expr");
    }


    private void analyseDeclStmt(Function function) throws CompileError {
        // decl_stmt -> let_decl_stmt | const_decl_stmt
        Token peek = peek();
        if (peek.getTokenType() == TokenType.CONST_KW)
            analyseConstDeclStmt(function);
        else if(peek.getTokenType() == TokenType.LET_KW)
            analyseLetDeclStmt(function);
    }

    private void analyseLetDeclStmt(Function function) throws CompileError {
        // let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
        initializeVariable(false, function);
    }

    private void analyseConstDeclStmt(Function function) throws CompileError {
        // const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
        initializeVariable(true, function);
    }

    private void initializeVariable(boolean isConst, Function function) throws CompileError {
        next(); // 移走const/let
        Token ident = expect(TokenType.IDENT);
        String identName = ident.getValueString();
        // 检查是否重名
        SymbolTable currentTable = symbolTableStack.getCurrentTable();
        if(currentTable.isDeclared(identName))
            throw new DuplicateError(ident.getStartPos(), "变量名字重复");

        expect(TokenType.COLON);

        Token identTypeToken = expect(TokenType.TY);

        // 初始化
        Variable variable = new Variable(ident.getValueString());
        variable.setInitialized(false); // 是否被初始化了
        variable.setIsConst(isConst);  // 是否是定值
        boolean isGlobal = symbolTableStack.isGlobalTable();
        if(isGlobal){ // 是全局变量吗
            variable.setSymbolType(SymbolType.GLOBAL);
            variable.setAddress(currentTable.getSymbolLength());
        }
        else{
            variable.setSymbolType(SymbolType.LOCAL);
            variable.setAddress(function.nextLocal());
        }
        variable.setVariableValueType(identTypeToken); // 是哪种数据类型，是void则抛异常
        variable.setLength(8);

        // 添加到符号表
        currentTable.addSymbol(variable, ident.getStartPos());

        // 准备开始对表达式进行分析
        if(peek().getTokenType() == TokenType.ASSIGN){
            next();
            if(isGlobal)
                function.addInstruction(new Instruction(Operation.GLOBA, variable.getAddress()));
            else function.addInstruction(new Instruction(Operation.LOCA, variable.getAddress()));
            ValueType exprReturnType = analyseExpr(function);
            if(variable.getValueType() != exprReturnType)
                throw new AnalyzeError(ErrorCode.InvalidVariableType,
                        currentToken().getStartPos(), "expr表达式返回类型不合预期");
            variable.setInitialized(true); // 有表达式，已经被初始化
            function.addInstruction(new Instruction(Operation.STORE_64));
        }
        else if(isConst){
            throw new ExpectedTokenError(TokenType.ASSIGN, currentToken());
        }
        expect(TokenType.SEMICOLON);
    }

    private void analyseIfStmt(Function function, int inWhile) throws CompileError {
        // if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
        next(); // 去掉检测过的if
        if(analyseExpr(function) == ValueType.VOID)
            throw new AnalyzeError(ErrorCode.InvalidReturnTYpe,
                    currentToken().getStartPos(), "expr表达式返回类型不合预期");

        int label1 = function.addInstruction(new Instruction(Operation.BR_FALSE, 0));
        SymbolTable symbolTable = new SymbolTable();
        symbolTableStack.push(symbolTable);
        analyseBlockStmt(function, inWhile);
        symbolTableStack.pop();
        if(peek().getTokenType() == TokenType.ELSE_KW){
            next();
            int label2 = function.addInstruction(new Instruction(Operation.BR, 0));
            function.setInstructionValue(label1, label2 - label1);
            if(peek().getTokenType() == TokenType.IF_KW){
                SymbolTable symbolTable2 = new SymbolTable();
                symbolTableStack.push(symbolTable2);
                analyseIfStmt(function, inWhile);
                symbolTableStack.pop();
            }
            else {
                analyseBlockStmt(function, inWhile);
            }
            int br0 = function.addInstruction(new Instruction(Operation.NOP)); // 模仿助教
            function.setInstructionValue(label2, br0 - label2);
        }
        else{
            int br0 = function.addInstruction(new Instruction(Operation.NOP)); // 模仿助教
            function.setInstructionValue(label1, br0 - label1);
        }
    }

    private void analyseWhileStmt(Function function, int inWhile) throws CompileError {
        // while_stmt -> 'while' expr block_stmt
        next();
        // 根据助教代码，br0指令作为while部分的开端
        int x = function.addInstruction(new Instruction(Operation.NOP));

        ValueType valueType = analyseExpr(function);
//        if(valueType != ValueType.INT)
//            throw new AnalyzeError(ErrorCode.InvalidReturnTYpe,
//                    currentToken().getStartPos(), "expr表达式返回类型不合预期");

        function.addInstruction(new Instruction(Operation.BR_TRUE, 1));
        int id = function.addInstruction(new Instruction(Operation.BR, 0));
        // 需要一个新的符号表
        SymbolTable symbolTable = new SymbolTable();
        symbolTableStack.push(symbolTable);
        analyseBlockStmt(function, x);
        symbolTableStack.pop();

        int ed = function.getInstructionCount();
        // while指令的结束
        function.addInstruction(new Instruction(Operation.BR, x - ed - 1));
        ed = function.addInstruction(new Instruction(Operation.NOP));
        Instruction jmp = function.getInstruction(id);
        jmp.setX(ed - id);

        ArrayList<Instruction> instructions = function.getInstructions();
        int i = 0;
        for(Instruction instruction : instructions){
            if(instruction.getRecordBreak() == 0x3f3f3f3f){
                instruction.setX(ed - i);
            }
            i++;
        }

        //
//        // 设置向前和向后的指令数
//        function.setInstructionValue(brForwardID, brBackID - brForwardID);
//        function.setInstructionValue(brBackID,  br0 - brBackID);
//
//        // 添加break和continue
//        function.setBreakPos(br0, brBackID);

    }

    private void analyseBreakStmt(Function function, int inWhile) throws CompileError {
        // break_stmt -> 'break' ';'
        // 首先检查是否在一个while块中
        if(inWhile == -1)
            throw new AnalyzeError(ErrorCode.InvalidInput,
                    currentToken().getStartPos(), "break只能用在while循环中");
        next(); // 移走break
        expect(TokenType.SEMICOLON);
        // 等待被填写
        function.addInstruction(new Instruction(Operation.BR, 0, 0x3f3f3f3f));
    }

    private void analyseContinueStmt(Function function, int inWhile) throws CompileError {
        // continue_stmt -> 'continue' ';'
        if(inWhile == -1)
            throw new AnalyzeError(ErrorCode.InvalidInput,
                    currentToken().getStartPos(), "continue只能用在while循环中");
        next(); // continue
        expect(TokenType.SEMICOLON);
        int id = function.addInstruction(new Instruction(Operation.BR, 0));
        function.setInstructionValue(id, inWhile - id);
    }

    private void analyseReturnStmt(Function function) throws CompileError {
        // return_stmt -> 'return' expr? ';'
        expect(TokenType.RETURN_KW);
        ValueType valueType = function.getReturnValueType();
        Token peek = peek();
        if(valueType == ValueType.VOID && peek.getTokenType() != TokenType.SEMICOLON)
            throw new AnalyzeError(ErrorCode.ExpectedToken,
                    currentToken().getStartPos(), "函数为void类型，下一个token应该是分号");
        function.addInstruction(new Instruction(Operation.ARGA,0));
        if(function.getReturnValueType() != analyseExpr(function))
            throw new AnalyzeError(ErrorCode.ExpectedToken,
                    currentToken().getStartPos(), "需要合适的token类型来进入expr");
        if(function.getReturnValueType() != ValueType.VOID)
            function.addInstruction(new Instruction(Operation.STORE_64));
        function.addInstruction(new Instruction(Operation.RET));
        expect(TokenType.SEMICOLON);
    }

    private boolean inFirstSetOfExprStmt(TokenType type)
    {
        return type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.MUL ||
                type == TokenType.COMMA || type == TokenType.DIV || type == TokenType.EQ ||
                type == TokenType.NEQ || type == TokenType.GT || type == TokenType.GE ||
                type == TokenType.LT || type == TokenType.LE || type == TokenType.ASSIGN ||
                type == TokenType.IDENT || type == TokenType.UINT_LITERAL ||
                type == TokenType.CHAR_LITERAL || type == TokenType.STRING_LITERAL ||
                type == TokenType.DOUBLE_LITERAL || type == TokenType.L_PAREN ||
                type == TokenType.R_PAREN || type == TokenType.AS_KW || type == TokenType.TY;
    }

    private void analyseEmptyStmt(Function function) throws CompileError {
        // empty_stmt -> ';'
        expect(TokenType.SEMICOLON);
    }

    // TODO
    private ValueType analyseExpr(Function function) throws CompileError {
        TokenTypeStack operatorStack = new TokenTypeStack();
        ValueTypeStack typeStack = new ValueTypeStack();

        operatorStack.push(TokenType.SHARP);

        if(!inFirstSetOfExprStmt(peek().getTokenType()))
            return ValueType.VOID;

        while (tokenizer.hasNext() && inFirstSetOfExprStmt(peek().getTokenType())){
            Token peek = peek();
            TokenType peekTokenType = peek.getTokenType();
            if(peekTokenType == TokenType.L_PAREN){
                if(operatorStack.getTop() <= typeStack.getTop()){
                    throw new AnalyzeError(ErrorCode.InvalidStructure, peek.getStartPos(), "两个表达式不能相邻");
                }
                typeStack.push(analyseGroupExpr(function));
            }
            else if(peekTokenType == TokenType.IDENT){
                if(operatorStack.getTop() <= typeStack.getTop()){
                    throw new AnalyzeError(ErrorCode.InvalidStructure, peek.getStartPos(), "两个表达式不能相邻");
                }
                next();
                // 注意这里移位了，不能再用上面定义的peek
                if(tokenizer.hasNext() && peek().getTokenType() == TokenType.L_PAREN){
                    previous();
                    typeStack.push(analyseCallExpr(function));
                }
                else if(tokenizer.hasNext() && peek().getTokenType() == TokenType.ASSIGN){
                    previous();
                    typeStack.push(analyseAssignExpr(function));
                }
                else{
                    previous();
                    typeStack.push(analyseIdentExpr(function));
                }
            }
            else if(peekTokenType == TokenType.UINT_LITERAL || peekTokenType == TokenType.DOUBLE_LITERAL || peekTokenType == TokenType.CHAR_LITERAL || peekTokenType == TokenType.STRING_LITERAL){
                if(operatorStack.getTop() <= typeStack.getTop()){
                    throw new AnalyzeError(ErrorCode.InvalidStructure, peek.getStartPos(), "两个表达式不能相邻");
                }
                typeStack.push(analyseLiteralExpr(function));
            }
            else if(peekTokenType == TokenType.MINUS && operatorStack.getTop() > typeStack.getTop()){
                typeStack.push(analyseNegateExpr(function));
            }
            else if(peekTokenType == TokenType.AS_KW){
                next();
                String type = expect(TokenType.TY).getValueString();
                // 注意&&和||的优先级
                if(operatorStack.getTop() > typeStack.getTop() || (!type.equals("int") && !type.equals("double")) || typeStack.getTop() <= 0){
                    throw new AnalyzeError(ErrorCode.InvalidStructure, peek.getStartPos(), "无法进行类型转换");
                }
                if (typeStack.getTopElement() == ValueType.INT && type.equals("double")){
                    typeStack.pop();
                    typeStack.push(ValueType.DOUBLE);
                    function.addInstruction(new Instruction(Operation.ITOF));
                }
                else if (typeStack.getTopElement() == ValueType.DOUBLE && type.equals("int")){
                    typeStack.pop();
                    typeStack.push(ValueType.INT);
                    function.addInstruction(new Instruction(Operation.FTOI));
                }
                else
                    throw new AnalyzeError(ErrorCode.InvalidStructure, peek.getStartPos(), "不满足int<->double的要求");
            }
            else{
                if (operatorStack.getTop() != typeStack.getTop() || peekTokenType == TokenType.ASSIGN || peekTokenType == TokenType.TY)
                    throw new AnalyzeError(ErrorCode.InvalidStructure, peek.getStartPos(), "表达式结构非法");
                else if(peekTokenType == TokenType.R_PAREN || peekTokenType == TokenType.COMMA){
                    if(typeStack.getTop() == 1)
                        return typeStack.getTopElement();
                    else break;
                }

                // 这里开始使用算符优先文法
                // 不会出现 (、ident、和前置-，这时一定是符号

                if (less(operatorStack.getTopElement(), peekTokenType)){
                    // 移入
                    operatorStack.push(next().getTokenType());
                }
                else{
                    // 归约
                    if (typeStack.getTopElement() != typeStack.getElement(typeStack.getTop() - 2))
                        throw new AnalyzeError(ErrorCode.TypeNotMatch, peek.getStartPos(), "type does not match!");
                    ValueType tmp = analyseOperatorExpr(typeStack.getTopElement(), operatorStack.getTopElement(), function);
                    typeStack.pop();
                    typeStack.pop();
                    operatorStack.pop();
                    typeStack.push(tmp);
                }
            }
        }
        while (operatorStack.getTopElement() != TokenType.SHARP)
        {
            if (typeStack.getTopElement() != typeStack.getElement(typeStack.getTop() - 2))
                throw new AnalyzeError(ErrorCode.TypeNotMatch, peek().getStartPos(), "type does not match!");
            typeStack.push(analyseOperatorExpr(typeStack.pop(), operatorStack.pop(), function));
            typeStack.pop();
        }
        return typeStack.getElement(0);
    }

    private ValueType analyseOperatorExpr(ValueType valueType, TokenType tokenType, Function function) throws AnalyzeError {
        switch (tokenType) {
            case PLUS -> {
                addInstructionInOperatorExpr(valueType, function, Operation.ADD_I, Operation.ADD_F, "数据类型不对，不能相加");
                return valueType;
            }
            case MINUS -> {
                addInstructionInOperatorExpr(valueType, function, Operation.SUB_I, Operation.SUB_F, "数据类型不对，不能相减");
                return valueType;
            }
            case MUL -> {
                addInstructionInOperatorExpr(valueType, function, Operation.MUL_I, Operation.MUL_F, "数据类型不对，不能相乘");
                return valueType;
            }
            case DIV -> {
                addInstructionInOperatorExpr(valueType, function, Operation.DIV_I, Operation.DIV_F, "数据类型不对，不能相除");
                return valueType;
            }
            case LE -> {
                addInstructionInOperatorExpr(valueType, function, Operation.CMP_I, Operation.CMP_F, "数据类型不对，不能比较<=");
                function.addInstruction(new Instruction(Operation.SET_GT));
                function.addInstruction(new Instruction(Operation.NOT));
                return ValueType.BOOL;
            }
            case GE -> {
                addInstructionInOperatorExpr(valueType, function, Operation.CMP_I, Operation.CMP_F, "数据类型不对，不能比较>=");
                function.addInstruction(new Instruction(Operation.SET_LT));
                function.addInstruction(new Instruction(Operation.NOT));
                return ValueType.BOOL;
            }
            case LT -> {
                addInstructionInOperatorExpr(valueType, function, Operation.CMP_I, Operation.CMP_F, "数据类型不对，不能比较<");
                function.addInstruction(new Instruction(Operation.SET_LT));
                return ValueType.BOOL;
            }
            case GT -> {
                addInstructionInOperatorExpr(valueType, function, Operation.CMP_I, Operation.CMP_F, "数据类型不对，不能比较>");
                function.addInstruction(new Instruction(Operation.SET_GT));
                return ValueType.BOOL;
            }
            case NEQ -> {
                addInstructionInOperatorExpr(valueType, function, Operation.CMP_I, Operation.CMP_F, "数据类型不对，不能比较!=");
                return ValueType.BOOL;
            }
            case EQ -> {
                addInstructionInOperatorExpr(valueType, function, Operation.CMP_I, Operation.CMP_F, "数据类型不对，不能比较==");
                function.addInstruction(new Instruction(Operation.NOT));
                return ValueType.BOOL;
            }
            default -> throw new AnalyzeError(ErrorCode.InvalidVariableType, currentToken().getStartPos(), "operator类型不对");
        }
    }

    private void addInstructionInOperatorExpr(ValueType valueType, Function function, Operation operation1, Operation operation2, String message) throws AnalyzeError {
        if (valueType == ValueType.INT)
            function.addInstruction(new Instruction(operation1));
        else if (valueType == ValueType.DOUBLE)
            function.addInstruction(new Instruction(operation2));
        else
            throw new AnalyzeError(ErrorCode.InvalidVariableType, currentToken().getStartPos(), message);
    }

    private ValueType analyseNegateExpr(Function function) throws CompileError {
        // negate_expr -> '-' expr
        expect(TokenType.MINUS);
        ValueType type = analyseExpr(function);
        if (type == ValueType.INT)
            function.addInstruction(new Instruction(Operation.NEG_I));
        else if (type == ValueType.DOUBLE)
            function.addInstruction(new Instruction(Operation.NEG_F));
        return type;
    }

    private ValueType analyseAssignExpr(Function function) throws CompileError {
        // assign_expr -> l_expr '=' expr
        // l_expr -> IDENT
        String name = expect(TokenType.IDENT).getValueString();
        Variable l_expr = (Variable) symbolTableStack.getSymbolByName(name, peek().getStartPos());
        if (l_expr.isConst())
            throw new AnalyzeError(ErrorCode.ConstValueChanged, peek().getStartPos(), "不能对const变量进行赋值");
        expect(TokenType.ASSIGN);
        if (l_expr.getSymbolType() == SymbolType.GLOBAL)
            function.addInstruction(new Instruction(Operation.GLOBA, l_expr.getAddress()));
        else if (l_expr.getSymbolType() == SymbolType.PARAM)
            function.addInstruction(new Instruction(Operation.ARGA, l_expr.getAddress()));
        else if (l_expr.getSymbolType() == SymbolType.LOCAL)
            function.addInstruction(new Instruction(Operation.LOCA, l_expr.getAddress()));
        else
            throw new AnalyzeError(ErrorCode.InvalidAssign, peek().getStartPos(), "非法赋值，l_expr不属于GLOBAL/PARA/LOCAL");
        ValueType type = analyseExpr(function);
        if (type != l_expr.getValueType())
            throw new AnalyzeError(ErrorCode.InvalidAssign, peek().getStartPos(), "非法赋值，左右表达式类型不同");
        function.addInstruction(new Instruction(Operation.STORE_64));
        return ValueType.VOID;
    }

    private ValueType analyseCallExpr(Function function) throws CompileError {
        // call_param_list -> expr (',' expr)*
        // call_expr -> IDENT '(' call_param_list? ')'
        String name = expect(TokenType.IDENT).getValueString();

        // 首先检查是否是标准库中的函数
        switch (name) {
            case "getint" -> {
                return standardIn(Operation.SCAN_I, ValueType.INT, function);
            }
            case "getdouble" -> {
                return standardIn(Operation.SCAN_F, ValueType.DOUBLE, function);
            }
            case "getchar" -> {
                return standardIn(Operation.SCAN_C, ValueType.INT, function);
            }
            case "putln" -> {
                return standardIn(Operation.PRINTLN, ValueType.VOID, function);
            }
            case "putint" -> {
                return standardOut(Operation.PRINT_I, ValueType.INT, function);
            }
            case "putdouble" -> {
                return standardOut(Operation.PRINT_F, ValueType.DOUBLE,function);
            }
            case "putchar" -> {
                return standardOut(Operation.PRINT_C, ValueType.INT, function);
            }
            case "putstr" -> {
                expect(TokenType.L_PAREN);
                if (peek().getTokenType() == TokenType.STRING_LITERAL)
                    analyseLiteralExpr(function);
                else if (analyseExpr(function) != ValueType.INT)
                    throw new AnalyzeError(ErrorCode.TypeNotMatch, peek().getStartPos(), "putstr参数应该是int类型");
                expect(TokenType.R_PAREN);
                function.addInstruction(new Instruction(Operation.PRINT_S));
                return ValueType.VOID;
            }
        }
        Function calledFunc;
        // 是否是递归调用
        if(name.equals(function.getName())){
            calledFunc = function;
        }
        else calledFunc = funcTable.searchFunc(name, currentToken().getStartPos());

        if(calledFunc.getReturnValueType() != ValueType.VOID){
            int temp = 1;
            function.addInstruction(new Instruction(Operation.STACKALLOC, temp));
        }

        // 准备参数，并检查参数列表和函数的声明是否匹配，包括参数个数和类型
        expect(TokenType.L_PAREN);

        for (ValueType type : calledFunc.getParamValueTypeList()){
            if (peek().getTokenType() == TokenType.R_PAREN)
                throw new AnalyzeError(ErrorCode.ParamNotEnough, peek().getStartPos(), "函数参数数目不足");
            else if (type != analyseExpr(function))
                throw new AnalyzeError(ErrorCode.TypeNotMatch, peek().getStartPos(), "函数参数类型不匹配");
            if (peek().getTokenType() != TokenType.COMMA && peek().getTokenType() != TokenType.R_PAREN)
                throw new AnalyzeError(ErrorCode.InvalidCallList, peek().getStartPos(), "函数列表结构非法");
            if (peek().getTokenType() == TokenType.COMMA)
                next();
        }

        expect(TokenType.R_PAREN);

        function.addInstruction(new Instruction(Operation.CALL, calledFunc.getAddress()));
        return calledFunc.getReturnValueType();
    }

    private ValueType standardIn(Operation operation, ValueType valueType, Function function) throws CompileError {
        expect(TokenType.L_PAREN);
        expect(TokenType.R_PAREN);
        function.addInstruction(new Instruction(operation));
        return valueType;
    }

    private ValueType standardOut(Operation operation, ValueType valueType, Function function) throws CompileError {
        expect(TokenType.L_PAREN);
        if (analyseExpr(function) != valueType)
            throw new AnalyzeError(ErrorCode.TypeNotMatch, peek().getStartPos(), "参数应该是int类型");
        expect(TokenType.R_PAREN);
        function.addInstruction(new Instruction(operation));
        return ValueType.VOID;
    }

    private ValueType analyseLiteralExpr(Function function) throws AnalyzeError {
        // literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL ｜ CHAR_LITERAL
        TokenType t = peek().getTokenType();
        if (t == TokenType.UINT_LITERAL){
            long value = Long.parseLong(peek().getValueString());
            function.addInstruction(new Instruction(Operation.PUSH, value));
            next();
            return ValueType.INT;
        }
        else if (t == TokenType.DOUBLE_LITERAL){
            double value = Double.parseDouble(peek().getValueString());
            function.addInstruction(new Instruction(Operation.PUSH, value));
            next();
            return ValueType.DOUBLE;
        }
        else if (t == TokenType.CHAR_LITERAL){
            long value = peek().getValueString().charAt(0);
            function.addInstruction(new Instruction(Operation.PUSH, value));
            next();
            return ValueType.INT;
        }
        else if (t == TokenType.STRING_LITERAL){
            SymbolTable globalTable = symbolTableStack.get(0);
            Variable variable = new Variable("", SymbolType.GLOBAL, ValueType.STRING);
            variable.setLength(peek().getValueString().length());
            int address = globalTable.getSymbolLength();
            variable.setAddress(address);

            funcNameAndStringMap.put(address, peek().getValueString());
            globalTable.addSymbol(variable, currentToken().getStartPos());
            function.addInstruction(new Instruction(Operation.PUSH, (long)address));
            next();
            return ValueType.INT;
        }
        return ValueType.VOID;
    }

    private ValueType analyseIdentExpr(Function function) throws CompileError {
        // ident_expr -> IDENT
        String name = expect(TokenType.IDENT).getValueString();
        Variable var = (Variable)symbolTableStack.getSymbolByName(name, currentToken().getStartPos());
        if (var.getSymbolType() == SymbolType.GLOBAL)
            function.addInstruction(new Instruction(Operation.GLOBA, var.getAddress()));
        else if (var.getSymbolType() == SymbolType.PARAM)
            function.addInstruction(new Instruction(Operation.ARGA, var.getAddress()));
        else if (var.getSymbolType() == SymbolType.LOCAL)
            function.addInstruction(new Instruction(Operation.LOCA, var.getAddress()));
        function.addInstruction(new Instruction(Operation.LOAD_64));
        return var.getValueType();
    }

    private ValueType analyseGroupExpr(Function function) throws CompileError {
        // group_expr -> '(' expr ')'
        expect(TokenType.L_PAREN);
        ValueType ret = analyseExpr(function);
        expect(TokenType.R_PAREN);
        return ret;
    }

    private boolean less(TokenType a,TokenType b)
    {
        // 只用考虑 + -  * / 和比较
        if(a == b) return false;
        else if(a == TokenType.SHARP) return b != TokenType.SHARP;
        else if(b == TokenType.SHARP) return false;
        else if(a == TokenType.LT || a == TokenType.GT || a == TokenType.LE || a == TokenType.GE){
            if(b == TokenType.LT || b == TokenType.GT || b == TokenType.LE || b == TokenType.GE || b == TokenType.SHARP)
                return false;
            return true;
        }
        else if(a == TokenType.PLUS || a == TokenType.MINUS){
            if(b == TokenType.MUL || b == TokenType.DIV) return true;
            return false;
        }
        else return false;
    }
}
