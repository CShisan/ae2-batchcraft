package cn.ae2bc.client;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternP2PUnitManagerModelResourceTest {
    private static final String BASE_MODEL =
            "assets/ae2_batchcraft/models/pp2p_unit_manager_base.json";
    private static final String GLASS_MODEL =
            "assets/ae2_batchcraft/models/part/pp2p_unit_manager_glass.json";
    private static final Pattern ELEMENT_BOUNDS = Pattern.compile(
            "\"from\": \\[([^]]+)],\\s*\"to\": \\[([^]]+)]");
    private static final Pattern TINT_INDEX = Pattern.compile("\"tintindex\": (\\d+)");

    @Test
    void frameUsesExpandedBoundsAndAllThreeAeColorVariants() throws Exception {
        String model = readText(BASE_MODEL);
        Set<Integer> coordinates = new HashSet<>();
        Set<Integer> tintIndices = new HashSet<>();

        var bounds = ELEMENT_BOUNDS.matcher(model);
        int elementCount = 0;
        while (bounds.find()) {
            elementCount++;
            collectIntegerCoordinates(bounds.group(1), coordinates);
            collectIntegerCoordinates(bounds.group(2), coordinates);
        }
        var tints = TINT_INDEX.matcher(model);
        while (tints.find()) {
            tintIndices.add(Integer.parseInt(tints.group(1)));
        }

        assertEquals(12, elementCount);
        assertEquals(Set.of(3, 5, 11, 13), coordinates);
        assertEquals(Set.of(1, 2, 3), tintIndices);
    }

    @Test
    void worldModelUsesSixFullGlassPanels() throws Exception {
        String model = readText(GLASS_MODEL);
        assertTrue(model.contains("\"glass\": \"ae2:block/glass/quartz_glass_a\""));

        var bounds = ELEMENT_BOUNDS.matcher(model);
        int elementCount = 0;
        while (bounds.find()) {
            elementCount++;
            double[] from = parseCoordinates(bounds.group(1));
            double[] to = parseCoordinates(bounds.group(2));
            int fullPanelAxes = 0;
            int thinAxes = 0;
            for (int axis = 0; axis < 3; axis++) {
                double size = to[axis] - from[axis];
                if (size == 6) {
                    fullPanelAxes++;
                } else if (size == 0.0625) {
                    thinAxes++;
                }
            }
            assertEquals(2, fullPanelAxes, bounds.group());
            assertEquals(1, thinAxes, bounds.group());
        }
        assertEquals(6, elementCount);
    }

    private static void collectIntegerCoordinates(String values, Set<Integer> result) {
        for (String value : values.split(",")) {
            result.add(Integer.parseInt(value.trim()));
        }
    }

    private static double[] parseCoordinates(String values) {
        String[] parts = values.split(",");
        return new double[]{
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim()),
                Double.parseDouble(parts[2].trim())
        };
    }

    private static String readText(String resource) throws Exception {
        try (var input = PatternP2PUnitManagerModelResourceTest.class.getClassLoader()
                .getResourceAsStream(resource)) {
            assertNotNull(input, resource);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
