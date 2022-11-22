package general;

public enum Role {
    ADMINISTRATOR("A"),
    PARTICIPANT("P");

    private String idLetter;

    private Role(String idLetter) {
        this.idLetter = idLetter;
    }

    public String getIdLetter() {
        return idLetter;
    }
}
