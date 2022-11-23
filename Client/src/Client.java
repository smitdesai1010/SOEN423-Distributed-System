import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.json.simple.JSONObject;

public class Client {
    final static String errorMessage = "Invalid input - Please try again.";
    static InputStreamReader is = new InputStreamReader(System.in);
    static BufferedReader br = new BufferedReader(is);

    public static void main(String[] args) {
        try {
            String userId = promptUserId();
            new Client().start(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String promptUserId() {
        String userId = null;
        boolean isValid = false;

        System.out.println("==== Login ====");
        while (!isValid) {
            System.out.print("Enter User Id: ");
            try {
                userId = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            isValid = validateUserId(userId);
            if (!isValid) {
                System.out.println(errorMessage + "\n\n");
            }
        }
        System.out.println("\n\n");
        return userId;
    }

    public static boolean validateUserId(String userId) {
        if (userId == null)
            return false;
        if (userId.length() < 5)
            return false;

        // TODO better method of getting server names?
        String[] serverNames = new String[] { "MTL", "TOR", "VAN" };
        String serverName = userId.substring(0, 3);
        if (!Arrays.asList(serverNames).contains(serverName.toUpperCase()))
            return false;
        if (userId.charAt(3) != 'A' && userId.charAt(3) != 'P')
            return false;

        return true;
    }

    // ! Temp
    interface ServerInterface {
    }
    // ! Temp

    // TODO return server
    public void getServer(String userId) {
        String serverName = "MTL"; // TODO serverName check for URL

        ServerInterface server;
        try {
            URL url = new URL(String.format("http://localhost:8080/%s?wsdl", serverName));
            QName qName = new QName("http://util/", "ServerService");
            Service service = Service.create(url, qName);
            server = service.getPort(ServerInterface.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (WebServiceException e) {
            e.printStackTrace();
        }

    }

    // TODO (String message) from server
    public String getUserInput() {
        // System.out.println(message);
        System.out.println(
                "reserve clientID eventID eventType - Reserves a ticket for a given clientID at the given eventID\n" +
                        "get clientID - Gets all active tickets for a given clientID (all cities)\n" +
                        "cancel clientID eventID - Cancels the ticket for given clientID at the given eventID\n" +
                        "exchange clientId old_eventID new_eventID new_eventType - Exchanges clientId's ticket for eventId with new_eventId of type new_eventType\n"
                        +
                        "add eventID eventType capacity - Adds a new reservation slot with a given eventID\n" +
                        "remove eventID eventType - Removes a reservation slot for a given eventID\n" +
                        "list eventType - Lists all available reservation slot for a given eventType (in all cities)\n"
                        +
                        "exit - Exits the client session");
        System.out.print("> ");
        String input = "";
        try {
            input = br.readLine();
        } catch (IOException e) {
            System.out.println("IOException in getUserInput: " + e.getMessage());
        }
        return input;
    }

    public void start(String userId) {
        // TODO get server implementation
        ServerInterface x; // ! temp
        String connectedServer = "MTL"; // TODO connectedServer
        String input = null;
        boolean running = input == null;
        while (running) {
            // TODO check with team to obtain user options from the server (ie.
            // getUserOptions(String userId) -> String)
            if (input == null) // initial load -> show welcome message
                System.out.println(
                        String.format("Welcome %s\nConnected Server: %s\n===============", userId, connectedServer));

            input = getUserInput();

            if (input == "exit") {
                running = false;
                break;
            }

            if (input.length() == 0)
                continue;

            executeRequest(input);
        }
    }

    public void executeRequest(String input) {
        if (!input.contains(" ")) {
            System.out.println(errorMessage);
            return;
        }
        String[] inputCommands = input.split(" ");

        if (inputCommands.length <= 1) {
            System.out.println(errorMessage);
            return;
        }
    }

}
