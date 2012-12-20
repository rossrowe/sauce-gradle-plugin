package com.saucelabs.gradle

import com.saucelabs.common.SauceOnDemandSessionIdProvider
import com.saucelabs.saucerest.SauceREST
import org.gradle.api.tasks.testing.TestDescriptor
import com.saucelabs.common.SauceOnDemandAuthentication
import org.gradle.api.tasks.testing.TestResult
import com.saucelabs.common.Utils

/**
 * @author Ross Rowe
 */
class SauceListener implements org.gradle.api.tasks.testing.TestListener {

    def events

    /**
     * The underlying {@link com.saucelabs.common.SauceOnDemandSessionIdProvider} instance which contains the Selenium session id.  This is typically
     * the unit test being executed.
     */
    def SauceOnDemandSessionIdProvider sessionIdProvider;

    /**
     * The instance of the Sauce OnDemand Java REST API client.
     */
    private SauceREST sauceREST;

    /**
     * Create the SauceREST instance to be used to perform Sauce REST API calls.
     * @param suite
     */
    @Override
    void beforeSuite(TestDescriptor suite) {
        SauceOnDemandAuthentication sauceOnDemandAuthentication = new SauceOnDemandAuthentication();
        this.sauceREST = new SauceREST(sauceOnDemandAuthentication.getUsername(), sauceOnDemandAuthentication.getAccessKey());
    }

    @Override
    void afterSuite(TestDescriptor suite, TestResult result) {
    }

    @Override
    void beforeTest(TestDescriptor testDescriptor) {
    }

    /**
     * Marks the job as passed/failed.
     * @param testDescriptor
     * @param result
     */
    @Override
    void afterTest(TestDescriptor testDescriptor, TestResult result) {
         if (result.getResultType() == TestResult.ResultType.FAILURE) {
             markJobAsFailed()
         }
         if (result.getResultType() == TestResult.ResultType.SUCCESS) {
             markJobAsPassed()
         }
    }

    void markJobAsFailed() {
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

    void markJobAsPassed() {
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
