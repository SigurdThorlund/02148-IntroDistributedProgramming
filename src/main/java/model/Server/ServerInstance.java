package model.Server;

import javafx.concurrent.Task;
import main.MainServer;
import networking.Commands.ClientCommand;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

import static networking.Commands.LocalCommand.*;
import static networking.Commands.ServerCommand.SERVER_SEED;
import static networking.Commands.ServerCommand.SERVER_SPAWN;

public class ServerInstance extends Task<Void> {
    private String lobbyName;
    private SequentialSpace consoleSpace;
    private SequentialSpace localCommunicationSpace;
    private SequentialSpace lobbySpace;


    public ServerInstance(String lobbyName, SpaceRepository repository, MainServer mainServer){
        this.lobbyName = lobbyName;
        mainServer.addTab(this);

        //Create new space to communicate with
        lobbySpace = new SequentialSpace();
        repository.add(lobbyName, lobbySpace);

        localCommunicationSpace = new SequentialSpace();

        try {
            localCommunicationSpace.put(SERVER_SPACE, lobbySpace);
            consoleSpace = mainServer.getConsoleSpace();
            localCommunicationSpace.put(SERVER_CONSOLE,consoleSpace);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void start(){
        new Thread(this).start();
    }

    @Override
    protected Void call() throws Exception {
        while (true){
            consoleSpace.put(SERVER_TEXT_CONSOLE,lobbyName,"Started lobby");
            ServerLobby serverLobby = new ServerLobby(lobbyName, localCommunicationSpace);
            serverLobby.start();

            //Game starts
            ServerGame serverGame = new ServerGame(lobbyName, localCommunicationSpace);
            serverGame.start();

            //Cleanup spaces for new game
            localCommunicationSpace.getAll(new ActualField(PLAYER_MAPPING),new FormalField(Integer.class), new FormalField(String.class));
            lobbySpace.getAll(new ActualField(SERVER_SPAWN),new FormalField(Integer.class), new FormalField(Integer.class));
            lobbySpace.getAll(new ActualField(SERVER_SEED),new FormalField(Integer.class));
            lobbySpace.getAll(new FormalField(ClientCommand.class),new FormalField(Integer.class), new FormalField(Object.class));
        }
    }
}