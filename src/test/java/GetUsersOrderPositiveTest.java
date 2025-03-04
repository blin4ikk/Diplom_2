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
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class GetUsersOrderPositiveTest {
    private final ArrayList<String> ingredients = new ArrayList<>();;
    private static ArrayList<String> ingredientIds ;
    private final Gson gson = new Gson();
    private static String ing1;
    private static String ing2;
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
        ingredientIds = getIngridients();
        ing1 = getRandomIngredientId();
        ing2 = getRandomIngredientId();
    }

    @Test
    @DisplayName("Получение списка заказов пользователя с авторизацией")
    public void getUsersOrder(){
        createUser(user);
        loginUser(loginUser);
        createOrderWithAuth(accessToken);
        Response response = getUsersOrders(accessToken);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true));
        System.out.println("Заказы пользователя:" + response.body().path("orders"));

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

    @Step("Получение списка заказов пользователя")
    public Response getUsersOrders(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .when()
                .get("/api/orders")
                .then()
                .extract().response();
    }

    @Step("Создание заказа с авторизацией")
    public void createOrderWithAuth(String accessToken) {
        ingredients.add(ing1);
        ingredients.add(ing2);
        CreateOrder createOrder = new CreateOrder(ingredients);
        given()
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken)
                .body(gson.toJson(createOrder))
                .when()
                .post("/api/orders")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().response();
    }

    @Step("Получение списка ингредиентов")
    public static ArrayList<String> getIngridients() {
        Response response = given()
                .when()
                .get("/api/ingredients")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().response();
        ingredientIds = response.body().path("data._id");
        return ingredientIds;
    }

    @Step("Выбор случайного ингредиента из полученного списка")
    public static String getRandomIngredientId() {
        Random random = new Random();
        return ingredientIds.get(random.nextInt(ingredientIds.size()));
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
