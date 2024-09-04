package pagesByW3cTouchActions;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.testng.asserts.SoftAssert;

import java.io.IOException;

public class CartCheckOutInfoPage extends HomePage{
    //Locators
    By firstNameText = AppiumBy.accessibilityId("test-First Name");
    By lastNameText = AppiumBy.accessibilityId("test-Last Name");
    By postalCodeText = AppiumBy.accessibilityId("test-Zip/Postal Code");
    By cancelButton = AppiumBy.accessibilityId("test-CANCEL");
    By continueButton = AppiumBy.accessibilityId("test-CONTINUE");

    //Constructor
    public CartCheckOutInfoPage(AppiumDriver driver) {
        super(driver);
    }

    //Actions
    @Step("Fill Out User Info")
    public CartCheckOutInfoPage fillOutUserInfo(String firstName,String lastName,String postalCode) throws IOException {

        action.type(firstNameText,firstName);
        action.type(lastNameText,lastName);
        action.type(postalCodeText,postalCode);
        return this;
    }

    @Step("Continue To CheckOut Overview")
    public CartCheckOutOverviewPage continueToCheckoutOverview() throws IOException {
        action.tab(continueButton);
        return new CartCheckOutOverviewPage(driver);
    }

    @Step("Return Back To Cart Page")
    public CartPage returnBackToCartPage()
    {
        action.tab(cancelButton);
        return new CartPage(driver);
    }

}
