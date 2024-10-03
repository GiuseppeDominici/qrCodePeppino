package com.library.qrcodepeppino.Exceptions;

public class LogoNotFoundException extends RuntimeException {
    public LogoNotFoundException() {
        super("Logo non trovato");
    }
}
