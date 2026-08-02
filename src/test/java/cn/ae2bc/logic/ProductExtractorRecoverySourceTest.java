package cn.ae2bc.logic;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductExtractorRecoverySourceTest {
    @Test
    void unrecoverableRemainderIsPersistedAtEveryCaller() throws Exception {
        String extractor = read("src/main/java/cn/ae2bc/logic/ProductExtractor.java");
        String provider = read("src/main/java/cn/ae2bc/mixin/PatternProviderLogicMixin.java");
        String output = read("src/main/java/cn/ae2bc/logic/PatternP2PTunnelOutputLogic.java");

        assertTrue(extractor.contains("overflowHandler.recover(new GenericStack"));
        assertTrue(provider.contains("AE2BC_RECOVERY"));
        assertTrue(provider.contains("this::ae2bc$queueRecovery"));
        assertTrue(output.contains("PRODUCT_EXTRACTION_RECOVERY"));
        assertTrue(output.contains("this::queueProductExtractionRecovery"));
    }

    private static String read(String path) throws Exception {
        return Files.readString(Path.of(path));
    }
}
