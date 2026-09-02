package pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import reuseable.AbstractClass;

import java.io.IOException;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

public class LandingPage extends AbstractClass {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

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
            HttpRequest request = HttpRequest.newBuilder(URI.create(normalizedUrl))
                    .timeout(REQUEST_TIMEOUT)
                    .header("User-Agent", "Mozilla/5.0 WebsiteHealthCheck/1.0")
                    .GET()
                    .build();
            HttpResponse<Void> response = HTTP_CLIENT.send(
                    request, HttpResponse.BodyHandlers.discarding());
            int responseCode = response.statusCode();
            URI finalUrl = response.uri();

            Assert.assertEquals(finalUrl.getScheme(), "https",
                    normalizedUrl + " redirected to a non-HTTPS URL: " + finalUrl);
            Assert.assertTrue(responseCode >= 200 && responseCode < 400,
                    normalizedUrl + " is not working. Status code: " + responseCode);

            System.out.println(normalizedUrl + " is working over HTTPS. Status code: "
                    + responseCode + ", final URL: " + finalUrl);
        } catch (HttpTimeoutException e) {
            Assert.fail(normalizedUrl + " exceeded the 15-second HTTP timeout", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(normalizedUrl + " check was interrupted", e);
        } catch (IOException e) {
            Assert.fail(normalizedUrl + " could not be reached: " + e.getMessage(), e);
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
