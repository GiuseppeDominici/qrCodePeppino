package com.library.qrcodepeppino.Utils;

import com.library.qrcodepeppino.Model.TemplateModel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class TemplateConfig {

    public static List<TemplateModel> getAvailableTemplates() {
        List<TemplateModel> templates = new ArrayList<>();

        templates.add(new TemplateModel(
                "Testo in alto",
                300,
                300,
                60,
                10,
                10,
                10,
                "Testo predefinito",
                Color.BLACK,
                Color.WHITE
        ));

        templates.add(new TemplateModel(
                "Testo in basso",
                300,
                300,
                10,
                60,
                10,
                10,
                "Un altro testo predefinito",
                Color.RED,
                Color.WHITE
        ));

        return templates;
    }


    public static TemplateModel getTemplateByName(String templateName) {
        List<TemplateModel> templates = getAvailableTemplates();

        for (TemplateModel template : templates) {
            if (template.getTemplateName().equalsIgnoreCase(templateName)) {
                return template;
            }
        }

        return null;
    }

}
