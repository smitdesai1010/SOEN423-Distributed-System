package Servers;

import java.util.*;


public class BackendServerTOR extends BackendServer {
	
	private static List<String> _participantsList  = Arrays.asList("TORP1234", "TORP4321", "TORP9083", "TORP2340");
	private static List<String>  _adminList        = Arrays.asList("TORA1234", "TORA4321", "TORA9083", "TORA2340");
	
	public BackendServerTOR() {
		super("TOR", _participantsList, _adminList);
		loadEvents();
	}
	
	public synchronized void loadEvents() {
		_events = new HashMap<>();
		_participantBookings = new HashMap<>();
		_events.put("ArtGallary", new HashMap<>());
		_events.put("Concerts", new HashMap<>());
		_events.put("Theatre", new HashMap<>());
		_events.get("ArtGallary").put("TORM061022", "99:VANP1234");
		updateParticipantBookings("VANP1234", "ArtGallary", "TORM061022");
		//updateParticipantBookings("MTLP1234", "ArtGallary", "TORM101010");
		_events.get("ArtGallary").put("TORA102938", "100");
		_events.get("Concerts").put("TORM101010", "100:TORP1276");
		updateParticipantBookings("TORP1276", "Concerts", "TORM101010");
		//updateParticipantBookings("MTLP1234", "Concerts", "TORM101010");
		_events.get("Concerts").put("TORA102938", "100");
		_events.get("Theatre").put("TORM091022", "100");
		//updateParticipantBookings("MTLP1234", "Theatre", "TORM091022");
		//_events.get("Theatre").put("TORA102938", "100");
	}
	
}
