import macaca.client.MacacaClient;
import com.fgnb.actions.utils.MacacaUtil;
import org.testng.annotations.*;

public class ${className} {

    private MacacaClient driver;

    <#-- 全局变量 变量名格式为g_xxx -->
    <#if globalVars?? && (globalVars?size>0)>
        <#list globalVars as globalVar>
            <#lt>    public static final Object g_${globalVar.name} = "${globalVar.value}";
        </#list>
    </#if>

    @BeforeSuite
    public void beforeSuite() throws Exception {
        <#-- ${port?c} 去除数字逗号分隔 -->
        driver = MacacaUtil.createMacacaClient("${deviceId}", ${port?c});
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
        <#lt>    public void testcase_${testcase.id}() throws Exception {
        <#lt>        ${testcase.testcase}
        <#lt>    }
    </#list>

    <#include "actions.ftl"/>
}