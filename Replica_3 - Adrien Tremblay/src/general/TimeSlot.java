package general;

public enum TimeSlot {
    MORNING("M"),
    AFTERNOON("A"),
    EVENING("E");

    private final String idLetter;

    private TimeSlot(String idLetter) {
        this.idLetter = idLetter;
    }

    public String getIdLetter() {
        return idLetter;
    }
}
