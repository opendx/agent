<#if deviceTestTask.pages?? && (deviceTestTask.pages?size>0)>
    <#list deviceTestTask.pages as page>
        <#if page.elements?? && (page.elements?size>0)>
            <#list page.elements as element>
                ${(element.findBy)[0]}(${(element.findBy)[1]} = "${element.value}")
                private WebElement ${page.name}_${element.name};<#if element.description?? && element.description!=''> // ${element.description}</#if>
            </#list>
        </#if>
        <#if page.bys?? && (page.bys?size>0)>
            <#list page.bys as by>
                private By ${page.name}_${by.name} = ${by.findBy[0]}.${by.findBy[1]}("${by.value}");<#if by.description?? && by.description!=''> // ${by.description}</#if>
            </#list>
        </#if>
    </#list>
</#if>
