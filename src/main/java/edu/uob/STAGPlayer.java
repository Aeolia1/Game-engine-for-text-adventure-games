package edu.uob;

import java.util.HashMap;

public class STAGPlayer extends STAGCharacter {
    private HashMap<String, STAGArtefact> inventory = new HashMap<>();
    private int health;

    public STAGPlayer(String name, String description) {
        super(name, description);
    }

    public HashMap<String,STAGArtefact> getInventory() {
        return inventory;
    }

    public void initializeHP(){
        health=3;
    }

    public int getHealth(){
        return health;
    }

    public void increaseHP(){
        if(health<3){
            health+=1;
        }
    }

    public void decreaseHP(){
        if(health==0) return;
        health-=1;
    }
}
