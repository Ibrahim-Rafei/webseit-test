package pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import reuseable.AbstractClass;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.HttpURLConnection;


import java.net.MalformedURLException;
import java.net.URL;

public class LandingPage extends AbstractClass {
    WebDriver driver ;
    public LandingPage(WebDriver driver){
        super(driver);
        this.driver = driver ;
        PageFactory.initElements(driver , this);
    }
    public void goTOLandingPage(){
        driver.get("http://nivontec.de/");
    }
    public  void testUrl(String data) throws IOException {
        driver.get(data);
        URL siteURL = new URL(data);
        WebElement body = driver.findElement(By.tagName("body"));
        try {
            // Attempt to open HTTP connection
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds
            connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            Assert.assertFalse(driver.getTitle().contains("404") ||  driver.getTitle().isEmpty()|| body.getText().contains("404"), data + " is returning a 404 error." + "\n");
            System.out.println(data + " is working. Status code: " + responseCode);

        } else {
            Assert.fail(data + " is not working. Status code: " + responseCode + "\n");
        }
        // Check the page title or any other element to determine if the page loaded correctly

       // Assert.assertFalse(!driver.getCurrentUrl().startsWith("https"), data + "is not SSL Certified");
        } catch (SSLHandshakeException e) {
            // Handle SSL-related exceptions (indicating HTTPS was expected)
            Assert.fail(data + " SSL certificate not working" + "\n");
        } catch (IOException e) {
            // Handle other IO exceptions
            e.printStackTrace();
        }

        }


}