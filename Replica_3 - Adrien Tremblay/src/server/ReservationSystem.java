package server;

import general.EventType;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.rmi.RemoteException;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ReservationSystem {
    public int requestClientNumber() throws RemoteException;

    // Administrator Methods
    public boolean addReservationSlot(String eventId, EventType eventType, int capacity);
    public boolean removeReservationSlot(String eventId, EventType eventType);
    public String listReservationSlotsAvailable(EventType eventType);
    // Participant Methods
    public boolean reserveTicket(String participantId, String eventId, EventType eventType);
    public boolean cancelTicket(String participantId, String eventId, EventType eventType);
    public String getEventSchedule(String participantId);
    public boolean exchangeTicket(String participantId, String eventId, EventType eventType, String newEventId, EventType newEventType);
}
