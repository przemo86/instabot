package pl.szewczyk.account;

/**
 * Created by przem on 29.09.2017.
 */
public enum Role {
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_USER("ROLE_USER");

    private String role;

    public String getRole() {
        return role;
    }

    Role(String val) {
        this.role = val;
    }
}
