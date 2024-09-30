package com.library.qrcodepeppino.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateModel {

    private String templateName;
    private int qrHeight;
    private int qrWidth;
    private int topBorderSize;
    private int bottomBorderSize;
    private int leftBorderSize;
    private int rightBorderSize;
    private String defaultText;
    private Color borderColor;
    private Color textColor;

}
