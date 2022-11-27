package server;

import general.City;
import general.Command;
import general.Event;
import general.EventType;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CityReservationSystem implements ReservationSystem {
    private static int nextClientNumber = 0;
    private City city;
    private  HashMap<EventType, HashMap<String, Event>> eventMap;
    private Logger logger;

    public CityReservationSystem(City city) {
        this.city = city;

        setupEventMap();

        // Configuring logger
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        try {
            FileHandler fh = new FileHandler(city + ".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UdpServerThread udpServerThread = new UdpServerThread(this);
        udpServerThread.start();
    }

    @Override
    public int requestClientNumber() {
        return nextClientNumber++;
    }

    @Override
    public boolean addReservationSlot(String eventId, EventType eventType, int capacity)  {
        synchronized (eventMap) {
            Event event = eventMap.get(eventType).get(eventId);
            if (event == null) {
                logger.severe("tried to add a reservation slot but could not find the event with id (" + eventId + ")!");
                return false;
            }

            boolean success = event.addReservationSlot(capacity);
            logRecord("addReservationSlot", new String[] {eventId, eventType.toString()}, success, String.valueOf(success));
            return success;
        }
    }

    @Override
    public boolean removeReservationSlot(String eventId, EventType eventType)  {
        synchronized (eventMap) {
            Event event = eventMap.get(eventType).get(eventId);
            if (event == null) {
                logger.severe("tried to add a reservation slot but could not find the event with id (" + eventId + ")!");
                return false;
            }
            boolean success = event.removeReservationSlot();
            logRecord("removeReservationSlot", new String[] {eventId, eventType.toString()}, success, String.valueOf(success));
            return success;
        }
    }

    @Override
    public String listReservationSlotsAvailable(EventType eventType)  {
        StringBuilder ret = new StringBuilder(eventType + "S: ");

        ArrayList<City> cityServersToFetch = getUpServers();

        for (City city : cityServersToFetch) {
            Payload payload = new Payload();
            payload.command = Command.LIST_RESERVATION_SLOTS_AVAILABLE;
            payload.eventType = eventType;
            String clientMessage = "reservations " + eventType;
            ret.append(sendUdpMessage(payload, city));
        }

        String retString = ret.toString();
        logRecord("listReservationSlotsAvailable", new String[] {eventType.toString()}, true, retString);
        return retString;
    }

    @Override
    public boolean reserveTicket(String participantId, String eventId, EventType eventType)  {
        synchronized (eventMap) {
            String foundIdAcronym = eventId.substring(0, 3);
            boolean reservingInOtherCity = !foundIdAcronym.equals(city.getIdAcronym());
            if (reservingInOtherCity) {
                City otherCity = City.idAcronymToCity(foundIdAcronym);
                if (otherCity == null) {
                    logger.severe("Failed to reserve ticket. (" + foundIdAcronym + ") is not a valid city acronym!");
                    return false;
                }

                Payload payload = new Payload();
                payload.command = Command.RESERVE_TICKET;
                payload.participantId = participantId;
                payload.eventId = eventId;
                payload.eventType = eventType;

                return sendUdpMessage(payload, otherCity).equals("true");
            }

            Event eventToReserve = eventMap.get(eventType).get(eventId);

            if (eventToReserve == null) {
                logger.severe("Failed to reserve ticket. Event with the id (" + eventId + ") cannot be found!");
                return false;
            }

            for (Event event : eventMap.get(eventType).values()) {
                if (event.getReservationSlot() != null && checkEventsSameDay(event.getId(), eventId) && event.getReservationSlot().getReservations().contains(participantId)) {
                    logger.severe("You cannot reserve more than one event of the same type in the same day");
                    return false;
                }
            }

            boolean success = eventToReserve.makeReservation(participantId);
            logRecord("reserveTicket", new String[] {participantId, eventId, eventType.toString()}, success, String.valueOf(success));
            return success;
        }
    }

    @Override
    public boolean cancelTicket(String participantId, String eventId, EventType eventType)  {
        synchronized (eventMap) {
            String foundIdAcronym = eventId.substring(0, 3);
            boolean cancellingInOtherCity = !foundIdAcronym.equals(city.getIdAcronym());
            if (cancellingInOtherCity) {
                City otherCity = City.idAcronymToCity(foundIdAcronym);
                if (otherCity == null) {
                    logger.severe("Failed to cancel ticket. (" + foundIdAcronym + ") is not a valid city acronym!");
                    return false;
                }

                Payload payload = new Payload();
                payload.command = Command.CANCEL_TICKET;
                payload.participantId = participantId;
                payload.eventId = eventId;
                payload.eventType = eventType;

                return sendUdpMessage(payload, otherCity).equals("true");
            }

            Event eventToCancel = eventMap.get(eventType).get(eventId);

            if (eventToCancel == null) {
                logger.severe("Failed to cancel ticket. Event with the id '" + eventId + "' cannot be found!");
                return false;
            }

            boolean success = eventToCancel.cancelReservation(participantId);
            logRecord("cancelTicket", new String[] {participantId, eventId, eventType.toString()}, success, String.valueOf(success));
            return success;
        }
    }

    @Override
    public String getEventSchedule(String participantId)  {
        StringBuilder ret = new StringBuilder();

        ArrayList<City> cityServersToFetch = getUpServers();

        for (City city : cityServersToFetch) {
            Payload payload = new Payload();
            payload.command = Command.GET_EVENT_SCHEDULE;
            payload.participantId = participantId;
            ret.append(sendUdpMessage(payload, city));
        }

        String retString = ret.toString();
        logRecord("getEventSchedule", new String[] {participantId}, true, retString);
        return retString;
    }

    @Override
    public boolean exchangeTicket(String participantId, String eventId, EventType eventType, String newEventId, EventType newEventType) {
        // check if the event is reserved for the participant
        boolean eventReserved;

        String foundIdAcronym = eventId.substring(0, 3);
        boolean cancellingInOtherCity = !foundIdAcronym.equals(city.getIdAcronym());
        City foundCity = null;
        if (!cancellingInOtherCity) {
            // check here
            eventReserved = checkForReservation(participantId, eventId, eventType);
        } else {
            // check in a foreign city
            for (City c : City.values()) {
                if (c.getIdAcronym().equals(foundIdAcronym)) {
                    foundCity = c;
                    break;
                }
            }
            if (foundCity == null) {
                logger.severe("Cannot cancel ticket. '" + foundIdAcronym + "' is not a valid city ID acronym!");
                return false;
            }

            Payload payload = new Payload();
            payload.command = Command.CHECK_FOR_RESERVATION;
            payload.participantId = participantId;
            payload.eventId = eventId;
            payload.eventType = eventType;

            eventReserved = sendUdpMessage(payload, foundCity).equals("true");
        }

        if (!eventReserved) {
            logger.severe("Cannot exchange ticket. '" + eventId + "' is not a reserved for participant " + participantId);
            return false;
        }

        // check if the new event has space
        boolean eventHasSpace;

        String foundIdAcronymNewEvent = newEventId.substring(0, 3);
        boolean reservingInOtherCity = !foundIdAcronymNewEvent.equals(city.getIdAcronym());
        City foundCityNewEvent = null;
        if (!reservingInOtherCity) {
            // check here
            eventHasSpace = checkIfEventHasSpace(newEventId, newEventType);
        } else {
            // check in a foreign city
            for (City c : City.values()) {
                if (c.getIdAcronym().equals(foundIdAcronymNewEvent)) {
                    foundCityNewEvent = c;
                    break;
                }
            }
            if (foundCityNewEvent == null) {
                logger.severe("Cannot cancel ticket. '" + foundIdAcronymNewEvent + "' is not a valid city ID acronym!");
                return false;
            }

            Payload payload = new Payload();
            payload.command = Command.CHECK_FOR_SPACE;
            payload.eventId = newEventId;
            payload.eventType = newEventType;

            eventHasSpace = sendUdpMessage(payload, foundCityNewEvent).equals("true");
        }

        if (!eventHasSpace) {
            logger.severe("Cannot exchange ticket. '" + newEventId + "' does not have space");
            return false;
        }

        // do the actual cancelling and booking in a hypothetically atomic manner

        // cancelling ticket
        boolean cancelSucceeded;
        if (cancellingInOtherCity) {
            Payload cancelPayload = new Payload();
            cancelPayload.command = Command.CANCEL_TICKET;
            cancelPayload.participantId = participantId;
            cancelPayload.eventId = eventId;
            cancelPayload.eventType = eventType;
            cancelSucceeded = sendUdpMessage(cancelPayload, foundCity).equals("true");
        } else {
           cancelSucceeded = cancelTicket(participantId, eventId, eventType);
        }

        boolean reserveSucceeded;
        if (reservingInOtherCity) {
            Payload reservePayload = new Payload();
            reservePayload.command = Command.RESERVE_TICKET;
            reservePayload.participantId = participantId;
            reservePayload.eventId = newEventId;
            reservePayload.eventType = newEventType;
            reserveSucceeded = sendUdpMessage(reservePayload, foundCityNewEvent).equals("true");
        } else {
            reserveSucceeded = reserveTicket(participantId, newEventId, newEventType);
        }

        if (!(cancelSucceeded || reserveSucceeded)) {
            if (!cancelSucceeded && !reserveSucceeded)  {
                logger.severe("HOLY SHIT! BOTH OPERATIONS FAILED!");
                return false;
            }

            logger.severe("HOLY SHIT! THE ATOMICITY FAILED! THE " + (!cancelSucceeded ? "CANCEL" : "RESERVE") + " operation failed!");
            return false;
        }

        return true;
    }

    public void setupEventMap() {
        this.eventMap = EventMapFactory.createEventMap(city);
    }

    public City getCity() {
        return city;
    }

    public HashMap<EventType, HashMap<String, Event>> getEventMap() {
        return eventMap;
    }

    private void logRecord(String requestName, String[] requestParameters, boolean requestSuccess, String response) {
        StringBuilder sb = new StringBuilder("Request ");
        sb.append(requestName).append("(");
        for (int i = 0 ; i < requestParameters.length ; i++) {
            String parameter = requestParameters[i];
            sb.append(parameter + ((i != requestParameters.length - 1) ? ", " : ""));
        }
        sb.append(")");
        sb.append(requestSuccess ? " SUCCEEDED!\n" : " FAILED!\n");
        sb.append("Response:\n");
        sb.append(response);
        logger.info(sb.toString());
    }

    public boolean checkForReservation(String participantId, String eventId, EventType eventType) {
        Event foundEvent = foundEvent = eventMap.get(eventType).get(eventId);

        if (foundEvent == null) {
            logger.severe("Event with id '" + eventId + "' cannot be found!");
            return false;
        }

        if (foundEvent.getReservationSlot() == null)  {
            logger.severe("Event with id '" + eventId + "' has not reservation slot!");
            return false;
        }

        if (!foundEvent.getReservationSlot().getReservations().contains(participantId))  {
            logger.severe("Event with id '" + eventId + "' does not have a reservation for participant " + participantId);
            return false;
        }

        return true;
    }

    public boolean checkIfEventHasSpace(String eventId, EventType eventType) {
        Event foundEvent = eventMap.get(eventType).get(eventId);

        if (foundEvent == null) {
            logger.severe("Cannot cancel ticket. Event with the id '" + eventId + "' cannot be found!");
            return false;
        }

        if (foundEvent.getReservationSlot() == null)  {
            logger.severe("Event with id '" + eventId + "' has not reservation slot!");
            return false;
        }

        if (foundEvent.getReservationSlot().isFull())  {
            logger.severe("Event with id '" + eventId + "' is full!");
            return false;
        }

        return true;
    }

    private static void listServerAvailability() {
        ArrayList<City> upCities = getUpServers();

        System.out.println("Server Status:");
        for (City city : City.values()) {
           System.out.println(city.name() + ": " + (upCities.contains(city) ? "UP" : "DOWN"));
        }
    }

    private static ArrayList<City> getUpServers() {
        ArrayList<City> ret = new ArrayList<City>();

        for (City city : City.values()) {
            if (!available(city.getPort()))
                ret.add(city);
        }

        return ret;
    }

    private static boolean checkEventsSameDay(String event1, String event2) {
        return (event1.substring(4).equals(event2.substring(4)));
    }

    private static String sendUdpMessage(Payload payload, City city) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(6400);
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(payload);
            final byte[] data = baos.toByteArray();

            DatagramSocket udpSocket = new DatagramSocket();
            InetAddress udpClientHost = InetAddress.getByName("localhost");
            // creating the client request packet using the clientMessage string
            DatagramPacket clientRequestPacket = new DatagramPacket(data, data.length, udpClientHost, city.getPort());
            udpSocket.send(clientRequestPacket);
            // creating a reply packet to hold the reply from the server
            DatagramPacket serverReplyPacket = new DatagramPacket(new byte[1000], 1000);
            // receiving the reply packet from the server
            udpSocket.receive(serverReplyPacket);

            // adding server reply to ret
            return new String(serverReplyPacket.getData(), 0, serverReplyPacket.getLength());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean available(int port) throws IllegalStateException {
        try (DatagramSocket ignored = new DatagramSocket(port)) {
            return true;
        } catch (BindException e) {
            return false;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}
