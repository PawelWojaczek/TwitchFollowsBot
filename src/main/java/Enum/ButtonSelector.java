package Enum;

public enum ButtonSelector {
    VIEW("div[id='OfferArea'] a[onClick*='AjaxLoader']"),
    FOLLOW("div[id='OfferArea'] button[onClick*='AjaxLoader']");

    String selector;

    ButtonSelector(String selector) {
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }
}
