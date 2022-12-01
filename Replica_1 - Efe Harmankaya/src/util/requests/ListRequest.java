package util.requests;

import util.IDTRS.ServerAction;

public class ListRequest extends ServerRequest {
    public ListRequest(String eventType) {
        super(ServerAction.list, eventType);
    }
}