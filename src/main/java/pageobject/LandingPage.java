package pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import reuseable.AbstractClass;

import java.io.IOException;
import java.io.InputStream;
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
            .version(HttpClient.Version.HTTP_1_1)
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
        URI httpsUrl = URI.create(normalizeUrl(data));
        URI httpUrl = withScheme(httpsUrl, "http");

        System.out.println("------------------------------------------------------------");
        System.out.println("DOMAIN         : " + httpsUrl.getHost());

        try {
            HttpResponse<InputStream> httpsResponse = send(httpsUrl);
            try (InputStream ignored = httpsResponse.body()) {
                assertSuccessful(httpsResponse, "HTTPS check");
                Assert.assertEquals(httpsResponse.uri().getScheme(), "https",
                        httpsUrl + " redirected to a non-HTTPS URL: " + httpsResponse.uri());
            }
            System.out.println("HTTPS          : PASS");
            System.out.println("HTTPS URL      : " + httpsResponse.uri());
            System.out.println("HTTPS STATUS   : " + httpsResponse.statusCode());

            HttpResponse<InputStream> redirectResponse = send(httpUrl);
            try (InputStream ignored = redirectResponse.body()) {
                assertSuccessful(redirectResponse, "HTTP redirect check");
                Assert.assertEquals(redirectResponse.uri().getScheme(), "https",
                        httpUrl + " does not redirect to HTTPS. Final URL: "
                                + redirectResponse.uri());
                Assert.assertNotEquals(redirectResponse.uri(), httpUrl,
                        httpUrl + " did not redirect");
            }
            System.out.println("HTTP START     : " + httpUrl);
            System.out.println("REDIRECT       : PASS");
            System.out.println("FINAL URL      : " + redirectResponse.uri());
            System.out.println("FINAL STATUS   : " + redirectResponse.statusCode());
        } catch (HttpTimeoutException e) {
            Assert.fail("Timed out after 15 seconds while checking " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail("Website check was interrupted for " + httpsUrl, e);
        } catch (IOException e) {
            Assert.fail(httpsUrl + " could not be reached: " + e.getMessage(), e);
        }
    }

    private static HttpResponse<InputStream> send(URI url)
            throws IOException, InterruptedException {
        System.out.println("REQUEST        : " + url);
        HttpRequest request = HttpRequest.newBuilder(url)
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", "Mozilla/5.0 WebsiteHealthCheck/1.0")
                .GET()
                .build();
        // Return after the headers arrive; the caller closes the body immediately.
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
    }

    private static void assertSuccessful(HttpResponse<?> response, String checkName) {
        int status = response.statusCode();
        Assert.assertTrue(status >= 200 && status < 400,
                checkName + " failed for " + response.uri() + ". Status code: " + status);
    }

    private static URI withScheme(URI url, String scheme) {
        try {
            return new URI(
                    scheme,
                    url.getUserInfo(),
                    url.getHost(),
                    -1,
                    url.getPath(),
                    url.getQuery(),
                    url.getFragment()
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not create HTTP URL from: " + url, e);
        }
    }

    /** Adds HTTPS by default and converts Unicode domains to DNS-safe Punycode. */
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
                    "https",
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
