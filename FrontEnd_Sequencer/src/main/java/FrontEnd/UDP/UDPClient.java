package FrontEnd.UDP;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UDPClient {
    public String call(JSONObject jsonObj, String IP, int port) {
        try (DatagramSocket aSocket = new DatagramSocket()) {

            byte[] data = jsonObj.toJSONString().getBytes(StandardCharsets.UTF_8);
            InetAddress aHost = InetAddress.getByName(IP);

            DatagramPacket request = new DatagramPacket(data, data.length, aHost, port);
            aSocket.send(request);

            byte[] buffer = new byte[10000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);

            return new String(reply.getData()).trim();
        }
        catch (IOException e) {
            System.out.println("Exception in UDP-Client: " + e.getMessage());
        }

        return "";
    }
}