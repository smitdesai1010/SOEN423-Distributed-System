package tests;

import client.User;
import general.City;
import general.EventType;
import general.Role;
import server.CityReservationSystem;
import server.Server;

import javax.xml.ws.Endpoint;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {
    User participantUser;
    User administratorUser;
    CityReservationSystem cityReservationSystemMontreal;
    CityReservationSystem cityReservationSystemToronto;

    public UserTest() {
        Thread montrealServerThread = new Thread() {
            @Override
            public void run() {
                cityReservationSystemMontreal = new CityReservationSystem(City.MONTREAL);
                Endpoint.publish(Server.endpointAddress + City.MONTREAL.getUrlExtension(), cityReservationSystemMontreal);
            }
        };
        montrealServerThread.run();

        Thread torontoServerThread = new Thread() {
            @Override
            public void run() {
                cityReservationSystemToronto = new CityReservationSystem(City.TORONTO);
                Endpoint.publish(Server.endpointAddress + City.TORONTO.getUrlExtension(), cityReservationSystemToronto);
            }
        };
        torontoServerThread.run();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        participantUser = new User(City.MONTREAL, Role.PARTICIPANT);
        administratorUser = new User(City.MONTREAL, Role.ADMINISTRATOR);
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        cityReservationSystemMontreal.setupEventMap();
        cityReservationSystemToronto.setupEventMap();
    }

    // FAILURE BEFORE THE COMMAND STAGE TESTS

    @org.junit.jupiter.api.Test
    void noCommand() throws RemoteException {
        participantUser.handleCommand("");
    }

    @org.junit.jupiter.api.Test
    void nonexistentCommand()  {
        participantUser.handleCommand("swag 1 2 3");
    }

    @org.junit.jupiter.api.Test
    void participantTriesToCallAdminCmd()  {
        participantUser.handleCommand("addReservationSlot MTLM011022 ART_GALLERY 69");
    }

    @org.junit.jupiter.api.Test
    void invalidNumberOfArgsForCmd()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" MTLM011022 ART_GALLERY hello hello hello");
    }

    @org.junit.jupiter.api.Test
    void invalidEventTypeArgInput()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" MTLM011022 BDSM_DUNGEON");
    }

    @org.junit.jupiter.api.Test
    void invalidIntegerArgInput()  {
        administratorUser.handleCommand("addReservationSlot MTLM011022 ART_GALLERY balls");
    }

    // RESERVE TICKET TESTS

    @org.junit.jupiter.api.Test
    void reserveTicketHomeServer()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" MTLA111022 ART_GALLERY");
        assertTrue(cityReservationSystemMontreal.getEventMap().get(EventType.ART_GALLERY).get("MTLA111022").getReservationSlot().getReservations().contains(participantUser.getId()));
    }

    @org.junit.jupiter.api.Test
    void reserveTicketHomeServerButForSomeoneElseAsAdmin()  {
        administratorUser.handleCommand("reserveTicket MTLP1234 MTLA111022 ART_GALLERY");
        assertTrue(cityReservationSystemMontreal.getEventMap().get(EventType.ART_GALLERY).get("MTLA111022").getReservationSlot().getReservations().contains("MTLP1234"));
    }

    @org.junit.jupiter.api.Test
    void reserveTicketHomeServerButForSomeoneElseAsParticipant()  {
        participantUser.handleCommand("reserveTicket MTLP1234 MTLA111022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void reserveTicketHomeServerButEventNotFound()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" MTLM111022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void reserveTicketHomeServerButNoReservationSlot()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" MTLM011022 ART_GALLERY");
        assertTrue(cityReservationSystemMontreal.getEventMap().get(EventType.ART_GALLERY).get("MTLM011022").getReservationSlot() == null);
    }

    @org.junit.jupiter.api.Test
    void reserveTicketHomeServerButSameTypeSameDayAlreadyExists()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" MTLA111022 ART_GALLERY");
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" MTLA111022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void reserveTicketHomeServerOutOfCapacity()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" MTLE011022 ART_GALLERY");
        administratorUser.handleCommand("reserveTicket MTLP6666 MTLE011022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void reserveTicketHomeServerButAlreadyHaveAReservation()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" MTLA111022 ART_GALLERY");
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" MTLA111022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void reserveTicketOtherServer()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" TORA011022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void reserveTicketOtherServerMultiple()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" TORA011022 ART_GALLERY");
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" TORA021022 ART_GALLERY");
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" TORA031022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void reserveTicketOtherServerMultipleTooMany()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" TORA011022 ART_GALLERY");
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" TORA021022 ART_GALLERY");
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" TORA031022 ART_GALLERY");
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" TORA041022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void reserveTicketOtherServerButItsDown()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" VANA111022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void reserveTicketInvalidCityPrefix()  {
        participantUser.handleCommand("reserveTicket " + participantUser.getId()  +" VAGA111022 ART_GALLERY");
    }

    // CANCEL TICKET TESTS
    @org.junit.jupiter.api.Test
    void cancelTicketHomeServer()  {
        participantUser.handleCommand("cancelTicket " + participantUser.getId()  +" MTLM111122 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void cancelTicketHomeServerButForSomeoneElseAsAdmin()  {
        administratorUser.handleCommand("cancelTicket MTLP0000 MTLM111122 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void cancelTicketHomeServerButForSomeoneElseAsParticipant()  {
        participantUser.handleCommand("cancelTicket MTLP6969 MTLM111122 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void cancelTicketHomeServerButEventNotFound()  {
        participantUser.handleCommand("cancelTicket " + participantUser.getId()  +" MTLM111130 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void cancelTicketHomeServerButNoReservationSlot()  {
        participantUser.handleCommand("cancelTicket " + participantUser.getId()  +" MTLM011022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void cancelTicketHomeServerButNoReservation()  {
        administratorUser.handleCommand("cancelTicket MTLP7777 MTLM111122 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void cancelTicketOtherServer()  {
        participantUser.handleCommand("cancelTicket " + participantUser.getId() +" TORM111122 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void cancelTicketOtherServerButItsDown()  {
        participantUser.handleCommand("cancelTicket " + participantUser.getId() +" VANA111022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void cancelTicketInvalidCityPrefix()  {
        participantUser.handleCommand("cancelTicket " + participantUser.getId() +" VAGA111022 ART_GALLERY");
    }

    // GET EVENT SCHEDULE TESTS

    @org.junit.jupiter.api.Test
    void getEventScheduleTest()  {
        participantUser.handleCommand("getEventSchedule " + participantUser.getId());
    }

    // EXCHANGE TICKET TESTS
    @org.junit.jupiter.api.Test
    void exchangeTicketTest() {
        participantUser.handleCommand("exchangeTicket " + participantUser.getId() + " MTLM111122 ART_GALLERY MTLA111022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void exchangeTicketTestOtherCity() {
        participantUser.handleCommand("exchangeTicket " + participantUser.getId() + " TORM111122 ART_GALLERY TORA011022 ART_GALLERY");
    }

    // ADD RESERVATION SLOT TESTS

    @org.junit.jupiter.api.Test
    void addReservationSlot()  {
        administratorUser.handleCommand("addReservationSlot MTLM011022 ART_GALLERY 7");
    }

    @org.junit.jupiter.api.Test
    void addReservationSlotInvalidEventId()  {
        administratorUser.handleCommand("addReservationSlot MTLM011030 ART_GALLERY 7");
    }

    @org.junit.jupiter.api.Test
    void addReservationSlotButThereIsAlreadyOne()  {
        administratorUser.handleCommand("addReservationSlot MTLA111022 ART_GALLERY 7");
    }

    // REMOVE RESERVATION SLOT TESTS

    @org.junit.jupiter.api.Test
    void removeReservationSlot()  {
        administratorUser.handleCommand("removeReservationSlot MTLA111022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void removeReservationSlotButThereisNoSlot()  {
        administratorUser.handleCommand("removeReservationSlot MTLM011022 ART_GALLERY");
    }

    @org.junit.jupiter.api.Test
    void removeReservationSlotInvalidEventId()  {
        administratorUser.handleCommand("removeReservationSlot MTLA111030 ART_GALLERY");
    }

    // LIST RESERVATION SLOTS AVAILABLE

    @org.junit.jupiter.api.Test
    void listReservationSlotsAvailableArtGallery()  {
        administratorUser.handleCommand("listReservationSlotsAvailable ART_GALLERY");
    }
}