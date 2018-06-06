package r.com.testingdot;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String email;
    public String token;
    public String ID;
    public User(){}
    public User(String ID,String email, String token){
        this.ID=ID;
        this.email=email;
        this.token=token;
    }

    public String getID() {
        return ID;
    }

    public void setId(String id) {
        ID = id;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
