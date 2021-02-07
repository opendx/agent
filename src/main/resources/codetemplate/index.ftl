<#list javaImports as javaImport>
    ${javaImport};
</#list>

public class ${className} {

    private String deviceId = "${deviceTestTask.deviceId}";
    private Device device = DeviceHolder.get(deviceId);
    private ${actionClassSimpleName} $ = new ${actionClassSimpleName}((${deviceClassSimpleName}) device);
    private ${driverClassSimpleName} driver = (${driverClassSimpleName}) device.getDriver();
    private Map<String, Object> vars = new HashMap();

    <#include "global_vars.ftl"/>
    <#include "pages.ftl"/>

    @BeforeSuite
    public void beforeSuite() throws Throwable {
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    <#include "before_after.ftl"/>
    <#include "testcases.ftl" />
    <#include "actions.ftl"/>

    private void freshDriver() {
        driver = (${driverClassSimpleName}) device.freshDriver();
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }
    private void print(Object o) {
        DebugActionTestListener.addPrintMsg(String.valueOf(o));
    }
}
