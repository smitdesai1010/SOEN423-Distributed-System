package general;

import java.io.Serializable;
import java.util.Calendar;
import java.util.logging.Logger;

public class Event implements Serializable {
    private String id;
    private City city;
    private TimeSlot timeSlot;
    private Calendar date;
    private ReservationSlot reservationSlot;
    private Logger logger;

    public Event(City city, TimeSlot timeSlot, Calendar date) {
        this.city = city;
        this.timeSlot = timeSlot;
        this.date = date;
        this.id = generateId(city, timeSlot, date);
        reservationSlot = null;
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public boolean addReservationSlot(int capacity) {
        if (reservationSlot != null)  {
            logger.severe("Cannot add a reservation slot as there is already one!");
            return false;
        }

        reservationSlot = new ReservationSlot(capacity);
        return true;
    }

    public boolean removeReservationSlot() {
        if (reservationSlot == null) {
            logger.severe("Tried to remove a reservation slot for event (" + id + ") but there is none!");
            return false;
        }

        reservationSlot = null;
        return true;
    }

    public boolean makeReservation(String participantId) {
        if (reservationSlot == null) {
            logger.severe("Cannot make a reservation for event (" + id  + ") as there is no reservation slot!");
            return false;
        }

        return reservationSlot.makeReservation(participantId);
    }

    public boolean cancelReservation(String participantId) {
        if (reservationSlot == null) {
            logger.severe("Cannot cancel a reservation for event (" + id  + ") as there is no reservation slot!");
            return false;
        }

        return reservationSlot.cancelReservation(participantId);
    }

    public String getId() {
        return id;
    }

    public City getCity() {
        return city;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Calendar getDate() {
        return date;
    }

    private String generateId(City city, TimeSlot timeSlot, Calendar date) {
        StringBuilder idSb = new StringBuilder();
        idSb.append(city.getIdAcronym());
        idSb.append(timeSlot.getIdLetter());
        idSb.append(String.format("%02d%02d%02d", date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH), date.get(Calendar.YEAR) % 100));
        return idSb.toString();
    }

    public ReservationSlot getReservationSlot() {
        return reservationSlot;
    }
}
