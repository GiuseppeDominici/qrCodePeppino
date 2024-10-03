package com.library.qrcodepeppino.Exceptions;

public class ImageUrlNotValidException extends RuntimeException {
    public ImageUrlNotValidException(String imgUrl) {
        super("L'URL fornito non è valido: " + imgUrl);
    }
}
