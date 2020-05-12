<#list javaImports as javaImport>
    <#lt>${javaImport};
</#list>

public class ${className} {

    private String deviceId = "${deviceTestTask.deviceId}";
    private ${driverClassSimpleName} driver;
    private ${actionClassSimpleName} $;
    private Map<String, Object> vars = new HashMap();

    <#include "global_vars.ftl"/>
    <#include "pages.ftl"/>

    @BeforeSuite
    public void beforeSuite() throws Throwable {
        Device device = DeviceHolder.get(deviceId);
        $ = new ${actionClassSimpleName}((${deviceClassSimpleName}) device);
        driver = (${driverClassSimpleName}) device.getDriver();
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    <#include "before_after.ftl"/>
    <#include "testcases.ftl" />
    <#include "actions.ftl"/>

    private void print(Object o) {
        DebugActionTestListener.addPrintMsg(String.valueOf(o));
    }
}
