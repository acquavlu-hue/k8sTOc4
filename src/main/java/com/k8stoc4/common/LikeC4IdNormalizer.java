package com.k8stoc4.common;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LikeC4IdNormalizer {

    private static final Pattern SCHEME_PATTERN =
            Pattern.compile("^([a-zA-Z][a-zA-Z0-9+.-]*):(?://)?");

    private LikeC4IdNormalizer() {
    }

    public static String normalize(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "unknown-service";
        }

        String value = removeQuotes(rawValue.trim());

        String scheme = extractScheme(value);

        // Rimuove scheme: http://, mongodb://, mongodb:, amqp://, ecc.
        value = SCHEME_PATTERN.matcher(value).replaceFirst("");

        // Rimuove user e password.
        int credentialsEnd = value.lastIndexOf('@');
        if (credentialsEnd >= 0) {
            value = value.substring(credentialsEnd + 1);
        }

        // Rimuove query string e fragment.
        value = value.split("[?#]", 2)[0];

        // Costruisce un ID includendo il protocollo.
        String candidate = scheme.isBlank()
                ? value
                : scheme + "-" + value;

        candidate = Normalizer.normalize(candidate, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)

                // Rimuove eventuali placeholder rimasti.
                .replaceAll("\\$\\{[^}]+}", "")

                // Sostituisce punti, slash, porte, virgole e altri caratteri.
                .replaceAll("[^a-z0-9]+", "-")

                // Comprimi più trattini.
                .replaceAll("-{2,}", "-")

                // Rimuove trattini iniziali e finali.
                .replaceAll("^-|-$", "");

        if (candidate.isBlank()) {
            return "unknown-service";
        }

        if (Character.isDigit(candidate.charAt(0))) {
            candidate = "service-" + candidate;
        }

        return candidate;
    }

    private static String extractScheme(String value) {
        Matcher matcher = SCHEME_PATTERN.matcher(value);

        return matcher.find()
                ? matcher.group(1).toLowerCase(Locale.ROOT)
                : "";
    }

    private static String removeQuotes(String value) {
        if (value.length() < 2) {
            return value;
        }

        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);

        if ((first == '\'' && last == '\'')
                || (first == '"' && last == '"')) {
            return value.substring(1, value.length() - 1).trim();
        }

        return value;
    }


    private static void print(String value) {
        System.out.println(normalize(value));
    }
}