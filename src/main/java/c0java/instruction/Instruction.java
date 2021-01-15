package c0java.instruction;

import java.util.Objects;

public class Instruction {
    private Operation opt;
    Integer x;
    Long x1;
    Double x2;
    Integer recordBreak = 0; //

    public Instruction(Operation opt) {
        this.opt = opt;
        this.x = 0;
    }

    public Instruction(Operation opt, Integer x) {
        this.opt = opt;
        this.x = x;
    }

    public Instruction(Operation opt, Long x) {
        this.opt = opt;
        this.x1 = x;
    }

    public Instruction(Operation opt, Double x) {
        this.opt = opt;
        this.x2 = x;
    }

    public Instruction(Operation opt, Integer x, Integer recordBreak) {
        this.opt = opt;
        this.x = x;
        this.recordBreak = recordBreak;
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

    public Integer getRecordBreak(){
        return recordBreak;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    @Override
    public String toString() {
        switch (this.opt) {
            case ADD:
            case DIV:
            case ILL:
            case MUL:
            case SUB:
            case WRT:
                return String.format("%s", this.opt);
            case LIT:
            case LOD:
            case STO:
                return String.format("%s %s", this.opt, this.x);
            default:
                return "ILL";
        }
    }
}
