package Servers;

import java.util.*;


public class BackendServerMTL extends BackendServer {
	
	private static List<String> _participantsList  = Arrays.asList("MTLP1234", "MTLA1234", "MTLP4321", "MTLP9083", "MTLP2340");
	private static List<String>  _adminList        = Arrays.asList("MTLA1234", "MTLA4321", "MTLA9083", "MTLA2340");
	
	public BackendServerMTL() {
		super("MTL", _participantsList, _adminList);
		loadEvents();
	}
	
	public synchronized void loadEvents() {
		_events = new HashMap<>();
		_participantBookings = new HashMap<>();
		_events.put("ArtGallary", new HashMap<>());
		_events.put("Concerts", new HashMap<>());
		_events.put("Theatre", new HashMap<>());
		_events.get("ArtGallary").put("MTLM051022", "100:MTLP1234");
		updateParticipantBookings("MTLP1234", "ArtGallary", "MTLM051022");
		_events.get("ArtGallary").put("MTLA102938", "100");
		_events.get("Concerts").put("MTLM101022", "100:MTLP1276-MTLP9083");
		updateParticipantBookings("MTLP1234", "Concerts", "MTLM101022");
		updateParticipantBookings("MTLP9083", "Concerts", "MTLM101022");
		_events.get("Concerts").put("MTLA102938", "100");
		//_events.get("Theatre").put("MTLM101010", "100");
		//_events.get("Theatre").put("MTLA102938", "100");
	}
	
	
}
