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

		_events.get("ArtGallary").put("MTLM010123", "5:MTLP5555-TORP5555");
		updateParticipantBookings("MTLP5555-TORP5555", "ArtGallary", "MTLM010123");
		_events.get("ArtGallary").put("MTLA010123", "0:MTLP1111-MTLP2222-MTLP3333-MTLP4444-MTLP5555");
		updateParticipantBookings("MTLP1111-MTLP2222-MTLP3333-MTLP4444-MTLP5555", "ArtGallary", "MTLA010123");
		_events.get("ArtGallary").put("MTLE010123", "10:");

		_events.get("Concerts").put("MTLM020123", "80:MTLP2222");
		updateParticipantBookings("MTLP2222", "Concerts", "MTLM020123");
		_events.get("Concerts").put("MTLA020123", "1:MTLP4444-MTLP5555-VANP5555-VANP2222");
		updateParticipantBookings("MTLP4444-MTLP5555-VANP5555-VANP2222", "Concerts", "MTLA020123");
		_events.get("Concerts").put("MTLE020123", "5:VANP5555-VANP4444");
		updateParticipantBookings("VANP5555-VANP4444", "Concerts", "MTLE020123");

		_events.get("Theatre").put("MTLM030123", "80:MTLP2222-MTLP5555");
		updateParticipantBookings("MTLP2222-MTLP5555", "Theatre", "MTLM030123");
		_events.get("Theatre").put("MTLA030123", "1:MTLP4444-MTLP5555-VANP5555-VANP2222");
		updateParticipantBookings("MTLP4444-MTLP5555-VANP5555-VANP2222", "Theatre", "MTLA030123");
		_events.get("Theatre").put("MTLE030123", "5:VANP5555-VANP2222");
		updateParticipantBookings("VANP5555-VANP2222", "Theatre", "MTLE030123");
	}
	
	
}
