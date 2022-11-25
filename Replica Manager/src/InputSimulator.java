import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class InputSimulator {
    public static void main(String args[]) {
        JSONObject samplePayload = new JSONObject();
        samplePayload.put("MethodName", "reserveTicket");
        samplePayload.put("participantID", "MTLP0000");
        samplePayload.put("eventType", "ArtGallery");
        samplePayload.put("eventID", "MTLA111022");
        samplePayload.put(jsonFieldNames.FRONTEND_PORT, ReplicaManager.REPLICA_MANAGER_PORT);
        samplePayload.put(jsonFieldNames.FRONTEND_IP, "localhost");

        JSONObject replyObject;
        try {
            replyObject = ReplicaManager.sendMessageToLocalHost(ReplicaManager.REPLICA_MANAGER_PORT, samplePayload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println(replyObject.toJSONString());
    }
}
