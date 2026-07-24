package cn.ae2bc.logic;

/**
 * Controls which products a task may return through its output endpoint.
 */
public enum ReturnMode {
    STRICT(1, "strict"),
    UNBLOCKED(0, "unblocked");

    private final int id;
    private final String serializedName;

    ReturnMode(int id, String serializedName) {
        this.id = id;
        this.serializedName = serializedName;
    }

    int getId() {
        return id;
    }

    public String getSerializedName() {
        return serializedName;
    }

    static ReturnMode fromId(int id) {
        return switch (id) {
            case 0 -> UNBLOCKED;
            case 1 -> STRICT;
            default -> UNBLOCKED;
        };
    }
}
