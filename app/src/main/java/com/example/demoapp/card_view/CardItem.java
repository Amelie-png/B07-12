package com.example.demoapp.card_view;

import java.util.ArrayList;
public class CardItem {
    public String childId;
    public String providerId;
    String name;
    int profilePic;
    int profileBackground;
    ArrayList<String> providerUserNames;

    public CardItem(String name, int profilePic, int profileBackground, String childID, String providerId, ArrayList<String> providerIDs) {
        this.name = name;
        this.profilePic = profilePic;
        this.profileBackground = profileBackground;
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