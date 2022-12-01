package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.simple.JSONObject;

import util.IDTRS.EventType;
import util.IDTRS.ServerAction;
import util.requests.ServerRequest;

public class UDP extends Thread {
    boolean running;
    DatagramSocket socket;
    IDTRS server;
    int port;

    public UDP(IDTRS server, int port) {
        super();
        this.server = server;
        this.running = true;
        this.port = port;
        try {
            this.socket = new DatagramSocket(this.port);
            // this.server.logger.info("Started UDP server on port: " +
            // String.valueOf(this.port));
        } catch (Exception e) {
            System.out.println("Exception in UDPServer creating DatagramSocket: " + e.getMessage());
            return;
        }
    }

    public void run() {
        while (running) {
            try {

                byte[] in = new byte[8192];
                DatagramPacket packet = new DatagramPacket(in, in.length);

                System.out.println("Waiting to receive...");
                socket.receive(packet);
                System.out.println("Received packet");
                ByteArrayInputStream bais = new ByteArrayInputStream(in);
                ObjectInputStream ois = new ObjectInputStream(bais);

                ServerRequest request = (ServerRequest) ois.readObject();

                // parse return address
                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                // this.server.logger
                // .info(String.format("Received UDP packet\nRequest Type: %s\nFor user:
                // %s\nFrom server: %s",
                // request.type, request.user.getClientId(),
                // request.user.getServer().name().toUpperCase()));

                // fetch response
                JSONObject response;
                // TODO check param ordering
                if (request.type.equals(ServerAction.list)) {
                    response = this.server.listReservationSlotsAvailable(request.id, EventType.valueOf(request.eventType));
                } else if (request.type.equals(ServerAction.reserve)) {
                    response = this.server.reserveTicket(request.id, request.eventId,
                            EventType.valueOf(request.eventType));
                } else if (request.type.equals(ServerAction.add)) {
                    response = this.server.addReservationSlot(request.eventId, EventType.valueOf(request.eventType),
                            request.capacity);
                } else if (request.type.equals(ServerAction.remove)) {
                    response = this.server.removeReservationSlot(request.eventId,
                            EventType.valueOf(request.eventType));
                } else if (request.type.equals(ServerAction.get)) {
                    response = this.server.getEventSchedule(request.id);
                } else if (request.type.equals(ServerAction.cancel)) {
                    // TODO fix request
                    response = this.server.cancelTicket(request.id, request.eventId,
                            EventType.valueOf(request.eventType));
                } else if (request.type.equals(ServerAction.exchange)) {
                    // TODO fix request
                    response = this.server.exchangeTicket(request.id, request.old_eventId,
                            EventType.valueOf(request.old_eventType),
                            request.new_eventId, EventType.valueOf(request.new_eventType));
                } else {
                    response = new JSONObject();
                    // TODO jsonFieldNames?
                    response.put("Data", "Invalid Server Request");
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(response);
                byte[] out = baos.toByteArray();

                // this.server.logger.info(
                // String.format("Sending UDP packet response back to %s for %s\nCompleted:
                // %s\nResponse:\n%s",
                // request.user.getServer().name().toUpperCase(), request.type.toString(),
                // response.status,
                // response.message));

                packet = new DatagramPacket(out, out.length, address, port);

                System.out.println("Sending response");
                socket.send(packet);
            } catch (Exception e) {
                System.out.println("Exception in UDPServer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
