package server;

import general.City;

public class Server {
    private static final String USAGE_MESSAGE = "Usage: server [MONTREAL/TORONTO/VANCOUVER]";

    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("Please provide a city name");
            System.out.println(USAGE_MESSAGE);
            return;
        }

        City city;
        try {
            city = City.valueOf(args[0]);
        } catch (Exception e) {
            System.out.println("Not a valid city name! Try again!");
            System.out.println(USAGE_MESSAGE);
            return;
        }

        System.out.println("I am a " + city.name() + " server! ~uwU!!");

        // ReservationSystem reservationSystem = new CityReservationSystem(city);
    }
}
