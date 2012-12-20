package com.saucelabs.gradle;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.Utils;
import com.saucelabs.saucerest.SauceREST;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A custom {@link org.gradle.api.tasks.testing.TestListener} which invokes the Sauce REST API
 * to mark Sauce Jobs as passed/failed, based on the test success/failure.
 *
 * @author Ross Rowe
 */
public class SauceListener implements org.gradle.api.tasks.testing.TestListener {

    private static final String SESSION_ID_PATTERN = "SauceOnDemandSessionID=(.+)";
    public static final String DEFAULT_RESULTS_LOCATION = "./build/test-results";
    public static final String TEST_RESULT_FILE = "{0}/TEST-{1}.xml";
    public static final String RESULTS_LOCATION_PROPERTY = "proj.test.resultsDir";

    private SauceREST sauceREST;

    /**
     * Constructs a new instance using the authentication details retrieved from the {@link SauceOnDemandAuthentication}
     * instance.
     */
    public SauceListener() {
        this(new SauceOnDemandAuthentication());
    }

    /**
     * Constructs a new instance for the given username/access key.
     * @param username the Sauce username
     * @param accessKey the Sauce access key
     */
    public SauceListener(String username, String accessKey) {
        this(new SauceOnDemandAuthentication(username, accessKey));
    }

    /**
     * Populates the {@link SauceREST} instance variable using the authentication details.
     * @param sauceOnDemandAuthentication contains the Sauce authentication details
     */
    public SauceListener(SauceOnDemandAuthentication sauceOnDemandAuthentication) {
        this.sauceREST = new SauceREST(sauceOnDemandAuthentication.getUsername(), sauceOnDemandAuthentication.getAccessKey());
    }

    @Override
    public void beforeSuite(TestDescriptor suite) {

    }

    /**
     * Parses the test output XML file to retrieve the stdout lines which include the Sauce session id,
     * and invokes the Sauce REST API to mark the corresponding Sauce job as passed/failed.
     * @param suite
     * @param result
     */
    @Override
    public void afterSuite(TestDescriptor suite, TestResult result) {

        String specResultsDir = System.getProperty(RESULTS_LOCATION_PROPERTY);
        if (specResultsDir == null)
            specResultsDir = DEFAULT_RESULTS_LOCATION;
        String specResultsFile = suite.getClassName();
        if (suite.getClassName() != null) {
            String specResultsXML = MessageFormat.format(TEST_RESULT_FILE, specResultsDir, specResultsFile);
            File file = new File(specResultsXML);
            System.out.println("Parsing file: " + file.getAbsolutePath() + " exists " + file.exists());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            try {
                db = dbf.newDocumentBuilder();
                Document doc = db.parse(file);
                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expr = xpath.compile("//testsuite/system-out/text()");
                String stdout = (String) expr.evaluate(doc, XPathConstants.STRING);
                BufferedReader reader = new BufferedReader(new StringReader(stdout));
                String line;
                while ((line = reader.readLine()) != null) {
                    Pattern pattern = Pattern.compile(SESSION_ID_PATTERN);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String sessionId = matcher.group(1);
                        if (result.getResultType() == TestResult.ResultType.FAILURE) {
                            markJobAsFailed(sessionId);
                        }
                        if (result.getResultType() == TestResult.ResultType.SUCCESS) {
                            markJobAsPassed(sessionId);
                        }
                    }
                }

            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeTest(TestDescriptor testDescriptor) {

    }

    @Override
    public void afterTest(TestDescriptor testDescriptor, TestResult result) {

    }

    /**
     * Marks a Sauce job as failed.
     * @param sessionId the Sauce job id
     */
    private void markJobAsFailed(String sessionId) {
        try {
            if (this.sauceREST != null && sessionId != null) {
                Map<String, Object> updates = new HashMap<String, Object>();
                updates.put("passed", false);
                Utils.addBuildNumberToUpdate(updates);
                sauceREST.updateJobInfo(sessionId, updates);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Marks a Sauce job as passed.
     * @param sessionId the Sauce job id
     */
    private void markJobAsPassed(String sessionId) {
        try {
            if (this.sauceREST != null && sessionId != null) {
                Map<String, Object> updates = new HashMap<String, Object>();
                updates.put("passed", true);
                Utils.addBuildNumberToUpdate(updates);
                sauceREST.updateJobInfo(sessionId, updates);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }
}
