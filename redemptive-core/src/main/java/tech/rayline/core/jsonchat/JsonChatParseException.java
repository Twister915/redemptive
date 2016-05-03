package tech.rayline.core.jsonchat;

public final class JsonChatParseException extends Exception {
    public JsonChatParseException(String message) {
        super(message);
    }

    public JsonChatParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
