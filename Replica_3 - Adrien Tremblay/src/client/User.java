package client;

import general.City;
import general.Command;
import general.EventType;
import general.Role;
import server.ReservationSystem;
import server.Server;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class User {
    private City city;
    private Role role;
    private String id;
    private ReservationSystem reservationSystem;
    private Logger logger;
    private ArrayList<String> eventsBookedInotherCities;

    public User(City city, Role role) {
        this.city = city;
        this.role = role;
        eventsBookedInotherCities = new ArrayList<String>();

        // connecting to the associated server
        try {
            URL url = new URL(Server.endpointAddress + city.getUrlExtension() +"?wsdl");
            QName qName = new QName("http://server/", "CityReservationSystemService");
            Service service = Service.create(url, qName);
            reservationSystem = service.getPort(ReservationSystem.class);
        } catch (Exception e) {
            System.out.println("Error: There is no matching server for your city!");
            System.exit(0);
            return;
        }

        // generate the id
        try {
            this.id = generateId(city, role, reservationSystem.requestClientNumber());
        } catch (Exception e) {
            System.out.println(e);
        }

        // Configuring logger
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        try {

            // This block configure the logger with handler and formatter
            FileHandler fh = new FileHandler(id + ".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void handleCommand(String command) {
        String[] userInput = command.split(" ");

        if (userInput[0] == "") {
            logger.warning("No command supplied");
            return;
        }

        Command cmd;
        try {
           cmd = Arrays.stream(Command.values()).filter(c -> c.getName().equals(userInput[0])).findAny().get();
        } catch (NoSuchElementException e) {
            logger.warning("(" + userInput[0] + ") is not a command!");
            return;
        }

        if (cmd.getAvailableTo() == Role.ADMINISTRATOR && getRole() == Role.PARTICIPANT) {
            logger.warning("Command invalid: Only administrators can perform this command (" + cmd + "), you are a participant!");
            return;
         }

        Object[] objectArgs = cmd.convertStringArgsToObjectArgs(Arrays.copyOfRange(userInput, 1, userInput.length));
        if (objectArgs == null)
            return;

        switch (cmd) {
            // Administrator Operations
            case ADD_RESERVATION_SLOT:
                logger.info((reservationSystem.addReservationSlot((String)objectArgs[0], (EventType) objectArgs[1], (Integer) objectArgs[2]) ? "Successfully added " : "Failed to add ") +
                    "a reservation slot for event id (" + ((String)objectArgs[0]) + ") of type (" + ((EventType)objectArgs[1]) + ")");
                break;
            case REMOVE_RESERVATION_SLOT:
                logger.info((reservationSystem.removeReservationSlot((String)objectArgs[0], (EventType) objectArgs[1]) ? "Successfully removed " : "Failed to remove ") +
                        "a reservation slot for event id (" + ((String)objectArgs[0]) + ") of type (" + ((EventType)objectArgs[1]) + ")");
                break;
            case LIST_RESERVATION_SLOTS_AVAILABLE:
                logger.info("Displaying Reservation Slots...\n" +
                reservationSystem.listReservationSlotsAvailable((EventType)objectArgs[0]) + "\n" +
                "Successfully displayed all available reservation slots available");
                break;
            // Participant Operations
            case RESERVE_TICKET:
                 String eventId = (String) objectArgs[1];
                 String foundIdAcronym = eventId.substring(0, 3);
                 boolean reservingInOtherCity = !foundIdAcronym.equals(city.getIdAcronym());

                 String participantIdForReservation = (String) objectArgs[0];
                 if (role == Role.PARTICIPANT && !participantIdForReservation.equals(id)) {
                     logger.severe("As a participant, you cannot book a reservation for someone else!");
                     return;
                 }

                 if (reservingInOtherCity && role == Role.PARTICIPANT && eventsBookedInotherCities.size() >= 3) {
                     int eventsBookedOtherCitiesThisWeek = 0;

                     for (String historicEventId : eventsBookedInotherCities) {
                         if (sameWeek(eventId, historicEventId))
                             eventsBookedOtherCitiesThisWeek++;
                     }

                     if (eventsBookedOtherCitiesThisWeek >= 3) {
                         logger.severe("As a participant, you can reserve a MAX of 3 events in other cities for any given week!");
                         return;
                     }
                 }

                 boolean success = reservationSystem.reserveTicket((String) objectArgs[0], (String)objectArgs[1], (EventType)objectArgs[2]);
                 logger.info((success ? "Successfully made " : "Failed to make ") +
                         "a reservation for you ("+id+") for event "+objectArgs[1]+"!");
                 if (reservingInOtherCity && success)
                     eventsBookedInotherCities.add(eventId);
                break;
            case GET_EVENT_SCHEDULE:
                logger.info("Displaying Event Schedule...\n" +
                reservationSystem.getEventSchedule((String) objectArgs[0]));
                break;
            case CANCEL_TICKET:
                String participantIdForCancellation = (String) objectArgs[0];
                if (role == Role.PARTICIPANT && !participantIdForCancellation.equals(id)) {
                    logger.severe("As a participant, you cannot cancel a reservation for someone else!");
                    return;
                }

                logger.info((reservationSystem.cancelTicket((String) objectArgs[0], (String)objectArgs[1], (EventType) objectArgs[2]) ? "Successfully cancelled a " : "Failed to cancel a ") +
                    "reservation for you ("+id+") for event "+objectArgs[1]+"!");
                break;
            case EXCHANGE_TICKET:
                logger.info((reservationSystem.exchangeTicket((String) objectArgs[0], (String)objectArgs[1], (EventType) objectArgs[2], (String) objectArgs[3], (EventType) objectArgs[4]) ? "Successfully exchanged " : "Failed to exchange ") +
                        " the tickets for you ("+id+") for events "+objectArgs[1]+" " + objectArgs[3] + "!");
                break;
            // General Operations
            case EXIT:
                logger.info("Shutting down user...");
                System.exit(0);
                break;
        }
    }

    private static String generateId(City city, Role role, int clientNum) {
        // generate the id
        StringBuilder idSb = new StringBuilder();
        idSb.append(city.getIdAcronym());
        idSb.append(role.getIdLetter());
        idSb.append(String.format("%04d", clientNum));
        return idSb.toString();
    }

    private static boolean sameWeek(String eventId1, String eventId2) {
        // Year and month equal
        if (eventId1.substring(8).equals(eventId2.substring(8)) && eventId1.substring(6, 8).equals(eventId2.substring(6, 8))) {
            // days in same week
            int weekNumEvent1 = Integer.valueOf(eventId1.substring(4, 6)) / 7;
            int weekNumEvent2 = Integer.valueOf(eventId2.substring(4, 6)) / 7;

            if (weekNumEvent1 == weekNumEvent2)
                return true;
        }

        return false;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ReservationSystem getServer() {
        return reservationSystem;
    }
}
