package macaca.client.commands;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.GetElementWay;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

import java.util.ArrayList;

public class Element {

    private MacacaDriver driver;
    // fix bug：The new element covers the old element,elementId moves from the MacacaDriver class to the Element class
    private String elementId;
    private Utils utils;
    private Boolean globalTap;

    public Element(String id, MacacaDriver driver) {
        this.elementId = id;
        this.driver = driver;
        this.globalTap = false;
        this.utils = new Utils(driver);
    }

    public void setElementId(Object elementId) {
        this.elementId = String.valueOf(elementId);
    }
    public String getElementId() {
        return this.elementId;
    }


    /**
     * <p>
     * Send a sequence of key strokes to the active element.<br>
     * Support: Android iOS Web(WebView)
     *
     * @param keys The keys sequence to be sent.
     * @return The currently instance of MacacaClient
     * @throws Exception
     */
    public void sendKeys(String keys) throws Exception {
        JSONObject jsonObject = new JSONObject();
        ArrayList<String> values = new ArrayList<String>();
        values.add(keys);
        jsonObject.put("value", values);
        setValue(jsonObject);
    }


    /**
     * @param jsonObject
     * @throws Exception
     */
    public void setValue(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("elementId", this.getElementId());
        utils.request("POST", DriverCommand.ELEMENT_VALUE, jsonObject);
    }

    /**
     * click this element
     * Support: Android iOS Web(WebView)
     *
     * @throws Exception
     */
    public void click() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        if (!globalTap) {
            jsonObject.put("elementId", this.getElementId());
            utils.request("POST", DriverCommand.CLICK_ELEMENT, jsonObject);
        } else {
            utils.request("POST", DriverCommand.CLICK, jsonObject);
            globalTap = false;
        }
    }


    /**
     * check if this element has target child element
     *
     * @param using way to find target element
     * @param value value to find target element,paired with using
     * @return
     * @throws Exception
     */
    @Deprecated
    public boolean hasChildElement(GetElementWay using, String value) throws Exception {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", value);
        jsonObject.put("using", using.getUsing());

        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("elementId", this.getElementId());

        JSONObject response = (JSONObject) utils.request("POST", DriverCommand.FIND_ELEMENT, jsonObject);
        JSONObject element = (JSONObject) response.get("value");
        //  mpdify for erroe :HiddenField: 'elementId' hides a field.
        Object eleId = (Object) element.get("ELEMENT");
        return eleId != null;
    }

    /**
     * get child elements
     *
     * @param using way to find target element
     * @param value value to find target element,paired with using
     * @return
     * @throws Exception
     */
    @Deprecated
    public JSONArray findChildElements(GetElementWay using, String value) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", value);
        jsonObject.put("using", using.getUsing());
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("elementId", this.getElementId());
        JSONObject response = (JSONObject) utils.request("POST", DriverCommand.FIND_ELEMENTS, jsonObject);
        JSONArray elements = (JSONArray) response.get("value");
        return elements;

    }

    /**
     * get count of child elements
     *
     * @param using way to find target element
     * @param value value to find target element,paired with using
     * @return
     * @throws Exception
     */
    @Deprecated
    public int countOfChildElements(GetElementWay using, String value) throws Exception {

        return findChildElements(using, value).size();
    }


    public String getText() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("elementId", this.getElementId());
        String text = (String) utils.request("GET", DriverCommand.GET_ELEMENT_TEXT, jsonObject);
        return text;
    }

    public void clearText() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("elementId", this.getElementId());
        utils.request("POST", DriverCommand.CLEAR_ELEMENT, jsonObject);
    }

    public void back() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("POST", DriverCommand.BACK, jsonObject);
    }


    /**
     * <p>
     * Get the result of a property of a element.<br>
     * Support: Android iOS Web(WebView).
     * iOS: 'isVisible', 'isAccessible', 'isEnabled', 'type', 'label', 'name', 'value',
     * Android: 'selected', 'description', 'text'
     *
     * @param name The property name of element
     * @return The property
     * @throws Exception
     */
    public Object getProperty(String name) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("elementId", this.getElementId());
        jsonObject.put("name", name);
        Object response = utils.request("GET", DriverCommand.GET_ELEMENT_PROPERTY, jsonObject);
        return response;
    }

    public Object getRect() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("elementId", this.getElementId());
        Object response = (Object) utils.request("GET", DriverCommand.GET_ELEMENT_RECT, jsonObject);
        return response;
    }

    public String getComputedCss(String propertyName) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("elementId", this.getElementId());
        jsonObject.put("propertyName", propertyName);
        String computedCss = (String) utils.request("GET", DriverCommand.GET_ELEMENT_VALUE_OF_CSS_PROPERTY, jsonObject);
        return computedCss;
    }

    @Deprecated
    public boolean isDisplayed() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("elementId", this.getElementId());
        boolean displayed = (Boolean) utils.request("GET", DriverCommand.IS_ELEMENT_DISPLAYED, jsonObject);
        return displayed;
    }

    public void touch(String action, JSONObject args) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        JSONArray array = new JSONArray();
        JSONObject actionObject = new JSONObject();
        actionObject.put("element", this.getElementId());
        actionObject.put("type", action);
        for (String key : args.keySet()) {
            String value = args.getString(key);
            actionObject.put(key, value);
        }
        array.add(actionObject);
        jsonObject.put("actions", array);
        utils.request("POST", DriverCommand.ACTIONS, jsonObject);
    }

    /**
     * get X coordinate for this element
     *
     * @return
     * @throws Exception
     */
    public double getOriginX() throws Exception {
        JSONObject rect = (JSONObject) getRect();
        double x = rect.getDoubleValue("x");
        return x;
    }

    /**
     * get Y coordinater for this element
     *
     * @return
     * @throws Exception
     */
    public double getOriginY() throws Exception {
        JSONObject rect = (JSONObject) getRect();
        double y = rect.getDoubleValue("y");
        return y;
    }

    /**
     * get width for this element
     *
     * @return
     * @throws Exception
     */
    public double getWidth() throws Exception {
        JSONObject rect = (JSONObject) getRect();
        double width = rect.getDoubleValue("width");
        return width;
    }

    /**
     * get height for this element
     *
     * @return
     * @throws Exception
     */
    public double getHeight() throws Exception {
        JSONObject rect = (JSONObject) getRect();
        double height = rect.getDoubleValue("height");
        return height;
    }

    /**
     * get CenterX coordinate for this element
     *
     * @return
     * @throws Exception
     */
    public double getCenterX() throws Exception {
        JSONObject rect = (JSONObject) getRect();
        double x = rect.getDoubleValue("x");
        double y = rect.getDoubleValue("y");
        double width = rect.getDoubleValue("width");
        double height = rect.getDoubleValue("height");
        double centerX = x + width / 2.0;
        return centerX;
    }

    /**
     * get CenterY coordinate for this element
     *
     * @return
     * @throws Exception
     */
    public double getCenterY() throws Exception {
        JSONObject rect = (JSONObject) getRect();
        double x = rect.getDoubleValue("x");
        double y = rect.getDoubleValue("y");
        double width = rect.getDoubleValue("width");
        double height = rect.getDoubleValue("height");
        double centerY = y + height / 2.0;
        return centerY;
    }


}
