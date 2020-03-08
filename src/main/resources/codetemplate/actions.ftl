<#-- <#if></#if>不能处于同一行，否则if不满足时，会出现空行 -->
<#if actions?? && (actions?size>0)>
    <#list actions as action>

        <#-- 方法注释 -->
        <#if action.name?? && action.name!=''>
            <#lt>    // ${action.name}
        </#if>
        <#-- 方法前缀 -->
        <#lt>    public ${action.returnValue} ${actionPrefix}${action.id?c}(<#rt>
        <#-- 方法参数 -->
        <#if action.params?? && (action.params?size>0)>
            <#list action.params as param>
                <#lt>${param.type} ${param.name}<#sep>, <#rt>
            </#list>
        </#if><#lt>) throws Throwable {
        <#-- 方法体 -->
        <#-- 基础action -->
        <#if action.type==1>
            <#lt>        <#if action.returnValue!='void'>return </#if>${action.invoke}(<#rt>
            <#if action.params?? && (action.params?size>0)>
                <#list action.params as param>
                    <#lt>${param.name}<#sep>, <#rt>
                </#list>
            </#if><#lt>);
        <#-- 非基础action -->
        <#else>
            <#-- 方法里的局部变量 -->
            <#if action.localVars?? && (action.localVars?size>0)>
                <#lt>        // 局部变量
                <#list action.localVars as localVar>
                    <#lt>        ${localVar.type} ${localVar.name} = ${localVar.value};<#if localVar.description?? && localVar.description!=''> // ${localVar.description}</#if>
                </#list>
            </#if>
            <#-- 方法里的步骤 -->
            <#if action.steps?? && (action.steps?size>0)>
                <#list action.steps as step>
                    <#-- 步骤注释 -->
                    <#lt>        // step${step.number?c}.<#if step.name?? && step.name!=''> ${step.name}</#if>
                    <#-- 是否要try包裹 -->
                    <#if step.handleException??>
                        <#lt>        try {
                    </#if>
                    <#-- (设备任务id && 测试用例)记录步骤的执行开始时间 -->
                    <#if deviceTestTask.id?? && action.type==3>
                        <#lt><#if step.handleException??>    </#if>        TestCaseTestListener.recordTestCaseStepTime(${deviceTestTask.id?c}, ${action.id?c}, true, ${step.number?c});
                    </#if>
                    <#-- 直接嵌入java代码 -->
                    <#if step.actionId==executeJavaCodeActionId>
                        <#list step.paramValues[0].paramValue?split("\n") as code>
                            <#lt><#if step.handleException??>    </#if>        ${code}
                        </#list>
                    <#else>
                        <#-- 非嵌入代码，步骤赋值，方法调用 -->
                        <#lt><#if step.handleException??>    </#if>        <#if step.evaluation?? && step.evaluation!=''>${step.evaluation} = </#if>${actionPrefix}${step.actionId?c}(<#rt>
                        <#if step.paramValues?? && (step.paramValues?size>0)>
                            <#list step.paramValues as paramValue>
                                <#lt>${paramValue.paramValue}<#sep>, <#rt>
                            </#list>
                        </#if><#lt>);
                    </#if>
                    <#-- (设备任务id && 测试用例)记录步骤的执行结束时间 -->
                    <#if deviceTestTask.id?? && action.type==3>
                        <#lt><#if step.handleException??>    </#if>        TestCaseTestListener.recordTestCaseStepTime(${deviceTestTask.id?c}, ${action.id?c}, false, ${step.number?c});
                    </#if>
                    <#if step.handleException??>
                        <#lt>        } catch (Throwable t) {
                                        <#-- 0.忽略，继续执行 1.抛出跳过异常 -->
                                        <#if step.handleException==1>
                                            <#lt>            throw new SkipException(t.getMessage());
                                        </#if>
                        <#lt>        }
                    </#if>
                </#list>
            </#if>
        </#if>
    }
    </#list>
</#if>