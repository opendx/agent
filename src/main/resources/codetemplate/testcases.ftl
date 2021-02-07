<#list testcases as testcase>
    @Test<#if deviceTestTask.id??>(description = "${testcase.description}"<#if testcase.dependsOnMethods??>, dependsOnMethods=${testcase.dependsOnMethods}</#if>)</#if>
    public void ${testcasePrefix}${testcase.id?c}() throws Throwable {
        ${testcase.invoke}
    }
</#list>
