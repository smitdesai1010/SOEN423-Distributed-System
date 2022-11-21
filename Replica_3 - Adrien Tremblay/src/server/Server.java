package server;

import general.City;

import javax.xml.ws.Endpoint;
import java.util.Scanner;

public class Server {
    public final static String endpointAddress = "http://localhost:8081/dtrs";

    public static void main(String args[]) {
        System.out.println("Launching a new server console...\n");

        Scanner s = new Scanner(System.in);

        City city;
        while (true) {
            System.out.println("What city is this server located in (MONTREAL, TORONTO, or VANCOUVER)?");
            try {
                city = City.valueOf(s.nextLine());
                break;
            } catch (Exception e) {
                System.out.println("Not a valid city name! Try again!");
            }
        }

        Endpoint endpoint = Endpoint.publish(endpointAddress + city.getUrlExtension(), new CityReservationSystem(city));
        System.out.println(endpoint.isPublished());
    }
}
