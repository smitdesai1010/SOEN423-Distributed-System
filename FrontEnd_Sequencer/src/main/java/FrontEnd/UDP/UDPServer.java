package FrontEnd.UDP;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.*;
import java.io.*;

public class UDPServer extends Thread{

    private final int PORT;

    public UDPServer(int p) {
        PORT = p;
    }

    public void run() {
        DatagramSocket aSocket = null;
        JSONParser parser = new JSONParser();

        System.out.println("Starting UDP Server");

        try {
            aSocket = new DatagramSocket(PORT);

            while(true) {
                byte[] requestByteArray = new byte[10000];
                DatagramPacket request = new DatagramPacket(requestByteArray, requestByteArray.length);
                aSocket.receive(request);

                String requestString = new String(request.getData()).trim();
                JSONObject requestObject = (JSONObject) parser.parse(requestString);
                String responseString = handleRequest(requestObject);

                byte[] responseByteArray = responseString.getBytes();
                DatagramPacket reply = new DatagramPacket(responseByteArray, responseByteArray.length, request.getAddress(), request.getPort());
                aSocket.send(reply);
            }
        }
        catch (IOException | ParseException e){
            System.out.println("Error on UDP Server " + e.getMessage());
        }
        finally {
            if(aSocket != null) aSocket.close();
        }
    }

    public String handleRequest(JSONObject jsonObj) {
        String response = "";
        return response;
    }
}
