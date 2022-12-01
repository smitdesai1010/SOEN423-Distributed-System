package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import util.requests.AddRequest;
import util.requests.GetRequest;
import util.requests.RemoveRequest;
import util.requests.ServerRequest;

public class DTRS implements IDTRS {
    String city;
    public int port;
    HashMap<EventType, HashMap<String, EventData>> serverData;
    UDP interServerUDP;

    public DTRS(String city) {
        this.city = city;
        this.port = getPort(city);
        this.serverData = ServerDataHelper.getStartupData(city);
        interServerUDP = new UDP(this, this.port);
        interServerUDP.start();
    }

    int getPort(String city) {
        switch (city) {
            case "Montreal":
                return ServerPort.MTL.PORT;
            case "Toronto":
                return ServerPort.TOR.PORT;
            case "Vancouver":
                return ServerPort.VAN.PORT;
            default:
                System.out.println("DEFAULT GETPORT");
                return ServerPort.MTL.PORT;
        }
    }

    enum jsonFieldNames {
        Success,
        Data;

        String key;

        jsonFieldNames() {
            this.key = this.name();
        }
    }

    @Override
    public JSONObject addReservationSlot(String eventId, EventType eventType, int capacity) {
        JSONObject response = new JSONObject();
        String eventLocationId = eventId.substring(0, 3);
        // admin operation on current server
        if (eventLocationId.equalsIgnoreCase(this.city)) {
            HashMap<String, EventData> events = this.serverData.get(eventType);
            if (events.containsKey(eventId)) {
                response.put(jsonFieldNames.Success.key, false);
                response.put(jsonFieldNames.Data.key,
                        String.format("Unable to add eventId: %s as it already exists.", eventId));
                // logResponse(ServerAction.add, user, params, response);
                return response;
            }

            events.put(eventId, new EventData(capacity));
            this.serverData.put(eventType, events);

            response.put(jsonFieldNames.Success.key, true);
            response.put(jsonFieldNames.Data.key, String.format("Successfully added eventId: %s", eventId));
            // logResponse(ServerAction.add, user, params, response);
            return response;
        }

        // admin operation on remote server
        for (ServerPort server : ServerPort.values()) {
            if (eventLocationId.equalsIgnoreCase(server.name())) {
                AddRequest request = new AddRequest(eventType.toString(), eventId, capacity);
                response = sendServerRequest(request, server);

                // logResponse(ServerAction.add, user, params, response);
                return response;
            }
        }

        // logResponse(ServerAction.add, user, params, response);
        response.put(jsonFieldNames.Success.key, false);
        response.put(jsonFieldNames.Data.key, String
                .format("Invalid eventId: %s - Unable to connect to remote server %s.", eventId, eventLocationId));
        return response;
    }

    @Override
    public JSONObject removeReservationSlot(String eventId, EventType eventType) {
        JSONObject response = new JSONObject();
        // if (!user.hasPermission(Permission.remove)) {
        // logNoPermission(ServerAction.remove, user);
        // return new Response(
        // "User doesn't have valid permissions to access : " +
        // Permission.remove.label.toUpperCase());
        // }
        String[] params = new String[] { eventId, eventType.toString() };
        String eventLocationId = eventId.substring(0, 3);
        // admin operation on current server
        if (eventLocationId.equalsIgnoreCase(this.city)) {
            HashMap<String, EventData> events = this.serverData.get(eventType);
            if (!events.containsKey(eventId)) {
                response.put(jsonFieldNames.Success.key, false);
                response.put(jsonFieldNames.Data.key, "Unable to remove event: eventId does not exist");
                // logResponse(ServerAction.remove, user, params, response);
                return response;
            }

            events.remove(eventId);
            this.serverData.put(eventType, events);
            response.put(jsonFieldNames.Success.key, true);
            response.put(jsonFieldNames.Data.key, String.format("Successfully removed eventId: %s", eventId));
            // logResponse(ServerAction.remove, user, params, response);
            return response;
        }

        // admin operation on remote server
        for (ServerPort server : ServerPort.values()) {
            if (eventLocationId.equalsIgnoreCase(server.name())) {
                RemoveRequest request = new RemoveRequest(eventType.toString(), eventId);
                response = sendServerRequest(request, server);
                // logResponse(ServerAction.remove, params, response);
                return response;
            }
        }

        response.put(jsonFieldNames.Success.key, false);
        response.put(jsonFieldNames.Data.key,
                String.format("Invalid eventId: %s - Unable to connect to remote server %s.",
                        eventId, eventLocationId));
        // logResponse(ServerAction.remove, user, params, response);
        return response;
    }

    @Override
    public JSONObject listReservationSlotsAvailable(EventType eventType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject reserveTicket(String participantId, String eventId, EventType eventType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject cancelTicket(String participantId, String eventId, EventType eventType) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getEventsById(String id) {
        StringBuilder events = new StringBuilder(this.city);
        // (EventType, EventId Hashmap)
        for (Map.Entry<EventType, HashMap<String, EventData>> eType : this.serverData.entrySet()) {
            if (eType.getKey().equals(EventType.None))
                continue;

            ArrayList<String> tempEvents = new ArrayList<>();
            // (eventId, EventData)
            for (Map.Entry<String, EventData> eData : eType.getValue().entrySet()) {
                if (eData.getValue().guests.contains(id))
                    tempEvents.add(eData.getKey());
            }
            events.append("\t" + eType.getKey().name() + " : " + tempEvents.toString() + "\n");
        }
        return events.toString();
    }

    @Override
    public JSONObject getEventSchedule(String participantId) {
        StringBuilder events = new StringBuilder(getEventsById(participantId));
        JSONObject response = new JSONObject();

        // operation on remote server - return events w/o fetching
        String homeServer = participantId.substring(0, 3);
        if (!homeServer.equalsIgnoreCase(this.city)) {
            response.put(jsonFieldNames.Success.key, true);
            response.put(jsonFieldNames.Data.key, events.toString());
            return response;
        }

        String[] params = new String[] { participantId };
        // operation on current (home) server
        // fetch remote server events
        for (ServerPort server : ServerPort.values()) {
            GetRequest request = new GetRequest(participantId);
            response = sendServerRequest(request, server);

            events.append(response.get(jsonFieldNames.Data.key));
        }

        response.put(jsonFieldNames.Success.key, true);
        response.put(jsonFieldNames.Data.key, events.toString());
//        logResponse(ServerAction.get, user, params, response);
        return response;
    }

    @Override
    public JSONObject exchangeTicket(String participantId, String eventId, EventType eventType, String newEventId,
            EventType newEventType) {
        // TODO Auto-generated method stub
        return null;
    }

    public JSONObject sendServerRequest(ServerRequest request, ServerPort server) {
        try {
            System.out.println("================");
            System.out.println(request.toString());
            System.out.println("================");
            DatagramSocket socket = new DatagramSocket();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(request);
            byte[] out = baos.toByteArray();

            DatagramPacket packet = new DatagramPacket(out, out.length, InetAddress.getByName("localhost"),
                    getPort(server.name()));
            System.out.println("Sending request to: " + server.name().toUpperCase() + " port: "
                    + String.valueOf(getPort(server.name())));

            // this.logger.info(String.format("Sending UDP request to %s for %s",
            // server.name().toUpperCase(),
            // request.type.toString()));

            socket.send(packet);

            byte[] in = new byte[8192];
            packet = new DatagramPacket(in, in.length);
            socket.receive(packet);
            ByteArrayInputStream bais = new ByteArrayInputStream(in);
            ObjectInputStream ois = new ObjectInputStream(bais);

            JSONObject response = new JSONObject();
            try {
                response = (JSONObject) ois.readObject();
            } catch (Exception e) {
                System.out.println("Exception in readObject sendServerRequest: " + e.getMessage());
                response.put(jsonFieldNames.Data.key, e.getMessage());
                response.put(jsonFieldNames.Success.key, false);
                return response;
            }

            // this.logger.info(String.format("Received UDP response from
            // %s\nResponse:\nCompleted: %s\nMessage:%s",
            // server.name().toUpperCase(), response.status, response.message));
            return response;
        } catch (Exception e) {
            System.out.println("Exception in sendRequest: " + e.getMessage());
            JSONObject response = new JSONObject();
            response.put(jsonFieldNames.Data.key, "Exception in sendRequest: " + e.getMessage());
            response.put(jsonFieldNames.Success.key, false);
            return response;
        }
    }
}
