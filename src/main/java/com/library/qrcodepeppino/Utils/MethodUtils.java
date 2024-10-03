package com.library.qrcodepeppino.Utils;

import com.library.qrcodepeppino.Exceptions.*;
import com.library.qrcodepeppino.Model.RequestData;
import com.library.qrcodepeppino.Model.ResponseImage;
import com.library.qrcodepeppino.Model.SeparateFields;
import com.library.qrcodepeppino.Model.TemplateModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

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

import static com.library.qrcodepeppino.Utils.TemplateConfig.getTemplateByName;

public class MethodUtils {

    private static final String regex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

    public static byte[] generateQrCodeImage(RequestData requestData) throws IOException, RuntimeException, WriterException {
        validateRequestData(requestData);

        setDefaultColorsIfNeeded(requestData);

        BufferedImage qrImage = createQrCodeImage(requestData);

        qrImage = applyCustomizations(requestData, qrImage);

        return convertImageToByteArray(qrImage);
    }

    private static void validateRequestData(RequestData requestData) {
        if (!StringUtils.hasLength(requestData.getRequestUrl())) {
            throw new UrlNotValidException(requestData.getRequestUrl());
        }

        if (requestData.hasTemplate()) {
            if (getTemplateByName(requestData.getTemplate()) == null) {
                throw new TemplateNotFoundException(requestData.getTemplate());
            }
        }
    }

    private static void setDefaultColorsIfNeeded(RequestData requestData) {
        if (!StringUtils.hasLength(requestData.getBackgroundColor())) {
            requestData.setBackgroundColor("#ffffff");
        }
        if (!StringUtils.hasLength(requestData.getQrCodeColor())) {
            requestData.setQrCodeColor("#000000");
        }
        if (!StringUtils.hasLength(requestData.getColorText())) {
            requestData.setColorText("#000000");
        }
    }

    private static BufferedImage createQrCodeImage(RequestData requestData) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        final int fixedQrWidth = 200;
        final int fixedQrHeight = 200;

        BitMatrix bitMatrix = qrCodeWriter.encode(
                requestData.getRequestUrl(),
                BarcodeFormat.QR_CODE,
                fixedQrWidth,
                fixedQrHeight
        );

        MatrixToImageConfig config = new MatrixToImageConfig(
                requestData.getQrCodeColorAsColor().getRGB(),
                requestData.getBackgroundColorAsColor().getRGB()
        );

        return MatrixToImageWriter.toBufferedImage(bitMatrix, config);
    }

    public static byte[] generateQrCodeBase(String text, RequestData requestData) throws WriterException, IOException {
        final int fixedQrWidth = 200;
        final int fixedQrHeight = 200;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        if (!StringUtils.hasLength(requestData.getRequestUrl())) {
            throw new UrlNotValidException(requestData.getRequestUrl());
        }

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, fixedQrWidth, fixedQrHeight);

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

        if (areZero && emptyString) {
            return generateQrCodeBase(requestData.getRequestUrl(), requestData);
        }

        if (emptyString) {
            return generateQrCodeBase(requestData.getRequestUrl(), requestData);
        }

        return generateQrCodeImage(requestData);
    }


    public static SeparateFields separateFields(RequestData requestData) {
        List<String> stringFields = new ArrayList<>();
        List<Integer> intFields = new ArrayList<>();

        Field[] fields = requestData.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();

            if (!fieldName.equals("requestUrl")) {
                try {
                    Object value = field.get(requestData);
                    if (value instanceof String) {
                        stringFields.add((String) value);
                    } else if (value instanceof Integer) {
                        intFields.add((Integer) value);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Accesso al campo fallito: " + fieldName, e);
                }
            }
        }

        return new SeparateFields(new ArrayList<>(stringFields), new ArrayList<>(intFields));
    }


    private static BufferedImage applyCustomizations(RequestData requestData, BufferedImage image) throws IOException {
        int whiteBoxSize = 0;
        final int fixedQrWidth = 200;
        final int fixedQrHeight = 200;

        if (StringUtils.hasLength(requestData.getLogoCenterUrl())) {
            whiteBoxSize = (int) (Math.min(fixedQrWidth, fixedQrHeight * 0.135));
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
        final int fixedQrWidth = 200;
        final int fixedQrHeight = 200;
        BufferedImage modifiedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D qrGraphics = modifiedImage.createGraphics();
        qrGraphics.drawImage(image, 0, 0, null);

        int whiteBoxX = (fixedQrWidth - whiteBoxSize) / 2;
        int whiteBoxY = (fixedQrHeight - whiteBoxSize) / 2;

        qrGraphics.setColor(Color.WHITE);
        qrGraphics.fillRect(whiteBoxX, whiteBoxY, whiteBoxSize, whiteBoxSize);

        qrGraphics.dispose();

        return modifiedImage;
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

    public static BufferedImage addCenterLogoIfProvided(RequestData requestData, BufferedImage image, int whiteBoxSize) throws IOException {
        if (!StringUtils.hasLength(requestData.getLogoCenterUrl())) {
            return image;
        }

        TemplateModel selectedTemplate = getTemplateByName(requestData.getTemplate());

        BufferedImage centerLogo = loadAndResizeLogo(requestData.getLogoCenterUrl(), whiteBoxSize, whiteBoxSize);

        if (selectedTemplate == null) {
            return addLogoToCenter(image, centerLogo, 0, 0, 0, 0); // Usa zero per i bordi
        }

        return addLogoToCenter(image, centerLogo,
                selectedTemplate.getTopBorderSize(),
                selectedTemplate.getBottomBorderSize(),
                selectedTemplate.getLeftBorderSize(),
                selectedTemplate.getRightBorderSize());
    }

    private static BufferedImage loadAndResizeLogo(String logoUrl, int width, int height) throws IOException {
        BufferedImage logoImage = loadImageFromUrl(logoUrl);

        return resizeImage(logoImage, width, height);
    }

    private static BufferedImage loadImageFromUrl(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new ImageNullException();
        }

        try {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);

            if (image == null) {
                throw new ImageUrlNotFoundException(imageUrl);
            }

            return image;
        } catch (MalformedURLException e) {
            throw new ImageUrlNotValidException(imageUrl);
        } catch (IOException e) {
            throw new IOException("Errore nel caricamento dell'immagine dall'URL: " + imageUrl, e);
        }
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        if (originalImage == null) {
            throw new ImageNullException();
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

    public static BufferedImage addBordersIfProvided(RequestData requestData, BufferedImage image) {
        if (!requestData.isHasTemplate()) {
            return image;
        }

        TemplateModel selectedTemplate = getTemplateByName(requestData.getTemplate());
        if (selectedTemplate == null) {
            throw new TemplateNotFoundException(requestData.getTemplate());
        }

        int topBorderSize = selectedTemplate.getTopBorderSize();
        int bottomBorderSize = selectedTemplate.getBottomBorderSize();
        int leftBorderSize = selectedTemplate.getLeftBorderSize();
        int rightBorderSize = selectedTemplate.getRightBorderSize();

        if (topBorderSize > 0 || bottomBorderSize > 0 || leftBorderSize > 0 || rightBorderSize > 0) {
            if (!StringUtils.hasLength(requestData.getBorderColor())) {
                throw new BorderColorNotValidException(requestData.getBorderColor());
            }
            return addBorder(image, topBorderSize, bottomBorderSize, leftBorderSize, rightBorderSize, requestData.getBorderColorAsColor());
        }

        return image;
    }

    public static void addLogoOrTextToBorderIfProvided(RequestData requestData, BufferedImage image, int whiteBoxSize) {
        if (!requestData.isHasTemplate()) {
            return;
        }

        TemplateModel selectedTemplate = getTemplateByName(requestData.getTemplate());
        if (selectedTemplate == null) {
            throw new TemplateNotFoundException(requestData.getTemplate());
        }

        if (StringUtils.hasLength(requestData.getBorderText())) {
            addTextToBorder(image, requestData.getBorderText(), selectedTemplate, 20);

            if ("bottom".equalsIgnoreCase(selectedTemplate.getPositionText())) {
                addTextToBorder(image, requestData.getBorderText(), selectedTemplate, 20);
            }
        }
    }

    public static BufferedImage addLogoToCenter(BufferedImage baseImage, BufferedImage logo, int topBorderSize, int bottomBorderSize, int leftBorderSize, int rightBorderSize) {
        if (baseImage == null) {
            throw new ImageNullException();
        }
        if (logo == null) {
            throw new LogoNotFoundException();
        }

        int effectiveWidth = baseImage.getWidth() - leftBorderSize - rightBorderSize;
        int effectiveHeight = baseImage.getHeight() - topBorderSize - bottomBorderSize;

        if (logo.getWidth() > effectiveWidth || logo.getHeight() > effectiveHeight) {
            throw new InvalidDimensionLogoException();
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
            throw new ImageNullException();
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


    /*public static BufferedImage addLogoToBorder(BufferedImage baseImage, BufferedImage logo, int fontSize, int logoMargin, int borderSize, String topOrBottom) {
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
    }*/

    public static void addTextToBorder(BufferedImage img, String text, TemplateModel template, int fontSize) {
        if (img == null) {
            throw new ImageNullException();
        }
        if (text == null || text.isEmpty()) {
            return;
        }

        Color textColor = template.getTextColor();

        Graphics2D g = img.createGraphics();
        g.setColor(textColor);
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g.setFont(font);

        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int ascent = metrics.getAscent();

        int textX = (img.getWidth() - textWidth) / 2;

        int textY;
        if (template.getPositionText().equalsIgnoreCase("top")) {
            textY = (template.getTopBorderSize() / 2) + (ascent / 2);
        } else if (template.getPositionText().equalsIgnoreCase("bottom")) {
            textY = img.getHeight() - (template.getBottomBorderSize() / 2) + (ascent / 2);
        } else {
            return;
        }

        g.drawString(text, textX, textY);
        g.dispose();
    }

    public static Color convertHexToColor(String colorHex, String colorField) {
        if (colorHex == null || !colorHex.matches(regex)) {
            throw new ColorNotValidException(colorField, colorHex);
        }
        return Color.decode(colorHex);
    }

}