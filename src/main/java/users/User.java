package users;

import java.util.List;
import java.util.UUID;

public class User {
    private String email;
    private String password;
    private String id;
    private boolean isConfirmed = false;
    private List<Rent> rents;
    public User() {}
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.id = UUID.randomUUID().toString();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
    public String getId() {
        return id;
    }
    public void setConfirmed() {
        this.isConfirmed = true;
    }
    public boolean isConfirmed() {
        return isConfirmed;
    }
}
