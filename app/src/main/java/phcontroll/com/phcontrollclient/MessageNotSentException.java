package phcontroll.com.phcontrollclient;

public class MessageNotSentException extends Exception {
    private static final long serialVersionUID = -3279582061542873813L;

    public MessageNotSentException(String message) {
        super(message);
    }
}