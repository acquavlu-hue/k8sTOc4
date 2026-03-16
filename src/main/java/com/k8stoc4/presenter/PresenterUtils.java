package com.k8stoc4.presenter;

public final class PresenterUtils {
    private PresenterUtils() {}

    public static String sanitizeNamespacedId(final String id) {
        final String[] splitId = id.split("\\.", 2);
        if (splitId.length == 1) {
            return splitId[0].replace(".", "-");
        } else {
            return splitId[0] + "." + splitId[1].replace(".", "-");
        }
    }

    public static String sanitizeComponentId(final String id) {
        return id.replace(".", "-");
    }
}
