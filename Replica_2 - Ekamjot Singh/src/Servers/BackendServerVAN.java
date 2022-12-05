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
		_events.put("ArtGallery", new HashMap<>());
		_events.put("Concerts", new HashMap<>());
		_events.put("Theatre", new HashMap<>());

		_events.get("ArtGallery").put("VANM010123", "5:MTLP5555-VANP5555");
		updateParticipantBookings("TORE010123", "ArtGallery", "TORE010123");
		_events.get("ArtGallery").put("VANA010123", "0:VANP2222-MTLP3333-MTLP5555");
		updateParticipantBookings("TORE010123", "ArtGallery", "TORE010123");
		_events.get("ArtGallery").put("VANE010123", "10:VANP2222");
		updateParticipantBookings("TORE010123", "ArtGallery", "TORE010123");

		_events.get("Concerts").put("VANM020123", "99:MTLP2222");
		updateParticipantBookings("TORE010123", "Concerts", "TORE010123");
		_events.get("Concerts").put("VANA020123", "1:TORP5555-MTLP5555-VANP5555-VANP2222");
		updateParticipantBookings("TORE010123", "Concerts", "TORE010123");
		_events.get("Concerts").put("VANE020123", "5:VANP5555-VANP4444");
		updateParticipantBookings("TORE010123", "Concerts", "TORE010123");

		_events.get("Theatre").put("VANM101010", "100");
		updateParticipantBookings("TORE010123", "Theatre", "TORE010123");
		_events.get("Theatre").put("VANA102938", "100");
		updateParticipantBookings("TORE010123", "Theatre", "TORE010123");
		_events.get("Theatre").put("VANA102938", "100");
		updateParticipantBookings("TORE010123", "Theatre", "TORE010123");
	}
	
}
