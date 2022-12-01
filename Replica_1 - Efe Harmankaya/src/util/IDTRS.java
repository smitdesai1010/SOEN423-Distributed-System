package util;

import org.json.simple.JSONObject;

public interface IDTRS {
    public enum EventType {
        // TODO fix spelling from README
        ArtGallery,
        Theatre,
        Concerts,
        None;

        public static String toString(EventType eventType) {
            if (eventType.equals(EventType.ArtGallery)) {
                return "ArtGallery";
            } else if (eventType.equals(EventType.Theatre)) {
                return "Theatre";
            } else if (eventType.equals(EventType.Concerts)) {
                return "Concerts";
            } else {
                return "None";
            }
        }

    };

    public enum ServerAction {
        // admin
        add,
        remove,
        list,

        // regular
        reserve,
        get,
        cancel,
        exchange;
    }

    public enum ServerPort {
        MTL(2111),
        TOR(2112),
        VAN(2113),
        NONE(-1);

        public final int PORT;

        private ServerPort(int PORT) {
            this.PORT = PORT;
        }

        public boolean validate() {
            if (this.PORT == ServerPort.NONE.PORT)
                return false;
            return true;
        }
    }

    Object city = null;

    // Admin
    public JSONObject addReservationSlot(String eventId, EventType eventType, int capacity);

    public JSONObject removeReservationSlot(String eventId, EventType eventType);

    public JSONObject listReservationSlotsAvailable(EventType eventType);

    // Regular
    public JSONObject reserveTicket(String participantId, String eventId, EventType eventType);

    public JSONObject cancelTicket(String participantId, String eventId, EventType eventType);

    public JSONObject getEventSchedule(String participantId);

    public JSONObject exchangeTicket(String participantId, String eventId, EventType eventType, String newEventId,
            EventType newEventType);
}
