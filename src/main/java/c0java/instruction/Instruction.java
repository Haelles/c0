package c0java.instruction;

import java.util.Objects;

public class Instruction {
    public static int[] instructionToBinaryCode = {
              0x00
            , 0x01
            , 0x02
            , 0x03
            , 0x04
            , 0x0a
            , 0x0b
            , 0x0c
            , 0x10
            , 0x11
            , 0x12
            , 0x13
            , 0x14
            , 0x15
            , 0x16
            , 0x17
            , 0x18
            , 0x19
            , 0x1a
            , 0x20
            , 0x21
            , 0x22
            , 0x23
            , 0x24
            , 0x25
            , 0x26
            , 0x27
            , 0x28
            , 0x29
            , 0x2a
            , 0x2b
            , 0x2c
            , 0x2d
            , 0x2e
            , 0x30
            , 0x31
            , 0x32
            , 0x34
            , 0x35
            , 0x36
            , 0x37
            , 0x38
            , 0x39
            , 0x3a
            , 0x41
            , 0x42
            , 0x43
            , 0x48
            , 0x49
            , 0x4a
            , 0x50
            , 0x51
            , 0x52
            , 0x54
            , 0x55
            , 0x56
            , 0x57
            , 0x58
            , 0xfe };
    private Operation opt;
    Integer x = null;
    Long x1 = null;
    Double x2 = null;
    Integer recordBreak = 0; //
    int chooseDataType = 0;

    public Instruction(Operation opt) {
        this.opt = opt;
        this.x = 0;
        chooseDataType = 0; // 没有操作数
    }

    public Instruction(Operation opt, Integer x) {
        this.opt = opt;
        this.x = x;
        chooseDataType = 1; // int操作数
    }

    public Instruction(Operation opt, Long x) {
        this.opt = opt;
        this.x1 = x;
        chooseDataType = 2; // long
    }

    public Instruction(Operation opt, Double x) {
        this.opt = opt;
        this.x2 = x;
        chooseDataType = 3; // double
    }

    public Instruction(Operation opt, Integer x, Integer recordBreak) {
        this.opt = opt;
        this.x = x;
        this.recordBreak = recordBreak;
        chooseDataType = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, x);
    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public Integer getX() {
        return x;
    }

    public Long getX1() {
        return x1;
    }

    public Double getX2() {
        return x2;
    }

    public Integer getRecordBreak(){
        return recordBreak;
    }

    public int operationToInt(){
        return opt.ordinal();
    }

    public int getChooseDataType(){
        return chooseDataType;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    @Override
    public String toString() {
        return switch (this.opt) {
            case NOP -> "nop   ";
            case PUSH -> "push  " + (x1 != null? x1: x2);
            case POP -> "pop   ";
            case POPN -> "popn  ";
            case DUP -> "dup   ";
            case LOCA -> "loca  " + x;
            case GLOBA -> "globa " + x;
            case ARGA -> "arga  " + x;
            case LOAD_64 -> "load.64";
            case STORE_64 -> "store.64";
            case ALLOC -> "alloc   ";
            case FREE -> "free    ";
            case STACKALLOC -> "stackalloc  " + x;
            case ADD_I -> "add.i    ";
            case ADD_F -> "add.f    ";
            case SUB_F -> "sub.f    ";
            case SUB_I -> "sub.i    ";
            case MUL_I -> "mul.i    ";
            case MUL_F -> "mul.f    ";
            case DIV_I -> "div.i    ";
            case DIV_F -> "div.f    ";
            case DIV_U -> "div.u    ";
            case SHL -> "shl    ";
            case SHR -> "shr    ";
            case SHRL -> "shrl   ";
            case AND -> "and    ";
            case OR -> "or    ";
            case NOT -> "not    ";
            case XOR -> "xor    ";
            case CMP_I -> "cmp.i    ";
            case CMP_F -> "cmp.f    ";
            case CMP_U -> "cmp.u    ";
            case NEG_I -> "neg.i    ";
            case NEG_F -> "neg.f    ";
            case ITOF -> "itof    ";
            case FTOI -> "ftoi    ";
            case SET_GT -> "set.gt    ";
            case SET_LT -> "set.lt    ";
            case BR -> "br    " + x;
            case BR_FALSE -> "br.false  " + x;
            case BR_TRUE -> "br.true   " + x;
            case RET -> "ret    ";
            case CALL -> "call    " + x;
            case CALLNAME -> "callname    " + x;
            case SCAN_C -> "scan.c    ";
            case SCAN_F -> "scan.f    ";
            case SCAN_I -> "scan.i    ";
            case PRINT_C -> "print.c    ";
            case PRINT_F -> "print.f    ";
            case PRINT_I -> "print.i    ";
            case PRINT_S -> "print.s    ";
            case PRINTLN -> "println    ";
            case PANIC -> "panic    ";
            default -> "ERROR";
        };
    }
}
