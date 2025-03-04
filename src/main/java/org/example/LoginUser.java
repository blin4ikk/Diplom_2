package org.example;

public class LoginUser {
    private String email;
    private String password;

    public LoginUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getMail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setMail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
