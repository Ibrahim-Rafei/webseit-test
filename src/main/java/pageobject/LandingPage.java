package pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import reuseable.AbstractClass;

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
    public  void testUrl(String data) throws MalformedURLException {
        driver.get(data);
        WebElement body = driver.findElement(By.tagName("body"));


        // Check the page title or any other element to determine if the page loaded correctly
        Assert.assertFalse(driver.getTitle().contains("404") ||  driver.getTitle().isEmpty()|| body.getText().contains("404"), data + " is returning a 404 error.");

        Assert.assertFalse(!driver.getCurrentUrl().startsWith("https"), data + "is not SSL Certified");


        }


}