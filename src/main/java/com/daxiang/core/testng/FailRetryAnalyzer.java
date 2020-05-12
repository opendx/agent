package com.daxiang.core.testng;

import lombok.extern.slf4j.Slf4j;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class FailRetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        TestDescription testDesc = (TestDescription) result.getTestContext().getAttribute(TestCaseTestListener.TEST_DESCRIPTION);
        Integer failRetryCount = testDesc.getFailRetryCount();

        if (retryCount < failRetryCount) {
            retryCount++;
            log.info("[{}]fail retry, testcaseId: {}, failRetryCount: {}, retryCount: {}", testDesc.getDeviceId(), testDesc.getTestcaseId(), failRetryCount, retryCount);
            return true;
        } else {
            return false;
        }
    }
}
