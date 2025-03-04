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

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderPositiveTest {
    private ArrayList<String> ingredients;
    private static ArrayList<String> ingredientIds;
    private final Gson gson = new Gson();
    private static String ing1;
    private static String ing2;
    private String mail = generateRandomEmail();
    private String password = generateRandomPassword();
    private String firstName= generateRandomName();
    private String accessToken;
    private String deleteMessage;
    User user = new User(mail, password, firstName);
    LoginUser loginUser = new LoginUser(mail, password);

    @Before
    public void setup() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        ingredientIds = getIngridients(); //получаем список всех ингредиентов
        ing1 = getRandomIngredientId();
        ing2 = getRandomIngredientId();
        ingredients = new ArrayList<>();
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrder() {
        ingredients.add(ing1);
        ingredients.add(ing2);
        Response response = createOrder(ingredients);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
        System.out.println("Заказ успешно создан, номер заказа: " + response.body().path("order.number") + ", в заказе: " + response.body().path("name"));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    public void createOrderWithOneIngredient() {
        createUser(user);
        loginUser(loginUser);
        ingredients.add(ing1);
        Response responseCreateOrder = createOrderWithAuth(ingredients);
        responseCreateOrder.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
        System.out.println("Заказ успешно создан, номер заказа: " + responseCreateOrder.body().path("order.number") + ", ингредиенты: " + responseCreateOrder.body().path("name"));
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
