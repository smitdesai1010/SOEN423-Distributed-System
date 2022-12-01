package util.requests;

import util.IDTRS.EventType;

public class ExchangeCondition {
    public boolean status;
    public EventType eventType;
    public String eventId;

    public ExchangeCondition() {
        this.status = false;
        this.eventType = EventType.None;
        this.eventId = null;
    }

    public ExchangeCondition(String eventId) {
        this.status = false;
        this.eventType = EventType.None;
        this.eventId = eventId;

    }

    public ExchangeCondition(EventType eventType, String eventId) {
        this.status = false;
        this.eventType = eventType;
        this.eventId = eventId;
    }

    public ExchangeCondition(boolean status, EventType eventType, String eventId) {
        this.status = status;
        this.eventType = eventType;
        this.eventId = eventId;
    }
}
