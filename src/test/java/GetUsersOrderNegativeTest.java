import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class GetUsersOrderNegativeTest {
    @Before
    public void setup() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
    }
    @Test
    @DisplayName("Получение списка заказов пользователя без авторизации")
    public void getUsersOrder(){
        Response response = getUsersOrders();
        response.then()
                .statusCode(401)
                .body("success", equalTo(false));
        System.out.println("Заказы не получены: " + response.body().path("message"));

    }

    @Step("Получение списка заказов пользователя")
    public Response getUsersOrders() {
        return given()
                .when()
                .get("/api/orders")
                .then()
                .extract().response();
    }

}
