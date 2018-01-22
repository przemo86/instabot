package pl.szewczyk.instagram;

import java.util.List;

/**
 * Created by przem on 18.11.2017.
 */
public class InstaUserListWrapper {
    private List<InstaUser> instaUser;

    public InstaUserListWrapper() {
    }

    public InstaUserListWrapper(List<InstaUser> instaUser) {
        this.instaUser = instaUser;
    }

    public List<InstaUser> getInstaUser() {
        return instaUser;
    }

    public void setInstaUser(List<InstaUser> instaUser) {
        this.instaUser = instaUser;
    }
}
