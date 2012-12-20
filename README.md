This project includes the source files for the Sauce Gradle helper library.

To include the helper as part of your build, add the following to your build.gradle file:

```
import com.saucelabs.gradle.SauceListener

buildscript {
    repositories {
        maven {
            url "https://repository-saucelabs.forge.cloudbees.com/release"
        }
    }
    dependencies {
        classpath group: 'com.saucelabs', name: 'saucerest', version: '1.0.2'
        classpath group: 'com.saucelabs', name: 'sauce_java_common', version: '1.0.14'
        classpath group: 'com.saucelabs.gradle', name: 'sauce-gradle-plugin', version: '0.0.1'
    }
}


gradle.addListener(new SauceListener("YOUR_SAUCE_USERNAME", "YOUR_SAUCE_ACCESS_KEY"))
```

You will also need to output the Selenium session id for each test, so that the SauceListener can associate the Sauce Job
with the pass/fail status.  To do this, include the following output:

    SauceOnDemandSessionID=SELENIUM_SESSION_ID


The SauceListener will be invoked when tests have finished executing, and will parse the test XML output to find the
session ids to associate with the pass/fail status.

A simple example demonstrating how to use the helper is located in the `example` directory.