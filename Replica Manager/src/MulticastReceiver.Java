import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastReceiver extends Thread{
    // agreed group address and port by multicast request sender and receiver
    private final String GROUP_ADDRESS = "255.1.2.3";
    private final int PORT = 3435;

    private MulticastSocket mSocket = null;
    InetAddress group = null;

    public MulticastReceiver(){
        try {
            group = InetAddress.getByName(GROUP_ADDRESS);
            mSocket = new MulticastSocket(PORT);
            mSocket.joinGroup(group);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public void run() {
        while(true){
            //TODO: discuss the max size of the buffer
            byte[] buffer =  new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                mSocket.receive(packet);
                String stringJSON = new String(buffer).trim();
                // TODO: parse String into JSONObject
                //JSONParser parser = new JSONParser();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }
}