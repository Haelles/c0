package c0java.error;

public enum ErrorCode {
    NoError, // Should be only used internally.
    StreamError,
    EOF,
    InvalidInput,
    InvalidIdentifier,
    IntegerOverflow, // int32_t overflow.
    NeedIdentifier,
    ConstantNeedValue,
    NoSemicolon,
    InvalidVariableDeclaration,
    IncompleteExpression,
    NotDeclared,
    AssignToConstant,
    DuplicateDeclaration,
    NotInitialized,
    InvalidAssignment,
    InvalidPrint,
    ExpectedToken,
    DuplicateDeclare,
    InvalidVariableType,
    InvalidReturnTYpe,
    InvalidStructure,
    IndexOutOfBound,
    TypeNotMatch,
    VariableNotDecl,
    ConstValueChanged,
    InvalidAssign,
    ParamNotEnough,
    InvalidCallList
}
