package util;

import java.util.HashMap;

import util.IDTRS.EventType;

public class ServerDataHelper {
    public static HashMap<EventType, HashMap<String, EventData>> getStartupData(String city) {
        switch (city) {
            case "Montreal":
                return MTL_DATA;
            case "Toronto":
                return TOR_DATA;
            case "Vancouver":
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
                    put("MTLM010122", new EventData(5, new String[] { "MTLP5555", "TORP5555" }));
                    put("MTLA010122", new EventData(0,
                            new String[] { "MTLP1111", "MTLP2222", "MTLP3333", "MTLP4444", "MTLP5555" }));
                    put("MTLE010122", new EventData(15));
                }
            });
            put(EventType.Concerts, new HashMap<String, EventData>() {
                {
                    put("MTLM020122", new EventData(5, new String[] { "MTLP5555", "TORP5555" }));
                    put("MTLA020122", new EventData(3, new String[] { "MTLP2222" }));
                    put("MTLE020122", new EventData(15, new String[] { "TORP5555" }));
                }
            });
            put(EventType.Theatre, new HashMap<String, EventData>() {
                {
                    put("MTLM030122", new EventData(5, new String[] { "MTLP5555" }));
                    put("MTLA030122", new EventData(3, new String[] { "VANP5555", "VANP2222" }));
                    put("MTLE030122", new EventData(15, new String[] { "MTLP2222" }));
                }
            });
        }
    };

    public static HashMap<EventType, HashMap<String, EventData>> TOR_DATA = new HashMap<EventType, HashMap<String, EventData>>() {
        {
            put(EventType.ArtGallery, new HashMap<String, EventData>() {
                {
                    put("TORM010122", new EventData(5, new String[] { "MTLP5555", "TORP5555" }));
                    put("TORA010122", new EventData(0,
                            new String[] { "MTLP1111", "MTLP2222", "MTLP3333", "MTLP4444", "MTLP5555" }));
                    put("TORE010122", new EventData(15));
                }
            });
            put(EventType.Concerts, new HashMap<String, EventData>() {
                {
                    put("TORM020122", new EventData(5, new String[] { "MTLP5555", "TORP5555" }));
                    put("TORA020122", new EventData(3, new String[] { "MTLP2222" }));
                    put("TORE020122", new EventData(15, new String[] { "TORP5555" }));
                }
            });
            put(EventType.Theatre, new HashMap<String, EventData>() {
                {
                    put("TORM030122", new EventData(5, new String[] { "MTLP5555" }));
                    put("TORA030122", new EventData(3, new String[] { "VANP5555", "VANP2222" }));
                    put("TORE030122", new EventData(15, new String[] { "MTLP2222" }));
                }
            });
        }
    };

    public static HashMap<EventType, HashMap<String, EventData>> VAN_DATA = new HashMap<EventType, HashMap<String, EventData>>() {
        {
            put(EventType.ArtGallery, new HashMap<String, EventData>() {
                {
                    put("VANM010122", new EventData(5, new String[] { "MTLP5555", "TORP5555" }));
                    put("VANA010122", new EventData(0,
                            new String[] { "MTLP1111", "MTLP2222", "MTLP3333", "MTLP4444", "MTLP5555" }));
                    put("VANE010122", new EventData(15));
                }
            });
            put(EventType.Concerts, new HashMap<String, EventData>() {
                {
                    put("VANM020122", new EventData(5, new String[] { "MTLP5555", "TORP5555" }));
                    put("VANA020122", new EventData(3, new String[] { "MTLP2222" }));
                    put("VANE020122", new EventData(15, new String[] { "TORP5555" }));
                }
            });
            put(EventType.Theatre, new HashMap<String, EventData>() {
                {
                    put("VANM030122", new EventData(5, new String[] { "MTLP5555" }));
                    put("VANA030122", new EventData(3, new String[] { "VANP5555", "VANP2222" }));
                    put("VANE030122", new EventData(15, new String[] { "MTLP2222" }));
                }
            });
        }
    };
}