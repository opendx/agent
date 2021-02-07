<#if beforeClass??>
    @BeforeClass
    public void beforeClass() throws Throwable {
        ${beforeClass}
    }
</#if>
<#if afterClass??>
    @AfterClass
    public void afterClass() throws Throwable {
        ${afterClass}
    }
</#if>
<#if beforeMethod??>
    @BeforeMethod
    public void beforeMethod() throws Throwable {
        ${beforeMethod}
    }
</#if>
<#if afterMethod??>
    @AfterMethod
    public void afterMethod() throws Throwable {
        ${afterMethod}
    }
</#if>
