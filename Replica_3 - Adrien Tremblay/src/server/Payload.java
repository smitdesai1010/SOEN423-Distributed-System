package server;

import general.Command;
import general.EventType;

import java.io.Serializable;

public class Payload implements Serializable {
    public Command command;
    public EventType eventType;
    String participantId;
    String eventId;
}
