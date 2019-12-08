<#list javaImports as javaImport>
    <#lt>${javaImport}
</#list>

public class ${className} {

    private AppiumDriver driver;
    private BasicAction $;
    private Map<String, Object> vars;

    <#-- pages -->
    <#if deviceTestTask.pages?? && (deviceTestTask.pages?size>0)>
        <#list deviceTestTask.pages as page>
            <#if page.elements?? && (page.elements?size>0)>
                <#list page.elements as element>
                    <#lt>    ${(element.findBy)[0]}(${(element.findBy)[1]} = "${element.value}")
                    <#lt>    private WebElement ${page.name}_${element.name};
                </#list>
            </#if>
        </#list>
    </#if>

    <#-- 全局变量 -->
    <#if deviceTestTask.globalVars?? && (deviceTestTask.globalVars?size>0)>
        <#list deviceTestTask.globalVars as globalVar>
            <#lt>    public static final ${globalVar.type} ${globalVar.name} = ${globalVar.value};
        </#list>
    </#if>

    @BeforeSuite
    public void beforeSuite() throws Throwable {
        driver = MobileDeviceHolder.get("${deviceTestTask.deviceId}").getAppiumDriver();
        $ = new BasicAction(driver);
        vars = new HashMap();
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
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
        <#lt>    @Test<#if deviceTestTask.id??>(description = "${deviceTestTask.deviceId}_${deviceTestTask.id?c}_${testcase.id?c}_${deviceTestTask.testPlan.enableRecordVideo}_${deviceTestTask.testPlan.failRetryCount}")</#if>
        <#lt>    public void testcase_${testcase.id?c}() throws Throwable {
        <#lt>        ${testcase.testcase}
        <#lt>    }
    </#list>

    <#include "actions.ftl"/>

    private void print(Object o) {
        DebugActionTestListener.addPrintMsg(String.valueOf(o));
    }
}