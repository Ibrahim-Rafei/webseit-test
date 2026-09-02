package pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import reuseable.AbstractClass;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class LandingPage extends AbstractClass {
    WebDriver driver;

    public LandingPage(WebDriver driver) {
        super(driver);
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void goTOLandingPage() {
        driver.get("http://nivontec.de/");
    }

    public void testUrl(String data) {
        String normalizedUrl = normalizeUrl(data);
        try {
            driver.get(normalizedUrl);
        } catch (TimeoutException e) {
            // Stop outstanding resources so WebDriver can continue with the next data row.
            try {
                ((JavascriptExecutor) driver).executeScript("window.stop();");
            } catch (RuntimeException ignored) {
                // Preserve the useful page-load timeout as the test failure.
            }
            Assert.fail(normalizedUrl + " exceeded the 15-second page-load timeout", e);
        }
        WebElement body = driver.findElement(By.tagName("body"));
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(normalizedUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Assert.assertFalse(driver.getTitle().contains("404")
                                || driver.getTitle().isEmpty()
                                || body.getText().contains("404"),
                        normalizedUrl + " is returning a 404 error.\n");
                System.out.println(normalizedUrl + " is working. Status code: " + responseCode);
            } else {
                Assert.fail(normalizedUrl + " is not working. Status code: " + responseCode + "\n");
            }
        } catch (SSLHandshakeException e) {
            Assert.fail(normalizedUrl + " SSL certificate not working", e);
        } catch (IOException e) {
            Assert.fail(normalizedUrl + " could not be reached: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Adds a default scheme and converts Unicode domain labels (for example,
     * "göttingen") to the ASCII/Punycode form required by DNS clients.
     */
    static String normalizeUrl(String data) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("URL must not be empty");
        }

        String value = data.trim();
        if (!value.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*$")) {
            value = "https://" + value;
        }

        try {
            URL url = new URL(value);
            String asciiHost = IDN.toASCII(url.getHost());
            return new URI(
                    url.getProtocol(),
                    url.getUserInfo(),
                    asciiHost,
                    url.getPort(),
                    url.getPath(),
                    url.getQuery(),
                    url.getRef()
            ).toASCIIString();
        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid URL in test data: " + data, e);
        }
    }
}
