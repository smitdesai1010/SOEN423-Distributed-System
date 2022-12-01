package util.requests;

import util.IDTRS.ServerAction;

public class CancelRequest extends ServerRequest {
    public CancelRequest(String user, String id, String eventId) {
        super(ServerAction.cancel, user, "", id, eventId);
    }
}