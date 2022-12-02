package util.requests;

import util.IDTRS.ServerAction;

public class GetRequest extends ServerRequest {
    public GetRequest(String id) {
        super(ServerAction.get, id, true);
    }
}