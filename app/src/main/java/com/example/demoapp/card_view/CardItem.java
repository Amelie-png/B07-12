package com.example.demoapp.card_view;

import java.util.ArrayList;
public class CardItem {
    public String childId;
    public String providerId;
    String firstName;
    String lastName;
    String childUsername;
    ArrayList<String> providerUserNames;

    public CardItem(String firstName, String lastName, String childUsername, String childID, String providerId, ArrayList<String> providerIDs) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.childUsername = childUsername;
        this.childId = childID;
        this.providerId = providerId;
        this.providerUserNames = providerIDs;
    }

    public void addProvider(String username){
        providerUserNames.add(username);
    }
    public String getChildId() { return childId; }
    public String getProviderId() { return providerId; }
}