package cn.ae2bc.mixin;

import cn.ae2bc.Ae2bcMod;
import cn.ae2bc.guide.GuideLocalization;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.util.Locale;

@Pseudo
@Mixin(targets = "appeng.client.guidebook.Guide", remap = false)
public abstract class GuideMixin {
    @Inject(method = "getParsedPage", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2bc$selectLocalizedGuidePage(ResourceLocation pageId,
                                                  CallbackInfoReturnable<Object> cir) {
        if (!pageId.getNamespace().equals(Ae2bcMod.MOD_ID)
                || !GuideLocalization.isRootPage(pageId.getPath())) {
            return;
        }

        var language = Minecraft.getInstance().getLanguageManager().getSelected();
        if (!GuideLocalization.isChinese(language)) {
            return;
        }

        var resourceManager = Minecraft.getInstance().getResourceManager();
        var localizedPath = GuideLocalization.localizedPath(language);
        var localizedResource = ResourceLocation.fromNamespaceAndPath(Ae2bcMod.MOD_ID, localizedPath);
        var resource = resourceManager.getResource(localizedResource).orElse(null);
        var normalizedLanguage = language.replace('-', '_').toLowerCase(Locale.ROOT);
        if (resource == null && !"zh_cn".equals(normalizedLanguage)) {
            localizedResource = ResourceLocation.fromNamespaceAndPath(
                    Ae2bcMod.MOD_ID, "ae2guide_localized/zh_cn/index.md");
            resource = resourceManager.getResource(localizedResource).orElse(null);
        }
        if (resource == null) {
            Ae2bcMod.LOGGER.warn("Missing localized guide resource for language {}", language);
            return;
        }

        try (var input = resource.open()) {
            Ae2bcMod.LOGGER.info("Using legacy localized guide page {} for language {} from {}",
                    pageId, language, localizedResource);
            var pageCompiler = Class.forName("appeng.client.guidebook.compiler.PageCompiler");
            var parse = pageCompiler.getMethod(
                    "parse", String.class, ResourceLocation.class, InputStream.class);
            cir.setReturnValue(parse.invoke(null, resource.sourcePackId(), pageId, input));
        } catch (Exception e) {
            Ae2bcMod.LOGGER.warn("Failed to load localized guide resource {}", localizedResource, e);
        }
    }
}
