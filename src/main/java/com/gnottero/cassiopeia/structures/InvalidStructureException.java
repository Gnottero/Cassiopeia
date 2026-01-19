package com.gnottero.cassiopeia.structures;




public class InvalidStructureException extends Exception {
    public InvalidStructureException() {
        super();
    }

    // Constructor with message
    public InvalidStructureException(String message) {
        super(message);
    }
}
