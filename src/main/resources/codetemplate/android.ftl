import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.android.AndroidDeviceHolder;
import io.appium.java_client.AppiumDriver;
import org.testng.annotations.*;
import com.daxiang.core.testng.listener.TestCaseTestListener;

public class ${className} {

    private AppiumDriver driver;

    <#-- 全局变量 -->
    <#if globalVars?? && (globalVars?size>0)>
        <#list globalVars as globalVar>
            <#lt>    public static final Object ${globalVar.name} = "${globalVar.value}";
        </#list>
    </#if>

    @BeforeSuite
    public void beforeSuite() throws Exception {
        driver = AndroidDeviceHolder.get("${deviceId}").getAppiumDriver();
    }

    <#if beforeClass??>
        <#lt>    @BeforeClass
        <#lt>    public void beforeClass() throws Exception {
        <#lt>        ${beforeClass}
        <#lt>    }
    </#if>

    <#if afterClass??>
        <#lt>    @AfterClass
        <#lt>    public void afterClass() throws Exception {
        <#lt>        ${afterClass}
        <#lt>    }
    </#if>

    <#if beforeMethod??>
        <#lt>    @BeforeMethod
        <#lt>    public void beforeMethod() throws Exception {
        <#lt>        ${beforeMethod}
        <#lt>    }
    </#if>

    <#if afterMethod??>
        <#lt>    @AfterMethod
        <#lt>    public void afterMethod() throws Exception {
        <#lt>        ${afterMethod}
        <#lt>    }
    </#if>

    <#list testcases as testcase>
        <#lt>    @Test<#if deviceTestTaskId??>(description = "${deviceId}_${deviceTestTaskId?c}_${testcase.id?c}")</#if>
        <#lt>    public void testcase_${testcase.id?c}() throws Exception {
        <#lt>        ${testcase.testcase}
        <#lt>    }
    </#list>

    <#include "actions.ftl"/>
}