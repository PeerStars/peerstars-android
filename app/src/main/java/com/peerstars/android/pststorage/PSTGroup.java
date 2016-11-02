package com.peerstars.android.pststorage;

/**
 * Created by bmiller on 10/28/2015.
 * Group container.
 */
public class PSTGroup {
    private int _id;
    private String _name;
    private String _address;
    private String _street;
    private String _city;
    private String _state;
    private String _zip;

    public PSTGroup() {
        this._id = 0;
        this._name = "";
        this._address = "";
        this._street = "";
        this._city = "";
        this._state = "";
        this._zip = "";
    }

    public PSTGroup(int id, String name, String address) {
        this._id = id;
        this._name = name;
        this._address = address;
        this._street = "";
        this._city = "";
        this._state = "";
        this._zip = "";
    }

    PSTGroup(int id, String name, String address, String street, String city, String state, String zip) {
        this._id = id;
        this._name = name;
        this._address = address;
        this._street = street;
        this._city = city;
        this._state = state;
        this._zip = zip;
    }

    public int getId() {
        return this._id;
    }

    public String getName() {
        return this._name;
    }

    public String getStreet() {
        return this._street;
    }

    public String getCity() {
        return this._city;
    }

    public String getState() {
        return this._state;
    }

    public String getAddress() {
        return this._address;
    }
}
