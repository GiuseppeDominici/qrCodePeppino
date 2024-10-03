package com.library.qrcodepeppino.Exceptions;

public class BorderColorNotValidException extends RuntimeException {
    public BorderColorNotValidException(String borderColor) {
        super("Inserire un colore valido per: " + borderColor);
    }
}
