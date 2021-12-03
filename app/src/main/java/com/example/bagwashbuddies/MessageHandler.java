package com.example.bagwashbuddies;

public class MessageHandler {
    private final String message;
    private final String sender;

    public MessageHandler(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }
}
