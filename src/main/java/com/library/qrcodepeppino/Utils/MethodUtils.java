package com.library.qrcodepeppino.Utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.library.qrcodepeppino.Model.RequestData;
import com.library.qrcodepeppino.Model.ResponseImage;
import com.library.qrcodepeppino.Model.SeparateFields;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.*;

public class MethodUtils {

    public static byte[] generateQrCodeImage(RequestData requestData) throws IOException, RuntimeException, WriterException {
        validateRequestData(requestData);

        setDefaultColorsIfNeeded(requestData);

        BufferedImage qrImage = createQrCodeImage(requestData);

        qrImage = applyCustomizations(requestData, qrImage);

        return convertImageToByteArray(qrImage);
    }

    private static void validateRequestData(RequestData requestData) {
        if (requestData.getQrWidth() == 0 && requestData.getQrHeight() == 0) {
            requestData.setQrWidth(200);
            requestData.setQrHeight(200);
        } else if (requestData.getQrWidth() < 200 || requestData.getQrHeight() < 200) {
            throw new RuntimeException("Se vuoi personalizzare le dimensioni, devono essere minimo 200");
        }

        if (!StringUtils.hasLength(requestData.getRequestUrl())) {
            throw new RuntimeException("Specificare un URL");
        }
    }

    private static void setDefaultColorsIfNeeded(RequestData requestData) {
        if (!StringUtils.hasLength(requestData.getBackgroundColor())) {
            requestData.setBackgroundColor("#ffffff");
        }
        if (!StringUtils.hasLength(requestData.getQrCodeColor())) {
            requestData.setQrCodeColor("#000000");
        }
    }

    private static BufferedImage createQrCodeImage(RequestData requestData) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                requestData.getRequestUrl(),
                BarcodeFormat.QR_CODE,
                requestData.getQrWidth(),
                requestData.getQrHeight()
        );

        MatrixToImageConfig config = new MatrixToImageConfig(
                requestData.getQrCodeColorAsColor().getRGB(),
                requestData.getBackgroundColorAsColor().getRGB()
        );

        return MatrixToImageWriter.toBufferedImage(bitMatrix, config);
    }

    private static BufferedImage applyCustomizations(RequestData requestData, BufferedImage image) throws IOException {
        int whiteBoxSize = 0;

        if (StringUtils.hasLength(requestData.getLogoCenterUrl())) {
            whiteBoxSize = (int) (Math.min(requestData.getQrWidth(), requestData.getQrHeight()) * 0.135);
            image = addWhiteBox(requestData, image, whiteBoxSize);
        }

        image = addBordersIfProvided(requestData, image);

        image = addCenterLogoIfProvided(requestData, image, whiteBoxSize);

        addLogoOrTextToBorderIfProvided(requestData, image, whiteBoxSize);

        return image;
    }

    private static byte[] convertImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    public static BufferedImage addWhiteBox(RequestData requestData, BufferedImage image, int whiteBoxSize) {
        BufferedImage modifiedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D qrGraphics = modifiedImage.createGraphics();
        qrGraphics.drawImage(image, 0, 0, null);

        int whiteBoxX = (requestData.getQrWidth() - whiteBoxSize) / 2;
        int whiteBoxY = (requestData.getQrHeight() - whiteBoxSize) / 2;

        qrGraphics.setColor(Color.WHITE);
        qrGraphics.fillRect(whiteBoxX, whiteBoxY, whiteBoxSize, whiteBoxSize);

        qrGraphics.dispose();

        return modifiedImage;
    }

    public static BufferedImage addCenterLogoIfProvided(RequestData requestData, BufferedImage image, int whiteBoxSize) throws IOException {
        if (!StringUtils.hasLength(requestData.getLogoCenterUrl())) {
            return image;
        }

        BufferedImage centerLogo = loadAndResizeLogo(requestData.getLogoCenterUrl(), whiteBoxSize, whiteBoxSize);  // Passa sia larghezza che altezza

        return addLogoToCenter(image, centerLogo, requestData.getTopBorderSize(), requestData.getBottomBorderSize(),
                requestData.getLeftBorderSize(), requestData.getRightBorderSize());
    }

    private static BufferedImage loadAndResizeLogo(String logoUrl, int width, int height) throws IOException {
        BufferedImage logoImage = loadImageFromUrl(logoUrl);

        return resizeImage(logoImage, width, height);
    }

    public static BufferedImage addBordersIfProvided(RequestData requestData, BufferedImage image) {
        boolean hasBorders = requestData.getTopBorderSize() != 0 || requestData.getBottomBorderSize() != 0 ||
                requestData.getLeftBorderSize() != 0 || requestData.getRightBorderSize() != 0;

        boolean hasBorderColor = StringUtils.hasLength(requestData.getBorderColor());

        boolean hasTextBorder = StringUtils.hasLength(requestData.getTextBorder());

        if (!hasBorders && (hasBorderColor || hasTextBorder)) {
            throw new RuntimeException("Inserire almeno un bordo per inserire il colore o il testo");
        }

        if (hasBorders && !hasBorderColor) {
            throw new RuntimeException("Inserire un colore per i bordi");
        }

        if (hasBorders) {
            return addBorder(image,
                    requestData.getTopBorderSize(),
                    requestData.getBottomBorderSize(),
                    requestData.getLeftBorderSize(),
                    requestData.getRightBorderSize(),
                    requestData.getBorderColorAsColor());
        }

        return image;
    }

    public static BufferedImage addLogoOrTextToBorderIfProvided(RequestData requestData, BufferedImage image, int whiteBoxSize) throws IOException {
        if (requestData.getTopOrBottom() == null) {
            throw new RuntimeException("Specificare il bordo sul quale inserire testo e logo");
        }

        String position = requestData.getTopOrBottom().toLowerCase();
        int borderSize = "top".equals(position) ? requestData.getTopBorderSize() : requestData.getBottomBorderSize();

        if (borderSize < 60) {
            throw new RuntimeException("Il bordo su cui inserire il logo o il testo deve essere minimo 60");
        }

        if (StringUtils.hasLength(requestData.getLogoBorderUrl())) {
            BufferedImage borderLogo = loadAndResizeLogo(requestData.getLogoBorderUrl(), whiteBoxSize, whiteBoxSize); // Usa il metodo esistente
            image = addLogoToBorder(image, borderLogo, 20, 10, borderSize, requestData.getTopOrBottom());
        }

        if (StringUtils.hasLength(requestData.getTextBorder())) {
            addTextToBorder(image, requestData.getTextBorder(), Color.BLACK, 20, borderSize, requestData.getTopOrBottom());
        }

        return image;
    }

    public static byte[] generateQrCodeBase(String text, RequestData requestData) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        if (!StringUtils.hasLength(requestData.getRequestUrl())) {
            throw new RuntimeException("Specificare un URL");
        }

        int width = requestData.getQrWidth() > 0 ? requestData.getQrWidth() : 200;
        int height = requestData.getQrHeight() > 0 ? requestData.getQrHeight() : 200;

        if (width < 200 || height < 200) {
            throw new RuntimeException("Le dimensioni devono essere almeno 200x200");
        }

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            MatrixToImageConfig config = new MatrixToImageConfig(
                    requestData.getQrCodeColorAsColor().getRGB(),
                    requestData.getBackgroundColorAsColor().getRGB()
            );

            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream, config);

            return pngOutputStream.toByteArray();
        }
    }

    public static byte[] qrCodeResult(RequestData requestData) throws WriterException, IOException {
        boolean areZero = areAllZero(separateFields(requestData).getIntFields());
        boolean emptyString = containsOnlyEmptyStrings(separateFields(requestData).getStringFields());

        int width = requestData.getQrWidth();
        int height = requestData.getQrHeight();

        if (areZero && emptyString) {
            width = 350;
            height = 350;
            System.out.println("Condizione base");
            return generateQrCodeBase(requestData.getRequestUrl(), requestData);
        }

        if (emptyString && (width != 0 || height != 0)) {
            if (width < 200 || height < 200) {
                throw new RuntimeException("Se vuoi personalizzare le dimensioni devono essere minimo 200");
            }
            System.out.println("Condizione base con controllo");
            return generateQrCodeBase(requestData.getRequestUrl(), requestData);
        }

        return generateQrCodeImage(requestData);
    }

    public static SeparateFields separateFields(RequestData requestData) {
        List<String> stringFields = new ArrayList<>();
        List<Integer> intFields = new ArrayList<>();

        Field[] fields = requestData.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true); // Permette l'accesso a campi privati
            String fieldName = field.getName();

            if (!fieldName.equals("requestUrl")) {
                try {
                    Object value = field.get(requestData);
                    if (value instanceof String) {
                        stringFields.add((String) value);
                    } else if (value instanceof Integer) {
                        intFields.add((Integer) value);
                    } else {
                        System.out.println("Campo ignorato: " + fieldName + " (tipo: " + value.getClass().getSimpleName() + ")");
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Accesso al campo fallito: " + fieldName, e);
                }
            }
        }

        return new SeparateFields(new ArrayList<>(stringFields), new ArrayList<>(intFields));
    }

    public static boolean areAllZero(List<Integer> l) {
        return l.stream().allMatch(value -> value == 0);
    }

    public static boolean containsOnlyEmptyStrings(List<String> l) {
        return l.stream().allMatch(string -> string == null || string.trim().isEmpty());
    }

    public static ResponseImage result(byte[] qrBytes) throws IOException {
        if (qrBytes == null || qrBytes.length == 0) {
            throw new IllegalArgumentException("L'array di byte QR non può essere nullo o vuoto");
        }

        BufferedImage qrCodeImage;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(qrBytes)) {
            qrCodeImage = ImageIO.read(inputStream);
            if (qrCodeImage == null) {
                throw new IOException("Impossibile leggere l'immagine QR dal byte array");
            }
        }

        String base64;
        try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(qrCodeImage, "PNG", pngOutputStream);
            base64 = Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
        }

        base64 = "data:image/png;base64," + base64;

        ResponseImage response = new ResponseImage();
        response.setImageBase64(base64);
        return response;
    }

    public static ResponseEntity<Object> handleRuntimeException(RuntimeException e) {
        Map<Class<? extends RuntimeException>, HttpStatus> exceptionMap = new HashMap<>() {{
            put(RuntimeException.class, HttpStatus.BAD_REQUEST);
            put(RuntimeException.class, HttpStatus.BAD_REQUEST);
            put(RuntimeException.class, HttpStatus.BAD_REQUEST);
            put(RuntimeException.class, HttpStatus.BAD_REQUEST);
            put(RuntimeException.class, HttpStatus.BAD_REQUEST);
            put(ColorNotValidException.class, HttpStatus.BAD_REQUEST);
        }};

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = "An unexpected error occurred";

        if (exceptionMap.containsKey(e.getClass())) {
            status = exceptionMap.get(e.getClass());
            errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
        }

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);

        return ResponseEntity.status(status).body(errorResponse);
    }

    private static BufferedImage loadImageFromUrl(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("L'URL dell'immagine non può essere nullo o vuoto.");
        }

        try {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);

            if (image == null) {
                throw new IOException("Impossibile caricare l'immagine dall'URL: " + imageUrl);
            }

            return image;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("L'URL fornito non è valido: " + imageUrl, e);
        } catch (IOException e) {
            throw new IOException("Errore nel caricamento dell'immagine dall'URL: " + imageUrl, e);
        }
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        if (originalImage == null) {
            throw new IllegalArgumentException("L'immagine originale non può essere null.");
        }

        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException("Le dimensioni di destinazione devono essere maggiori di zero.");
        }

        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = outputImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.drawImage(originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);

        g2d.dispose();

        return outputImage;
    }

    public static BufferedImage addLogoToCenter(BufferedImage baseImage, BufferedImage logo, int topBorderSize, int bottomBorderSize, int leftBorderSize, int rightBorderSize) {
        if (baseImage == null) {
            throw new IllegalArgumentException("L'immagine di base non può essere null.");
        }
        if (logo == null) {
            throw new IllegalArgumentException("Il logo non può essere null.");
        }

        int effectiveWidth = baseImage.getWidth() - leftBorderSize - rightBorderSize;
        int effectiveHeight = baseImage.getHeight() - topBorderSize - bottomBorderSize;

        if (logo.getWidth() > effectiveWidth || logo.getHeight() > effectiveHeight) {
            throw new IllegalArgumentException("Il logo è troppo grande per essere centrato nell'area disponibile.");
        }

        int logoX = (effectiveWidth - logo.getWidth()) / 2 + leftBorderSize;
        int logoY = (effectiveHeight - logo.getHeight()) / 2 + topBorderSize;

        BufferedImage imageWithLogo = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imageWithLogo.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(baseImage, 0, 0, null);
        g.drawImage(logo, logoX, logoY, null);

        g.dispose();

        return imageWithLogo;
    }

    public static BufferedImage addBorder(BufferedImage img, int topBorderSize, int bottomBorderSize, int leftBorderSize, int rightBorderSize, Color borderColor) {
        if (img == null) {
            throw new IllegalArgumentException("L'immagine non può essere null.");
        }

        if (topBorderSize < 0 || bottomBorderSize < 0 || leftBorderSize < 0 || rightBorderSize < 0) {
            throw new IllegalArgumentException("Le dimensioni del bordo non possono essere negative.");
        }

        int newWidth = img.getWidth() + leftBorderSize + rightBorderSize;
        int newHeight = img.getHeight() + topBorderSize + bottomBorderSize;

        BufferedImage imgWithBorder = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imgWithBorder.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(borderColor);
        g.fillRect(0, 0, newWidth, newHeight);

        g.drawImage(img, leftBorderSize, topBorderSize, null);
        g.dispose();

        return imgWithBorder;
    }

    public static BufferedImage addLogoToBorder(BufferedImage baseImage, BufferedImage logo, int fontSize, int logoMargin, int borderSize, String topOrBottom) {
        if (baseImage == null) {
            throw new IllegalArgumentException("L'immagine di base non può essere null.");
        }
        if (logo == null) {
            throw new IllegalArgumentException("Il logo non può essere null.");
        }

        int logoY;
        if (topOrBottom.equalsIgnoreCase("top")) {
            logoY = logoMargin;
        } else if (topOrBottom.equalsIgnoreCase("bottom")) {
            logoY = baseImage.getHeight() - logo.getHeight() - logoMargin;
        } else {
            throw new IllegalArgumentException("Specificare 'top' o 'bottom' per la posizione del logo.");
        }

        Graphics2D g2 = baseImage.createGraphics();
        g2.drawImage(logo, logoMargin, logoY, null);
        g2.dispose();

        return baseImage;
    }

    public static BufferedImage addTextToBorder(BufferedImage img, String text, Color textColor, int fontSize, int borderSize, String topOrBottom) {
        if (img == null) {
            throw new IllegalArgumentException("L'immagine non può essere null.");
        }
        if (text == null || text.isEmpty()) {
            return img;
        }

        Graphics2D g = img.createGraphics();
        g.setColor(textColor);
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g.setFont(font);

        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();

        int textX = (img.getWidth() - textWidth) / 2;
        int textY;

        if (topOrBottom.equalsIgnoreCase("top")) {
            textY = borderSize / 2 + textHeight / 2;
        } else if (topOrBottom.equalsIgnoreCase("bottom")) {
            textY = img.getHeight() - borderSize / 2 + textHeight / 2;
        } else {
            throw new IllegalArgumentException("Specificare 'top' o 'bottom' per la posizione del testo.");
        }

        g.drawString(text, textX, textY);
        g.dispose();

        return img;
    }

    public static Color convertHexToColor(String colorHex, String colorField) {
        if (colorHex == null || !colorHex.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
            throw new ColorNotValidException("Il codice del colore per " + colorField + " non è valido: " + colorHex);
        }
        return Color.decode(colorHex);
    }

    public static class ColorNotValidException extends RuntimeException {
        public ColorNotValidException(String message) {
            super(message);
        }
    }

}