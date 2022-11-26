public class jsonFieldNames {
    // todo: would this be better off as a hashmap or enum?

    // request
    public static final String METHOD_NAME = "MethodName";
    public static final String ADMIN_ID = "adminID"; // unused by me
    public static final String EVENT_TYPE = "eventType";
    public static final String EVENT_ID = "eventID";
    public static final String PARTICIPANT_ID = "participantID";
    public static final String NEW_EVENT_TYPE = "new_eventType";
    public static final String NEW_EVENT_ID = "new_eventID";
    public static final String CAPACTIY = "capacity";
    public static final String SEQUENCE_NUMBER = "sequenceNumber";
    public static final String FRONTEND_IP = "frontend-IP";
    public static final String FRONTEND_PORT = "port";
    public static final String REPLICAMANAGER_IP = "IP";

    // response
    public static final String SUCCESS = "Success";
    public static final String DATA = "Data";

    // method names
    public static final String ADD_RESERVATION_SLOT = "addReservationSlot";
    public static final String REMOVE_RESERVATION_SLOT = "removeReservationSlot";
    public static final String LIST_RESERVATION_SLOT_AVAILABLE = "listReservationSlotAvailable";
    public static final String RESERVE_TICKET = "reserveTicket";
    public static final String GET_EVENT_SCHEDULE = "getEventSchedule";
    public static final String CANCEL_TICKET = "cancelTicket";
    public static final String EXCHANGE_TICKET = "exchangeTicket";
}
