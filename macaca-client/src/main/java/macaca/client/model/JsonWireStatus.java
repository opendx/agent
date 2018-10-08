package macaca.client.model;

public enum JsonWireStatus {

    Success(0, "The command executed successfully."),
    NoSuchElement(7, "An element could not be located on the page using the given search parameters."),
    NoSuchFrame(8, "A request to switch to a frame could not be satisfied because the frame could not be found."),
    UnknownCommand(9, "The requested resource could not be found, or a request was received using an HTTP method that is not supported by the mapped resource."),
    StaleElementReference(10, "An element command failed because the referenced element is no longer attached to the DOM."),
    ElementNotVisible(11, "An element command could not be completed because the element is not visible on the page."),
    InvalidElementState(12, "An element command could not be completed because the element is in an invalid state (e.g. attempting to click a disabled element)."),
    UnknownError(13, "An unknown server-side error occurred while processing the command."),
    ElementIsNotSelectable(15, "An attempt was made to select an element that cannot be selected."),
    JavaScriptError(17, "An error occurred while executing user supplied JavaScript."),
    XPathLookupError(19, "An error occurred while searching for an element by XPath."),
    Timeout(21, "An operation did not complete before its timeout expired."),
    NoSuchWindow(23, "A request to switch to a different window could not be satisfied because the window could not be found."),
    InvalidCookieDomain(24, "An illegal attempt was made to set a cookie under a different domain than the current page."),
    UnableToSetCookie(25, "A request to set a cookie\'s value could not be satisfied."),
    UnexpectedAlertOpen(26, "A modal dialog was open, blocking this operation."),
    NoAlertOpenError(27, "An attempt was made to operate on a modal dialog when one was not open."),
    ScriptTimeout(28, "A script did not complete before its timeout expired."),
    InvalidElementCoordinates(29, "The coordinates provided to an interactions operation are invalid."),
    IMENotAvailable(30, "IME was not available."),
    IMEEngineActivationFailed(31, "An IME engine could not be started."),
    InvalidSelector(32, "Argument was an invalid selector (e.g. XPath/CSS)."),
    Default(-1, "");

    private int code;
    private String message;

    JsonWireStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static JsonWireStatus findByStatus(int code) {
        for (JsonWireStatus exception : JsonWireStatus.values()) {
            if (exception.code == code) {
                return exception;
            }
        }
        return Default;
    }

    public String message() {
        return message;
    }

}
