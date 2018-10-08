package macaca.client.common;

public class DriverCommand {

    // session
    public static final String CREATE_SESSION = "session";
    public static final String GET_SESSIONS = "sessions";
    public static final String SESSION = "session/:sessionId";

    // status
    public static final String STATUS = "status";

    // timeout
    public static final String IMPLICITLY_WAIT = SESSION + "/timeouts/implicit_wait";

    // screenshot
    public static final String SCREENSHOT = SESSION + "/screenshot";

    // source
    public static final String GET_SOURCE = SESSION + "/source";

    // context
    public static final String CONTEXTS = SESSION + "/contexts";
    public static final String CONTEXT = SESSION + "/context";

    public static final String GET_TOAST = SESSION + "/getToast";

    // element
    public static final String CLICK = SESSION + "/click";
    public static final String KEYS = SESSION + "/keys";
    public static final String ACTIONS = SESSION + "/actions";
    public static final String FIND_CHILD_ELEMENT = SESSION + "/element/:elementId/element";
    public static final String FIND_CHILD_ELEMENTS = SESSION + "/element/:elementId/elements";
    public static final String FIND_ELEMENT = SESSION + "/element";
    public static final String FIND_ELEMENTS = SESSION + "/elements";
    public static final String ELEMENT_VALUE = SESSION + "/element/:elementId/value";
    public static final String CLICK_ELEMENT = SESSION + "/element/:elementId/click";
    public static final String GET_ELEMENT_TEXT = SESSION + "/element/:elementId/text";
    public static final String IS_ELEMENT_DISPLAYED = SESSION + "/element/:elementId/displayed";
    public static final String CLEAR_ELEMENT = SESSION + "/element/:elementId/clear";
    public static final String GET_ELEMENT_ATTRIBUTE = SESSION + "/element/:elementId/attribute/:name";
    public static final String GET_ELEMENT_PROPERTY = SESSION + "/element/:elementId/property/:name";
    public static final String GET_ELEMENT_RECT = SESSION + "/element/:elementId/rect";
    public static final String GET_ELEMENT_VALUE_OF_CSS_PROPERTY = SESSION + "/element/:elementId/css/:propertyName";

    // execute
    public static final String EXECUTE_SCRIPT = SESSION + "/execute";

    // title
    public static final String GET_TITLE = SESSION + "/title";

    // alert
    public static final String ACCEPT_ALERT = SESSION + "/accept_alert";
    public static final String DISMISS_ALERT = SESSION + "/dismiss_alert";
    public static final String ALERT_TEXT = SESSION + "/alert_text";

    // url
    public static final String URL = SESSION + "/url";
    public static final String BACK = SESSION + "/back";
    public static final String FORWARD = SESSION + "/forward";
    public static final String REFRESH = SESSION + "/refresh";
    public static final String COOKIE = SESSION + "/cookie";

    // window
    public static final String WINDOW_HANDLE = SESSION + "/window_handle";
    public static final String WINDOW_SIZE = SESSION + "/window/:windowHandle/size";
    public static final String WINDOW_HANDLES = SESSION + "/window_handles";
    public static final String WINDOW = "/window";
    public static final String SET_WINDOW_SIZE = SESSION + "/setWindowSize";
    public static final String MAXIMIZE_WINDOW = SESSION + "/window/:windowHandle/maximize";
    public static final String FRAME = SESSION + "/frame";

}
