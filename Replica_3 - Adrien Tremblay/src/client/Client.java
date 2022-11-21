package client;

import general.City;
import general.Role;
import java.util.Scanner;

public class Client {
    public static void main(String args[]) {
        System.out.println("Launching a new user console...");

        Scanner s = new Scanner(System.in);
        // getting the user city
        City city;
        while (true) {
            System.out.println("What city are based in (MONTREAL, TORONTO, or VANCOUVER)?");
            try {
                city = City.valueOf(s.nextLine());
                break;
            } catch (Exception e) {
                //System.out.println("Not a valid city name! Try again!");
                city = City.MONTREAL;
                break;
            }
        }

        // getting the user role
        Role role;
        while (true) {
            System.out.println("What role are you (ADMINISTRATOR, PARTICIPANT)?");
            try {
                role = Role.valueOf(s.nextLine());
                break;
            } catch (Exception e) {
                //System.out.println("Not a role! Try again!");
                role = Role.ADMINISTRATOR;
                break;
            }
        }

        // Creating the user
        User user = new User(city, role);
        System.out.println("Your unique user ID is: " + user.getId());

        // User input loop
        while (true) {
            System.out.println(
                (role == Role.ADMINISTRATOR ?
                    " ------------ADMINISTRATOR-COMMANDS---------------\n" +
                    "addReservationSlot [eventId] [eventType] [capacity]\n" +
                    "removeReservationSlot [eventId] [eventType]\n" +
                    "listReservationSlotsAvailable [eventType]\n"
                    : "") +
                    "------------PARTICIPANT-COMMANDS---------------\n" +
                    "reserveTicket [participantId] [eventId] [eventType]\n" +
                    "getEventSchedule [participantId]\n" +
                    "cancelTicket [participantId] [eventId] [eventType]\n" +
                    "----------------GENERAL-COMMANDS---------------\n" +
                    "exit (Exits the client)\n" +
                    "-----------------------------------------------\n" +
                    "What do you want to do?\n"
            );

            user.handleCommand(s.nextLine());
        }
    }
}
