package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

// PLEASE READ:
// The tests in this file will fail by default for a template skeleton, your job is to pass them
// and maybe write some more, read up on how to write tests at
// https://junit.org/junit5/docs/current/user-guide/#writing-tests
final class BasicCommandTests {

  private GameServer server;

  // Make a new server for every @Test (i.e. this method runs before every @Test test case)
  @BeforeEach
  void setup() {
      File entitiesFile = Paths.get("config/basic-entities.dot").toAbsolutePath().toFile();
      File actionsFile = Paths.get("config/basic-actions.xml").toAbsolutePath().toFile();
      server = new GameServer(entitiesFile, actionsFile);
  }

  // Test to spawn a new server and send a simple "look" command
  @Test
  void testLookingAroundStartLocation() {
    String response = server.handleCommand("player I : look  ").toLowerCase();
    assertTrue(response.contains("empty room"), "Did not see description of room in response to look");
    assertTrue(response.contains("magic potion"), "Did not see description of artifacts in response to look");
    assertTrue(response.contains("wooden trapdoor"), "Did not see description of furniture in response to look");
    String response2 = server.handleCommand("player II : look").toLowerCase();
    assertTrue(response2.contains("empty room"), "Did not see description of room in response to look");
    assertTrue(response2.contains("magic potion"), "Did not see description of artifacts in response to look");
    assertTrue(response2.contains("wooden trapdoor"), "Did not see description of furniture in response to look");
  }
  // Add more unit tests or integration tests here.
  @Test
  void testPlayersName(){
    String response = server.handleCommand("player 8 : look  ").toLowerCase();
    assertTrue(response.contains("invalid"), "Is valid player name, pls check.");
    String response2 = server.handleCommand("play_er : look  ").toLowerCase();
    assertTrue(response2.contains("invalid"), "Is valid player name, pls check.");
    String response3 = server.handleCommand("play'er '' : look  ").toLowerCase();
    assertTrue(response3.contains("empty room"), "Is invalid player name, pls check.");
    String response4 = server.handleCommand("player-aANNA : look  ").toLowerCase();
    assertTrue(response4.contains("empty room"), "Is invalid player name, pls check.");
  }

  @Test
  void testBuildInCMD(){
    //--- test goto , look
    String response = server.handleCommand("player : look around  ").toLowerCase();
    assertTrue(response.contains("invalid"), "Is valid build in, pls check.");
    response = server.handleCommand("player : go look  ").toLowerCase();
    assertTrue(response.contains("build-in"), "Is valid build in, pls check.");
    response = server.handleCommand("player : goto the forest  ").toLowerCase();
    assertTrue(response.contains("we can only understand goto location"), "Is valid build in, pls check.");
    response = server.handleCommand("player : goto forest cabin  ").toLowerCase();
    assertTrue(response.contains("we can only understand goto location"), "Is valid build in, pls check.");

    //---test get
    response = server.handleCommand("player : I get KEY ").toLowerCase();
    assertTrue(response.contains("build-in"), "Is valid build in, pls check.");
    response = server.handleCommand("player : get a potion  ").toLowerCase();
    assertTrue(response.contains("only understand get"), "Is valid build in, pls check.");
    response = server.handleCommand("player : get potion ").toLowerCase();
    assertTrue(response.contains("picked"), "Is invalid build in, pls check.");

    //--- test drop
    response = server.handleCommand("  player :   drop  magic   potion    ").toLowerCase();
    assertTrue(response.contains("only understand drop"), "Is valid build in, pls check.");
    response = server.handleCommand(" player : drop  ").toLowerCase();
    assertTrue(response.contains("what do you want"), "Is valid build in, pls check.");
    response = server.handleCommand("player : drop potion ").toLowerCase();
    assertTrue(response.contains("dropped"), "Is invalid build in, pls check.");
    response = server.handleCommand("player : drop potion ").toLowerCase();
    assertTrue(response.contains("empty"), "Is valid build in, pls check.");

    //---test inv/inventory
    response = server.handleCommand("  player :   inv    ").toLowerCase();
    assertTrue(response.contains("empty"), "Is invalid build in, pls check.");
    response = server.handleCommand(" player : inventory   ").toLowerCase();
    assertTrue(response.contains("empty"), "Is valid build in, pls check.");
    response = server.handleCommand("player : my inv ").toLowerCase();
    assertTrue(response.contains("build-in"), "Is valid build in, pls check.");
    response = server.handleCommand("player :  inventory  ONE  ").toLowerCase();
    assertTrue(response.contains("only understand "), "Is valid build in, pls check.");

    //---test health
    response = server.handleCommand("  player :   health   ").toLowerCase();
    assertTrue(response.contains("3"), "Is invalid build in, pls check.");
    response = server.handleCommand(" player :    my   health   ").toLowerCase();
    assertTrue(response.contains("build-in"), "Is valid build in, pls check.");
    response = server.handleCommand("player : health   level ").toLowerCase();
    assertTrue(response.contains("we can only"), "Is valid build in, pls check.");
  }



}
