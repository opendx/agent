<#if actions?? && (actions?size>0)>
    <#list actions as action>
        <#--方法注释-->
        <#if action.name?? && action.name!=''>
            <#lt>    // ${action.name}
        </#if>
        <#lt>    public <#rt>
        <#if action.hasReturnValue==1>
            <#lt>Object <#rt>
        <#else>
            <#lt>void <#rt>
        </#if>
        <#lt>${methodPrefix}${action.id}<#rt>
        <#lt>(<#rt>
        <#-- 方法参数，参数格式p_xxx -->
        <#if action.params?? && (action.params?size>0)>
            <#list action.params as param>
                <#lt>Object p_${param.name}<#rt>
                <#if param_has_next>
                    <#lt>, <#rt>
                </#if>
            </#list>
        </#if>
        <#lt>) throws Exception {
        <#--方法体-->
        <#-- 基础action -->
        <#if action.type==1>
            <#lt>        <#if action.hasReturnValue==1>return </#if>new ${action.className}(<#if action.needDriver==1>driver</#if>).excute(<#rt>
            <#if action.params?? && (action.params?size>0)>
                <#list action.params as param>
                    <#lt>p_${param.name}<#rt>
                    <#if param_has_next>
                        <#lt>, <#rt>
                    </#if>
                </#list>
            </#if><#lt>);
        <#-- 非基础action -->
        <#else>
            <#--方法里的局部变量-->
            <#if action.localVars?? && (action.localVars?size>0)>
                <#list action.localVars as localVar>
                    <#-- 局部变量，格式v_xxx -->
                    <#lt>        Object v_${localVar.name} = <#if localVar.value?? && localVar.value!=''>"${localVar.value}"<#else>null</#if>;
                </#list>
            </#if>
            <#--步骤-->
            <#if action.steps?? && (action.steps?size>0)>
                <#list action.steps as step>
                    <#--步骤注释-->
                    <#lt>        // ${step.number}.<#if step.name?? && step.name!=''>${step.name}</#if>
                    <#-- 方法内的步骤 使用局部变量v_xxx赋值-->
                    <#lt>        <#if step.evaluation?? && step.evaluation!=''>v_${step.evaluation} = </#if>${methodPrefix}${step.actionId}(<#rt>
                    <#if step.paramValues?? && (step.paramValues?size>0)>
                        <#list step.paramValues as paramValue>
                            <#if paramValue.paramValue?? && paramValue.paramValue!=''>
                                <#-- 全局变量 -->
                                <#if paramValue.paramValue?starts_with('${') && paramValue.paramValue?ends_with('}')>
                                    <#lt>g_${paramValue.paramValue?substring(2,(paramValue.paramValue)?length-1)}<#rt>
                                <#-- 方法参数 -->
                                <#elseif paramValue.paramValue?starts_with('#{') && paramValue.paramValue?ends_with('}')>
                                    <#lt>p_${paramValue.paramValue?substring(2,(paramValue.paramValue)?length-1)}<#rt>
                                <#-- 局部变量 -->
                                <#elseif paramValue.paramValue?starts_with('@{') && paramValue.paramValue?ends_with('}')>
                                    <#lt>v_${paramValue.paramValue?substring(2,(paramValue.paramValue)?length-1)}<#rt>
                                <#-- 普通字符串 -->
                                <#else>
                                    <#lt>"${paramValue.paramValue}"<#rt>
                                </#if>
                            <#else>
                                <#lt>null<#rt>
                            </#if>
                            <#if paramValue_has_next>
                                <#lt>, <#rt>
                            </#if>
                        </#list>
                    </#if><#lt>);
                </#list>
            </#if>
            <#-- 方法返回值 使用局部变量v_xxx返回-->
            <#if action.hasReturnValue==1>
                <#lt>        return v_${action.returnValue};
            </#if>
        </#if>
    }
    </#list>
</#if>