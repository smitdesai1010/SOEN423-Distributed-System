package util.requests;

import java.io.Serializable;

import util.IDTRS.ServerAction;

public class ServerRequest implements Serializable {
    public ServerAction type;
    public String eventType = "N/A";

    public String id = "N/A";
    public String eventId = "N/A";

    // only ServerAction.add
    public int capacity = 0;

    // only SeverAction.exchange
    public String old_eventId = "N/A";
    public String new_eventId = "N/A";
    public String new_eventType = "N/A";
    public String old_eventType = "N/A";

    public ServerRequest() {
    }

    // construct ServerAction.list
    public ServerRequest(ServerAction type, String id, String eventType, boolean _unused) {
        this.type = type;
        this.eventType = eventType;
    }

    // construct ServerAction.get
    // note - differentiating bool added from list
    public ServerRequest(ServerAction type, String id, boolean _unused) {
        this.type = type;
        this.id = id;
    }

    // construct ServerAction.remove
    public ServerRequest(ServerAction type, String eventType, String eventId) {
        this.type = type;
        this.eventType = eventType;
        this.eventId = eventId;
    }

    // construct ServerAction.reserve
    // construct ServerAction.cancel
    public ServerRequest(ServerAction type, String eventType, String id, String eventId) {
        this.type = type;
        this.eventType = eventType;
        this.id = id;
        this.eventId = eventId;
    }

    // construct ServerAction.add
    public ServerRequest(ServerAction type, String eventType, String eventId, int capacity) {
        this.type = type;
        this.eventType = eventType;
        this.eventId = eventId;
        this.capacity = capacity;
    }

    // ServerAction.exchange
    public ServerRequest(ServerAction type, String id, String old_eventId, String new_eventId,
            String new_eventType) {
        this.type = type;
        this.id = id;
        this.old_eventId = old_eventId;
        this.new_eventId = new_eventId;
        this.new_eventType = new_eventType;
    }

    @Override
    public String toString() {
        return String.format(
                "\nType: %s\nEventType: %s\nId: %s\nEventId: %s\nCapacity: %d\nold_eventId: %s\nnew_eventId: %s\nnew_eventType: %s",
                this.type.toString(),
                this.eventType, this.id, this.eventId, this.capacity, this.old_eventId, this.new_eventId,
                this.new_eventType);
    }
}
