package util.requests;

import util.IDTRS.ServerAction;

public class AddRequest extends ServerRequest {
    public AddRequest(String eventType, String eventId, int capacity) {
        super(ServerAction.add, eventType, eventId, capacity);
    }
}