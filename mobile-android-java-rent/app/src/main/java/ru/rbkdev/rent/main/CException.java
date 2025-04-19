package ru.rbkdev.rent.main;

public class CException extends Exception {

    private final String m_message;

    public CException(String message) {
        this.m_message = message;
    }

    @Override
    public String getMessage() {
        return m_message;
    }
}