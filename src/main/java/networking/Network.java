package networking;

public class Network {
    //Default values
    public static final String HEADER = "tcp://";
    public static final String CONNECTION_TYPE =  "keep";
    public static final String CONNECTION_POINT = "/entry";
    public static final String DEFAULT_PORT = "2148";

    private static String currentPort = DEFAULT_PORT;

    public static String createLobbyURI(String ip, String lobbyName){
        return buildURI(ip,'/' + lobbyName);
    }

    public static String createEntryURI(String ip){
        return buildURI(ip, CONNECTION_POINT);
    }

    public static String createGate(String ip){
        return buildURI(ip, "");
    }
    private static String buildURI(String ip, String location){
        String[] ipWithPort = ip.split(":");

        //Is format IP:PORT?
        if (ipWithPort.length == 2) {
            currentPort = ipWithPort[1];
            return Network.HEADER + ip + location + "?" + CONNECTION_TYPE;
        }
        return Network.HEADER + ip + ':' + currentPort + location + '?' + CONNECTION_TYPE;
    }
}