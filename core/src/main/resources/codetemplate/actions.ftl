<#if actions?? && (actions?size>0)>
    <#list actions as action>
        <#-- 方法注释 -->
        <#if action.name?? && action.name!=''>
            <#lt>    // ${action.name}
        </#if>
        <#lt>    public <#rt>
        <#-- 返回值 -->
        <#if action.hasReturnValue==1>
            <#lt>Object <#rt>
        <#else>
            <#lt>void <#rt>
        </#if>
        <#-- 方法名 -->
        <#lt>${methodPrefix}${action.id}<#rt>
        <#lt>(<#rt>
        <#-- 方法参数 -->
        <#if action.params?? && (action.params?size>0)>
            <#list action.params as param>
                <#lt>Object ${param.name}<#rt>
                <#if param_has_next>
                    <#lt>, <#rt>
                </#if>
            </#list>
        </#if>
        <#lt>) throws Exception {
        <#-- 方法体 -->
        <#-- 基础action -->
        <#if action.type==1>
            <#lt>        <#if action.hasReturnValue==1>return </#if>new ${action.className}(<#if action.needDriver==1>driver</#if>).excute(<#rt>
            <#if action.params?? && (action.params?size>0)>
                <#list action.params as param>
                    <#lt>${param.name}<#rt>
                    <#if param_has_next>
                        <#lt>, <#rt>
                    </#if>
                </#list>
            </#if><#lt>);
        <#-- 非基础action -->
        <#else>
            <#-- 方法里的局部变量 -->
            <#if action.localVars?? && (action.localVars?size>0)>
                <#list action.localVars as localVar>
                    <#lt>        Object ${localVar.name} = ${localVar.value};
                </#list>
            </#if>
            <#-- 方法里的步骤 -->
            <#if action.steps?? && (action.steps?size>0)>
                <#list action.steps as step>
                    <#-- 步骤注释 -->
                    <#lt>        // ${step.number}.<#if step.name?? && step.name!=''>${step.name}</#if>
                    <#-- 步骤赋值，方法调用 -->
                    <#lt>        <#if step.evaluation?? && step.evaluation!=''>${step.evaluation} = </#if>${methodPrefix}${step.actionId}(<#rt>
                    <#if step.paramValues?? && (step.paramValues?size>0)>
                        <#list step.paramValues as paramValue>
                            <#lt>${paramValue.paramValue}<#rt>
                            <#if paramValue_has_next>
                                <#lt>, <#rt>
                            </#if>
                        </#list>
                    </#if><#lt>);
                </#list>
            </#if>
            <#-- 方法返回值 -->
            <#if action.hasReturnValue==1>
                <#lt>        return ${action.returnValue};
            </#if>
        </#if>
    }
    </#list>
</#if>