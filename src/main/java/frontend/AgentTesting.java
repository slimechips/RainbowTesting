package frontend;

import java.util.ArrayList;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AgentTesting {
    final static String AGENT1_USERNAME = "agent1@company.com";
    final static String AGENT1_PASSWORD = "Password_123";
    String customerFirstName;
    String customerLastName;
    String customerName;
    WebDriver driver;
    WebDriverWait wait;
    Boolean agentLoggedIn, customerSelected;
    ArrayList<String> tabs;


    public AgentTesting(WebDriver driver, String customerFirstName, String customerLastName) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 30);
        this.customerFirstName = customerFirstName;
        this.customerLastName = customerLastName;
        this.customerName = customerFirstName + " " + customerLastName;
        this.tabs = new ArrayList<String>(driver.getWindowHandles());
        this.agentLoggedIn = false;
        this.customerSelected = false;
    }

    private void openNewTab() {
        ((JavascriptExecutor) driver).executeScript("window.open('about:blank','_blank');");
        tabs = new ArrayList<String>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabs.size() - 1));
    }

    private void switchToFirstTab() {
        driver.switchTo().window(tabs.get(0));
    }

    private void closeAgentTab() {
        driver.close();
        switchToFirstTab();
    }

    private void agentLogin() throws InterruptedException {
        driver.get("https://web-sandbox.openrainbow.com/app/1.68.4/index.html");
        WebElement usernameField = driver.findElement(By.id("username"));
        usernameField.sendKeys(AGENT1_USERNAME);
        WebElement submitButton =
                driver.findElement(By.xpath("//square-button[@label-dyn='continue']"));
        wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        Thread.sleep(1500);
        submitButton.click();
        WebElement passwordField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='password']")));
        wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        passwordField.sendKeys(AGENT1_PASSWORD);
        WebElement connectButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//square-button[@label-dyn='connect']")));
        Thread.sleep(1500);
        connectButton.click();

        wait.until(ExpectedConditions
                .urlToBe("https://web-sandbox.openrainbow.com/app/1.68.4/index.html#/main/home"));
        String dismissModalButtonXpath =
                "//button[@class='buttonTour' and contains(.,'Remind me later')]";
        WebElement dismissModalButton = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(dismissModalButtonXpath)));
        Thread.sleep(1500);
        dismissModalButton.click();
        agentLoggedIn = true;
    }

    private void getCustomer() {
        String customerNameXpath =
                String.format("//div[@id='cell' and contains(., '%s')]", customerName);
        WebElement contactName = driver.findElement(By.xpath(customerNameXpath));
        contactName.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chattextarea")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("chattextarea")));
        customerSelected = true;
    }

    public Boolean receivedMessage(String message) throws InterruptedException, CustomException {
        openNewTab();
        agentLogin();
        if (!agentLoggedIn)
            throw new CustomException("Agent not logged in");
        getCustomer();
        if (!customerSelected)
            throw new CustomException("Customer not selected");
        String messageXpath = String.format("//div[@class='line' and contains(text(),'%s')]", message);
        Boolean messageReceived = driver.findElements(By.xpath(messageXpath)).size() > 0;
        closeAgentTab();
        return messageReceived;
    }

    public void sendMessage(String message) throws InterruptedException, CustomException {
        openNewTab();
        agentLogin();
        if (!agentLoggedIn)
            throw new CustomException("Agent not logged in");
        getCustomer();
        if (!customerSelected)
            throw new CustomException("Customer not selected");
        WebElement chatTextArea = driver.findElement(By.id("chattextarea"));
        chatTextArea.sendKeys(message);
        chatTextArea.sendKeys(Keys.RETURN);
        closeAgentTab();
    }

}