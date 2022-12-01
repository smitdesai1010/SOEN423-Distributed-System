package util.requests;

import util.IDTRS.ServerAction;

public class ListRequest extends ServerRequest {
    public ListRequest(String id, String eventType) {
        super(ServerAction.list, id, eventType, true);
    }
}