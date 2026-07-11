package com.example.da1androidnative.ui.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String formatDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        try {
            SimpleDateFormat isoParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoParser.parse(value);
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("es", "AR"));
            return date != null ? formatter.format(date) : value;
        } catch (Exception e) {
            return value.replace("T", " ");
        }
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "AR"));
        return formatter.format(date);
    }
}
