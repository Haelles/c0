package c0java.error;

public class OutputError extends Exception{
    private String message;
    public OutputError(String message){
        this.message = message;
    }

    @Override
    public String toString(){
        return message;
    }
}
