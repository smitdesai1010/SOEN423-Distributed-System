package Servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientUDP extends Thread {

	String _ip = ""; // server IP
	String _port = ""; // server port
	String _args = "";
	public static ArrayList<String> _responses = new ArrayList<String>();
	
	public ClientUDP(String ip, String port, String args) {
		_ip = ip;
		_port = port;
		_args = args;
	}

	public void setIpAndPort(String IP, String port) {
		_ip = IP;
		_port = port;
	}

	public void setArgs(String args) {
		_args = args;
	}

	public String[] getResponses() {
		return (String[]) _responses.toArray();
	}
	
	public void clearResponses() {
		_responses.clear();
	}

	@Override
	public void run() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			DatagramPacket request = null;
			byte[] data;
			InetAddress aHost;
			int serverPort;

			data = _args.getBytes();
			aHost = InetAddress.getByName(_ip);
			serverPort = Integer.parseInt(_port);

			request = new DatagramPacket(data, data.length, aHost, serverPort);
			socket.send(request);

			// receiving reply.........
			byte[] serverData = new byte[10000];
			DatagramPacket reply = new DatagramPacket(serverData, serverData.length);
			socket.receive(reply);
			synchronized(this) {
				_responses.add(new String(reply.getData()).trim());
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
	}

}
