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
import org.json.simple.parser.JSONParser;
import util.requests.*;

import javax.xml.ws.Response;

public class DTRS implements IDTRS {
    String city;
    String prefix;
    public int port;
    HashMap<EventType, HashMap<String, EventData>> serverData;
    UDP interServerUDP;

    public DTRS(String city) throws Exception {
        this.city = city;
        this.prefix = getPrefix(city);
        this.port = getPort(city);
        this.serverData = ServerDataHelper.getStartupData(city);
        interServerUDP = new UDP(this, this.port);
        interServerUDP.start();
    }

    private String getPrefix(String city) throws Exception {
        switch (city) {
            case "Montreal":
                return "MTL";
            case "Toronto":
                return "TOR";
            case "Vancouver":
                return "VAN";
            default:
                throw new Exception("Prefix not found");
        }
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

    JSONObject createResponse(Boolean success, String data) {
        JSONObject response = new JSONObject();
        response.put(jsonFieldNames.Success.key, success);
        response.put(jsonFieldNames.Data.key, data);
        return response;
    }

    @Override
    public JSONObject addReservationSlot(String eventId, EventType eventType, int capacity) {
        JSONObject response = new JSONObject();
        String eventLocationId = eventId.substring(0, 3);
        // admin operation on current server
        if (eventLocationId.equalsIgnoreCase(this.prefix)) {
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
        String[] params = new String[] { eventId, eventType.toString() };
        String eventLocationId = eventId.substring(0, 3);
        // admin operation on current server
        if (eventLocationId.equalsIgnoreCase(this.prefix)) {
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

    private String printEvents(EventType eventType) {
        HashMap<String, EventData> events = this.serverData.get(eventType);
        StringBuilder out = new StringBuilder();
        // (eventId, EventData)
        for (Map.Entry<String, EventData> e : events.entrySet()) {
            out.append(String.format("%s\n%s\n", e.getKey(), e.getValue().toString()));
        }
        return out.toString();
    }

    @Override
    public JSONObject listReservationSlotsAvailable(String adminId, EventType eventType) {
        // current server events
        StringBuilder events = new StringBuilder(printEvents(eventType));

        JSONObject response = new JSONObject();
        // called from remote - return w/o further inter-server communication
        String locationId = adminId.substring(0,3);
        if (!this.prefix.equalsIgnoreCase(locationId)) {
            response.put(jsonFieldNames.Success.key, true);
            response.put(jsonFieldNames.Data.key, events.toString());
            return response;
        }

        String[] params = new String[] { eventType.toString() };
        // fetch remote server events
        for (ServerPort server : ServerPort.values()) {
            if (server.name().equalsIgnoreCase(locationId) || server.PORT == -1) continue;
            ListRequest request = new ListRequest(adminId, eventType.toString());
            response = sendServerRequest(request, server);
            if ((boolean) response.get(jsonFieldNames.Success.key))
                events.append(response.get(jsonFieldNames.Data.key));
        }
        response.put(jsonFieldNames.Success.key, true);
        response.put(jsonFieldNames.Data.key, events.toString());
//        logResponse(ServerAction.list, user, params, response);
        return response;
    }

    @Override
    public JSONObject reserveTicket(String participantId, String eventId, EventType eventType) {
        String[] params = new String[] { participantId, eventId, eventType.toString() };

        String homeServer = participantId.substring(0, 3);
        String eventLocationId = eventId.substring(0, 3);
        if (eventLocationId.equalsIgnoreCase(this.prefix)) { // operation on current server
            HashMap<String, EventData> eventData = this.serverData.get(eventType);
            if (!eventData.containsKey(eventId)) {
//                logResponse(ServerAction.reserve, user, params, response);
                return createResponse(false, String.format("Unable to reserve eventId: %s - Does not exist.", eventId));
            }

            if (eventData.get(eventId).capacity < 1) {
//                logResponse(ServerAction.reserve, user, params, response);
                return createResponse(false, String.format("Unable to reserve eventID: %s - No remaining tickets.", eventId));
            }
            if (eventData.get(eventId).guests.contains(participantId)) {
//                logResponse(ServerAction.reserve, user, params, response);
                return createResponse(false, String.format(
                        "Unable to reserve eventId: %s for clientId: %s - Client already has a reservation", eventId, participantId));
            }

            // check if called through remote server - to ensure max 3 remote reservations
            if (!eventLocationId.equalsIgnoreCase(homeServer)) {
                int count = 0;
                // (eventId, EventData)
                for (Map.Entry<String, EventData> e : this.serverData.get(eventType).entrySet()) {
                    if (e.getValue().guests.contains(participantId))
                        count++;
                }
                if (++count > 3) {
//                    logResponse(ServerAction.reserve, user, params, response);
                    return createResponse(false, "Unable to reserve event - Maximum of 3 remote reservations per city.");
                }
            }

            eventData.get(eventId).addGuest(new String(participantId));

//            logResponse(ServerAction.reserve, user, params, response);
            return createResponse(true, String.format("Successfully reserved eventType: %s eventId: %s for clientId: %s",
                    eventType.name(), eventId, participantId));
        }

        // operation on remote server
        for (ServerPort server : ServerPort.values()) {
            if (server.PORT == -1 || server.name().equalsIgnoreCase(homeServer)) continue;
            if (eventLocationId.equalsIgnoreCase(server.name())) {
                ReserveRequest request = new ReserveRequest(eventType.toString(), participantId, eventId);
                return sendServerRequest(request, server);
            }
        }

//        logResponse(ServerAction.reserve, user, params, response);
        return createResponse(false, String.format("Invalid eventId: %s - Unable to connect to remote server.", eventId));
    }

    @Override
    public JSONObject cancelTicket(String participantId, String eventId, EventType eventType) {
        String eventLocationId = eventId.substring(0, 3);
        // operation on current server
        String[] params = new String[] { participantId, eventId };
        if (eventLocationId.equalsIgnoreCase(this.prefix)) {
            // (key, value) => (eventType, eventData HashMap)
            for (Map.Entry<EventType, HashMap<String, EventData>> event : this.serverData.entrySet()) {
                // (key, value) => (eventId, eventData)
                for (Map.Entry<String, EventData> eventData : event.getValue().entrySet()) {
                    if (eventData.getKey().equalsIgnoreCase(eventId)) {
                        if (!eventData.getValue().guests.contains(participantId)) {
//                            logResponse(ServerAction.cancel, user, params, response);
                            return createResponse(false, String.format(
                                    "Unable to cancel ticket - %s does not have a reservation for %s", participantId, eventId));
                        }

                        this.serverData.get(event.getKey()).get(eventData.getKey()).removeGuest(participantId);

//                        logResponse(ServerAction.cancel, user, params, response);
                        return createResponse(true, "Successfully canceled the ticket for eventId: " + eventId);
                    }
                }
            }
//            logResponse(ServerAction.cancel, user, params, response);
            return createResponse(false, "Unable to cancel ticket - eventId does not exist.");
        }

        String homeServer = participantId.substring(0,3);
        // operation on remote server
        for (ServerPort server : ServerPort.values()) {
            if (server.PORT == -1 || server.name().equalsIgnoreCase(homeServer)) continue;
            if (server.name().equalsIgnoreCase(eventLocationId)) {
                CancelRequest request = new CancelRequest(participantId, eventId, eventType.name());
                return sendServerRequest(request, server);
            }
        }

//        logResponse(ServerAction.cancel, user, params, response);
        return createResponse(false, String.format("Invalid eventId: %s - Unable to connect to remote server.", eventId));
    }

    private String getEventsById(String id) {
        StringBuilder events = new StringBuilder(this.city + "\n");
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
        if (!homeServer.equalsIgnoreCase(this.prefix)) {
            response.put(jsonFieldNames.Success.key, true);
            response.put(jsonFieldNames.Data.key, events.toString());
            return response;
        }

        String[] params = new String[] { participantId };
        // operation on current (home) server
        // fetch remote server events
        for (ServerPort server : ServerPort.values()) {
            if (server.name().equalsIgnoreCase(homeServer) || server.PORT == -1) continue;
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
        String[] params = new String[] { participantId, eventId, newEventId,
                newEventType.toString() };

        String oldLocationId = eventId.substring(0, 3);
        String newLocationId = newEventId.substring(0, 3);

        ExchangeCondition old_cond = new ExchangeCondition(eventId);
        ExchangeCondition new_cond = new ExchangeCondition(newEventType,
                newEventId);

        // * both operations on the same server (current) -> no remote connection
        if (oldLocationId.equalsIgnoreCase(newLocationId) &&
                oldLocationId.equalsIgnoreCase(this.prefix)) {
            // check if old_eventId is valid for id input
            // (key, value) => (eventType, eventData HashMap)
            for (Map.Entry<EventType, HashMap<String, EventData>> event : this.serverData.entrySet()) {
                // (key, value) => (eventId, eventData)
                for (Map.Entry<String, EventData> eventData : event.getValue().entrySet()) {
                    if (eventData.getKey().equalsIgnoreCase(eventId)) {
                        if (!eventData.getValue().guests.contains(participantId)) {
//                            logResponse(ServerAction.cancel, user, params, response);
                            return createResponse(false, String.format(
                                    "Unable to exchange ticket - %s does not have a reservation to exchange for %s", participantId,
                                    eventId));
                        }
                        old_cond = new ExchangeCondition(true, event.getKey(), eventData.getKey());
                    }
                }
            }

            if (!old_cond.status) {
//                logResponse(ServerAction.exchange, user, params, response);
                return createResponse(false, "Unable to exchange tickets - Invalid eventId");
            }

            EventType nEventType = new_cond.eventType;
            if (nEventType.equals(EventType.None)) {
//                logResponse(ServerAction.exchange, user, params, response);
                return createResponse(false, "Unable to exchange tickets - Invalid eventType");
            }

            EventData new_eventData = this.serverData.get(nEventType)
                    .get(new_cond.eventId);
            if (new_eventData.guests.contains(participantId)) {
//                logResponse(ServerAction.exchange, user, params, response);
                return createResponse(false, String.format("Cannot exchange tickets for %s as %s already has a ticket",
                        new_cond.eventId, participantId));
            }
            if (new_eventData.capacity <= 0) {
//                logResponse(ServerAction.exchange, user, params, response);
                return createResponse(false, String.format("Cannot exchange tickets for %s as there is no capacity",
                        new_cond.eventId));
            }
            // success perform exchange and return
            this.serverData.get(old_cond.eventType).get(old_cond.eventId).removeGuest(participantId);
            this.serverData.get(nEventType).get(new_cond.eventId).addGuest(participantId);

            return createResponse(true, String.format("Successfully exchanged tickets - %s exchanged for %s", old_cond.eventId,
                    new_cond.eventId));
        }

        String homeServer = participantId.substring(0, 3);
        // * both operations on the same server (not this server) -> find server &
        // call
        if (oldLocationId.equalsIgnoreCase(newLocationId)) {
            // operation on remote server
            for (ServerPort server : ServerPort.values()) {
                if (server.PORT == -1 || server.name().equalsIgnoreCase(homeServer))
                    continue;
                if (server.name().equalsIgnoreCase(oldLocationId)) {
                    ExchangeRequest request = new ExchangeRequest(participantId, eventId,
                            newEventId,
                            newEventType.toString());
                    return sendServerRequest(request, server);
                }
            }
        }

        // * one condition on current & one condition on remote
        if (oldLocationId.equalsIgnoreCase(this.prefix) ||
                newLocationId.equalsIgnoreCase(this.prefix)) {
            ExchangeCondition currentCond;
            ExchangeCondition remoteCond;

            if (oldLocationId.equalsIgnoreCase(this.prefix)) {
                currentCond = new ExchangeCondition(false, null, eventId);
                remoteCond = new ExchangeCondition(false, newEventType, newEventId);
            } else { // newLocationId.equalsIgnoreCase(this.name)
                currentCond = new ExchangeCondition(false, newEventType, newEventId);
                remoteCond = new ExchangeCondition(false, null, eventId);
            }

            JSONObject response = new JSONObject();
            // (key, value) => (eventType, eventData HashMap)
            for (Map.Entry<EventType, HashMap<String, EventData>> event : this.serverData.entrySet()) {
                // (key, value) => (eventId, eventData)
                for (Map.Entry<String, EventData> eventData : event.getValue().entrySet()) {
                    if (eventData.getKey().equalsIgnoreCase(currentCond.eventId)) {
                        if (!eventData.getValue().guests.contains(participantId) && currentCond.eventType == null) {
//                            logResponse(ServerAction.cancel, user, params, response);
                            return createResponse(false, String.format(
                                    "Unable to exchange ticket - %s does not have a reservation to exchange for %s", participantId,
                                    currentCond.eventId));
                        }
                        currentCond.eventType = event.getKey();
                        currentCond.status = true;
                    }
                }
            }

            if (!currentCond.status) {
//                logResponse(ServerAction.exchange, user, params, response);
                return createResponse(false, "Unable to exchange tickets - Invalid eventId");
            }
            String remoteLocationId = remoteCond.eventId.substring(0, 3);
            // currentCond is valid attempt remoteCond
            for (ServerPort server : ServerPort.values()) {
                if (server.PORT == -1 || server.name().equalsIgnoreCase(homeServer)) continue;
                if (server.name().equalsIgnoreCase(remoteLocationId)) {
                    ServerRequest request;
                    if (oldLocationId.equalsIgnoreCase(this.prefix)) { // reserve on remote server
                        request = new ReserveRequest(newEventType.toString(), participantId,
                                remoteCond.eventId);
                    } else { // cancel on remote server
                        request = new CancelRequest(participantId, remoteCond.eventId, remoteCond.eventType.name());
                    }

                    response = sendServerRequest(request, server);

                    if ((boolean) response.get(jsonFieldNames.Success.key)) { // remoteCond success -> proceed with currentCond
                        if (oldLocationId.equalsIgnoreCase(this.prefix)) { // currentCond = cancel
                            this.serverData.get(currentCond.eventType).get(currentCond.eventId)
                                    .removeGuest(participantId);
                        } else { // currentCond = reserve
                            this.serverData.get(currentCond.eventType).get(currentCond.eventId)
                                    .addGuest(participantId);
                        }
                        response = createResponse(true, String.format("Successfully exchanged ticket - exchanged %s for %s",
                                eventId, newEventId));
                    }
//                    logResponse(ServerAction.exchange, user, params, response);
                    return response;
                }
            }
//            logResponse(ServerAction.exchange, user, params, response);
            return createResponse(false, "Unable to exchange ticket - Invalid new event information");
        }

        // * both operations on different servers
        // attempt exchange operation from remote server
        for (ServerPort server : ServerPort.values()) {
            if (server.PORT == -1 || server.name().equalsIgnoreCase(homeServer)) continue;
            ExchangeRequest request = new ExchangeRequest(participantId, eventId, newEventId, newEventType.toString());
//            logResponse(ServerAction.exchange, user, params, response);
            return sendServerRequest(request, server);
        }

//        logResponse(ServerAction.exchange, user, params, response);
        return createResponse(false, "Unable to exchange ticket - Invalid event information");
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
                    getPort(server.name));
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
