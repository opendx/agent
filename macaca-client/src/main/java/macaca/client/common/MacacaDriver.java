package macaca.client.common;

import com.alibaba.fastjson.JSONObject;

public class MacacaDriver {
    private String sessionId;
    private String windowHandle;
    // fix bug：The new element covers the old element,elementId moves from the MacacaDriver class to the Element class
    // private String elementId;
    private String context;
    private JSONObject capabilities;
    private String host = "localhost";
    private  int port = 3456;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setWindowHandle(String windowHandle) {
        this.windowHandle = windowHandle;
    }

    public String getWindowHandle() {
        return this.windowHandle;
    }

    // fix bug：The new element covers the old element,elementId moves from the MacacaDriver class to the Element class
//    public void setElementId(Object elementId) {
//        this.elementId = String.valueOf(elementId);
//    }
//
//    public String getElementId() {
//        return this.elementId;
//    }

    public JSONObject getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(JSONObject capabilities) {
        this.capabilities = capabilities;
    }

    public void setRemote(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setRemoteHost(String host) {
        this.host = host;
    }

    public void setRemotePort(int port) {
        this.port = port;
    }

    public String getHost() {
        return this.host;
    }

    public String getPort() {
        return String.valueOf(this.port);
    }

}
