<#if deviceTestTask.globalVars?? && (deviceTestTask.globalVars?size>0)>
    <#list deviceTestTask.globalVars as globalVar>
        public static final ${globalVar.type} ${globalVar.name} = ${globalVar.value};<#if globalVar.description?? && globalVar.description!=''> // ${globalVar.description}</#if>
    </#list>
</#if>
