package util.requests;

import util.IDTRS.ServerAction;

public class CancelRequest extends ServerRequest {
    public CancelRequest(String id, String eventId, String eventType) {
        super(ServerAction.cancel, eventType, id, eventId);
    }
}