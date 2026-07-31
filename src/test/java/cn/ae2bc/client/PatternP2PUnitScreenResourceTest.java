package cn.ae2bc.client;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternP2PUnitScreenResourceTest {
    private static final String MANAGER_SCREEN =
            "assets/ae2/screens/ae2_batchcraft/pattern_p2p_unit_manager.json";
    private static final String INPUT_SCREEN =
            "assets/ae2/screens/ae2_batchcraft/pattern_p2p_tunnel_input.json";
    private static final String OUTPUT_SCREEN =
            "assets/ae2/screens/ae2_batchcraft/pattern_p2p_tunnel_output.json";

    @Test
    void configurationScreensExposeTheSharedPageNavigation() throws Exception {
        assertPagedConfigurationScreen(MANAGER_SCREEN);
        assertPagedConfigurationScreen(INPUT_SCREEN);
    }

    @Test
    void taskEndpointsExposeTheResetTaskButton() throws Exception {
        assertTrue(readText(MANAGER_SCREEN).contains("\"resetTask\""));
        assertTrue(readText(INPUT_SCREEN).contains("\"resetTask\""));
        assertTrue(readText(OUTPUT_SCREEN).contains("\"resetTask\""));
    }

    private static void assertPagedConfigurationScreen(String resource) throws Exception {
        String screen = readText(resource);

        assertTrue(screen.contains("\"generatedBackground\": {\"width\": 176"), resource);
        assertTrue(screen.contains("\"close\""), resource);
        assertTrue(screen.contains("\"page_title\""), resource);
        assertTrue(screen.contains("\"scale\": 1.2"), resource);
    }

    private static String readText(String resource) throws Exception {
        try (var input = PatternP2PUnitScreenResourceTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, resource);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
