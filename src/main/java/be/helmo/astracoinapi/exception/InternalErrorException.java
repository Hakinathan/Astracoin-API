package be.helmo.astracoinapi.exception;

public class InternalErrorException extends Throwable{
    private final String msg;

    public InternalErrorException(String msg){

        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
