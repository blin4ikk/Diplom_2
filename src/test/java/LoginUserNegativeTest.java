import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.LoginUser;
import org.example.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class LoginUserNegativeTest {
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
        createUser(mail, password, name);
    }

    @Test
    @DisplayName("Логин с неправильным паролем")
    public void loginUncorrectPassword() {
        LoginUser loginUser = new LoginUser(mail, mail);
        Response responseLogin = loginUser(loginUser);
        responseLogin
                .then()
                .statusCode(401)
                .body("success", equalTo(false));
        System.out.println("Пользователь не залогинился: " + responseLogin.body().path("message"));
    }

    @Test
    @DisplayName("Логин несуществующего пользователя")
    public void loginNonExsistUser() {
        LoginUser loginUser = new LoginUser(password, password);
        Response responseLogin = loginUser(loginUser);
        responseLogin
                .then()
                .statusCode(401)
                .body("success", equalTo(false));
        System.out.println("Пользователь не залогинился: " + responseLogin.body().path("message"));
    }

    @Test
    @DisplayName("Логин без почты")
    public void loginWithoutEmail() {
        LoginUser loginUser = new LoginUser(null, password);
        Response responseLogin = loginUser(loginUser);
        responseLogin
                .then()
                .statusCode(401)
                .body("success", equalTo(false));
        System.out.println("Пользователь не залогинился: " + responseLogin.body().path("message"));
    }

    @Test
    @DisplayName("Логин без почты")
    public void loginWithoutPassword() {
        LoginUser loginUser = new LoginUser(mail, null);
        Response responseLogin = loginUser(loginUser);
        responseLogin
                .then()
                .statusCode(401)
                .body("success", equalTo(false));
        System.out.println("Пользователь не залогинился: " + responseLogin.body().path("message"));
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
    private void createUser(String mail, String password, String name) {
        User user = new User(mail, password, name);
        Response response = given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(user))
                .when()
                .post("/api/auth/register")
                .then()
                .body("success", equalTo(true))
                .extract().response();
        accessToken = response.body().path("accessToken");
    }

    @Step("Логин пользователя")
    private Response loginUser(LoginUser loginUser) {
        return given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(loginUser))
                .when()
                .post("/api/auth/login")
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
