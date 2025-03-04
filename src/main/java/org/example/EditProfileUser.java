package org.example;

public class EditProfileUser {
    private String email;
    private String name;

    public EditProfileUser(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public String getMail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setMail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }
}
