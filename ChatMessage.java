package com.company;
import java.io.*;

public class ChatMessage implements Serializable {

    protected static final long serialVersionUID = 1112122200L;

    static final int MESSAGE = 1, close = 2;

    private int type;

    private String message;

    ChatMessage(int type, String message) {

        this.type = type;

        this.message = message;

    }
    int getType() {
        return type;

    }

    String getMessage() {

        return message;
    }
}
