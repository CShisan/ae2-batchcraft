package cn.ae2bc.mixin;

import cn.ae2bc.guide.GuideLocalization;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideLocalizationTest {
    @Test
    void recognizesChineseLanguageVariants() {
        assertTrue(GuideLocalization.isChinese("zh_cn"));
        assertTrue(GuideLocalization.isChinese("ZH-CN"));
        assertTrue(GuideLocalization.isChinese("zh"));
        assertFalse(GuideLocalization.isChinese("en_us"));
    }

    @Test
    void acceptsBothRootPageIdForms() {
        assertTrue(GuideLocalization.isRootPage("index.md"));
        assertTrue(GuideLocalization.isRootPage("index"));
        assertFalse(GuideLocalization.isRootPage("machines.md"));
    }

    @Test
    void keepsChineseRegionWhenAResourceExistsAndNormalizesSeparators() {
        assertEquals("ae2guide_localized/zh_cn/index.md", GuideLocalization.localizedPath("ZH-CN"));
        assertEquals("ae2guide_localized/zh_tw/index.md", GuideLocalization.localizedPath("zh-TW"));
        assertEquals("ae2guide_localized/zh_cn/index.md", GuideLocalization.localizedPath(""));
    }
}
