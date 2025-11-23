package com.example.demoapp.card_view;

import java.util.ArrayList;
public class CardItem {
    String name;
    int profilePic;
    int profileBackground;

    String childUserName;
    String parentUserName;
    ArrayList<String> providerUserNames;

    public CardItem(String name, int profilePic, int profileBackground, String childID, String parentID, ArrayList<String> providerIDs) {
        this.name = name;
        this.profilePic = profilePic;
        this.profileBackground = profileBackground;
    }

    public void addProvider(String username){
        providerUserNames.add(username);
    }
}