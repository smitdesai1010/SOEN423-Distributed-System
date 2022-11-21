package general;

public enum City {
    MONTREAL("/montreal", "MTL", 1111),
    TORONTO("/toronto", "TOR", 1112),
    VANCOUVER("/vancouver", "VAN", 1113);

    private final String urlExtension;
    private final String idAcronym;
    private final int port;

   private City(String urlExtension, String idAcronym, int port) {
       this.urlExtension = urlExtension;
       this.idAcronym = idAcronym;
       this.port = port;
   }

    public String getUrlExtension() {
        return urlExtension;
    }

    public String getIdAcronym() {
        return idAcronym;
    }

    public int getPort() {
        return port;
    }

    public static City idAcronymToCity(String idAcronym) {
       City foundCity = null;

       for (City city : City.values()) {
           if (city.getIdAcronym().equals(idAcronym)) {
               foundCity = city ;
               break;
           }
       }

       return foundCity;
    }
}
