package FrontEnd;

import org.json.simple.JSONObject;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ServerInterface {
//    String addReservationSlot(String adminID, String eventType, String eventID, int capacity);
//    String removeReservationSlot(String adminID, String eventType, String eventID);
//    String listReservationSlotAvailable(String adminID, String eventType);
//
//    String reserveTicket(String participantID, String eventType, String eventID);
//    String getEventSchedule(String participantID);
//    String cancelTicket(String participantID, String eventType, String eventID);
//    String exchangeTicket(String participantID, String eventType, String eventID, String new_eventType, String new_eventID);

    String executeRequest(JSONObject obj);
}


