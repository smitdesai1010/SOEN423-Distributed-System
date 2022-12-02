package util.requests;

import util.IDTRS.ServerAction;

public class ExchangeRequest extends ServerRequest {
    public ExchangeRequest(String id, String old_eventId, String new_eventId, String new_eventType) {
        super(ServerAction.exchange, id, old_eventId, new_eventId, new_eventType);
    }
}
