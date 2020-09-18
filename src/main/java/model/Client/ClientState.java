package model.Client;

public enum ClientState {
    LANDING("ClientLanding"),
    LOBBY("ClientLobby"),
    GAME("ClientGame");

    String state;
    ClientState(String state){
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }
}
