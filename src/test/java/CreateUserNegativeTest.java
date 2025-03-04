import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class CreateUserNegativeTest {
    private String mail;
    private String password;
    private String name;
    private String accessToken;
    private String deleteMessage;
    private final Gson gson = new Gson();

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        mail = generateRandomEmail();
        password = generateRandomPassword();
        name = generateRandomName();
    }

    @Test
    @DisplayName("Создание пользователя с такими же данными")
    public void createDublicateUser() {
        User user = new User(mail, password, name);
        Response response = createUser(user);
        response
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
        accessToken = response.body().path("accessToken");
        System.out.println("Пользователь успешно создан: " + user.getMail() + " " + user.getName() + " " + user.getPassword());

        Response responseDublicate = createUser(user);
        responseDublicate
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
        System.out.println("При попытке создать пользователя с такими же данными: " + responseDublicate.body().path("message"));
    }

    @Test
    @DisplayName("Создание пользователя без почты")
    public void createNewUserWithoutEmail() {
        User user = new User(null, password, name);
        Response response = createUser(user);
        response
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
        System.out.println("Ошибка при попытке создать пользователя без почты :" + response.body().path("message"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    public void createNewUserWithoutPassword() {
        User user = new User(mail, null, name);
        Response response = createUser(user);
        response
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
        System.out.println("Ошибка при попытке создать пользователя без пароля :" + response.body().path("message"));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    public void createNewUserWithoutName() {
        User user = new User(mail, password, null);
        Response response = createUser(user);
        response
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
        System.out.println("Ошибка при попытке создать пользователя без имени :" + response.body().path("message"));
    }

    @Test
    @DisplayName("Создание пользователя с некорректной маской почты")
    public void createNewUserUncorrectEmail() {
        User user = new User(password, password, name);
        Response response = createUser(user);
        response
                .then()
                .statusCode(500)
                .body(containsString("Internal Server Error"));
        System.out.println("Ошибка при попытке создать пользователя с некорректной маской почты :" + response.body().path("message"));
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

    @Step("Создание пользователя")
    private Response createUser(User user) {
        return given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(user))
                .when()
                .post("/api/auth/register")
                .then()
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
