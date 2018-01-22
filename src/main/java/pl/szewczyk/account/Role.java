package pl.szewczyk.account;

/**
 * Created by przem on 29.09.2017.
 */
public enum Role {
    ROLE_USER("ROLE_USER", "role.user"),
    ROLE_ADMIN("ROLE_ADMIN", "role.admin");

    private String role;
    private String description;

    public String getRole() {
        return role;
    }

    public String getDescription() {
        return description;
    }

        Role(String val, String desc) {
        this.role = val;
        this.description = desc;
    }
}
