package com.example;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

final class XPathLocators {
    private XPathLocators() {}

    static By byAnyText(String text) {
        String textLiteral = asXpathLiteral(text);
        return AppiumBy.xpath("//*[(@text=" + textLiteral + " or @label=" + textLiteral + " or @name=" + textLiteral
                + " or normalize-space(.)=" + textLiteral + ")]");
    }

    private static String asXpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        String[] parts = value.split("'");
        StringBuilder concat = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            concat.append("'").append(parts[i]).append("'");
            if (i < parts.length - 1) {
                concat.append(", \"'\", ");
            }
        }
        concat.append(")");
        return concat.toString();
    }
}
