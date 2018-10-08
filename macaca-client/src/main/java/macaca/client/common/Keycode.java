package macaca.client.common;

// https://github.com/macacajs/webdriver-keycode/blob/master/lib/webdriver-keycode.js

public enum Keycode implements CharSequence {

    NULL('\uE000'),
    CANCEL('\uE001'),
    HELP('\uE002'),
    BACK_SPACE('\uE003'),
    TAB('\uE004'),
    CLEAR('\uE005'),
    RETURN('\uE006'),
    ENTER('\uE007'), SHIFT('\uE008'),
    CONTROL('\uE009'), ALT('\uE00A'),
    PAUSE('\uE00B'),
    ESCAPE('\uE00C'),
    SPACE('\uE00D'),
    PAGE_UP('\uE00E'),
    PAGE_DOWN('\uE00F'),
    END('\uE010'),
    HOME('\uE011'), ARROW_LEFT('\uE012'),
    ARROW_UP('\uE013'),
    ARROW_RIGHT('\uE014'),
    ARROW_DOWN('\uE015'),
    INSERT('\uE016'),
    DELETE('\uE017'),
    SEMICOLON('\uE018'),
    EQUALS('\uE019'),

    // Number pad keys
    NUMPAD0('\uE01A'),
    NUMPAD1('\uE01B'),
    NUMPAD2('\uE01C'),
    NUMPAD3('\uE01D'),
    NUMPAD4('\uE01E'),
    NUMPAD5('\uE01F'),
    NUMPAD6('\uE020'),
    NUMPAD7('\uE021'),
    NUMPAD8('\uE022'),
    NUMPAD9('\uE023'),
    MULTIPLY('\uE024'),
    ADD('\uE025'),
    SEPARATOR('\uE026'),
    SUBTRACT('\uE027'),
    DECIMAL('\uE028'),
    DIVIDE('\uE029'),

    // Function keys
    F1('\uE031'),
    F2('\uE032'),
    F3('\uE033'),
    F4('\uE034'),
    F5('\uE035'),
    F6('\uE036'),
    F7('\uE037'),
    F8('\uE038'),
    F9('\uE039'),
    F10('\uE03A'),
    F11('\uE03B'),
    F12('\uE03C'),

    META('\uE03D'),
    COMMAND(Keycode.META),
    ZENKAKU_HANKAKU('\uE040');

    private final char keyCode;

    Keycode(Keycode key) {
        this(key.charAt(0));
    }

    Keycode(char keyCode) {
        this.keyCode = keyCode;
    }

    public char charAt(int index) {
        if (index == 0) {
            return keyCode;
        }
        return 0;
    }

    public int length() {
        return 1;
    }

    public CharSequence subSequence(int start, int end) {
        if (start == 0 && end == 1) {
            return String.valueOf(keyCode);
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    public String toString() {
        return String.valueOf(keyCode);
    }


    public static Keycode getKeyFromUnicode(char key) {
        for (Keycode unicodeKey : values()) {
            if (unicodeKey.charAt(0) == key) {
                return unicodeKey;
            }
        }

        return null;
    }

}
