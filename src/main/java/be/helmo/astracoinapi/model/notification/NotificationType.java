package be.helmo.astracoinapi.model.notification;

public enum NotificationType {

    ONE_PERCENT_UP("Plus value de +1%"),

    ONE_PERCENT_DOWN("Plus value de -1%");

    private final String msg;

    NotificationType(String msg){
        this.msg = msg;
    }


    public String getMsg() {
        return msg;
    }
}
