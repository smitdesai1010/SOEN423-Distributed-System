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

import FrontEnd.ServerInterface;

public class Client {
    final String serverURL = "http://192.168.43.40:9000/?wsdl";
    final String[] serverNames = new String[] { "MTL", "TOR", "VAN" };
    ServerInterface server;

    final static String errorMessage = "Invalid input - Please try again.\n";
    static InputStreamReader is = new InputStreamReader(System.in);
    static BufferedReader br = new BufferedReader(is);

    String userId;

    public static void main(String[] args) {
        try {
            new Client().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String promptUserId() {
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

    public boolean validateUserId(String userId) {
        if (userId == null)
            return false;
        if (userId.length() < 5)
            return false;

        String serverName = userId.substring(0, 3);
        if (!Arrays.asList(serverNames).contains(serverName.toUpperCase()))
            return false;
        if (userId.charAt(3) != 'A' && userId.charAt(3) != 'P')
            return false;

        return true;
    }

    public ServerInterface getServer() {
        ServerInterface server = null;
        try {
            URL url = new URL(serverURL);
            QName qName = new QName("http://FrontEnd/", "ServerImplementationService");
            Service service = Service.create(url, qName);
            server = service.getPort(ServerInterface.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (WebServiceException e) {
            e.printStackTrace();
        }

        return server;
    }

    // TODO options based on user role
    public String getUserInput() {
        System.out.println(
                "reserve clientID eventID eventType - Reserves a ticket for a given clientID at the given eventID\n" +
                        "get clientID - Gets all active tickets for a given clientID (all cities)\n" +
                        "cancel clientID eventID eventType - Cancels the ticket for given clientID at the given eventID\n"
                        +
                        "exchange clientId old_eventID old_eventType new_eventID new_eventType - Exchanges clientId's ticket for eventId with new_eventId of type new_eventType\n"
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

    boolean running = false;
    public void start() {
        userId = promptUserId();
        server = getServer();
        if (server == null) {
            System.out.println("Server is null");
            return;
        }

        String connectedServer = "MTL";
        String input = null;
        running = input == null;
        while (running) {
            if (input == null) // initial load -> show welcome message
                System.out.println(
                        String.format("Welcome %s\nConnected Server: %s\n===============", userId,
                                connectedServer));

            input = getUserInput();
            if (input.equalsIgnoreCase("exit")) {
                running = false;
                break;
            }

            if (input.length() == 0)
                continue;

            executeRequest(input);
        }
    }

    enum MethodName {
        addReservationSlot,
        removeReservationSlot,
        listReservationSlotAvailable,
        reserveTicket,
        getEventSchedule,
        cancelTicket,
        exchangeTicket;

        String value;

        MethodName() {
            this.value = this.name();
        }
    };

    private HashMap<String, MethodName> methodNames = new HashMap<String, MethodName>() {
        {
            put("add", MethodName.addReservationSlot);
            put("remove", MethodName.removeReservationSlot);
            put("list", MethodName.listReservationSlotAvailable);
            put("reserve", MethodName.reserveTicket);
            put("get", MethodName.getEventSchedule);
            put("cancel", MethodName.cancelTicket);
            put("exchange", MethodName.exchangeTicket);
        }
    };

    MethodName getMethodName(String methodName) {
        return methodNames.get(methodName.toLowerCase());
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

        MethodName methodName = getMethodName(inputCommands[0]);
        if (methodName == null) {
            System.out.println(errorMessage);
            return;
        }

        JSONObject request = createRequest(methodName, inputCommands);
        if (request == null) {
            System.out.println(errorMessage);
            return;
        }

        String response = server.executeRequest(request.toJSONString());
        System.out.println(response);
    }

    enum JSONFieldNames {
        MethodName,
        adminID,
        participantID,
        eventType,
        eventID,
        capacity,
        new_eventType,
        new_eventID;

        String key;

        JSONFieldNames() {
            this.key = this.name();
        }
    }

    JSONObject createRequest(MethodName methodName, String[] inputCommands) {
        HashMap<String, Object> req = new HashMap<String, Object>() {
            {
                put(JSONFieldNames.MethodName.key, methodName.value);
            }
        };

        if (!validateRequest(methodName, inputCommands))
            return null;

        switch (methodName) {
            case addReservationSlot:
                // add eventID eventType capacity
                // MethodName adminId eventType eventId capacity
                req.put(JSONFieldNames.adminID.key, userId);
                req.put(JSONFieldNames.eventType.key, EventType.fromString(inputCommands[2]));
                req.put(JSONFieldNames.eventID.key, inputCommands[1]);
                req.put(JSONFieldNames.capacity.key, Integer.parseInt(inputCommands[3]));
                break;
            case removeReservationSlot:
                // remove eventID eventType
                // MethodName adminId eventType eventId'
                req.put(JSONFieldNames.adminID.key, userId);
                req.put(JSONFieldNames.eventType.key, EventType.fromString(inputCommands[2]));
                req.put(JSONFieldNames.eventID.key, inputCommands[1]);
                break;
            case listReservationSlotAvailable:
                // list eventType
                // MethodName adminId eventType
                req.put(JSONFieldNames.adminID.key, userId);
                req.put(JSONFieldNames.eventType.key, EventType.fromString(inputCommands[1]));
                break;
            case cancelTicket:
            case reserveTicket:
                // cancel clientID eventID eventType
                // reserve clientID eventID eventType
                // MethodName participantId eventType eventId
                // ? No need to pass in userId?
                req.put(JSONFieldNames.participantID.key, inputCommands[1]);
                req.put(JSONFieldNames.eventType.key, EventType.fromString(inputCommands[3]));
                req.put(JSONFieldNames.eventID.key, inputCommands[2]);
                break;
            case getEventSchedule:
                // get clientID
                // MethodName participantId
                req.put(JSONFieldNames.participantID.key, inputCommands[1]);
                break;
            case exchangeTicket:
                // exchange clientId old_eventID old_eventType new_eventID new_eventType
                // MethodName participantId eventType eventId new_eventType new_eventId
                req.put(JSONFieldNames.participantID.key, inputCommands[1]);
                req.put(JSONFieldNames.eventType.key, EventType.fromString(inputCommands[3]));
                req.put(JSONFieldNames.eventID.key, inputCommands[2]);
                req.put(JSONFieldNames.new_eventType.key, EventType.fromString(inputCommands[5]));
                req.put(JSONFieldNames.new_eventID.key, inputCommands[4]);
                break;
            default:
                return null;
        }
        return new JSONObject(req);
    }

    enum EventType {
        ArtGallery,
        Concerts,
        Theatre;

        static String fromString(String eString) {
            for (EventType eventType : EventType.values()) {
                if (eventType.name().equalsIgnoreCase(eString)) {
                    return eventType.name();
                }
            }
            return null;
        }
    }

    boolean validateEventType(String eventType) {
        return EventType.fromString(eventType) != null;
    }

    // TODO admin permissions?
    boolean validateRequest(MethodName methodName, String[] inputCommands) {
        switch (methodName) {
            case addReservationSlot:
                // add eventID eventType capacity
                // MethodName adminId eventType eventId capacity
                if (inputCommands.length != 4)
                    return false;
                try {
                    Integer.parseInt(inputCommands[3]);
                } catch (Exception e) {
                    return false;
                }
                return validateEventType(inputCommands[2]);
            case removeReservationSlot:
                // remove eventID eventType
                // MethodName adminId eventType eventId
                if (inputCommands.length != 3)
                    return false;
                return validateEventType(inputCommands[2]);
            case listReservationSlotAvailable:
                // list eventType
                // MethodName adminId eventType
                if (inputCommands.length != 2)
                    return false;

                return validateEventType(inputCommands[1]);
            case cancelTicket:
                // cancel clientID eventID eventType
                // MethodName participantId eventType eventId
                if (inputCommands.length != 4)
                    return false;

                return validateEventType(inputCommands[3]);
            case reserveTicket:
                // reserve clientID eventID eventType
                // MethodName participantId eventType eventId
                if (inputCommands.length != 4)
                    return false;

                return validateEventType(inputCommands[3]);
            case getEventSchedule:
                // get clientID
                // MethodName participantId
                if (inputCommands.length != 2)
                    return false;
                return true;
            case exchangeTicket:
                // exchange clientId old_eventID old_eventType new_eventID new_eventType
                // MethodName participantId eventType eventId new_eventType new_eventId
                if (inputCommands.length != 6)
                    return false;
                return validateEventType(inputCommands[3]) && validateEventType(inputCommands[5]);
            default:
                System.out.println("Invalid methodName in validateRequest");
                return false;
        }
    }
}
