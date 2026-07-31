package cn.ae2bc.logic;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductExtractorRecoverySourceTest {
    @Test
    void unrecoverableRemainderIsHandedToWorldRecoveryAtEveryCaller() throws Exception {
        String extractor = read("src/main/java/cn/ae2bc/logic/ProductExtractor.java");
        String provider = read("src/main/java/cn/ae2bc/mixin/PatternProviderLogicMixin.java");
        String output = read("src/main/java/cn/ae2bc/logic/PatternP2PTunnelOutputLogic.java");

        assertTrue(extractor.contains("overflowHandler.recover(remainder.copy())"));
        assertTrue(provider.contains("Platform.spawnDrops"));
        assertTrue(output.contains("Platform.spawnDrops"));
    }

    private static String read(String path) throws Exception {
        return Files.readString(Path.of(path));
    }
}
