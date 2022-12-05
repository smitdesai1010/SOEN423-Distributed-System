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

        // Art Gallery Events
        Event artGalleryEvent1 = new Event(City.MONTREAL, TimeSlot.MORNING, new GregorianCalendar(2023, 01, 01));
        artGalleryEvent1.addReservationSlot(7);
        artGalleryEvent1.makeReservation("MTLP5555");
        artGalleryEvent1.makeReservation("TORP5555");
        eventMap.get(EventType.ART_GALLERY).put(artGalleryEvent1.getId(), artGalleryEvent1);

        Event artGalleryEvent2 = new Event(City.MONTREAL, TimeSlot.AFTERNOON, new GregorianCalendar(2023, 01, 01));
        artGalleryEvent2.addReservationSlot(5);
        artGalleryEvent2.makeReservation("MTLP1111");
        artGalleryEvent2.makeReservation("TORP2222");
        artGalleryEvent2.makeReservation("TORP3333");
        artGalleryEvent2.makeReservation("TORP4444");
        artGalleryEvent2.makeReservation("TORP5555");
        eventMap.get(EventType.ART_GALLERY).put(artGalleryEvent2.getId(), artGalleryEvent2);

        Event artGalleryEvent3 = new Event(City.MONTREAL, TimeSlot.EVENING, new GregorianCalendar(2023, 01, 01));
        artGalleryEvent3.addReservationSlot(10);
        eventMap.get(EventType.ART_GALLERY).put(artGalleryEvent3.getId(), artGalleryEvent3);

        // SPECIAL JUST FOR ME EVENT
        Event artGalleryEvent4 = new Event(City.MONTREAL, TimeSlot.EVENING, new GregorianCalendar(2023, 02, 13));
        System.out.println(artGalleryEvent4.getId());
        eventMap.get(EventType.ART_GALLERY).put(artGalleryEvent4.getId(), artGalleryEvent4);

        // Concert Events
        Event concertEvent1 = new Event(City.MONTREAL, TimeSlot.MORNING, new GregorianCalendar(2023, 01, 02));
        concertEvent1.addReservationSlot(81);
        concertEvent1.makeReservation("MTLP2222");
        eventMap.get(EventType.CONCERT).put(concertEvent1.getId(), concertEvent1);

        Event concertEvent2 = new Event(City.MONTREAL, TimeSlot.AFTERNOON, new GregorianCalendar(2023, 01, 02));
        concertEvent2.addReservationSlot(5);
        concertEvent2.makeReservation("MTLP4444");
        concertEvent2.makeReservation("MTLP5555");
        concertEvent2.makeReservation("VANP5555");
        concertEvent2.makeReservation("VANP2222");
        eventMap.get(EventType.CONCERT).put(concertEvent2.getId(), concertEvent2);

        Event concertEvent3 = new Event(City.MONTREAL, TimeSlot.EVENING, new GregorianCalendar(2023, 01, 02));
        concertEvent3.addReservationSlot(7);
        concertEvent3.makeReservation("VANP4444");
        concertEvent3.makeReservation("VANP5555");
        eventMap.get(EventType.CONCERT).put(concertEvent3.getId(), concertEvent3);

        // Theatre Events
        Event theatreEvent1 = new Event(City.MONTREAL, TimeSlot.MORNING, new GregorianCalendar(2023, 01, 03));
        theatreEvent1.addReservationSlot(82);
        theatreEvent1.makeReservation("MTLP2222");
        theatreEvent1.makeReservation("MTLP5555");
        eventMap.get(EventType.THEATRE).put(theatreEvent1.getId(), theatreEvent1);

        Event theatreEvent2 = new Event(City.MONTREAL, TimeSlot.AFTERNOON, new GregorianCalendar(2023, 01, 03));
        theatreEvent2.addReservationSlot(5);
        theatreEvent2.makeReservation("MTLP4444");
        theatreEvent2.makeReservation("MTLP5555");
        theatreEvent2.makeReservation("VANP5555");
        theatreEvent2.makeReservation("VANP2222");
        eventMap.get(EventType.THEATRE).put(theatreEvent2.getId(), theatreEvent2);

        Event theatreEvent3 = new Event(City.MONTREAL, TimeSlot.EVENING, new GregorianCalendar(2023, 01, 03));
        theatreEvent3.addReservationSlot(7);
        theatreEvent3.makeReservation("VANP5555");
        theatreEvent3.makeReservation("VANP2222");
        eventMap.get(EventType.THEATRE).put(theatreEvent3.getId(), theatreEvent3);

        return eventMap;
    }

    private static HashMap<EventType, HashMap<String, Event>> createCityReservationSystemToronto() {
        HashMap<EventType, HashMap<String, Event>> eventMap = generateBaseEventMap();

        // Art Gallery Events
        Event artGalleryEvent1 = new Event(City.TORONTO, TimeSlot.MORNING, new GregorianCalendar(2023, 01, 01));
        artGalleryEvent1.addReservationSlot(7);
        artGalleryEvent1.makeReservation("MTLP5555");
        artGalleryEvent1.makeReservation("TORP5555");
        eventMap.get(EventType.ART_GALLERY).put(artGalleryEvent1.getId(), artGalleryEvent1);

        Event artGalleryEvent2 = new Event(City.TORONTO, TimeSlot.AFTERNOON, new GregorianCalendar(2023, 01, 01));
        artGalleryEvent2.addReservationSlot(4);
        artGalleryEvent2.makeReservation("TORP1111");
        artGalleryEvent2.makeReservation("TORP2222");
        artGalleryEvent2.makeReservation("MTLP3333");
        artGalleryEvent2.makeReservation("MTLP5555");
        eventMap.get(EventType.ART_GALLERY).put(artGalleryEvent2.getId(), artGalleryEvent2);

        Event artGalleryEvent3 = new Event(City.TORONTO, TimeSlot.EVENING, new GregorianCalendar(2023, 01, 01));
        artGalleryEvent3.addReservationSlot(11);
        artGalleryEvent3.makeReservation("VANP2222");
        eventMap.get(EventType.ART_GALLERY).put(artGalleryEvent3.getId(), artGalleryEvent3);

        // Concert Events
        Event concertEvent1 = new Event(City.TORONTO, TimeSlot.MORNING, new GregorianCalendar(2023, 01, 02));
        concertEvent1.addReservationSlot(100);
        concertEvent1.makeReservation("MTLP2222");
        eventMap.get(EventType.CONCERT).put(concertEvent1.getId(), concertEvent1);

        Event concertEvent2 = new Event(City.TORONTO, TimeSlot.AFTERNOON, new GregorianCalendar(2023, 01, 02));
        concertEvent2.addReservationSlot(5);
        concertEvent2.makeReservation("TORP5555");
        concertEvent2.makeReservation("MTLP5555");
        concertEvent2.makeReservation("VANP5555");
        concertEvent2.makeReservation("VANP2222");
        eventMap.get(EventType.CONCERT).put(concertEvent2.getId(), concertEvent2);

        Event concertEvent3 = new Event(City.TORONTO, TimeSlot.EVENING, new GregorianCalendar(2023, 01, 02));
        concertEvent3.addReservationSlot(7);
        concertEvent3.makeReservation("VANP4444");
        concertEvent3.makeReservation("VANP5555");
        eventMap.get(EventType.CONCERT).put(concertEvent3.getId(), concertEvent3);

        // Theatre Events
        Event theatreEvent1 = new Event(City.TORONTO, TimeSlot.MORNING, new GregorianCalendar(2023, 01, 03));
        theatreEvent1.addReservationSlot(6);
        theatreEvent1.makeReservation("MTLP2222");
        theatreEvent1.makeReservation("MTLP5555");
        eventMap.get(EventType.THEATRE).put(theatreEvent1.getId(), theatreEvent1);

        Event theatreEvent2 = new Event(City.TORONTO, TimeSlot.AFTERNOON, new GregorianCalendar(2023, 01, 03));
        theatreEvent2.addReservationSlot(5);
        theatreEvent2.makeReservation("MTLP4444");
        theatreEvent2.makeReservation("MTLP5555");
        theatreEvent2.makeReservation("VANP5555");
        theatreEvent2.makeReservation("VANP2222");
        eventMap.get(EventType.THEATRE).put(theatreEvent2.getId(), theatreEvent2);

        Event theatreEvent3 = new Event(City.TORONTO, TimeSlot.EVENING, new GregorianCalendar(2023, 01, 03));
        theatreEvent3.addReservationSlot(4);
        theatreEvent3.makeReservation("VANP5555");
        theatreEvent3.makeReservation("VANP2222");
        eventMap.get(EventType.THEATRE).put(theatreEvent3.getId(), theatreEvent3);

        return eventMap;
    }

    private static HashMap<EventType, HashMap<String, Event>> createCityReservationSystemVancouver() {
        HashMap<EventType, HashMap<String, Event>> eventMap = generateBaseEventMap();

        // Art Gallery Events
        Event artGalleryEvent1 = new Event(City.VANCOUVER, TimeSlot.MORNING, new GregorianCalendar(2023, 01, 01));
        artGalleryEvent1.addReservationSlot(7);
        artGalleryEvent1.makeReservation("MTLP5555");
        artGalleryEvent1.makeReservation("VANP5555");
        eventMap.get(EventType.ART_GALLERY).put(artGalleryEvent1.getId(), artGalleryEvent1);

        Event artGalleryEvent2 = new Event(City.VANCOUVER, TimeSlot.AFTERNOON, new GregorianCalendar(2023, 01, 01));
        artGalleryEvent2.addReservationSlot(4);
        artGalleryEvent2.makeReservation("VANP1111");
        artGalleryEvent2.makeReservation("VANP2222");
        artGalleryEvent2.makeReservation("MTLP3333");
        artGalleryEvent2.makeReservation("MTLP5555");
        eventMap.get(EventType.ART_GALLERY).put(artGalleryEvent2.getId(), artGalleryEvent2);

        Event artGalleryEvent3 = new Event(City.VANCOUVER, TimeSlot.EVENING, new GregorianCalendar(2023, 01, 01));
        artGalleryEvent3.addReservationSlot(11);
        artGalleryEvent3.makeReservation("VANP2222");
        eventMap.get(EventType.ART_GALLERY).put(artGalleryEvent3.getId(), artGalleryEvent3);

        // Concert Events
        Event concertEvent1 = new Event(City.VANCOUVER, TimeSlot.MORNING, new GregorianCalendar(2023, 01, 02));
        concertEvent1.addReservationSlot(100);
        concertEvent1.makeReservation("MTLP2222");
        eventMap.get(EventType.CONCERT).put(concertEvent1.getId(), concertEvent1);

        Event concertEvent2 = new Event(City.VANCOUVER, TimeSlot.AFTERNOON, new GregorianCalendar(2023, 01, 02));
        concertEvent2.addReservationSlot(5);
        concertEvent2.makeReservation("TORP5555");
        concertEvent2.makeReservation("MTLP5555");
        concertEvent2.makeReservation("VANP5555");
        concertEvent2.makeReservation("VANP2222");
        eventMap.get(EventType.CONCERT).put(concertEvent2.getId(), concertEvent2);

        Event concertEvent3 = new Event(City.VANCOUVER, TimeSlot.EVENING, new GregorianCalendar(2023, 01, 02));
        concertEvent3.addReservationSlot(7);
        concertEvent3.makeReservation("VANP4444");
        concertEvent3.makeReservation("VANP5555");
        eventMap.get(EventType.CONCERT).put(concertEvent3.getId(), concertEvent3);

        // Theatre Events
        Event theatreEvent1 = new Event(City.VANCOUVER, TimeSlot.MORNING, new GregorianCalendar(2023, 01, 03));
        theatreEvent1.addReservationSlot(6);
        theatreEvent1.makeReservation("MTLP2222");
        theatreEvent1.makeReservation("MTLP5555");
        eventMap.get(EventType.THEATRE).put(theatreEvent1.getId(), theatreEvent1);

        Event theatreEvent2 = new Event(City.VANCOUVER, TimeSlot.AFTERNOON, new GregorianCalendar(2023, 01, 03));
        theatreEvent2.addReservationSlot(5);
        theatreEvent2.makeReservation("MTLP4444");
        theatreEvent2.makeReservation("MTLP5555");
        theatreEvent2.makeReservation("VANP5555");
        theatreEvent2.makeReservation("VANP2222");
        eventMap.get(EventType.THEATRE).put(theatreEvent2.getId(), theatreEvent2);

        Event theatreEvent3 = new Event(City.VANCOUVER, TimeSlot.EVENING, new GregorianCalendar(2023, 01, 03));
        theatreEvent3.addReservationSlot(4);
        theatreEvent3.makeReservation("VANP5555");
        theatreEvent3.makeReservation("VANP2222");
        eventMap.get(EventType.THEATRE).put(theatreEvent3.getId(), theatreEvent3);

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
