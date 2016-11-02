package com.peerstars.android.pstauthentication;

/**
 * Created by bmiller on 8/26/2015.
 */
public class PSTAuthenticatedUser {

    // create all class scope objects
    private String _user = "";
    private String _firstName = "";
    private String _lastName = "";
    private String _password = "";
    private String _token = "";
    private String _tokenTimestamp = "";
    private String _passwordKey = "";

    // Create the getters and setters
    public void setUser(String user) {
        this._user = user;
    }

    public void setFirstName(String firstName) {
        this._firstName = firstName;
    }

    public void setLastName(String lastName) {
        this._lastName = lastName;
    }

    public void setPassword(String password) {
        this._password = password;
    }

    public void setToken(String token) {
        this._token = token;
    }

    public void setTokenTimestamp(String tokenTimestamp) {
        this._tokenTimestamp = tokenTimestamp;
    }

    public void setPasswordKeyword(String passwordKey) {
        this._passwordKey = passwordKey;
    }

    public String getUser() {
        return this._user;
    }

    public String getFirstName() {
        return this._firstName;
    }

    public String getLastName() {
        return this._lastName;
    }

    public String getPassword() {
        return this._password;
    }

    public String getToken() {
        return this._token;
    }

    public String getTokenTimestamp() {
        return this._tokenTimestamp;
    }

    public String getPasswordKeyword() {
        return this._passwordKey;
    }
}
