package cn.ae2bc.logic;

import cn.ae2bc.pattern.MaterialOutputForm;

public enum PatternP2PUnitPortType {
    DROP("drop"),
    PICKUP("pickup"),
    PLACE("place"),
    BREAK("break"),
    TRANSFER("transfer"),
    RETURN("return"),
    REDSTONE("redstone"),
    ENERGY("energy");

    private final String serializedName;

    PatternP2PUnitPortType(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static PatternP2PUnitPortType forOutputForm(MaterialOutputForm form) {
        return switch (form) {
            case NORMAL -> TRANSFER;
            case DROP -> DROP;
            case PLACE -> PLACE;
        };
    }
}
