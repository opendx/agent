<#list testcases as testcase>
    <#lt>    @Test<#if deviceTestTask.id??>(description = "${testcase.description}"<#if testcase.dependsOnMethods??>, dependsOnMethods=${testcase.dependsOnMethods}</#if>)</#if>
    <#lt>    public void ${testcasePrefix}${testcase.id?c}() throws Throwable {
    <#lt>        ${testcase.invoke}
    <#lt>    }
</#list>
