package Servers;

import org.json.simple.JSONObject;


public interface ServiceInterface {

    // for Administrators only
    JSONObject addReservationSlot(String eventID, String eventType, int capacity);

    JSONObject removeReservationSlot(String eventID, String eventType);

    JSONObject listReservationSlotAvailable(String eventType);

    // for both Participants and Administers
    JSONObject reserveTicket(String participantID, String eventID, String eventType);

    JSONObject getEventSchedule(String ParticipantID);

    JSONObject cancelTicket(String participantID, String eventID);

    JSONObject exchangeTicket(String participant, String eventID, String newEventID, String newEventType);

    boolean verifyUser(String userID, boolean isAdmin);

}
