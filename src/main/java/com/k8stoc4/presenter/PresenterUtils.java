package com.k8stoc4.presenter;

public final class PresenterUtils {
    private PresenterUtils() {}

    public static String sanitizeNamespacedId(final String id) {
        final String[] splitId = id.split("\\.", 2);
        if (splitId.length == 1) {
            return sanitize(splitId[0]);
        } else {
            return splitId[0] + "." + sanitize(splitId[1]);
        }
    }

    public static String sanitizeComponentId(final String id) {
        return sanitize(id);
    }

    private static String sanitize(final String s) {
        return s.replace(".", "-").replace("/", "-").replace(":", "-");
    }
}
