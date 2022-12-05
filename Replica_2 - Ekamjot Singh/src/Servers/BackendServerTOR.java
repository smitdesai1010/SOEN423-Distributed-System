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

		_events.get("ArtGallary").put("TORM010123", "5:MTLP5555-TORP5555");
		updateParticipantBookings("MTLP5555-TORP5555", "ArtGallary", "TORM010123");
		_events.get("ArtGallary").put("TORA010123", "0:TORP1111-TORP2222-MTLP3333-MTLP5555");
		updateParticipantBookings("TORP1111-TORP2222-MTLP3333-MTLP5555", "ArtGallary", "TORA010123");
		_events.get("ArtGallary").put("TORE010123", "10:VANP2222");
		updateParticipantBookings("VANP2222", "ArtGallary", "TORE010123");

		_events.get("Concerts").put("TORM020123", "99:MTLP2222");
		updateParticipantBookings("MTLP2222", "Concerts", "TORM020123");
		_events.get("Concerts").put("TORA020123", "1:TORP5555-MTLP5555-VANP5555-VANP2222");
		updateParticipantBookings("TORP5555-MTLP5555-VANP5555-VANP2222", "Concerts", "TORA020123");
		_events.get("Concerts").put("TORE020123", "5:VANP5555-VANP4444");
		updateParticipantBookings("VANP5555-VANP4444", "Concerts", "TORE020123");

		_events.get("Theatre").put("TORM030123", "4:MTLP2222-MTLP5555");
		updateParticipantBookings("MTLP2222-MTLP5555", "Theatre", "TORM030123");
		_events.get("Theatre").put("TORA030123", "1:MTLP4444-MTLP5555-VANP5555-VANP2222");
		updateParticipantBookings("MTLP4444-MTLP5555-VANP5555-VANP2222", "Theatre", "TORA030123");
		_events.get("Theatre").put("TORE030123", "2:VANP5555-VANP2222");
		updateParticipantBookings("VANP5555-VANP2222", "Theatre", "TORE030123");

	}
	
}
