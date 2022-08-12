package edu.uob;

import java.util.*;
import java.util.List;

public class STAGController {
    private FileSystem model;
    private StringBuilder outPut;
    private STAGPlayer currentPlayer;
    private final ArrayList<STAGLocation> locList;
    private final TreeMap<String, HashSet<GameAction>> actionsList;
    private String cmdString;

    public STAGController(FileSystem parsedFile){
        model=parsedFile;
        locList=model.getLocationsList();
        actionsList=model.getActionsList();
    }

    public String handleCmd(String command){
        String[] split = command.trim().split(":");
        String playerName = split[0].trim();
        if(!isValidName(playerName.toLowerCase())){
            return "Invalid Player Name!\n";
        }
        if(split.length==1){
            return "Empty Command!\n";
        }
        cmdString = split[1].trim().toLowerCase();
        String[] cmd = split[1].toLowerCase().trim().split("\s+");
        ArrayList<String>splitCMD = new ArrayList<>(Arrays.asList(cmd));
        STAGLocation location = findPlayerLoc(playerName);
        return playAction(location,splitCMD);
    }

    // check if build-in command is valid or not
    public boolean isValidBuild(ArrayList<String> cmd){
        List<String> buildIn = Arrays.asList("inv","inventory","look","health","get","goto","drop");
        for(String word : cmd){
            if(buildIn.contains(word) && (!cmd.get(0).equals(word))){
                return false;
            }
        }
        return true;
    }

    // play action depends on the keywords included in the incoming command
    public String playAction(STAGLocation location, ArrayList<String> cmd){
        outPut = new StringBuilder();
        if(!isValidBuild(cmd)){
            return "Build-in command should follow user manual.\n";
        }
        if(cmd.get(0).equals("inv") || cmd.get(0).equals("inventory")){
            getInventory(cmd);
        } else if(cmd.get(0).equals("look") && cmd.size()==1){
            getLook(location);
        } else if(cmd.get(0).equals("get")){
            getGet(location, cmd);
        } else if(cmd.get(0).equals("drop")){
            getDrop(location,cmd);
        } else if(cmd.get(0).equals("goto")){
            getGoto(location,cmd);
        } else if(cmdString.startsWith("health")){
            getHealth(cmd);
        }
        else{
            playCustomAction(location,cmd);
        }
        return outPut.toString();
    }

   public void playCustomAction(STAGLocation location, ArrayList<String> cmd){
        //0. check if it has multiple keywords
       ArrayList<String> keyWord = getKeyword(cmd); // generate keyword list
       // 1. search for trigger keywords , if not exited, return warning
       if(keyWord.size()==0){
           outPut.append("Invalid action\n");
           return;
       }

       // 2.If trigger keyword existed, one or more than one , find its hashset action list
       HashSet<GameAction> actionList = new HashSet<>();
       HashSet<GameAction> gameActions;
       ArrayList<GameAction> gAction;
       int cnt = 0;
       for(String key: keyWord){
           gameActions = actionsList.get(key);
           gAction = getGameAction(gameActions,cmd);
           actionList.addAll(gAction);
           if(gAction.size()!=0){
               cnt++;    //--- if the key has matched game actions, add 1
           }
       }

       // 3. check if game action is more than 1 or is 0, return warning.
       if(actionList.size()==0){ //----if no game action is matched
           outPut.append(keyWord).append(": miss a subject in command\n");
           return;
       }else if(actionList.size()>1){ // --- if more than one gameAction are matched
           outPut.append(keyWord).append(": There is more than one thing you can do.\n");
           return;
       } else if(cnt != keyWord.size()){
           outPut.append(keyWord).append(": doing one thing at a time pls.\n");
           return;
       }

       // 3. As a BARE MINIMUM, a valid action command
       // MUST contain a trigger word/phrase and AT LEAST ONE subject.
       // check if the action's required subjects are all existed in current location or player's inv.
       List<GameAction> actionList1 = new ArrayList<>(actionList);
       GameAction gameAction = actionList1.get(0);
       if(!isValidAction(location,gameAction)){
           outPut.append(keyWord).append(" is not a valid action. (miss subjects in current location or inv)\n");
           return;
       }

       //Produce: artefacts, HP, location, characters, furniture
       consumeItems(location, gameAction);
       produceItems(location, gameAction);
       if(currentPlayer.getHealth()==0){
           restartGame(location);
           return;
       }
       outPut.append(gameAction.getNarration()).append("\n");
   }

   public  ArrayList<String> getKeyword(ArrayList<String> cmd){
       ArrayList<String> keyWord = new ArrayList<>();
       ArrayList<String> single = new ArrayList<>();
       ArrayList<String> phrase = new ArrayList<>();
       for(String s: actionsList.keySet()){
           if(s.contains("\s")){
               phrase.add(s.toLowerCase());
           }else {
               single.add(s.toLowerCase());
           }
       }
       for(String word: cmd){
           if(single.contains(word)){
               keyWord.add(word);
           }
       }
       for(String key : phrase){
           if(cmdString.contains(key.toLowerCase())){ // check trigger word/phrases
               keyWord.add(key);
           }
       }
        return keyWord;
   }

   public void produceItems(STAGLocation location, GameAction gAction){
        if(gAction.getProducedItems().size()==0){
            return;
        }
        ArrayList<String>produced = gAction.getProducedItems();
        ArrayList<String> locNames = model.getLocNames();

        // 1. check if produced items contain location， char, art, fur and HP
       // if it's location, then add the loc name into the path of the current location.
       for(String key : produced){
           if(locNames.contains(key)){
               location.getPaths().add(key);
           }
           if(key.equalsIgnoreCase("health")){
               currentPlayer.increaseHP();
           }
           // those characters, artefacts and furniture coming from other locations
           for(STAGLocation loc: locList){
               if(loc.getCharacters().containsKey(key)){
                   STAGCharacter cha = loc.getCharacters().get(key);
                   loc.getCharacters().remove(key);
                   location.getCharacters().put(key,cha);
               }
               else if(loc.getArtefacts().containsKey(key)){
                   STAGArtefact art = loc.getArtefacts().get(key);
                   loc.getArtefacts().remove(key);
                   location.getArtefacts().put(key,art);

               }
               else if(loc.getFurniture().containsKey(key)){
                   STAGFurniture fur = loc.getFurniture().get(key);
                   location.getFurniture().put(key,fur);
                   loc.getFurniture().remove(key);
               }
           }

       }
   }

   public void consumeItems(STAGLocation location, GameAction gAction){
        if(gAction.getConsumedItems().size()==0){
            return;
        }
       //When an entity is consumed it should be removed from its current location added into the storeroom location.
       ArrayList<String> consumedItems = gAction.getConsumedItems();
       STAGLocation storage = model.getStoreRoom();
       // check if consumed items existed in current location's path,fur,char and player's inventory.
       for(String key : consumedItems){
           location.getPaths().remove(key);
           if(key.equalsIgnoreCase("health")){
               currentPlayer.decreaseHP();
           }
           else if(currentPlayer.getInventory().containsKey(key)){
               STAGArtefact artefact = currentPlayer.getInventory().get(key);
               storage.getArtefacts().put(key,artefact);
               currentPlayer.getInventory().remove(key);
           }
           else if(location.getArtefacts().containsKey(key)){
               STAGArtefact artefact = location.getArtefacts().get(key);
               storage.getArtefacts().put(key,artefact);
               location.getArtefacts().remove(key);
           }
           else if(location.getFurniture().containsKey(key)){
               STAGFurniture furniture = location.getFurniture().get(key);
               storage.getFurniture().put(key,furniture);
               location.getFurniture().remove(key);
           }
           else if(location.getCharacters().containsKey(key)){
               STAGCharacter character = location.getCharacters().get(key);
               storage.getCharacters().put(key,character);
               location.getCharacters().remove(key);
           }
       }
   }

   public void restartGame(STAGLocation location){
        //When a player's health runs out (i.e. when it becomes zero) they should lose all the items in their inventory
       // (which are dropped in the location where they ran out of health) They should then be transported to the start
       // location of the game and their health level restored to the full level (i.e. 3).
       currentPlayer.initializeHP();
       location.getArtefacts().putAll(currentPlayer.getInventory());
       currentPlayer.getInventory().clear();
       location.getCharacters().remove(currentPlayer.getName());
       locList.get(0).getCharacters().put(currentPlayer.getName(),currentPlayer);
       outPut.append("you died and lost all of your items, you must return to the start of the game\n");
   }

   public boolean isValidAction(STAGLocation location, GameAction gAction){
        ArrayList<String>subjects =  gAction.getSubjects();
        int cnt=0;
        for(String sub : subjects){
            if(location.getArtefacts().containsKey(sub)) cnt++;
            if(location.getFurniture().containsKey(sub)) cnt++;
            if(location.getCharacters().containsKey(sub)) cnt++;
            if(currentPlayer.getInventory().containsKey(sub)) cnt++;
        }
       return subjects.size() == cnt;
   }

   public ArrayList<GameAction> getGameAction(HashSet<GameAction> gameActions, ArrayList<String> cmd){
        ArrayList<GameAction> gAction = new ArrayList<>();
        for(GameAction g: gameActions){
            if(hasSubject(cmd,g)){
                gAction.add(g);
            }
        }
        return gAction;
   }

   public boolean hasSubject(ArrayList<String> cmd, GameAction gAction){
        ArrayList<String>subjects = gAction.getSubjects();
        for(String sub: subjects){
            if (cmd.contains(sub.toLowerCase())){
                return true;
            }
        }
        return false;
   }

    public STAGLocation findPlayerLoc(String playerName){
        STAGLocation location = null;
        //(Single-player version) Check if there is a player in the player list in the model,
        // if not, add the new player to the initial address and add it to the first place in the player list.
        for(STAGLocation loc : locList){
            if(loc.getCharacters().containsKey(playerName)){
                location = loc;
                // set current player
                currentPlayer = (STAGPlayer) loc.getCharacters().get(playerName);
            }
        }
        if(location == null){
            STAGPlayer player = new STAGPlayer(playerName,"This is a player with name: " + playerName + "\n");
            player.initializeHP();
            locList.get(0).getCharacters().put(playerName,player);
            // set current player
            currentPlayer=player;
            location = locList.get(0);
        }
        return location;
    }

//-------------build-in commands ------------------
    public void getInventory(ArrayList<String> cmd){
        if(cmd.size()>1){
            outPut.append("We can only understand inventory or inv.\n");
            return;
        }
        if(currentPlayer.getInventory().size()==0){
            outPut.append("You are empty handed.\n");
            return;
        }
        outPut.append("You are carrying:\n");
        for(STAGArtefact art : currentPlayer.getInventory().values()){
            outPut.append(art.getName()).append(": ").append(art.getDescription()).append("\n");
        }
    }

    // play look action
    public void getLook(STAGLocation location){
        outPut.append("You are in ").append(location.getDescription()).append(". You can see:\n");
        for(STAGArtefact art : location.getArtefacts().values()){
            outPut.append(art.getName()).append(": ").append(art.getDescription()).append("\n");
        }
        for(STAGFurniture fur: location.getFurniture().values()){
            outPut.append(fur.getName()).append(": ").append(fur.getDescription()).append("\n");
        }
        for(STAGCharacter cha: location.getCharacters().values()){
            if(!cha.getName().equals(currentPlayer.getName())){
                outPut.append(cha.getName()).append(": ").append(cha.getDescription()).append("\n");
            }
        }
        outPut.append("You can access from here:\n");
        for(String path : location.getPaths()){
            outPut.append(path).append("\n");
        }
    }

    // play goto action
    public void getGoto(STAGLocation location, ArrayList<String> cmd){
        if(cmd.size()>2){
            outPut.append("We can only understand goto location.\n");
            return;
        } else if (cmd.size()==1){
            outPut.append("Where do you want to goto?\n");
        }
        String path = null;
        for(String l: location.getPaths()){
            if(cmd.contains(l.toLowerCase())){
                location.getCharacters().remove(currentPlayer.getName());
                path=l;
            }
        }
        if(path==null) {
            outPut.append("You can't go there.\n");
            return;
        }
        for(STAGLocation loc : locList){
            if(loc.getName().equals(path)){
                loc.getCharacters().put(currentPlayer.getName(),currentPlayer);
                getLook(loc);
                return;
            }
        }
    }

    //play drop action
    public void getDrop(STAGLocation location, ArrayList<String> cmd){
        if(cmd.size()>2){
            outPut.append("We can only understand drop <item>.\n");
            return;
        } else if (cmd.size()==1){
            outPut.append("What do you want to drop?\n");
        }
        String key;
        // if inventory is empty
        if(currentPlayer.getInventory().size()==0){
            outPut.append("Inventory is already empty. You can't drop anything.\n");
            return;
        }
        // drop the artifacts if it exists in the current player's inventory
        for(STAGArtefact art : currentPlayer.getInventory().values()){
             // convert each artefact's name into lowercase
            key = art.getName();
            if(cmd.contains(key.toLowerCase())){
                currentPlayer.getInventory().remove(key);
                location.getArtefacts().put(key,art);
                outPut.append("Dropped ").append(key).append("\n");
                return;
            }
        }
        // if item didn't exist in the inventory.
        outPut.append("You haven't got that.\n");
    }

    //play get action
    public void getGet(STAGLocation location, ArrayList<String> cmd){
        if(cmd.size()>2){
            outPut.append("We can only understand get <item>.\n");
            return;
        } else if(cmd.size()==1){
            outPut.append("What do you want to get?\n");
            return;
        }
        String key;
        for(STAGArtefact art: location.getArtefacts().values()){
            key = art.getName();
            if(cmd.contains(key.toLowerCase())){
                location.getArtefacts().remove(key);
                currentPlayer.getInventory().put(key,art);
                outPut.append("You picked up a ").append(art.getName()).append("\n");
                return;
            }
        }

        outPut.append("You can't see any such thing.\n");
    }

    // show player's health
    public void getHealth(ArrayList<String> cmd){
        if(cmd.size()>1){
            outPut.append("We can only understand health.\n");
            return;
        }
        int hpLevel = currentPlayer.getHealth();
        outPut.append("Current HP: ").append(hpLevel).append("\n");
    }

    //check if player's name is valid
    public boolean isValidName(String name){
        for(int i=0; i<name.length(); i++){
            boolean isValid =false;
            if (Character.isLetter(name.charAt(i))){
                isValid = true;
            }else if(Character.isSpaceChar(name.charAt(i))){
                isValid=true;
            }else if((name.charAt(i)==45) || (name.charAt(i)==39)){
                isValid=true;
            }
            if(!isValid){
                return false;
            }
        }


        return true;
    }



}
