package Servers;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class Server {
    final static int MONTREAL_REPLICA_PORT = 2301;
    final static int TORONTO_REPLICA_PORT = 2302;
    final static int VANCOUVER_REPLICA_PORT = 2303;



    public static void main(String[] args){

        if (args.length != 1) {
            System.out.println("Please provide a city name");
            return;
        }

        ServiceInterface service;
        DatagramSocket socketUDP;
        int port;
        switch(args[0]) {
            case "Montreal":
                service = new ServiceMTL();
                port = MONTREAL_REPLICA_PORT;
                break;
            case "Toronto":
                service = new ServiceTOR();
                port = TORONTO_REPLICA_PORT;
                break;
            case "Vancouver":
                service = new ServiceVAN();
                port = VANCOUVER_REPLICA_PORT;
                break;
            default:
                System.out.println("Not a valid city name!");
                return;
        }

        try {
           socketUDP = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        while(true){
            DatagramPacket packet = new DatagramPacket(new byte[1000], 1000);
            // creating reply object
            JSONObject replyObject = new JSONObject();
            try {
                socketUDP.receive(packet);

                String clientRequestString = new String(packet.getData(), 0, packet.getLength());
                JSONParser jsonParser = new JSONParser();
                JSONObject clientRequestObject = null;
                try {
                    clientRequestObject = (JSONObject) jsonParser.parse(clientRequestString);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                boolean die = false;
                switch ((String) clientRequestObject.get(jsonFieldNames.METHOD_NAME)) {
                    case "die" : {
                        die = true;
                        break;
                    }
                    case "addReservationSlot": {
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        String eventType = (String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE);
                        int capacity = Math.toIntExact((Long) clientRequestObject.get(jsonFieldNames.CAPACTIY));
                        replyObject = service.addReservationSlot(eventId, eventType, capacity);
                        break;
                    }
                    case "removeReservationSlot": {
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        String eventType = (String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE);
                        replyObject = service.removeReservationSlot(eventId, eventType);
                        break;
                    }
                    case "listReservationSlotAvailable": {
                        String eventType = (String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE);
                        replyObject = service.listReservationSlotAvailable(eventType);
                        break;
                    }
                    case "reserveTicket": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        String eventType = (String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE);
                        replyObject = service.reserveTicket(participantId, eventId, eventType);
                        break;
                    }
                    case "getEventSchedule": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        replyObject = service.getEventSchedule(participantId);
                        break;
                    }
                    case "cancelTicket": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        String eventType = (String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE);
                        replyObject = service.cancelTicket(participantId, eventId);
                        break;
                    }
                    case "exchangeTicket": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        String eventType = (String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE);
                        String NewEventId = (String) clientRequestObject.get(jsonFieldNames.NEW_EVENT_ID);
                        String NewEventType = (String) clientRequestObject.get(jsonFieldNames.NEW_EVENT_TYPE);
                        replyObject = service.exchangeTicket(participantId, eventId, NewEventId, NewEventType);
                        break;
                    }
                    default:
                        System.out.println("Unclear what the client is asking for!");
                }

                final byte[] replyObjectData = replyObject.toJSONString().getBytes();

                // Create a packet for the reply
                DatagramPacket serverReplyPacket = new DatagramPacket(replyObjectData, replyObjectData.length, packet.getAddress(), packet.getPort());
                // Send the reply
                socketUDP.send(serverReplyPacket);
                if (die)
                    break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
