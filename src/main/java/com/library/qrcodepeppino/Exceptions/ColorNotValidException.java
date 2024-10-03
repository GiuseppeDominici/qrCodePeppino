package com.library.qrcodepeppino.Exceptions;

public class ColorNotValidException extends RuntimeException {

    public ColorNotValidException(String colorHex, String colorField) {
        super("Il codice del colore per " + colorHex + " non è valido: " + colorField);
    }
}
