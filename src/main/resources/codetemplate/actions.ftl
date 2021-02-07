<#assign javaCodeId = executeJavaCodeActionId>
<#assign deviceTestTaskId = deviceTestTask.id!-1>

<#-- <#if></#if>不能处于同一行，否则if不满足时，会出现空行 -->
<#if actions?? && (actions?size>0)>
    <#list actions as action>
        <#-- 方法注释 -->
        <#if action.name?? && action.name!=''>// ${action.name}</#if>
        public ${action.returnValueType} ${actionPrefix}${action.id?c}(
        <#if action.params?? && (action.params?size>0)>
            <#list action.params as param>${param.type} ${param.name}<#sep>,</#list>
        </#if>) throws Throwable {
            <#-- 方法体 -->
            <#-- 基础action -->
            <#if action.type==1>
                <#if action.returnValueType!='void'>return </#if>${action.invoke}(
                <#if action.params?? && (action.params?size>0)>
                    <#list action.params as param>${param.name}<#sep>,</#list>
                </#if>);
            <#-- 非基础action -->
            <#else>
                <#-- 局部变量 -->
                <#if action.localVars?? && (action.localVars?size>0)>
                    // 局部变量
                    <#list action.localVars as localVar>
                        ${localVar.type} ${localVar.name} = ${localVar.value};<#if localVar.description?? && localVar.description!=''> // ${localVar.description}</#if>
                    </#list>
                </#if>
                <#-- setUp -->
                <#if action.setUp ?? && (action.setUp?size>0)>
                    // setUp
                    try {
                        <@steps data=action.setUp startFlag=3 endFlag=4 isTestcase=(action.type==3) actionId=action.id/>
                    } catch (Throwable t) {
                        throw new SkipException("setUp执行失败: " + t.getMessage(), t);
                    }
                    // steps
                </#if>
                <#-- 有tearDown需要try包裹steps -->
                <#if action.tearDown?? && (action.tearDown?size>0)>
                    try {
                </#if>
                <#-- steps -->
                <#if action.steps ?? && (action.steps?size>0)>
                    <@steps data=action.steps startFlag=1 endFlag=2 isTestcase=(action.type==3) actionId=action.id/>
                </#if>
                <#-- tearDown -->
                <#if action.tearDown?? && (action.tearDown?size>0)>
                    } finally {
                        // tearDown
                        <@steps data=action.tearDown startFlag=5 endFlag=6 isTestcase=(action.type==3) actionId=action.id/>
                    }
                </#if>
            </#if>
        }
    </#list>
</#if>

<#macro steps data startFlag endFlag isTestcase actionId>
    <#list data as step>
        <#-- 步骤注释 -->
        // step${step.number?c}.<#if step.name?? && step.name!=''>${step.name}</#if>
        <#-- 记录步骤开始时间 -->
        <#if deviceTestTaskId!=-1 && isTestcase>
            TestCaseTestListener.recordStepTime(${deviceTestTaskId?c}, ${actionId?c}, ${step.number?c}, ${startFlag});
        </#if>
        <#-- 是否try包裹 -->
        <#if step.handleException??>
            try {
        </#if>
        <#-- 嵌入java代码 -->
        <#if step.actionId==javaCodeId>
            <#list step.args[0]?split("\n") as code>${code}</#list>
        <#else>
            <#-- 非嵌入代码，步骤赋值，方法调用 -->
            <#if step.evaluation?? && step.evaluation!=''>${step.evaluation} = </#if>${actionPrefix}${step.actionId?c}(
            <#if step.args?? && (step.args?size>0)>
                <#list step.args as arg>${arg}<#sep>,</#list>
            </#if>);
        </#if>
        <#if step.handleException??>
            } catch (Throwable t) {
            <#-- 0.忽略，继续执行 1.抛出跳过异常 -->
            <#if step.handleException==1>throw new SkipException(t.getMessage(), t);</#if>
            }
        </#if>
        <#-- 记录步骤结束时间 -->
        <#if deviceTestTaskId!=-1 && isTestcase>
            TestCaseTestListener.recordStepTime(${deviceTestTaskId?c}, ${actionId?c}, ${step.number?c}, ${endFlag});
        </#if>
    </#list>
</#macro>
