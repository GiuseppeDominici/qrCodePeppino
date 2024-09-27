package com.library.qrcodepeppino.Model;

import com.library.qrcodepeppino.Utils.MethodUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestData {

    private String requestUrl;
    private String qrCodeColor;
    private String backgroundColor;
    private String borderColor;
    private String textBorder;
    private String topOrBottom;
    private String logoCenterUrl;
    private String logoBorderUrl;
    private int qrWidth;
    private int qrHeight;
    private int topBorderSize;
    private int bottomBorderSize;
    private int leftBorderSize;
    private int rightBorderSize;

    public Color getBorderColorAsColor() {
        return MethodUtils.convertHexToColor(borderColor, "borderColor");
    }

    public Color getBackgroundColorAsColor() {
        return MethodUtils.convertHexToColor(backgroundColor, "backgroundColor");
    }

    public Color getQrCodeColorAsColor() {
        return MethodUtils.convertHexToColor(qrCodeColor, "qrCodeColor");
    }

}
