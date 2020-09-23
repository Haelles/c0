package error;

public enum ErrorCode {
    ErrNoError, // Should be only used internally.
    ErrStreamError,
    ErrEOF,
    ErrInvalidInput,
    ErrInvalidIdentifier,
    ErrIntegerOverflow, // int32_t overflow.
    ErrNoBegin,
    ErrNoEnd,
    ErrNeedIdentifier,
    ErrConstantNeedValue,
    ErrNoSemicolon,
    ErrInvalidVariableDeclaration,
    ErrIncompleteExpression,
    ErrNotDeclared,
    ErrAssignToConstant,
    ErrDuplicateDeclaration,
    ErrNotInitialized,
    ErrInvalidAssignment,
    ErrInvalidPrint
}
