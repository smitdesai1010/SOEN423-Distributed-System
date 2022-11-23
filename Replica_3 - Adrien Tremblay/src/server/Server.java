package server;

import general.City;
import general.EventType;
import jdk.jfr.internal.LogLevel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static server.UdpServerThread.eventMapToReservationSlotAvailableCount;
import static server.UdpServerThread.eventMapToString;

public class Server {
    private static final String USAGE_MESSAGE = "Usage: server [MONTREAL/TORONTO/VANCOUVER] [port]";

    public static void main(String args[]) {
        Logger serverLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        try {
            FileHandler fh = new FileHandler("server.log");
            serverLogger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverLogger.info("test");

        if (args.length != 2) {
            serverLogger.severe("Please provide a city name and port");
            serverLogger.severe(USAGE_MESSAGE);
            return;
        }

        City city;
        try {
            city = City.valueOf(args[0]);
        } catch (Exception e) {
            serverLogger.severe("Not a valid city name!");
            serverLogger.severe(USAGE_MESSAGE);
            return;
        }

        int port;
        try {
            port = Integer.valueOf(args[1]);
        } catch (NumberFormatException e) {
            serverLogger.severe("The port could not be formatted as an integer");
            return;
        }

        CityReservationSystem cityReservationSystem;
        DatagramSocket udpSocket;
        try {
            cityReservationSystem = new CityReservationSystem(city);
            udpSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            serverLogger.severe("socket error");
            return;
        }

        while (true) {
            try {
                // Create a packet for the client request
                DatagramPacket clientRequestPacket = new DatagramPacket(new byte[1000], 1000);
                // Receive a request using the packet we just created
                udpSocket.receive(clientRequestPacket);

                String clientRequestString = new String(clientRequestPacket.getData(), 0, clientRequestPacket.getLength());
                JSONParser jsonParser = new JSONParser();
                JSONObject clientRequestObject = (JSONObject) jsonParser.parse(clientRequestString);

                // todo remove repeated code by extracting stuff to methods
                boolean succ = true;
                String replyString = "";
                switch ((String) clientRequestObject.get(jsonFieldNames.METHOD_NAME)) {
                    case "addReservationSlot": {
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        int capacity = Integer.valueOf((String) clientRequestObject.get(jsonFieldNames.CAPACTIY));
                        succ = cityReservationSystem.addReservationSlot(eventId, eventType, capacity);
                    }
                    case "removeReservationSlot": {
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        succ = cityReservationSystem.removeReservationSlot(eventId, eventType);
                    }
                    case "listReservationSlotAvailable": {
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        replyString = eventMapToReservationSlotAvailableCount(cityReservationSystem.getEventMap(), eventType);
                        break;
                    }
                    case "reserveTicket": {
                        serverLogger.info("test2");
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        succ = cityReservationSystem.reserveTicket(participantId, eventId, eventType);
                        break;
                    }
                    case "getEventSchedule": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        replyString = eventMapToString(cityReservationSystem.getEventMap(), cityReservationSystem.getCity(), participantId);
                        break;
                    }
                    case "cancelTicket": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        succ = cityReservationSystem.cancelTicket(participantId, eventId, eventType);
                        break;
                    }
                    case "exchangeTicket": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        String NewEventId = (String) clientRequestObject.get(jsonFieldNames.NEW_EVENT_ID);
                        EventType NewEventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.NEW_EVENT_TYPE));
                        succ = cityReservationSystem.exchangeTicket(participantId, eventId, eventType, NewEventId, NewEventType);
                    }
                    default:
                        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, "Unclear what the client is asking for!");
                }

                // creating reply object
                JSONObject replyObject = new JSONObject();
                replyObject.put(jsonFieldNames.SUCCESS, succ);
                replyObject.put(jsonFieldNames.DATA, replyString);
                final byte[] replyObjectData = replyObject.toJSONString().getBytes();

                // Create a packet for the reply
                DatagramPacket serverReplyPacket = new DatagramPacket(replyObjectData, replyObjectData.length, clientRequestPacket.getAddress(), clientRequestPacket.getPort());
                // Send the reply
                udpSocket.send(serverReplyPacket);
            } catch (Exception e) {
                serverLogger.severe(e.getStackTrace().toString());
            }
        }
    }

    public static EventType jsonStringEventTypeToEnum(String jsonStringEventType) {
        EventType eventType = null;
        switch (jsonStringEventType) {
            case "ArtGallery":
                eventType = EventType.ART_GALLERY;
                break;
            case "Concerts":
                eventType = EventType.CONCERT;
                break;
            case "Theatre":
                eventType = EventType.THEATRE;
                break;
            default:
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME, "Failure with conversion from json EventType to the ENUM!");
        }
        return eventType;
    }
}
