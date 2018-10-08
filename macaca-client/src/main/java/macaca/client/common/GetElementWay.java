package macaca.client.common;

public enum GetElementWay {
    ID("id"),
    CSS("selector"),
    NAME("name"),
    XPATH("xpath"),
    CLASS_NAME("class name"),
    LINK_TEXT("link text"),
    PARTIAL_LINK_TEXT("partial link text"),
    TAG_NAME("tag name");

    private String using;

    GetElementWay(String using) {
        this.using = using;
    }

    public String getUsing() {
        return this.using;
    }
}
