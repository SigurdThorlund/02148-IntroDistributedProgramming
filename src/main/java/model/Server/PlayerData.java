package model.Server;

import org.jspace.SequentialSpace;

public class PlayerData {
    private String name;
    private int id;
    private int playerDeathVote;
    private boolean isAlive;
    private SequentialSpace communicationSpace;
    private int shotsFired;

    public PlayerData(String name, int playerID){
        this.name = name;
        id = playerID;
        isAlive = true;
    }

    public String getName() {
        return name;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getId() {
        return id;
    }

    public int incrementDeathVote(){
        return ++playerDeathVote;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }
}