package com.hadii.striff.diagram.plantuml;

public class PUMLDrawException extends Exception {

    public PUMLDrawException(String message, Exception e) {
        super(message, e);
    }

    public PUMLDrawException(String message) {
        super(message);
    }
}
