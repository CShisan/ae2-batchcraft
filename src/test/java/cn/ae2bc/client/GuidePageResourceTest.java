package cn.ae2bc.client;

import guideme.libs.mdast.MdAst;
import guideme.libs.mdast.MdastOptions;
import guideme.libs.mdast.YamlFrontmatterExtension;
import guideme.libs.mdast.gfm.GfmTableMdastExtension;
import guideme.libs.mdast.mdx.MdxMdastExtension;
import guideme.libs.mdx.MdxSyntax;
import guideme.libs.micromark.extensions.YamlFrontmatterSyntax;
import guideme.libs.micromark.extensions.gfm.GfmTableSyntax;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuidePageResourceTest {
    private static final String ENGLISH_GUIDE = "assets/ae2_batchcraft/ae2guide/index.md";
    private static final String CHINESE_GUIDE =
            "assets/ae2_batchcraft/ae2guide_localized/zh_cn/index.md";
    private static final String INPUT_RECIPE =
            "data/ae2_batchcraft/recipe/pattern_p2p_tunnel_input.json";
    private static final String PLACER_RECIPE =
            "data/ae2_batchcraft/recipe/wp2pp_placer.json";
    private static final String ENERGY_TUNNEL_RECIPE =
            "data/ae2_batchcraft/recipe/pattern_p2p_tunnel_energy.json";
    private static final String PLACER_SCREEN =
            "assets/ae2/screens/ae2_batchcraft/wp2pp_placer.json";
    private static final String ENERGY_TUNNEL_SCREEN =
            "assets/ae2/screens/ae2_batchcraft/pattern_p2p_tunnel_energy.json";
    private static final String AE2_PATTERN_PROVIDER_TAG = "data/ae2/tags/item/pattern_provider.json";

    @Test
    void bothItemsPointToTheEnglishDefaultGuidePage() throws Exception {
        String page = readText(ENGLISH_GUIDE);

        assertTrue(page.startsWith("---\n"));
        assertTrue(page.contains("title: AE2 BatchCraft"));
        assertTrue(page.contains("- ae2_batchcraft:pattern_p2p_tunnel_input"));
        assertTrue(page.contains("- ae2_batchcraft:pattern_p2p_tunnel_output"));
        assertTrue(page.contains("- ae2_batchcraft:pattern_p2p_tunnel_energy"));
        assertTrue(page.contains("- ae2_batchcraft:wp2pp_placer"));
        assertTrue(page.contains("## Pattern P2P Tunnel (Energy)"));
        assertTrue(page.contains("every Pattern P2P Tunnel output"));
        assertTrue(page.contains("Passive receive"));
        assertTrue(page.contains("## Wireless Pattern P2P Placer"));
        assertTrue(page.contains("four-color frequency square"));
        assertTrue(page.contains("Left-click the four-color frequency square"));
        assertTrue(page.contains("main or sub endpoint immediately receives the frequency"));
        assertTrue(page.contains("## Important Notes"));
        assertGuideParses(page);
    }

    @Test
    void simplifiedChineseGuideIsAvailableAsALocalizedOverride() throws Exception {
        String page = readText(CHINESE_GUIDE);

        assertTrue(page.startsWith("---\n"));
        assertTrue(page.contains("## 值得注意"));
        assertTrue(page.contains("## 样板 P2P 通道（输入）"));
        assertTrue(page.contains("## 样板 P2P 通道（输出）"));
        assertTrue(page.contains("## 样板 P2P 通道（能量）"));
        assertTrue(page.contains("被动接收"));
        assertTrue(page.contains("主动拉取"));
        assertTrue(page.contains("## 无线样板P2P放置器"));
        assertTrue(page.contains("四色频率方块"));
        assertTrue(page.contains("- ae2_batchcraft:wp2pp_placer"));
        assertTrue(page.contains("- ae2_batchcraft:pattern_p2p_tunnel_energy"));
        assertFalse(page.contains("<!--"));
        assertFalse(page.equals(readText(ENGLISH_GUIDE)));
        assertGuideParses(page);
    }

    @Test
    void inputRecipeAcceptsBothPatternProviderForms() throws Exception {
        String recipe = readText(INPUT_RECIPE);
        assertTrue(recipe.contains("\"pattern\": [\n    \"IRI\",\n    \"ETE\",\n    \"IPI\"\n  ]"));
        assertTrue(recipe.contains("\"I\": {\n      \"item\": \"minecraft:iron_ingot\"\n    }"));
        assertTrue(recipe.contains("\"P\": {\n      \"tag\": \"ae2:pattern_provider\"\n    }"));

        String tag = readText(AE2_PATTERN_PROVIDER_TAG);
        assertTrue(tag.contains("\"ae2:cable_pattern_provider\""));
        assertTrue(tag.contains("\"ae2:pattern_provider\""));
    }

    @Test
    void placerRecipeUsesTheWirelessTerminalAndBothEndpoints() throws Exception {
        String recipe = readText(PLACER_RECIPE);
        assertTrue(recipe.contains("\"item\": \"ae2:wireless_terminal\""));
        assertTrue(recipe.contains("\"item\": \"ae2_batchcraft:pattern_p2p_tunnel_input\""));
        assertTrue(recipe.contains("\"item\": \"ae2_batchcraft:pattern_p2p_tunnel_output\""));
        assertTrue(recipe.contains("\"id\": \"ae2_batchcraft:wp2pp_placer\""));
    }

    @Test
    void energyTunnelRecipeReplacesTheInputRecipeProviderWithAnEnergyAcceptor() throws Exception {
        String recipe = readText(ENERGY_TUNNEL_RECIPE);
        assertTrue(recipe.contains("\"pattern\": [\n    \"IRI\",\n    \"ETE\",\n    \"IAI\"\n  ]"));
        assertTrue(recipe.contains("\"I\": {\n      \"item\": \"minecraft:iron_ingot\"\n    }"));
        assertTrue(recipe.contains("\"R\": {\n      \"item\": \"minecraft:redstone\"\n    }"));
        assertTrue(recipe.contains("\"E\": {\n      \"item\": \"minecraft:ender_pearl\"\n    }"));
        assertTrue(recipe.contains("\"T\": {\n      \"item\": \"ae2:me_p2p_tunnel\"\n    }"));
        assertTrue(recipe.contains("\"A\": {\n      \"item\": \"ae2:energy_acceptor\"\n    }"));
        assertTrue(recipe.contains("\"id\": \"ae2_batchcraft:pattern_p2p_tunnel_energy\""));
    }

    @Test
    void energyTunnelScreenProvidesBothEnergyModes() throws Exception {
        String screen = readText(ENERGY_TUNNEL_SCREEN);
        assertTrue(screen.contains("\"width\": 176"));
        assertTrue(screen.contains("\"height\": 83"));
        assertTrue(screen.contains("\"passive\""));
        assertTrue(screen.contains("\"active\""));
        assertTrue(screen.contains("gui.ae2_batchcraft.energy.mode_value"));
        assertTrue(screen.contains("gui.ae2_batchcraft.energy.pull_interval"));
    }

    @Test
    void placerScreenSeparatesSlotsAndUsesRelativeDirectionLayout() throws Exception {
        String screen = readText(PLACER_SCREEN);

        assertTrue(screen.contains("\"CONFIG\""));
        assertTrue(screen.contains("\"STORAGE\""));
        assertTrue(screen.contains("\"PLAYER_INVENTORY\""));
        assertTrue(screen.contains("\"PLAYER_HOTBAR\""));
        assertTrue(screen.contains("\"width\": 176"));
        assertTrue(screen.contains("\"height\": 216"));
        assertFalse(screen.contains("\"scale\""));
        assertTrue(screen.contains("\"align\": \"RIGHT\""));
        assertTrue(screen.contains("\"align\": \"CENTER\""));
        assertTrue(screen.contains("\"directionFront\""));
        assertTrue(screen.contains("\"directionLeft\""));
        assertTrue(screen.contains("\"directionRight\""));
        assertTrue(screen.contains("\"directionBack\""));
        assertTrue(screen.contains("\"frequencyText\""));
        assertTrue(screen.contains("\"frequency\""));
    }

    private static String readText(String resource) throws Exception {
        try (var input = GuidePageResourceTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, resource);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void assertGuideParses(String page) {
        var options = new MdastOptions()
                .withSyntaxExtension(MdxSyntax.INSTANCE)
                .withSyntaxExtension(YamlFrontmatterSyntax.INSTANCE)
                .withSyntaxExtension(GfmTableSyntax.INSTANCE)
                .withMdastExtension(MdxMdastExtension.INSTANCE)
                .withMdastExtension(YamlFrontmatterExtension.INSTANCE)
                .withMdastExtension(GfmTableMdastExtension.INSTANCE);
        assertDoesNotThrow(() -> MdAst.fromMarkdown(page, options));
    }

}
