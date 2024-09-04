package testCases;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.ITestResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pagesByW3cTouchActions.LoginPage;
import testCasesByW3cTouchActions.BaseTest;

import static utils.JDBCManager.*;
import utils.JsonManager;
import static utils.RandomDataGenerator.*;
import static utils.W3CTouchActions.Direction.*;

import java.io.IOException;
import java.sql.SQLException;

@Epic("SwagLabs Android App")
@Feature("Checkout")
@Story("Verify to Checkout Cart")
@Listeners(utils.TestNGListners.class)
public class CheckOutCart extends BaseTest {

    //Variables
    JsonManager json1,json2;

    @BeforeClass
    public void prepareTestDataForLogin() throws IOException, SQLException {

        String dbQuery1 = "SELECT Id,Username,Password,Status FROM yehiadb1.swaglabsusers Order by Id Asc";
        String dbQuery2 = "SELECT Id,Name,Content FROM yehiadb1.swaglabsmassages Order by Id Asc";
        String jsonFilePath = "src/test/resources/TestDataJsonFiles/LoginData.json";

        //JsonKeys shall be filled by the same order of table columns of database query
        String[] jsonKeysForUsers = {"Id","Username","Password","Status"};
        String[] jsonKeysForMassages = {"Id","Name","Content"};

        //In Case of writing one JsonMainKey for all records, the Records will represent an array of Json objects
        //In Case of writing JsonMainKey for every record, Each Record will represent an object value for the corresponding JsonMainKey,In this case JsonMainKeys shall be filled by the same order of table rows on database
        String jsonMainKeyForUsers = "Users";
        String jsonMainKeyForMassages = "Massages";

        json1 = new JsonManager(jsonFilePath);
        JSONObject obj1 = setJsonObjectFromDBForNestedArrayOfJsonObjects(dbQuery1,jsonKeysForUsers,jsonMainKeyForUsers);
        JSONObject obj2 = setJsonObjectFromDBForNestedArrayOfJsonObjects(dbQuery2,jsonKeysForMassages,jsonMainKeyForMassages);
        JSONObject[] obj = {obj1, obj2};
        setJsonFileFromMultipleJsonObjects(obj,jsonFilePath);
    }

    @BeforeClass
    public void prepareTestDataForProducts() throws IOException, SQLException {

        String dbQuery = "SELECT Id,Name,Price,Quantity,Description FROM yehiadb1.swaglabsproducts Order By Id Asc";
        String jsonFilePath = "src/test/resources/TestDataJsonFiles/Products.json";

        //JsonKeys shall be filled by the same order of table columns of database query
        String[] jsonKeys = {"Id","Name","Price","Quantity","Description"};

        //In Case of writing one JsonMainKey for all records, the Records will represent an array of Json objects
        //In Case of writing JsonMainKey for every record, Each Record will represent an object value for the corresponding JsonMainKey,In this case JsonMainKeys shall be filled by the same order of table rows on database
        String jsonMainKey = "Products";

        json2 = new JsonManager(jsonFilePath);
        setJsonFileFromDBForNestedArrayOfJsonObjects(dbQuery,jsonFilePath,jsonKeys,jsonMainKey);
    }

    @Test
     public void checkOutCart() throws IOException, ParseException {
        new LoginPage(driver)
                .setUsername(json1.getValueFromArray("Users[0].Username"))
                .setPassword(json1.getValueFromArray("Users[0].Password"))
                .clickLoginButtonSuccess()
                .assertHomePageIsOpened()

                .addProductToCartByButton(json2.getValueFromArray("Products[0].Name"))
                .scrollToProduct(json2.getValueFromArray("Products[1].Name"),DOWN)
                .addProductToCartByDragAndDrop(json2.getValueFromArray("Products[1].Name"))
                .scrollToProduct(json2.getValueFromArray("Products[2].Name"),UP)
                .openProductDetailsPage(json2.getValueFromArray("Products[2].Name"))
                .zoomInProductPicture(100)
                .zoomOutProductPicture(100)
                .scrollToAddToCartButton(DOWN)
                .addProductToCart()

                .openCartPageFromHeader()
                .scrollToCheckoutButton(DOWN)
                .proceedToFillUserInfo()

                .fillOutUserInfo(generateName(),generateName(),generateInteger())
                .continueToCheckoutOverview()

                .removeProductFromCartBySwipe(json2.getValueFromArray("Products[0].Name"))

                .verifyProductQuantity(json2.getValueFromArray("Products[2].Name")
                        ,json2.getValueFromArray("Products[2].Quantity"))
                .verifyProductPrice(json2.getValueFromArray("Products[2].Name")
                        ,json2.getValueFromArray("Products[2].Price"))
                .verifyProductDescription(json2.getValueFromArray("Products[2].Name")
                        ,json2.getValueFromArray("Products[2].Description"))

                .scrollToPaymentInfoView(DOWN)
                .verifyPaymentInfo("SauceCard #31337")
                .verifyShippingMethod("FREE PONY EXPRESS DELIVERY!")
                .verifyTotalPriceOfProducts("41.02")
                .scrollToFinishButton(DOWN)
                .finishCartCheckOut()
                .assertCheckoutSuccessfulMessage("THANK YOU FOR YOU ORDER");
    }

}
