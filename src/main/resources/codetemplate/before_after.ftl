<#if beforeClass??>
    <#lt>    @BeforeClass
    <#lt>    public void beforeClass() throws Throwable {
    <#lt>        ${beforeClass}
    <#lt>    }
</#if>
<#if afterClass??>
    <#lt>    @AfterClass
    <#lt>    public void afterClass() throws Throwable {
    <#lt>        ${afterClass}
    <#lt>    }
</#if>
<#if beforeMethod??>
    <#lt>    @BeforeMethod
    <#lt>    public void beforeMethod() throws Throwable {
    <#lt>        ${beforeMethod}
    <#lt>    }
</#if>
<#if afterMethod??>
    <#lt>    @AfterMethod
    <#lt>    public void afterMethod() throws Throwable {
    <#lt>        ${afterMethod}
    <#lt>    }
</#if>
