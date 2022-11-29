package Sequencer;

import FrontEnd.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Sequencer {
    static int sequence_number = 0;
    private final int PORT = 3435;
    private final String GROUP_ADDRESS = "225.1.2.3";
    private MulticastSocket mSocket = null;
    InetAddress group = null;

    public Sequencer() throws IOException {
        mSocket = new MulticastSocket();
        group = InetAddress.getByName(GROUP_ADDRESS);
    }

    public void multicast(JSONObject obj, Logger logger) throws IOException {
        obj = assignSequenceNumber(obj);
        logger.addToLogs("Received object for Multicast: " + obj.toString());
        DatagramPacket packet = new DatagramPacket(obj.toJSONString().getBytes(), obj.toJSONString().length(), group, PORT);
        mSocket.send(packet);
        logger.addToLogs("Object multicasted with sequence number: " + (sequence_number - 1));
    }

    private JSONObject assignSequenceNumber(JSONObject obj) {
        obj.put("sequenceNumber", sequence_number++);
        return obj;
    }
}
