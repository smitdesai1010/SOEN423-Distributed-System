package FrontEnd.UDP;
//
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//
//import java.net.*;
//import java.io.*;
//
//public class UDPServer extends Thread{
//
//    private final DTRSServerClass DTRSServerObj;
//    private final int PORT;
//
//    public UDPServer(DTRSServerClass obj, int p) {
//        DTRSServerObj = obj;
//        PORT = p;
//    }
//
//    public void run() {
//
//        JSONParser parser = new JSONParser();
//        System.out.println("Starting FrontEnd - UDP Server");
//
//        try (DatagramSocket aSocket = new DatagramSocket(PORT)) {
//
//            while (true) {
//                byte[] requestByteArray = new byte[10000];
//                DatagramPacket request = new DatagramPacket(requestByteArray, requestByteArray.length);
//                aSocket.receive(request);
//
//                String requestString = new String(request.getData()).trim();
//                JSONObject requestObject = (JSONObject) parser.parse(requestString);
//                String responseString = handleRequest(requestObject);
//
//                byte[] responseByteArray = responseString.getBytes();
//                DatagramPacket reply = new DatagramPacket(responseByteArray, responseByteArray.length, request.getAddress(), request.getPort());
//                aSocket.send(reply);
//            }
//        }
//        catch (IOException | ParseException e) {
//            System.out.println("Error on FrontEnd - UDP Server" + e.getMessage());
//        }
//    }
//
//    private String handleRequest(JSONObject requestObject) {
//        return "";
//    }
//}