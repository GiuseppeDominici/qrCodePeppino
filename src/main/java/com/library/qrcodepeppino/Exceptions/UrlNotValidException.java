package com.library.qrcodepeppino.Exceptions;

public class UrlNotValidException extends RuntimeException {

    public UrlNotValidException(String url) {
        super("L'url inserito non è valido: " + url);
    }

}
