<#list javaImports as javaImport>
    <#lt>${javaImport}
</#list>

public class ${className} {

    private AppiumDriver driver;
    private BasicAction $;
    private Map<String, Object> vars;

    <#-- 全局变量 -->
    <#if globalVars?? && (globalVars?size>0)>
        <#list globalVars as globalVar>
            <#lt>    public static final ${globalVar.type} ${globalVar.name} = ${globalVar.value};
        </#list>
    </#if>

    @BeforeSuite
    public void beforeSuite() throws Throwable {
        driver = MobileDeviceHolder.get("${deviceId}").getAppiumDriver();
        $ = new BasicAction(driver);
        vars = new HashMap();
    }

    <#if beforeClass??>
        <#lt>    @BeforeClass
        <#lt>    public void beforeClass() throws Throwable {
        <#lt>        ${beforeClass}
        <#lt>    }
    </#if>

    <#if afterClass??>
        <#lt>    @AfterClass
        <#lt>    public void afterClass() throws Throwable {
        <#lt>        ${afterClass}
        <#lt>    }
    </#if>

    <#if beforeMethod??>
        <#lt>    @BeforeMethod
        <#lt>    public void beforeMethod() throws Throwable {
        <#lt>        ${beforeMethod}
        <#lt>    }
    </#if>

    <#if afterMethod??>
        <#lt>    @AfterMethod
        <#lt>    public void afterMethod() throws Throwable {
        <#lt>        ${afterMethod}
        <#lt>    }
    </#if>

    <#list testcases as testcase>
        <#lt>    @Test<#if deviceTestTaskId??>(description = "${deviceId}_${deviceTestTaskId?c}_${testcase.id?c}_${enableRecordVideo}")</#if>
        <#lt>    public void testcase_${testcase.id?c}() throws Throwable {
        <#lt>        ${testcase.testcase}
        <#lt>    }
    </#list>

    <#include "actions.ftl"/>

    private void print(Object o) {
        DebugActionTestListener.addPrintMsg(String.valueOf(o));
    }
}