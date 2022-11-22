package server;

import general.City;
import general.Event;
import general.EventType;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdpServerThread extends Thread{
    private final CityReservationSystem cityReservationSystem;

    public UdpServerThread(CityReservationSystem cityReservationSystem) {
        this.cityReservationSystem = cityReservationSystem;
    }

    @Override
    public void run() {
        DatagramSocket udpSocket = null;
        try {
            udpSocket = new DatagramSocket(cityReservationSystem.getCity().getPort());
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                // Create a packet for the client request
                DatagramPacket clientRequestPacket = new DatagramPacket(new byte[1000], 1000);
                // Receive a request using the packet we just created
                udpSocket.receive(clientRequestPacket);

                final ByteArrayInputStream bais = new ByteArrayInputStream(clientRequestPacket.getData());
                final ObjectInputStream oos = new ObjectInputStream(bais);
                Payload payload = (Payload) oos.readObject();

                String replyString = "";

                switch (payload.command) {
                    case GET_EVENT_SCHEDULE:
                        replyString = eventMapToString(cityReservationSystem.getEventMap(), cityReservationSystem.getCity(), payload.participantId);
                        break;
                    case LIST_RESERVATION_SLOTS_AVAILABLE:
                        replyString = eventMapToReservationSlotAvailableCount(cityReservationSystem.getEventMap(), payload.eventType);
                        break;
                    case RESERVE_TICKET:
                        replyString = String.valueOf(cityReservationSystem.reserveTicket(payload.participantId, payload.eventId, payload.eventType));
                        break;
                    case CANCEL_TICKET:
                        replyString = String.valueOf(cityReservationSystem.cancelTicket(payload.participantId, payload.eventId, payload.eventType));
                        break;
                    case CHECK_FOR_RESERVATION:
                        replyString = String.valueOf(cityReservationSystem.checkForReservation(payload.participantId, payload.eventId, payload.eventType));
                        break;
                    case CHECK_FOR_SPACE:
                        replyString = String.valueOf(cityReservationSystem.checkIfEventHasSpace(payload.eventId, payload.eventType));
                        break;
                    default:
                        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, "Unclear what the client is asking for!");
                }

                // Create a packet for the reply
                DatagramPacket serverReplyPacket = new DatagramPacket(replyString.getBytes(), replyString.getBytes().length, clientRequestPacket.getAddress(), clientRequestPacket.getPort());
                // Send the reply
                udpSocket.send(serverReplyPacket);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    private static String eventMapToString(HashMap<EventType, HashMap<String, Event>> eventMap, City city, String participantId) {
        StringBuilder ret = new StringBuilder("---------------------" + city.name() + "---------------------\n");

        for (EventType eventType : EventType.values()) {
            ret.append("-> ").append(eventType.toString()).append(" Events\n");

            for (Event event : eventMap.get(eventType).values()) {
                if (event.getReservationSlot() == null || !event.getReservationSlot().getReservations().contains(participantId))
                    continue;

                ret.append("    -> ").append(event.getId()).append(", Reservations: [");
                ArrayList<String> reservations = event.getReservationSlot().getReservations();
                for (int i = 0 ; i < reservations.size() ; i++) {
                    ret.append(reservations.get(i)).append(i != event.getReservationSlot().getReservations().size() - 1 ? ", " : "");
                }
                ret.append("]").append(" (").append(event.getReservationSlot().getReservations().size()).append(" / ").append(event.getReservationSlot().getCapacity()).append(")\n");
            }
        }

        return ret.toString();
    }

    private static String eventMapToReservationSlotAvailableCount(HashMap<EventType, HashMap<String, Event>> eventMap, EventType eventType) {
        StringBuilder ret = new StringBuilder();

        Collection<Event> eventsCollection = eventMap.get(eventType).values();
        Event[] events = eventsCollection.toArray(new Event[0]);

        for (int i = 0 ; i < events.length ; i++) {
            if (events[i].getReservationSlot() != null)
                ret.append(events[i].getId()).append(" ").append(events[i].getReservationSlot().getCapacity() - events[i].getReservationSlot().getReservations().size()).append((i != events.length - 1) ? ", " : ".");
        }

        return ret.toString();
    }
}
