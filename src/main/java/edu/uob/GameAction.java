package edu.uob;

import java.util.ArrayList;

public class GameAction
{
    private ArrayList<String> subjects = new ArrayList<>();
    private ArrayList<String> consumedItems = new ArrayList<>();
    private ArrayList<String> producedItems = new ArrayList<>();
    private String narration;

    public GameAction(){
        narration = null;
    }

    public void addSubject(String subject) {
        subjects.add(subject);
    }

    public ArrayList<String> getSubjects() {
        return subjects;
    }

    public void setConsumedItem(String consumedItem) {
        consumedItems.add(consumedItem);
    }

    public ArrayList<String> getConsumedItems() {
        return consumedItems;
    }

    public ArrayList<String> getProducedItems() {
        return producedItems;
    }

    public void setProducedItem(String producedItem) {
        producedItems.add(producedItem);
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getNarration() {
        return narration;
    }
}

