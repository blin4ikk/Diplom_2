import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.CreateOrder;
import org.example.LoginUser;
import org.example.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class CreateOrderNegativeTest {
    private ArrayList<String> ingredients;
    private final Gson gson = new Gson();
    private final String mail = generateRandomEmail();
    private final String password = generateRandomPassword();
    private final String firstName= generateRandomName();
    private String accessToken;
    private String deleteMessage;
    User user = new User(mail, password, firstName);
    LoginUser loginUser = new LoginUser(mail, password);

    @Before
    public void setup() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        ingredients = new ArrayList<>();
    }

    @Test
    @DisplayName("Неуспешное создание заказа с некорректным хешем")
    public void createWithUncorrectIngredientOrder() {
        ingredients.add(mail);
        Response response = createOrder(ingredients);
        response.then()
                .statusCode(500)
                .body(containsString("Internal Server Error"));
        System.out.println("Заказ не создан, ошибка: " + response.asString());
    }

    @Test
    @DisplayName("Неуспешное создание заказа без ингредиентов с авторизацией")
    public void createOrderNoIngredient() {
        createUser(user);
        loginUser(loginUser);
        Response responseCreateOrder = createOrderWithAuth(ingredients);
        responseCreateOrder.then()
                .statusCode(400)
                .body("success", equalTo(false));
        System.out.println("Заказа не создан, ошибка:" + responseCreateOrder.asString());
    }

    @After
    public void cleanup() {
        if (accessToken != null) {
            deleteUser(accessToken);
            System.out.println(deleteMessage);
        } else {
            System.out.println(deleteMessage);
        }
    }

    @Step("Создание заказа")
    public Response createOrder(ArrayList<String> ingredients) {
        CreateOrder createOrder = new CreateOrder(ingredients);
        return given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(createOrder))
                .when()
                .post("/api/orders")
                .then()
                .extract().response();
    }

    @Step("Создание заказа с авторизацией")
    public Response createOrderWithAuth(ArrayList<String> ingredients) {
        CreateOrder createOrder = new CreateOrder(ingredients);
        return given()
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken)
                .body(gson.toJson(createOrder))
                .when()
                .post("/api/orders")
                .then()
                .extract().response();
    }

    @Step("Создание пользователя")
    private void createUser(User user) {
        Response response = given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(user))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().response();
        accessToken = response.body().path("accessToken");
    }

    @Step("Логин пользователя")
    private void loginUser(LoginUser loginUser) {
        given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(loginUser))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().response();
    }

    @Step("Генерация почты")
    private String generateRandomEmail() {
        return "user_" + RandomStringUtils.randomAlphanumeric(6) + "@yandex.ru";
    }

    @Step("Генерация пароля")
    private String generateRandomPassword() {
        return RandomStringUtils.randomNumeric(6);
    }

    @Step("Генерация имени")
    private String generateRandomName() {
        return "Test_" + RandomStringUtils.randomAlphabetic(4);
    }

    @Step("Удаление пользователя")
    private void deleteUser(String accessToken) {
        Response response = given()
                .when()
                .header("Authorization", accessToken)
                .delete("/api/auth/user")
                .then()
                .statusCode(202)
                .body("success", equalTo(true))
                .extract().response();
        deleteMessage = response.body().path("message");
    }
}
