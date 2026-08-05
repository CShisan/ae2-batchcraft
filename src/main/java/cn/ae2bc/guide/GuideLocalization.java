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

    public static String localizedPath(String language) {
        return localizedPath(language, "index.md");
    }

    public static String localizedPath(String language, String pagePath) {
        var normalized = language == null ? "" : language.replace('-', '_').toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || !isChinese(normalized)) {
            normalized = "zh_cn";
        }
        var normalizedPagePath = pagePath == null || pagePath.isBlank() ? "index.md" : pagePath.replace('\\', '/');
        if ("index".equals(normalizedPagePath)) {
            normalizedPagePath = "index.md";
        }
        if (normalizedPagePath.startsWith("/") || normalizedPagePath.contains("../")) {
            normalizedPagePath = "index.md";
        }
        return "ae2guide/_" + normalized + "/" + normalizedPagePath;
    }
}
