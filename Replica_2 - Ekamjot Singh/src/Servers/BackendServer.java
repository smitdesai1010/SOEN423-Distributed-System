package Servers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BackendServer {
	
	public String _name                              = "";
	public String _ip                                = "";
	public String _port                              = "";
	
	HashSet<String> _participants                    = null;
	HashSet<String> _admins                          = null;
	
	// {(_name) => (_ip:_port)}
	public static HashMap<String, String> _allServers = null;
	public static String[] eventTypes = {"ArtGallary", "Theatre", "Concerts"};
	
	// {(Event)==>{(eventID)==>(cap:Pid-Pid-pid:)}}
	HashMap<String, HashMap<String, String>> _events = null;
	
	//{(participantID ==> ((eventType)=>(eventID-eventID-eventID))}
	HashMap<String, HashMap<String, String>> _participantBookings     = null;
	ServerUDP serverUDP                              = null;
	
	public BackendServer(){
		_name = "unnamed";
		_port = "9999";
		_participants = new HashSet<String>();
		_admins =  new HashSet<String>();
		loadServers();
		startListeningServer();	
	}
	
	public BackendServer(String name, List<String> participants, List<String> admins){
		_name = name;
		_participants = new HashSet<String>(participants);
		_admins = new HashSet<String>(admins); 
		loadServers();
		startListeningServer();
	}
	
	private void startListeningServer() {
		_port = _allServers.get(_name).split(":")[1];
		serverUDP = new ServerUDP(_port, this);
		serverUDP.start();
	}

	private void loadServers() {
		_allServers = new HashMap<>();
		String ip;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		_allServers.put("MTL", ip + ":9127");
		_allServers.put("TOR", ip + ":3294");
		_allServers.put("VAN", ip + ":5035");
	}
	
	public synchronized void addParticipant(String participantID) {
		_participants.add(participantID);
	}
	
	public synchronized void removeParticipant(String participantID) {
		_participants.remove(participantID);
	}
	
	public synchronized void addAdmin(String adminID) {
		_admins.add(adminID);
	}
	
	public synchronized void removeAdmin(String adminID) {
		_admins.remove(adminID);
	}
	
	public void setServerName(String name) {
		_name = name;
	}
	
	public synchronized void log(String logString){
		try{
			FileWriter fw = new FileWriter(_name + "log.txt", true);
		    BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
		    //System.out.println(timeStamp + logString);
		    out.println("[" + timeStamp + "] "+  logString);
		    out.close();
		    bw.close();
		    fw.close();
			}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean bookSlot(String clientID, String eventType, String eventID) {
		String eventDetails[] = _events.get(eventType).get(eventID).split(":");
		int capacity = Integer.parseInt(eventDetails[0]); 
		
		//checking if time slot is not already booked
		if(getAllEventIds(clientID) == null || getAllEventIds(clientID).indexOf(eventID) != -1) return false;
	
		
		// checking if user has any booking of same event type on the same day
		// and user does not book the same slot again
		String prefix = eventID.substring(0, eventID.length()-7);
		String postfix = eventID.substring(eventID.length()-4);
		if(_participantBookings.get(clientID) != null && _participantBookings.get(clientID).get(eventType) != null) {
			String bookedEventID = _participantBookings.get(clientID).get(eventType);
			if(bookedEventID.indexOf(prefix + "M" + postfix) != -1 || 
			   bookedEventID.indexOf(prefix + "A" + postfix) != -1 ||
			   bookedEventID.indexOf(prefix + "E" + postfix) != -1) 
			{
			    this.log("ERROR: User already have an other booking of same type on the same day.");
				return false;
			}
			
		}
		
		if(capacity > 1) {
			String updatedBookings = Integer.toString(--capacity) + ":";
			if( eventDetails.length > 1) {
				updatedBookings += eventDetails[1] + "-" + clientID;
			}else {
				updatedBookings += clientID;
			}
			synchronized(this){
				_events.get(eventType).put(eventID, updatedBookings);
				updateParticipantBookings(clientID, eventType, eventID);
			}
			this.log("Successfully booked " + eventID + " for " + clientID);
			return true;
		}
		this.log("ERROR: No more capacity left for event " + eventID);
		return false;
	}

	public String getAllEventIds(String clientID) {
		String allEventIds = "";
		String temp = "";
		System.out.println("getAllEventID");
	    if(!_participantBookings.containsKey(clientID)) return "";
		temp = _participantBookings.get(clientID).get("Theatre");
		allEventIds += temp == null ? "" : temp;
		temp = _participantBookings.get(clientID).get("ArtGallary");
		allEventIds += temp == null ? "" : ("-" + temp);
		temp = _participantBookings.get(clientID).get("Concerts");
		allEventIds += temp == null ? "" : ("-" + temp);
		System.out.println(allEventIds);
		return allEventIds;
	}

	public int getCapacity(String eventID, String eventType){
		return 100;
//		if(_events.get(eventType) == null) return 0;
//		if(_events.get(eventType).get(eventID) == null) return 0;
//		String details = _events.get(eventType).get(eventID);
//		return Integer.parseInt(details.substring(0, details.indexOf(':')));
	}

	public void updateParticipantBookings(String clientId, String eventType, String eventID) {
		String[] clientIDs = clientId.split("-");
		if(clientIDs.length < 1) return;
		for(String clientID : clientIDs){
			_participantBookings.putIfAbsent(clientID, new HashMap<>());
			_participantBookings.get(clientID).putIfAbsent(eventType, "");
			String currentBookings = _participantBookings.get(clientID).get(eventType);
			if(currentBookings.isEmpty())
				_participantBookings.get(clientID).put(eventType,eventID);
			else
				_participantBookings.get(clientID).put(eventType, currentBookings + "-" + eventID);
		}

	}
	
}
