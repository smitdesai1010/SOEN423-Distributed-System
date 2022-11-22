package general;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum Command {
    // Administrator Commands
    ADD_RESERVATION_SLOT("addReservationSlot", Role.ADMINISTRATOR, new Class<?>[] {String.class, EventType.class, Integer.class}),
    REMOVE_RESERVATION_SLOT("removeReservationSlot", Role.ADMINISTRATOR, new Class<?>[] {String.class, EventType.class}),
    LIST_RESERVATION_SLOTS_AVAILABLE("listReservationSlotsAvailable", Role.ADMINISTRATOR, new Class<?>[] {EventType.class}),
    // Participant Commands
    RESERVE_TICKET("reserveTicket", Role.PARTICIPANT, new Class<?>[] {String.class, String.class, EventType.class}),
    GET_EVENT_SCHEDULE("getEventSchedule", Role.PARTICIPANT, new Class<?>[] {String.class}),
    CANCEL_TICKET("cancelTicket", Role.PARTICIPANT, new Class<?>[] {String.class, String.class, EventType.class}),
    EXCHANGE_TICKET("exchangeTicket", Role.PARTICIPANT, new Class<?>[] {String.class, String.class, EventType.class, String.class, EventType.class}),
    // General Commands
    EXIT("exit", Role.PARTICIPANT, new Class<?>[] {}),
    // UDP Only Commands
    CHECK_FOR_RESERVATION(),
    CHECK_FOR_SPACE();

    private final String name;
    private final Role availableTo;
    private Class<?>[] inputTypes;

    private Command() {
        name = "";
        availableTo = null;
    }

    private Command(String name, Role availableTo, Class<?>[] inputTypes) {
        this.name = name;
        this.availableTo = availableTo;
        this.inputTypes = inputTypes;
    }

    public Object[] convertStringArgsToObjectArgs(String[] args) {
        if (args.length != inputTypes.length) {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning("Please provide the correct number of arguments for this command!");
            return null;
        }

        Object[] objectArgs = new Object[args.length];

        for (int i = 0 ; i < args.length ; i++) {
            if (inputTypes[i] == String.class) {
                objectArgs[i] = args[i];
            } else if (inputTypes[i] == EventType.class) {
                try {
                    EventType eventType = EventType.valueOf(args[i]);
                    objectArgs[i] = eventType;
                } catch (Exception e) {
                    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, "Cannot convert (" + args[i] + ") Into an EventType!");
                    return null;
                }
            } else if (inputTypes[i] == Integer.class) {
                try {
                    int integer = Integer.valueOf(args[i]);
                    objectArgs[i] = integer;
                } catch (Exception e) {
                    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, "Cannot convert (" + args[i] + ") Into an Integer!");
                    return null;
                }
            }
        }

        return  objectArgs;
    }

    public String getName() {
        return name;
    }

    public Role getAvailableTo() {
        return availableTo;
    }

    public Class<?>[] getInputTypes() {
        return inputTypes;
    }
}
