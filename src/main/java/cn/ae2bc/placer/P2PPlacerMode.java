package cn.ae2bc.placer;

import com.mojang.serialization.Codec;

public enum P2PPlacerMode {
    INPUT("input"),
    OUTPUT("output");

    public static final Codec<P2PPlacerMode> CODEC = Codec.STRING.xmap(
            P2PPlacerMode::fromSerializedName, P2PPlacerMode::getSerializedName);

    private final String serializedName;

    P2PPlacerMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static P2PPlacerMode fromSerializedName(String name) {
        return INPUT.serializedName.equals(name) ? INPUT : OUTPUT;
    }
}
