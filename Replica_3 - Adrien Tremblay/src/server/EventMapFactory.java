package server;

import general.City;
import general.Event;
import general.EventType;
import general.TimeSlot;

import java.util.GregorianCalendar;
import java.util.HashMap;

public class EventMapFactory {
    public static HashMap<EventType, HashMap<String, Event>> createEventMap(City city) {
       switch (city) {
           case MONTREAL:
              return createCityReservationSystemMontreal();
           case TORONTO:
               return createCityReservationSystemToronto();
           case VANCOUVER:
           default:
               return createCityReservationSystemVancouver();
       }
    }

    private static HashMap<EventType, HashMap<String, Event>> createCityReservationSystemMontreal() {
        HashMap<EventType, HashMap<String, Event>> eventMap = generateBaseEventMap();

        Event eventNoSlot = new Event(City.MONTREAL, TimeSlot.MORNING, new GregorianCalendar(2022, 10, 01));
        eventMap.get(EventType.ART_GALLERY).put(eventNoSlot.getId(), eventNoSlot);

        Event eventWithSlot = new Event(City.MONTREAL, TimeSlot.AFTERNOON, new GregorianCalendar(2022, 10, 11));
        eventWithSlot.addReservationSlot(10);
        eventMap.get(EventType.ART_GALLERY).put(eventWithSlot.getId(), eventWithSlot);

        Event eventWithSlotCapacityOne = new Event(City.MONTREAL, TimeSlot.EVENING, new GregorianCalendar(2022, 10, 01));
        eventWithSlotCapacityOne.addReservationSlot(1);
        eventMap.get(EventType.ART_GALLERY).put(eventWithSlotCapacityOne.getId(), eventWithSlotCapacityOne);

        Event eventWithReservation = new Event(City.MONTREAL, TimeSlot.MORNING, new GregorianCalendar(2022, 11, 11));
        eventWithReservation.addReservationSlot(5);
        eventWithReservation.getReservationSlot().makeReservation("MTLP0000");
        eventWithReservation.getReservationSlot().makeReservation("MTLP6969");
        eventMap.get(EventType.ART_GALLERY).put(eventWithReservation.getId(), eventWithReservation);

        return eventMap;
    }

    private static HashMap<EventType, HashMap<String, Event>> createCityReservationSystemToronto() {
        HashMap<EventType, HashMap<String, Event>> eventMap = generateBaseEventMap();

        Event eventWithSlot1 = new Event(City.TORONTO, TimeSlot.AFTERNOON, new GregorianCalendar(2022, 10, 01));
        eventWithSlot1.addReservationSlot(10);
        eventMap.get(EventType.ART_GALLERY).put(eventWithSlot1.getId(), eventWithSlot1);

        Event eventWithSlot2 = new Event(City.TORONTO, TimeSlot.AFTERNOON, new GregorianCalendar(2022, 10, 02));
        eventWithSlot2.addReservationSlot(10);
        eventMap.get(EventType.ART_GALLERY).put(eventWithSlot2.getId(), eventWithSlot2);

        Event eventWithSlot3 = new Event(City.TORONTO, TimeSlot.AFTERNOON, new GregorianCalendar(2022, 10, 03));
        eventWithSlot3.addReservationSlot(10);
        eventMap.get(EventType.ART_GALLERY).put(eventWithSlot3.getId(), eventWithSlot3);

        Event eventWithSlot4 = new Event(City.TORONTO, TimeSlot.AFTERNOON, new GregorianCalendar(2022, 10, 04));
        eventWithSlot4.addReservationSlot(10);
        eventMap.get(EventType.ART_GALLERY).put(eventWithSlot4.getId(), eventWithSlot4);

        Event eventWithReservation = new Event(City.TORONTO, TimeSlot.MORNING, new GregorianCalendar(2022, 11, 11));
        eventWithReservation.addReservationSlot(5);
        eventWithReservation.getReservationSlot().makeReservation("MTLP0000");
        eventMap.get(EventType.ART_GALLERY).put(eventWithReservation.getId(), eventWithReservation);

        return eventMap;
    }

    private static HashMap<EventType, HashMap<String, Event>> createCityReservationSystemVancouver() {
        HashMap<EventType, HashMap<String, Event>> eventMap = generateBaseEventMap();
        return  eventMap;
    }

    private static HashMap<EventType, HashMap<String, Event>> generateBaseEventMap() {
        HashMap<EventType, HashMap<String, Event>> eventMap = new HashMap<EventType, HashMap<String, Event>>();

        eventMap.put(EventType.ART_GALLERY, new HashMap<String, Event>());
        eventMap.put(EventType.CONCERT, new HashMap<String, Event>());
        eventMap.put(EventType.THEATRE, new HashMap<String, Event>());

        return eventMap;
    }

}
