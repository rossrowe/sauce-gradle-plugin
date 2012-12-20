package com.saucelabs.gradle;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.common.Utils;
import com.saucelabs.saucerest.SauceREST;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ross Rowe
 */
public class SauceListener implements org.gradle.api.tasks.testing.TestListener {

    private SauceREST sauceREST;

    private SauceOnDemandSessionIdProvider sessionIdProvider;

    @Override
    public void beforeSuite(TestDescriptor suite) {
        System.out.println("Inside BeforeSuite");
        SauceOnDemandAuthentication sauceOnDemandAuthentication = new SauceOnDemandAuthentication();
        this.sauceREST = new SauceREST(sauceOnDemandAuthentication.getUsername(), sauceOnDemandAuthentication.getAccessKey());
    }

    @Override
    public void afterSuite(TestDescriptor suite, TestResult result) {
        System.out.println("Inside AfterSuite");
    }

    @Override
    public void beforeTest(TestDescriptor testDescriptor) {
        System.out.println("Inside BeforeTest");
    }

    @Override
    public void afterTest(TestDescriptor testDescriptor, TestResult result) {
        System.out.println("Inside AfterTest");
        if (result.getResultType() == TestResult.ResultType.FAILURE) {
            markJobAsFailed();
        }
        if (result.getResultType() == TestResult.ResultType.SUCCESS) {
            markJobAsPassed();
        }
    }

    private void markJobAsFailed() {
        try {
            if (this.sauceREST != null && sessionIdProvider != null) {
                String sessionId = sessionIdProvider.getSessionId();
                if (sessionId != null) {
                    Map<String, Object> updates = new HashMap<String, Object>();
                    updates.put("passed", false);
                    Utils.addBuildNumberToUpdate(updates);
                    sauceREST.updateJobInfo(sessionId, updates);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }

    private void markJobAsPassed() {
        try {
            if (this.sauceREST != null && sessionIdProvider != null) {
                String sessionId = sessionIdProvider.getSessionId();
                if (sessionId != null) {
                    Map<String, Object> updates = new HashMap<String, Object>();
                    updates.put("passed", true);
                    Utils.addBuildNumberToUpdate(updates);
                    sauceREST.updateJobInfo(sessionId, updates);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }
}
