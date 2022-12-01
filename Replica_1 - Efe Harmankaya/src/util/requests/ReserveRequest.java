package util.requests;

import util.IDTRS.ServerAction;

// regular
public class ReserveRequest extends ServerRequest {
    public ReserveRequest(String eventType, String id, String eventId) {
        super(ServerAction.reserve, eventType, id, eventId);
    }
}