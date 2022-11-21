package general;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ReservationSlot {
    private ArrayList<String> reservations;
    private int capacity;
    private Logger logger;

    public ReservationSlot( int capacity) {
        reservations = new ArrayList<>();
        this.capacity = capacity;
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public ArrayList<String> getReservations() {
        return reservations;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean makeReservation(String participantId) {
        if (isFull()) {
            logger.severe("Reservation is already at max capacity!");
            return false;
        }

        // This case is unreachable currently but leaving it in for safety
        if (reservations.contains(participantId)) {
            logger.severe("This participant already has a reservation!");
            return false;
        }

        return reservations.add(participantId);
    }

    public boolean cancelReservation(String participantId) {
        if (!reservations.contains(participantId)) {
            logger.severe("Cannot Cancel reservation for event . Participant " + participantId + " does not have a reservation!");
            return false;
        }

        return reservations.remove(participantId);
    }

    public boolean isFull() {
        return capacity == getReservations().size();
    }
}
