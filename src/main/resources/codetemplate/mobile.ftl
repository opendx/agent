import com.daxiang.core.MobileDeviceHolder;
import io.appium.java_client.AppiumDriver;
import org.testng.annotations.*;
import org.testng.SkipException;
import com.daxiang.core.testng.listener.TestCaseTestListener;
import com.daxiang.action.appium.BasicAction;
import org.openqa.selenium.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

public class ${className} {

    private AppiumDriver driver;
    private BasicAction $;

    <#-- 全局变量 -->
    <#if globalVars?? && (globalVars?size>0)>
        <#list globalVars as globalVar>
            <#lt>    public static final Object ${globalVar.name} = ${globalVar.value};
        </#list>
    </#if>

    @BeforeSuite
    public void beforeSuite() throws Throwable {
        driver = MobileDeviceHolder.get("${deviceId}").getAppiumDriver();
        $ = new BasicAction(driver);
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
        <#lt>    @Test<#if deviceTestTaskId??>(description = "${deviceId}_${deviceTestTaskId?c}_${testcase.id?c}")</#if>
        <#lt>    public void testcase_${testcase.id?c}() throws Throwable {
        <#lt>        ${testcase.testcase}
        <#lt>    }
    </#list>

    <#include "actions.ftl"/>
}