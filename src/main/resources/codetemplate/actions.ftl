<#if actions?? && (actions?size>0)>
    <#list actions as action>
        <#-- 方法注释 -->
        <#if action.name?? && action.name!=''>
            <#lt>    // ${action.name}
        </#if>
        <#-- 返回值 -->
        <#lt>    public ${action.returnValue} <#rt>
        <#-- 方法名 -->
        <#lt>${methodPrefix}${action.id?c}<#rt>
        <#lt>(<#rt>
        <#-- 方法参数 -->
        <#if action.params?? && (action.params?size>0)>
            <#list action.params as param>
                <#lt>${param.type} ${param.name}<#sep>, <#rt>
            </#list>
        </#if>
        <#lt>) throws Throwable {
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
                <#list action.localVars as localVar>
                    <#lt>        ${localVar.type} ${localVar.name} = ${localVar.value};
                </#list>
            </#if>
            <#-- 方法里的步骤 -->
            <#if action.steps?? && (action.steps?size>0)>
                <#list action.steps as step>
                    <#-- 步骤注释 -->
                    <#lt>        // ${step.number?c}.<#if step.name?? && step.name!=''>${step.name}</#if>
                    <#-- (设备任务id && 测试用例)记录步骤的执行开始时间 -->
                    <#lt>        <#if deviceTestTaskId?? && action.type==3>TestCaseTestListener.recordTestCaseStepTime(${action.id?c}, "start", ${step.number});</#if>
                    <#if step.handleException??>
                        try{
                    </#if>
                    <#-- 直接嵌入java代码 -->
                    <#if step.actionId==executeJavaCodeActionId>
                        ${step.paramValues[0].paramValue}
                    <#else>
                    <#-- 步骤赋值，方法调用 -->
                        <#lt>        <#if step.evaluation?? && step.evaluation!=''>${step.evaluation} = </#if>${methodPrefix}${step.actionId?c}(<#rt>
                        <#if step.paramValues?? && (step.paramValues?size>0)>
                            <#list step.paramValues as paramValue>
                                <#lt>${paramValue.paramValue}<#sep>, <#rt>
                            </#list>
                        </#if><#lt>);
                    </#if>
                    <#if step.handleException??>
                        } catch (Throwable t) {
                            <#-- 0.忽略，继续执行 1.抛出跳过异常 -->
                            <#if step.handleException==1>
                                throw new SkipException(t.getMessage());
                            </#if>
                        }
                    </#if>
                    <#-- (设备任务id && 测试用例)记录步骤的执行结束时间 -->
                    <#lt>        <#if deviceTestTaskId?? && action.type==3>TestCaseTestListener.recordTestCaseStepTime(${action.id?c}, "end", ${step.number?c});</#if>
                </#list>
            </#if>
        </#if>
    }
    </#list>
</#if>