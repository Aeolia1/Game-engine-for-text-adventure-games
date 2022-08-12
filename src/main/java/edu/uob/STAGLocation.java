package edu.uob;

import java.util.ArrayList;
import java.util.HashMap;

public class STAGLocation extends GameEntity{
    private HashMap<String, STAGArtefact> artefacts = new HashMap<>();
    private HashMap<String, STAGFurniture> furniture = new HashMap<>();
    private HashMap<String, STAGCharacter> characters = new HashMap<>();
    private ArrayList<String>paths= new ArrayList<>();

    public STAGLocation(String name, String description) {
        super(name, description);
    }

    public void addPaths(String path){
        paths.add(path);
    }

    public ArrayList<String> getPaths() {
        return paths;
    }

    public void setArtefacts(String artName, String desc){
        artefacts.put(artName, new STAGArtefact(artName,desc));
    }

    public HashMap<String, STAGArtefact> getArtefacts(){
        return artefacts;
    }

    public void setFurniture(String furName, String desc){
        furniture.put(furName, new STAGFurniture(furName,desc));
    }

    public HashMap<String, STAGFurniture> getFurniture() {
        return furniture;
    }

    public void setCharacters(String chaName, String desc){
        characters.put(chaName, new STAGCharacter(chaName,desc));
    }

    public HashMap<String, STAGCharacter> getCharacters() {
        return characters;
    }
}
