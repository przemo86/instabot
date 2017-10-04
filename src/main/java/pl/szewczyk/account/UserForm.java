package pl.szewczyk.account;

import pl.szewczyk.instagram.InstaUser;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

@PasswordCorrect(message = "{user.password.confirm.error}")
public class UserForm {

    private static final String NOT_BLANK_MESSAGE = "{notBlank.message}";
    private static final String EMAIL_MESSAGE = "{email.message}";
    private static final String EMAIL_EXISTS_MESSAGE = "{email-exists.message}";
    private static final String PASSWORD_CONFIRM_WRONG = "{user.password.confirm.error}";

    private String email;

    private String password;
    private String repeatPassword;

    private String expires;
    private boolean locked;
    @NotNull
    private Role role;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private Set<InstaUser> instaUsers = new HashSet<>();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<InstaUser> getInstaUsers() {
        return instaUsers;
    }

    public void setInstaUsers(Set<InstaUser> instaUsers) {
        this.instaUsers = instaUsers;
    }


    public Account createAccount() {
        Account account = new Account(getEmail(), getPassword(), Role.ROLE_USER);
        account.setLocked(isLocked());

        try {
            account.setExpires(sdf.parse(getExpires()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return account;
    }

    public UserForm(){

    }

    public UserForm(Account account) {
        this.email = account.getEmail();
        this.password = account.getPassword();
        if (account.getExpires() != null)
            this.expires = sdf.format(account.getExpires());
        this.locked = account.isLocked();
        this.role = account.getRole();
        this.instaUsers = account.getInstaUsers();
    }

    @Override
    public String toString() {
        return "UserForm{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", repeatPassword='" + repeatPassword + '\'' +
                ", expires=" + expires +
                ", locked=" + locked +
                ", role='" + role + '\'' +
                ", instaUsers=" + instaUsers +
                '}';
    }
}
