<#list javaImports as javaImport>
    <#lt>${javaImport};
</#list>

public class ${className} {

    private <#if isAndroid>AndroidDriver<#else>IOSDriver</#if> driver;
    private <#if isAndroid>AndroidAction<#else>IosAction</#if> $;
    private Map<String, Object> vars;
    <#-- pages -->
    <#if deviceTestTask.pages?? && (deviceTestTask.pages?size>0)>
        <#list deviceTestTask.pages as page>
            <#if page.elements?? && (page.elements?size>0)>

                <#lt>    // page.${page.name} Elements
                <#list page.elements as element>
                    <#lt>    ${(element.findBy)[0]}(${(element.findBy)[1]} = "${element.value}")
                    <#lt>    private WebElement ${page.name}_${element.name};<#if element.description?? && element.description!=''> // ${element.description}</#if>
                </#list>
            </#if>
            <#if page.bys?? && (page.bys?size>0)>

                <#lt>    // page.${page.name} Bys
                <#list page.bys as by>
                    <#lt>    private By ${page.name}_${by.name} = ${by.findBy[0]}.${by.findBy[1]}("${by.value}");<#if by.description?? && by.description!=''> // ${by.description}</#if>
                </#list>
            </#if>
        </#list>
    </#if>
    <#-- 全局变量 -->
    <#if deviceTestTask.globalVars?? && (deviceTestTask.globalVars?size>0)>

        <#lt>    // 全局变量
        <#list deviceTestTask.globalVars as globalVar>
            <#lt>    public static final ${globalVar.type} ${globalVar.name} = ${globalVar.value};<#if globalVar.description?? && globalVar.description!=''> // ${globalVar.description}</#if>
        </#list>
    </#if>

    @BeforeSuite
    public void beforeSuite() throws Throwable {
        AppiumDriver appiumDriver = MobileDeviceHolder.get("${deviceTestTask.deviceId}").getAppiumDriver();
        <#if isAndroid>
            <#lt>        driver = (AndroidDriver) appiumDriver;
            <#lt>        $ = new AndroidAction(driver);
        <#else>
            <#lt>        driver = (IOSDriver) appiumDriver;
            <#lt>        $ = new IosAction(driver);
        </#if>
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

        <#lt>    @Test<#if deviceTestTask.id??>(description = "${testcase.description}"<#if testcase.dependsOnMethods??>, dependsOnMethods=${testcase.dependsOnMethods}</#if>)</#if>
        <#lt>    public void ${testcasePrefix}${testcase.id?c}() throws Throwable {
        <#lt>        ${testcase.invoke}
        <#lt>    }
    </#list>
    <#include "actions.ftl"/>

    // 打印
    private void print(Object o) {
        DebugActionTestListener.addPrintMsg(String.valueOf(o));
    }
}