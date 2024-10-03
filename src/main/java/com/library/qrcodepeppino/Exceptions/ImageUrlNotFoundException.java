package com.library.qrcodepeppino.Exceptions;

public class ImageUrlNotFoundException extends RuntimeException {
    public ImageUrlNotFoundException(String imgUrl) {
        super("Impossibile caricare l'immagine dall'URL: " + imgUrl);
    }
}
