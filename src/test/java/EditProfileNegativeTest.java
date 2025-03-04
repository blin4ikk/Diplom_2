import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.EditProfileUser;
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
public class EditProfileNegativeTest {
    private String mail;
    private String password;
    private String name;
    private String editMail;
    private String editName;
    private String accessToken;
    private String deleteMessage;
    private final Gson gson = new Gson();
    public EditProfileNegativeTest(String editMail, String editName){
        this.editMail = editMail;
        this.editName = editName;
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        mail = generateRandomEmail();
        password = generateRandomPassword();
        name = generateRandomName();
        editMail = generateRandomEmail();
        editName = generateRandomName();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"z@y.f", "n"},
                {"edit only mail", null},
                {null, "edit only name"},
                {null, null}
        });
    }

    @Test
    @DisplayName("Редактирование данных пользователя без авторизации")
    public void editUserProfileWithoutAuth() {
        createUser(mail, password, name);
        Response response = editUserWithoutAuth();
        response.then()
                .statusCode(401)
                .body("success", equalTo(false));
        System.out.println("Данные пользователя " + mail +"не измены, ошибка: " + response.body().path("message"));
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

    @Step("Создание пользователя")
    private void createUser(String mail, String password, String name) {
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

    @Step("Редактирование данных пользователя без авторизации")
    private Response editUserWithoutAuth() {
        EditProfileUser editProfileUser = new EditProfileUser(editMail, editName);
        return given()
                .header("Content-Type", "application/json")
                .body(editProfileUser)
                .when()
                .patch("/api/auth/user")
                .then()
                .extract().response();
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
