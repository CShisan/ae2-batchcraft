package cn.ae2bc.guide;

import java.util.Locale;

/** Small, Minecraft-independent rules used when selecting the localized guide page. */
public final class GuideLocalization {
    private GuideLocalization() {
    }

    public static boolean isChinese(String language) {
        if (language == null || language.isBlank()) {
            return false;
        }
        var normalized = language.replace('-', '_').toLowerCase(Locale.ROOT);
        return normalized.equals("zh") || normalized.startsWith("zh_");
    }

    public static boolean isRootPage(String path) {
        return "index".equals(path) || "index.md".equals(path);
    }

    public static String localizedPath(String language) {
        var normalized = language == null ? "" : language.replace('-', '_').toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || !isChinese(normalized)) {
            normalized = "zh_cn";
        }
        return "ae2guide_localized/" + normalized + "/index.md";
    }
}
