package com.library.qrcodepeppino.Exceptions;

public class InvalidDimensionLogoException extends RuntimeException {
    public InvalidDimensionLogoException() {
        super("Le dimensioni del logo sono invalide. Selezionare un'altro logo");
    }
}
