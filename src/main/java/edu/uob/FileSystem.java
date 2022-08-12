package edu.uob;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class FileSystem {
    private final ArrayList<STAGLocation>locationsList = new ArrayList<>();
    private final TreeMap<String, HashSet<GameAction>> actionsList = new TreeMap<>();
    private ArrayList<String> locNames = new ArrayList<>();


    public FileSystem(File entitiesFile, File actionFile){
        setEntities(entitiesFile);
        setActions(actionFile);
    }

    public void setEntities(File entitiesFile){
        Parser parser = new Parser();
        try{
            FileReader reader = new FileReader(entitiesFile);
            parser.parse(reader);
            Graph wholeDocument = parser.getGraphs().get(0);
            ArrayList<Graph> sections = wholeDocument.getSubgraphs();

            // The locations will always be in the first subgraph
            ArrayList<Graph> locations = sections.get(0).getSubgraphs();
            for (Graph location : locations){
                Node locationDetails = location.getNodes(false).get(0);
                String desc = locationDetails.getAttribute("description");
                String locationName = locationDetails.getId().getId();
                locNames.add(locationName);

                STAGLocation eLocation = new STAGLocation(locationName,desc);

                //set different entities display in the location
                ArrayList<Graph>subEntities = location.getSubgraphs(); //every entity subgraph has sub entities
                for (Graph e : subEntities){
                    // Get entity type of each location subgraph(artefacts, characters, furniture)
                    String entityName = e.getId().getId(); //artefacts
                    // Get entities list for each entity type
                    ArrayList<Node> nodeName = e.getNodes(false); //potion...
                    for(Node node : nodeName){
                        String nName = node.getId().getId();
                        String nDesc = node.getAttribute("description");
                        switch (entityName) {
                            case "artefacts" -> eLocation.setArtefacts(nName, nDesc);
                            case "furniture" -> eLocation.setFurniture(nName, nDesc);
                            case "characters" -> eLocation.setCharacters(nName, nDesc);
                        }
                    }
                }
                // Add location to a location list.
                locationsList.add(eLocation);
            }

            // The paths will always be in the second subgraph
            ArrayList<Edge> paths = sections.get(1).getEdges();
            for (Edge path : paths){
                Node fromLocation = path.getSource().getNode();
                String fromName = fromLocation.getId().getId();
                Node toLocation = path.getTarget().getNode();
                String toName = toLocation.getId().getId();
                for(STAGLocation l : locationsList){
                    if(l.getName().equals(fromName)){
                        l.addPaths(toName);
                    }
                }
            }

        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void setActions(File actionFile){
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(actionFile);
            Element root = document.getDocumentElement();
            NodeList actions = root.getChildNodes();
            // Get the first action (only the odd items are actually actions - 1, 3, 5 etc.)

            for(int i=1; i< actions.getLength(); i++){
                if(i % 2 != 0){
                    Element action = (Element)actions.item(i);
                    //---test----
                    GameAction gAction = new GameAction();
                    //-----//
                    //----set subjects for each gameAction hashset
                    Element subjects = (Element) action.getElementsByTagName("subjects").item(0);
                    NodeList subEntities = subjects.getElementsByTagName("entity");
                    for (int j=0; j<subEntities.getLength();j++){
                        String entity = subEntities.item(j).getTextContent();
                        gAction.addSubject(entity);
                    }

                    //----set consumed items for each gameAction hashset
                    Element consumed = (Element) action.getElementsByTagName("consumed").item(0);
                    NodeList conEntities = consumed.getElementsByTagName("entity");
                    for(int j=0; j<conEntities.getLength(); j++){
                        String entity = conEntities.item(j).getTextContent();
                        gAction.setConsumedItem(entity);
                    }

                    //---set produced items for each gameAction hashset
                    Element produced = (Element) action.getElementsByTagName("produced").item(0);
                    NodeList proEntities = produced.getElementsByTagName("entity");
                    for(int j=0; j<proEntities.getLength(); j++){
                        String entity = proEntities.item(j).getTextContent();
                        gAction.setProducedItem(entity);
                    }

                    //---set narration for each action hashset
                    String narration = action.getElementsByTagName("narration").item(0).getTextContent();
                    gAction.setNarration(narration);
                    //--- add new generated gameAction to a hashset
                    //---set trigger name and action hash set as key/value pair for actionsList
                    Element triggers = (Element) action.getElementsByTagName("triggers").item(0);
                    NodeList keywords = triggers.getElementsByTagName("keyword");
                    for(int j=0; j< keywords.getLength(); j++){
                        String key = keywords.item(j).getTextContent();
                        if(actionsList.containsKey(key)){
                            if(!isExist(gAction,key)){
                                actionsList.get(key).add(gAction);
                            }
                        }
                        else{
                            HashSet<GameAction> gSet = new HashSet<>();
                            gSet.add(gAction);
                            actionsList.put(key,gSet);
                        }
                    }
                }
            }
        } catch(ParserConfigurationException | SAXException | IOException pce) {
            pce.printStackTrace();
        }
    }

    public boolean isExist(GameAction gAction, String key){
        for(GameAction a : actionsList.get(key)){
            if(a.getSubjects().equals(gAction.getSubjects())){
                return true;
            }
        }
        return false;
    }

    public ArrayList<STAGLocation> getLocationsList() {
        return locationsList;
    }

    public TreeMap<String, HashSet<GameAction>> getActionsList() {
        return actionsList;
    }

    public ArrayList<String> getLocNames() {
        return locNames;
    }

    public STAGLocation getStoreRoom(){
        int index = locNames.indexOf("storeroom");
        return locationsList.get(index);
    }
}
