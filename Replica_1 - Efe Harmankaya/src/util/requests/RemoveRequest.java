package util.requests;

import util.IDTRS.ServerAction;

public class RemoveRequest extends ServerRequest {
    public RemoveRequest(String eventType, String eventId) {
        super(ServerAction.remove, eventType, eventId);
    }
}