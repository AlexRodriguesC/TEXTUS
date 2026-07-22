package ch.supsi.documentapplication.service.async.documentpipeline;

public final class PageTextThresholds {

    public static final int MIN_PAGE_TEXT_LENGTH = 40;

    private PageTextThresholds() {}

    public static boolean needsOcr(String text) {
        if (text == null) {
            return true;
        }
        return text.trim().length() < MIN_PAGE_TEXT_LENGTH;
    }

    public static String normalizePageText(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\r", "")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
