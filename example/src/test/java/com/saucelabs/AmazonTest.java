package com.saucelabs;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class AmazonTest {

    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication();

    public
    @Rule
    TestName testName = new TestName();

    private WebDriver driver;
    private String sessionId;

    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("version", "5");
        capabilities.setCapability("platform", Platform.XP);
        capabilities.setCapability("name", "xxTest :"
                + testName.getMethodName());
        this.driver = new RemoteWebDriver(
                new URL("http://" + authentication.getUsername() + ":" +
                        authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
                capabilities);
        String sessionId = (((RemoteWebDriver) driver).getSessionId()).toString();
        System.out.println("SauceOnDemandSessionID=" + sessionId);
    }

    @Test
    public void amazon() throws Exception {
        driver.get("http://www.amazon.com/");
        assertEquals("Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more", driver.getTitle());
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
    }


}
