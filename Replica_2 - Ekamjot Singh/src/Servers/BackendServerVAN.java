package Servers;

import java.util.*;


public class BackendServerVAN extends BackendServer {
	
	private static List<String> _participantsList  = Arrays.asList("VANP1234", "VANP4321", "VANP9083", "VANP2340");
	private static List<String>  _adminList        = Arrays.asList("VANA1234", "VANA4321", "VANA9083", "VANA2340");
	
	public BackendServerVAN() {
		super("VAN", _participantsList, _adminList);
		loadEvents();
	}
	
	public synchronized void loadEvents() {
		_events = new HashMap<>();
		_participantBookings = new HashMap<>();
		_events.put("ArtGallary", new HashMap<>());
		_events.put("Concerts", new HashMap<>());
		_events.put("Theatre", new HashMap<>());
		_events.get("ArtGallary").put("VANM101010", "100");
		//updateParticipantBookings("MTLP1234", "ArtGallary", "VANM101010");
		_events.get("ArtGallary").put("VANA102938", "100");
		_events.get("Concerts").put("VANM101010", "100:VANP1276-VANP9083");
		updateParticipantBookings("VANP1276", "Concerts", "VANM101010");
		updateParticipantBookings("VANP9083", "Concerts", "VANM101010");
		_events.get("Concerts").put("VANA102938", "100");
		_events.get("Theatre").put("VANM101010", "100");
		_events.get("Theatre").put("VANA102938", "100");
	}
	
}
