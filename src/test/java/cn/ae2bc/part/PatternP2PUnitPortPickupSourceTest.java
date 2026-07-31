package cn.ae2bc.part;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternP2PUnitPortPickupSourceTest {
    @Test
    void resetsPickupStrategiesBeforeScanningEntities() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/cn/ae2bc/part/PatternP2PUnitPortPart.java"));
        int pickupMethod = source.indexOf("private boolean pickupEntities");
        int reset = source.indexOf("strategy.reset();", pickupMethod);
        int entityScan = source.indexOf("level.getEntitiesOfClass(ItemEntity.class", pickupMethod);

        assertTrue(pickupMethod >= 0 && reset > pickupMethod && entityScan > reset,
                "pickup strategies must be reset before each entity scan");
    }
}
