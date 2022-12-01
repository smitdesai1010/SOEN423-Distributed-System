package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class EventData implements Serializable {
    int capacity;
    ArrayList<String> guests;

    public EventData() {
        this.capacity = 0;
        this.guests = new ArrayList<>();
    }

    public EventData(int capacity) {
        this.capacity = capacity;
        this.guests = new ArrayList<>();
    }

    public EventData(int capacity, String[] guests) {
        this.capacity = capacity;
        this.guests = new ArrayList<String>(Arrays.asList(guests));
    }

    public void addGuest(String id) {
        this.guests.add(id);
        this.capacity--;
    }

    public void removeGuest(String id) {
        this.guests.remove(id);
        this.capacity++;
    }

    private String getGuests() {
        if (this.guests.size() == 0)
            return "N/A";

        return this.guests.toString();
    }

    @Override
    public String toString() {
        return "\tcapacity: " + String.valueOf(this.capacity) + " guests: " + this.getGuests();
    }
}