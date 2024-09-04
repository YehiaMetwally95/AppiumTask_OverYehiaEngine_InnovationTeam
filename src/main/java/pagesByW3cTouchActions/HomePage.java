package pagesByW3cTouchActions;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import utils.CustomSoftAssert;
import utils.W3CTouchActions;

public class HomePage {
    //Variables
    AppiumDriver driver;
    W3CTouchActions action;

    //Locators
    By menuIcon = AppiumBy.accessibilityId("test-Menu");
    By cartIcon = AppiumBy.accessibilityId("test-Cart");
    By footer = AppiumBy.accessibilityId("new UiSelector().text(\"© 2024 Sauce Labs. All Rights Reserved.\")");

    //Constructor
    public HomePage(AppiumDriver driver) {
        this.driver = driver;
        action = new W3CTouchActions(driver);
    }

    //Actions
    @Step("Open Menu From Header")
    public MenuPage openMenuFromHeader()
    {
        action.tab(menuIcon);
        return new MenuPage(driver);
    }

    public CartPage openCartPageFromHeader()
    {
        action.tab(cartIcon);
        return new CartPage(driver);
    }

    @Step("Open Twitter From Footer")
    public void openTwitterPageFromFooter()
    {

    }

    @Step("Open Facebook From Footer")
    public void openFacebookPageFromFooter()
    {

    }

    @Step("Open GooglePlus From Footer")
    public void openGooglePlusPageFromFooter()
    {

    }

    @Step("Open Linkedin From Footer")
    public void openLinkedinPageFromFooter()
    {

    }

    //Scrolling
    @Step("Scroll Down to Footer")
    public HomePage scrollToFooter()
    {
        action.swipeIntoScreen(footer, W3CTouchActions.Direction.DOWN);
        return this;
    }

}
