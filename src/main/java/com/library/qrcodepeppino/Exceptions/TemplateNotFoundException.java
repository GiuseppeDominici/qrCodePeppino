package com.library.qrcodepeppino.Exceptions;

public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String templateName) {
        super("Il template selezionato è vuoto o non è valido: " + templateName);
    }

}
