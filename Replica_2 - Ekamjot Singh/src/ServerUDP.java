package Servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

public class ServerUDP extends Thread{
	String _ip = " ";
	BackendServer _mainBackendServer = null;
	int _port = -1;
	
	public ServerUDP(String port, BackendServer mainBackendServer) {
		_mainBackendServer = mainBackendServer;
		try {
			_ip = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		_port = Integer.parseInt(port);
	}
	
	@Override
	public void run() {
		int port = _port;
		DatagramSocket serverSocket   = null;
		DatagramPacket receivedPacket = null;
		DatagramPacket reply          = null;
		
		try {
			serverSocket =  new DatagramSocket(port);
			byte[] c_buffer = new byte[10000];
			System.out.println("listening at ================> " + port);
			while(true) {
				receivedPacket = new DatagramPacket(c_buffer, c_buffer.length);
				serverSocket.receive(receivedPacket);
				byte[] buffer = receivedPacket.getData();
				String request = new String(receivedPacket.getData()).trim();
				String myReply = processRequest(request);
				buffer = myReply.getBytes();
				reply = new DatagramPacket(buffer, buffer.length, receivedPacket.getAddress(),receivedPacket.getPort());
				serverSocket.send(reply);
				c_buffer = new byte[10000]; // renewing buffer
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private String processRequest(String request) {
		String[] arg = request.split(":");
		
		switch(arg[0]) {
			case "getReservationsSlots": {
				if(_mainBackendServer._events.get(arg[1]) == null) return "No available reservation slots";
				return _mainBackendServer._events.get(arg[1]).toString();
			}
			case "getMySchedule": {
				HashMap<String, String> localSchedule = _mainBackendServer._participantBookings.get(arg[1]);
			    String s = (localSchedule != null ) ? localSchedule.toString() : "{No bookings to show}";
			    return _mainBackendServer._name + " ==> " + s;
		    }
			case "cancelBooking": {
				String details = _mainBackendServer._events.get(arg[1]).get(arg[2]);
				_mainBackendServer._events.get(arg[1]).put(arg[2], removeID(details, arg[3]));
				return "true";
			}
			case "bookTicket": {
				boolean success = _mainBackendServer.bookSlot(arg[1], arg[2], arg[3]);
				if(success) return "true";
				return "false";
			}
		    case "getCapacity": {
				return Integer.toString(_mainBackendServer.getCapacity(arg[1], arg[2]));
		    }
			case "hasTicket": {
				return Boolean.toString(_mainBackendServer.getAllEventIds(arg[1]).indexOf(arg[2]) != -1);
			}
		    default:
			    return "";
		}
	}

	private String removeID(String temp, String eventID) {
		int idx = temp.indexOf(eventID);
		if(idx != -1) {
			if(temp.length() == eventID.length()) {
				return "";
			}else if(temp.substring(0,eventID.length()).equals(eventID)) {
				return temp.substring(eventID.length()+1);
			}else if(temp.substring(temp.length() - eventID.length()).equals(eventID)) {
				return temp.substring(0,temp.length() - eventID.length()-1);
			}else {
				return temp.substring(0,idx) + temp.substring(idx+eventID.length()+1);
			}
		}
		return temp;

	}

}