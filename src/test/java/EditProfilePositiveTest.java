import com.google.gson.Gson;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.EditProfileUser;
import org.example.LoginUser;
import org.example.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class EditProfilePositiveTest {
    private String mail;
    private String password;
    private String name;
    private final String editMail;
    private final String editName;
    private String accessToken;
    private String deleteMessage;
    private final Gson gson = new Gson();

    public EditProfilePositiveTest(String editMail, String editName){
        this.editMail = editMail;
        this.editName = editName;
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        mail = generateRandomEmail();
        password = generateRandomPassword();
        name = generateRandomName();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"z@y.f", "n"},
                {"t 1", "t 1"},
                {"пятнадцатьбуков", "BLACK"},
                {"verylongemailtestforlong@verylongemailtestforlong.verylongemailtestforlong", "verylongemailtestforlong@verylongemailtestforlong.verylongemailtestforlong"},
                {"edit only mail", null},
                {null, "edit only name"},
                {null, null}
        });
    }

    @Test
    @DisplayName("Редактирование имени и почты пользователя")
    @Issue("Здесь баг в в коде - после сохранения меняется регистр у всех полей на строчный, из-за этого тест может флапать")
    public void editUserProfile() {
        createUser(mail, password, name);
        loginUser(mail, password);
        Response response = editUser(accessToken);
        response
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
        //Проверяем, что если изменили только одно поле, то оно осталось без изменений,
        // а второе изменилось, иначе проверяем, что изменились оба поля
        if (editMail == null && editName != null) {
            response.then().body("user.email", equalTo(mail));
            response.then().body("user.name", equalTo(editName));
        }
        if (editName == null && editMail != null) {
            response.then().body("user.email", equalTo(editMail));
            response.then().body("user.name", equalTo(name));
        }
        if (editName == null && editMail == null) {
            response.then().body("user.email", equalTo(mail));
            response.then().body("user.name", equalTo(name));
        }
        else if (editName != null && editMail != null){
            response.then().body("user.email", equalTo(editMail));
            response.then().body("user.name", equalTo(editName));
        }
        System.out.println("Данные пользователя " + mail +" измены, новые данные: " + response.body().path("user"));
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

    @Step("Генерация почты")
    public String generateRandomEmail() {
        return "user_" + RandomStringUtils.randomAlphanumeric(6) + "@yandex.ru";
    }

    @Step("Генерация пароля")
    public String generateRandomPassword() {
        return RandomStringUtils.randomNumeric(6);
    }

    @Step("Генерация имени")
    public String generateRandomName() {
        return "Test_" + RandomStringUtils.randomAlphabetic(4);
    }

    @Step("Создание пользователя")
    public void createUser(String mail, String password, String name) {
        User user = new User(mail, password, name);
        Response response =  given()
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
    public void loginUser(String mail, String password) {
        LoginUser loginUser = new LoginUser(mail, password);
        given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(loginUser))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Step("Редактирование профиля пользователя")
    public Response editUser(String accessToken){
        EditProfileUser editUserData = new EditProfileUser(editMail, editName);

        return given()
                .header("Authorization", accessToken)
                .header("Content-Type", "application/json")
                .body(gson.toJson(editUserData))
                .patch("/api/auth/user")
                .then()
                .extract().response();
    }

    @Step("Удаление пользователя")
    public void deleteUser(String accessToken) {
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
