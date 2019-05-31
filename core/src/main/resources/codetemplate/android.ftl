import macaca.client.MacacaClient;
import org.testng.annotations.Test;
import com.fgnb.actions.utils.MacacaUtil;
import org.testng.annotations.BeforeSuite;

public class ${className} {

    private MacacaClient driver;

    <#-- 全局变量 变量名格式为g_xxx -->
    <#if globalVars?? && (globalVars?size>0)>
        <#list globalVars as globalVar>
            <#lt>    public static final Object g_${globalVar.name} = "${globalVar.value}";
        </#list>
    </#if>

    <#if isBeforeSuite>
        <#lt>    @BeforeSuite
    <#else>
        <#--非debug-->
        <#if deviceTestTaskId?? && testcaseId??>
            <#lt>    @Test(description = "${deviceId}_${deviceTestTaskId?c}_${testcaseId?c}")
        <#--debug-->
        <#else>
            <#lt>    @Test
        </#if>
    </#if>
    public void test() throws Exception {
        <#-- ${port?c} 去除数字逗号分隔 -->
        driver = MacacaUtil.createMacacaClient("${deviceId}",${port?c});
        ${testMethod}
    }

    <#include "actions.ftl"/>
}