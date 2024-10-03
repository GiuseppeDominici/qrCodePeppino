package com.library.qrcodepeppino.Exceptions;

public class ImageNullException extends RuntimeException {
    public ImageNullException() {
        super("L'imagine non può essere vuota");
    }
}
