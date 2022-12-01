package util;

import java.util.HashMap;

import util.IDTRS.EventType;

/*
    Montreal {
        ArtGallery : {
            { eventId: "MTLM010123", guests: ["MTLP5555", "TORP5555"], capacity: 5 },
            { eventId: "MTLA010123", guests: ["MTLP1111", "MTLP2222", "MTLP3333", "MTLP4444", "MTLP5555"], capacity: 0 },
            { eventId: "MTLE010123", guests: [], capacity: 10 }
        },
        Concerts: {
            { eventId: "MTLM020123", guests: ["MTLP2222"], capacity: 80 },
            { eventId: "MTLA020123", guests: ["MTLP4444", "MTLP5555", "VANP5555", "VANP2222"], capacity: 1 },
            { eventId: "MTLE020123", guests: ["VANP5555", "VANP4444"], capacity: 5 }
        },
        Theatre: {
            { eventId: "MTLM030123", guests: ["MTLP2222", "MTLP5555"], capacity: 80 },
            { eventId: "MTLA030123", guests: ["MTLP4444", "MTLP5555", "VANP5555", "VANP2222"], capacity: 1 },
            { eventId: "MTLE030123", guests: ["VANP5555", "VANP2222"], capacity: 5 }
        }
    }

    Toronto {
        ArtGallery : {
            { eventId: "TORM010123", guests: ["MTLP5555", "TORP5555"], capacity: 5 },
            { eventId: "TORA010123", guests: ["TORP1111", "TORP2222", "MTLP3333", "MTLP5555"], capacity: 0 },
            { eventId: "TORE010123", guests: ["VANP2222"], capacity: 10 }
        },
        Concerts: {
            { eventId: "TORM020123", guests: ["MTLP2222"], capacity: 99 },
            { eventId: "TORA020123", guests: ["TORP5555", "MTLP5555", "VANP5555", "VANP2222"], capacity: 1 },
            { eventId: "TORE020123", guests: ["VANP5555", "VANP4444"], capacity: 5 }
        },
        Theatre: {
            { eventId: "TORM030123", guests: ["MTLP2222", "MTLP5555"], capacity: 4 },
            { eventId: "TORA030123", guests: ["MTLP4444", "MTLP5555", "VANP5555", "VANP2222"], capacity: 1 },
            { eventId: "TORE030123", guests: ["VANP5555", "VANP2222"], capacity: 2 }
        }
    }

    Vancouver {
        ArtGallery : {
            { eventId: "VANM010123", guests: ["MTLP5555", "VANP5555"], capacity: 5 },
            { eventId: "VANA010123", guests: ["VANP1111", "VANP2222", "MTLP3333", "MTLP5555"], capacity: 0 },
            { eventId: "VANE010123", guests: ["VANP2222"], capacity: 10 }
        },
        Concerts: {
            { eventId: "VANM020123", guests: ["MTLP2222"], capacity: 99 },
            { eventId: "VANA020123", guests: ["VANP5555", "MTLP5555", "VANP5555", "VANP2222"], capacity: 1 },
            { eventId: "VANE020123", guests: ["VANP5555", "VANP4444"], capacity: 5 }
        },
        Theatre: {
            { eventId: "VANM030123", guests: ["MTLP2222", "MTLP5555"], capacity: 4 },
            { eventId: "VANA030123", guests: ["MTLP4444", "MTLP5555", "VANP5555", "VANP2222"], capacity: 1 },
            { eventId: "VANE030123", guests: ["VANP5555", "VANP2222"], capacity: 2 }
        }
    }
 */

public class ServerDataHelper {
    public static HashMap<EventType, HashMap<String, EventData>> getStartupData(String city) {
        switch (city) {
            case "Montreal":
                System.out.println("Montreal DATA");
                return MTL_DATA;
            case "Toronto":
                System.out.println("Toronto DATA");
                return TOR_DATA;
            case "Vancouver":
                System.out.println("Vancouver DATA");
                return VAN_DATA;
            default:
                System.out.println("DEFAULT GETSTARTUPDATA");
                return MTL_DATA;
        }
    }

    public static HashMap<EventType, HashMap<String, EventData>> MTL_DATA = new HashMap<EventType, HashMap<String, EventData>>() {
        {
            put(EventType.ArtGallery, new HashMap<String, EventData>() {
                {
                    put("MTLM010123", new EventData(5, new String[] { "MTLP5555", "TORP5555" }));
                    put("MTLA010123", new EventData(0,
                            new String[] { "MTLP1111", "MTLP2222", "MTLP3333", "MTLP4444", "MTLP5555" }));
                    put("MTLE010122", new EventData(10));
                }
            });
            put(EventType.Concerts, new HashMap<String, EventData>() {
                {
                    put("MTLM020123", new EventData(80, new String[] { "MTLP2222" }));
                    put("MTLA020123", new EventData(1, new String[] { "MTLP4444", "MTLP5555", "VANP5555", "VANP2222" }));
                    put("MTLE020123", new EventData(5, new String[] { "VANP5555", "VANP4444" }));
                }
            });
            put(EventType.Theatre, new HashMap<String, EventData>() {
                {
                    put("MTLM030123", new EventData(80, new String[] { "MTLP2222", "MTLP5555" }));
                    put("MTLA030123", new EventData(1, new String[] { "MTLP4444", "MTLP5555", "VANP5555", "VANP2222" }));
                    put("MTLE030123", new EventData(5, new String[] { "VANP5555", "VANP2222" }));
                }
            });
        }
    };

    public static HashMap<EventType, HashMap<String, EventData>> TOR_DATA = new HashMap<EventType, HashMap<String, EventData>>() {
        {
            put(EventType.ArtGallery, new HashMap<String, EventData>() {
                {
                    put("TORM010123", new EventData(5, new String[] { "MTLP5555", "TORP5555" }));
                    put("TORA010123", new EventData(0,
                            new String[] { "TORP1111", "TORP2222", "MTLP3333", "MTLP5555" }));
                    put("TORE010123", new EventData(10, new String[] { "VANP2222" }));
                }
            });
            put(EventType.Concerts, new HashMap<String, EventData>() {
                {
                    put("TORM020123", new EventData(99, new String[] { "MTLP2222" }));
                    put("TORA020123", new EventData(1, new String[] { "TORP5555", "MTLP5555", "VANP5555", "VANP2222" }));
                    put("TORE020123", new EventData(5, new String[] { "VANP5555", "VANP4444" }));
                }
            });
            put(EventType.Theatre, new HashMap<String, EventData>() {
                {
                    put("TORM030123", new EventData(4, new String[] { "MTLP2222", "MTLP5555" }));
                    put("TORA030123", new EventData(1, new String[] { "MTLP4444", "MTLP5555", "VANP5555", "VANP2222" }));
                    put("TORE030123", new EventData(2, new String[] { "VANP5555", "VANP2222" }));
                }
            });
        }
    };

    public static HashMap<EventType, HashMap<String, EventData>> VAN_DATA = new HashMap<EventType, HashMap<String, EventData>>() {
        {
            put(EventType.ArtGallery, new HashMap<String, EventData>() {
                {
                    put("VANM010123", new EventData(5, new String[] { "MTLP5555", "VANP5555" }));
                    put("VANA010123", new EventData(0,
                            new String[] { "VANP1111", "VANP2222", "MTLP3333", "MTLP5555" }));
                    put("VANE010123", new EventData(10, new String[] { "VANP2222" }));
                }
            });
            put(EventType.Concerts, new HashMap<String, EventData>() {
                {
                    put("VANM020123", new EventData(99, new String[] { "MTLP2222" }));
                    put("VANA020123", new EventData(1, new String[] { "VANP5555", "MTLP5555", "VANP5555", "VANP2222" }));
                    put("VANE020123", new EventData(5, new String[] { "VANP5555", "VANP4444" }));
                }
            });
            put(EventType.Theatre, new HashMap<String, EventData>() {
                {
                    put("VANM030123", new EventData(4, new String[] { "MTLP2222", "MTLP5555" }));
                    put("VANA030123", new EventData(1, new String[] { "MTLP4444", "MTLP5555", "VANP5555", "VANP2222" }));
                    put("VANE030123", new EventData(2, new String[] { "VANP5555", "VANP2222" }));
                }
            });
        }
    };
}