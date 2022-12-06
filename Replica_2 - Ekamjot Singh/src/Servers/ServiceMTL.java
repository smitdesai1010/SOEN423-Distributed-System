package Servers;

import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class ServiceMTL implements ServiceInterface{
    private final BackendServerMTL delegateMTL;
    private JSONObject replyObjectJSON;

    private final String SUCCESS = "Success";
    private final String DATA = "Data";
    public ServiceMTL(){
        delegateMTL = new BackendServerMTL();
        replyObjectJSON = new JSONObject();
    }


    @Override
    public JSONObject addReservationSlot(String eventID, String eventType, int capacity) {
        delegateMTL.log("REQUEST: addReservationSlot called for eventID " + eventID + " Type: " + eventType + " capacity: " + capacity);
        if(eventID.substring(0,3).equals(delegateMTL._name)){
            if (!delegateMTL._events.containsKey(eventType)) {
                replyObjectJSON.put(SUCCESS, false);
                replyObjectJSON.put( DATA, "The reservation slot is already available");
                delegateMTL.log(replyObjectJSON.get(DATA).toString());
                return replyObjectJSON;
            }

            synchronized (this) {
                if (delegateMTL._events.get(eventType) == null || delegateMTL._events.get(eventType).isEmpty()) {
                    delegateMTL._events.put(eventType, new HashMap<String, String>());
                    delegateMTL._events.get(eventType).put(eventID, Integer.toString(capacity));
                    replyObjectJSON.put(SUCCESS, true);
                    replyObjectJSON.put( DATA,
                            "Reservation slot was successfully added for " + eventID + " " + eventType + " " + capacity);
                    delegateMTL.log(replyObjectJSON.get(DATA).toString());
                    return replyObjectJSON;
                }
            }

            replyObjectJSON.put(SUCCESS, false);
            replyObjectJSON.put( DATA, "addReservation Request failed due to some internal error.");
            delegateMTL.log(replyObjectJSON.get(DATA).toString());
            return replyObjectJSON;
        }
        else {
            String[] config = delegateMTL._allServers.get(eventID.substring(0, 3)).split(":");
            String request = "addReservationSlot:"+ eventID + ":" + eventType + ":" + capacity;
            ClientUDP udpClient = new ClientUDP(config[0], config[1], request);
            udpClient.start();
            try {
                udpClient.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ArrayList<String> s = ClientUDP._responses;
            if (s.get(0).equals("true")) {
                ClientUDP._responses.clear();
                replyObjectJSON.put(SUCCESS, true);
                replyObjectJSON.put( DATA, "The Reservation slot was removed successfully!");
                delegateMTL.log(replyObjectJSON.get(DATA).toString());
                return replyObjectJSON;

            }
            replyObjectJSON.put(SUCCESS, false);
            replyObjectJSON.put( DATA, "The Reservation slot was Not removed successfully!");
            delegateMTL.log(replyObjectJSON.get(DATA).toString());
            return replyObjectJSON;
        }

    }

    @Override
    public JSONObject removeReservationSlot(String eventID, String eventType) {
        if(eventID.substring(0,3).equals(delegateMTL._name)){
            delegateMTL.log("REQUEST: removeReservationSlot: " + eventID + " " + eventType);
            if (delegateMTL._events.get(eventType) == null || delegateMTL._events.get(eventType).isEmpty() ||
                    delegateMTL._events.get(eventType).get(eventID) == null ||
                    delegateMTL._events.get(eventType).get(eventID).isEmpty() )
            {
                replyObjectJSON.put(SUCCESS, false);
                replyObjectJSON.put( DATA, "ERROR: Reservation Slot of type "+eventType + " and ID " + eventID +
                        " is not available.");
                delegateMTL.log(replyObjectJSON.get(DATA).toString());
                return replyObjectJSON;
            }

            String[] eventDetails = delegateMTL._events.get(eventType).get(eventID).split(":");

            if (eventDetails.length >= 2 && !eventDetails[1].isEmpty()) { // checking if someone already booked the slot
                replyObjectJSON.put(SUCCESS, false);
                replyObjectJSON.put( DATA, "ERROR: you cannot remove the slot since it is already booked by someone.");
                delegateMTL.log(replyObjectJSON.get(DATA).toString());
                return replyObjectJSON;
            }

            delegateMTL._events.get(eventType).remove(eventID);
            replyObjectJSON.put(SUCCESS, true);
            replyObjectJSON.put( DATA, "Reservation Slot is removed Successfully.");
            delegateMTL.log(replyObjectJSON.get(DATA).toString());
            return replyObjectJSON;
        }
        else {
            String[] config = delegateMTL._allServers.get(eventID.substring(0, 3)).split(":");
            String request = "removeReservationSlot:"+ eventID + ":" + eventType;
            ClientUDP udpClient = new ClientUDP(config[0], config[1], request);
            udpClient.start();
            try {
                udpClient.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ArrayList<String> s = ClientUDP._responses;
            if (s.get(0).equals("true")) {
                ClientUDP._responses.clear();
                replyObjectJSON.put(SUCCESS, true);
                replyObjectJSON.put( DATA, "The Reservation Slot was removed Successfully.");
                delegateMTL.log(replyObjectJSON.get(DATA).toString());
                return replyObjectJSON;
            }
            replyObjectJSON.put(SUCCESS, false);
            replyObjectJSON.put( DATA, "Reservation Slot is not removed Successfully.");
            delegateMTL.log(replyObjectJSON.get(DATA).toString());
            return replyObjectJSON;

        }
    }

    @Override
    public JSONObject listReservationSlotAvailable(String eventType) {
        delegateMTL.log("REQUEST: listReservationSlotAvailable for event Type " + eventType + "");
        broadcastRequestUDP("getReservationsSlots:" + eventType);
        ArrayList<String> s = ClientUDP._responses;
        s.add(delegateMTL._events.get(eventType).toString());
        Object[] objs = s.toArray();
        String[] arr = new String[objs.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = objs[i].toString();
        }
        String data =  (arr.length == 0) ?
                "No reservations slot are available for "+eventType+"." :
                Arrays.toString(arr);

        replyObjectJSON.put(SUCCESS, arr.length > 0);
        replyObjectJSON.put( DATA, data);
        delegateMTL.log(data);
        ClientUDP._responses.clear();
        return replyObjectJSON;
    }

    @Override
    public JSONObject reserveTicket(String participantID, String eventID, String eventType) {
        delegateMTL.log("REQUEST: reserveTicket for " + participantID + " " + eventID + " " + eventType);
        boolean success = false;
        // checking that participant has at most 3 events booked in the other cities
        // overall in a week
        if (checkBookingLimit(participantID, eventID, eventType)){
            if(eventID.substring(0,3).equals(delegateMTL._name)){
                success =  delegateMTL.bookSlot(participantID, eventType, eventID);
            }
            else{
                String[] config = BackendServerMTL._allServers.get(eventID.substring(0,3)).split(":");
                String request = "bookTicket:" + participantID + ":" + eventType + ":" +eventID;
                ClientUDP udpClient = new ClientUDP(config[0], config[1], request);
                udpClient.start();
                try {
                    udpClient.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ArrayList<String> s = ClientUDP._responses;

            //    delegateMTL.log(Boolean.toString(s == null));
                delegateMTL.log(s.get(0));
                success = s.get(0).equals("true");
                ClientUDP._responses.clear();
            }
        }

        replyObjectJSON.put(SUCCESS, success);
        replyObjectJSON.put( DATA, "The ticket was " + (success ? "" : "Not")+ " reserved successfully.");
        delegateMTL.log(replyObjectJSON.get(DATA).toString());
        return replyObjectJSON;
    }

    @Override
    public JSONObject getEventSchedule(String ParticipantID) {
        delegateMTL.log("REQUEST: getEventSchedule " + ParticipantID);
        String[] schedule = null;
//        if (ParticipantID.substring(0, 3).equals(delegateMTL._name)) {
//            schedule = new String[1];
//            HashMap<String, String> booking = delegateMTL._participantBookings.get(ParticipantID);
//            schedule[0] = "Schedule: " + " ==> " + (booking != null ? booking.toString() : "{}");
//        }else{
        broadcastRequestUDP("getMySchedule:" + ParticipantID);
        schedule = new String[BackendServer._allServers.size()];
        HashMap<String, String> booking = delegateMTL._participantBookings.get(ParticipantID);
        schedule[0] = delegateMTL._name + " ==> " + (booking != null ? booking.toString() : "{}");
        ArrayList<String> s = ClientUDP._responses;
        Object[] objs = s.toArray();
        for (int i = 1; i < schedule.length; i++) {
            schedule[i] = objs[i - 1].toString();
            //System.out.println(schedule[i] = objs[i - 1].toString());
        }
        ClientUDP._responses.clear();
        //  }

        replyObjectJSON.put(SUCCESS,schedule.length > 0);
        replyObjectJSON.put( DATA, "Schedule returned with " + schedule.length + " bookings.");
        delegateMTL.log(replyObjectJSON.get(DATA).toString());
        return replyObjectJSON;
    }

    @Override
    public JSONObject cancelTicket(String participantID, String eventID) {
        delegateMTL.log("REQUEST: cancelTicket " + participantID + " " + eventID);
        String calEventType = "";
        if(eventID.substring(0, 3).equals(delegateMTL._name)) {
            delegateMTL.log("there 0");
            delegateMTL.log(delegateMTL._participantBookings.toString());
            if(delegateMTL._participantBookings.containsKey(participantID)) {
                //delegateMTL.log("there 00");
                for(String eventType : BackendServer.eventTypes) {
                    String temp = delegateMTL._participantBookings.get(participantID).get(eventType);
                    if(temp ==null || temp.length() < 1) continue;
                    int idx = temp.indexOf(eventID);
                    if(idx != -1) {
                        delegateMTL._participantBookings.get(participantID).put(eventType,removeID(temp,eventID));
                        calEventType = eventType;
                        delegateMTL.log("there 1");
                        break;
                    }
                }
                if(calEventType.equals("")){
                    replyObjectJSON.put(SUCCESS,false);
                    replyObjectJSON.put( DATA, "The ticket is not present in the schedule.");
                    delegateMTL.log(replyObjectJSON.get(DATA).toString());
                    return replyObjectJSON;
                }
            }
        }

        // if(eventID.substring(0, 3).equals(delegateMTL._name)) {
        //delegateMTL.log("there 2");
        if (eventID.substring(0, 3).equals(delegateMTL._name)) {
            delegateMTL.log("there 3");
            String temp = delegateMTL._events.get(calEventType).get(eventID);
            //temp = temp.substring(temp.indexOf(":") != -1 ? temp.indexOf(":") : 0);
            int idx = temp.indexOf(participantID);
            if(idx != -1) {
                delegateMTL._events.get(calEventType).put(eventID, removeID(temp,participantID));
                // delegateMTL._events.get(calEventType).put(eventID, "");
                replyObjectJSON.put(SUCCESS,true);
                replyObjectJSON.put( DATA, "The Ticket was canceled successfully.");
                delegateMTL.log(replyObjectJSON.get(DATA).toString());
                return replyObjectJSON;
            }
        }else{
           // delegateMTL.log("there 4 " + eventID.substring(0, 3));
            String[] config = BackendServerMTL._allServers.get(eventID.substring(0,3)).split(":");
            String request = "removeBooking:" + calEventType + ":" + eventID + ":" + participantID;
            ClientUDP udpClient = new ClientUDP(config[0], config[1], request);
            udpClient.start();
            try {
                udpClient.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ArrayList<String> s = ClientUDP._responses;
            if(s.get(0).equals("true")){
                ClientUDP._responses.clear();
                replyObjectJSON.put(SUCCESS,true);
                replyObjectJSON.put( DATA, "The Ticket was canceled successfully.");
                delegateMTL.log(replyObjectJSON.get(DATA).toString());
                return replyObjectJSON;
            }
            ClientUDP._responses.clear();
        }

        ClientUDP._responses.clear();
        replyObjectJSON.put(SUCCESS,false);
        replyObjectJSON.put( DATA, "The Ticket was not found.");
        delegateMTL.log(replyObjectJSON.get(DATA).toString());
        return replyObjectJSON;
    }

    @Override
    public boolean verifyUser(String userID, boolean isAdmin) {
        delegateMTL.log("REQUEST: verification request for userID " + userID);
        return isAdmin ? delegateMTL._admins.contains(userID) : delegateMTL._participants.contains(userID);
    }


    @Override
    public JSONObject exchangeTicket(String participantID, String eventID, String newEventID, String newEventType) {
        delegateMTL.log("REQUEST: exchangeTickets for " + participantID + " " + eventID + " " + newEventID
                +  " " + newEventType);
        if(!participantID.substring(0,3).equals(delegateMTL._name)){
            replyObjectJSON.put(SUCCESS,false);
            replyObjectJSON.put( DATA, "ERROR: please try to exchange ticket through your home server");
            delegateMTL.log(replyObjectJSON.get(DATA).toString());
            return replyObjectJSON;
        }
        if(hasTicket(participantID, eventID)){
            int capacity = getCapacity(newEventID, newEventType);
            if(capacity < 1 ) {
                replyObjectJSON.put(SUCCESS,false);
                replyObjectJSON.put( DATA, "ERROR: new event if fully booked");
                delegateMTL.log(replyObjectJSON.get(DATA).toString());
                return replyObjectJSON;
            }
            if((Boolean) cancelTicket(participantID, eventID).get(SUCCESS)){
                if((Boolean) reserveTicket(participantID, newEventID, newEventType).get(SUCCESS)){
                    replyObjectJSON.put(SUCCESS,true);
                    replyObjectJSON.put( DATA, "Ticket were exchanged Successfully.");
                    delegateMTL.log(replyObjectJSON.get(DATA).toString());
                    return replyObjectJSON;
                }
                replyObjectJSON.put(SUCCESS,false);
                replyObjectJSON.put( DATA, "ERROR: Ticket were exchanged half successfully");
                delegateMTL.log(replyObjectJSON.get(DATA).toString());
                return replyObjectJSON;
            }
        }
        replyObjectJSON.put(SUCCESS,false);
        replyObjectJSON.put( DATA, "Client " + participantID + " does not have ticket for event " + eventID);
        delegateMTL.log(replyObjectJSON.get(DATA).toString());
        return replyObjectJSON;
    }

    /////////////////////////// HELPER FUNCTIONS /////////////////////////////////////////
    public int getCapacity(String eventID, String eventType) {
        if(eventID.substring(0,3).equals(delegateMTL._name)){
            return delegateMTL.getCapacity(eventID,eventType);
        }else{
            String[] config = BackendServerMTL._allServers.get(eventID.substring(0,3)).split(":");
            String request = "getCapacity:" + eventID + ":" + eventType;
            ClientUDP udpClient = new ClientUDP(config[0], config[1], request);
            udpClient.start();
            try {
                udpClient.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ArrayList<String> s = ClientUDP._responses;
            int ret = Integer.parseInt(s.get(0));
            ClientUDP._responses.clear();
            return ret;
        }
    }

    private boolean hasTicket(String participantID, String eventID) {
        if(eventID.substring(0,3).equals(delegateMTL._name))
            return delegateMTL.getAllEventIds(participantID).indexOf(eventID) != -1;
        String request = "hasTicket:" + participantID + ":" + eventID;
        sendRequestUDP(request, eventID.substring(0,3));
        ArrayList<String> s = ClientUDP._responses;
        boolean ret = s.get(0).equals("true");
        ClientUDP._responses.clear();
        return ret;
    }

    private void broadcastRequestUDP(String request) {
        Set<String> servers = BackendServerMTL._allServers.keySet();
        ClientUDP[] allThreads = new ClientUDP[servers.size() - 1];

        int ctr = 0;
        for (String server : servers) {
            if (server.equals(delegateMTL._name))
                continue;
            String[] config = BackendServerMTL._allServers.get(server).split(":");
            allThreads[ctr] = new ClientUDP(config[0], config[1], request);
            allThreads[ctr].start();
            ++ctr;
        }

        for (int i = 0; i < allThreads.length; i++) {
            try {
                allThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendRequestUDP(String request, String Destination) {
        Set<String> servers = BackendServerMTL._allServers.keySet();
        ClientUDP[] allThreads = new ClientUDP[servers.size() - 1];
        String[] config = BackendServerMTL._allServers.get(Destination).split(":");
        ClientUDP client = new ClientUDP(config[0], config[1], request);
        client.start();
        try {
            client.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String removeID(String temp, String eventID) {
        int idx = temp.indexOf(eventID);
        int si = 0;
        String ret = "";
        if(idx != -1) {
            System.out.println("temp: " + temp);
            if(temp.indexOf(':') != -1 &&
                    temp.substring(0, temp.indexOf(':')).chars().allMatch( Character::isDigit )) {
                String cap = temp.substring(0, temp.indexOf(':'));
                si = cap.length() + 1;
                ret = (Integer.parseInt(cap) + 1) + ":";
                System.out.println(cap + " " + ret);
            }
            if(temp.substring(si,temp.length()).equals(eventID)) {
                return ret + "";
            }else if(temp.substring(si,eventID.length()).equals(eventID)) {
                return ret + temp.substring(eventID.length()+1);
            }else if(temp.substring(temp.length() - eventID.length()).equals(eventID)) {
                return ret + temp.substring(si,temp.length() - eventID.length()-1);
            }else {
                return ret + temp.substring(si,idx) + temp.substring(idx+eventID.length());
            }
        }
        return temp;

    }

    private boolean checkBookingLimit(String participantID, String eventID, String eventType) {
        return true;
//        if (delegateMTL._participantBookings.get(participantID) == null)
//            return true;
//        Calendar calendar = Calendar.getInstance();
//        int year = Integer.parseInt("20" + eventID.substring(eventID.length() - 2, eventID.length()));
//        int month = Integer.parseInt(eventID.substring(eventID.length() - 4, eventID.length() - 2));
//        int day = Integer.parseInt(eventID.substring(eventID.length() - 6, eventID.length() - 4));
//        calendar.set(year, month, day);
//        int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
//
//        String temp = delegateMTL._participantBookings.get(participantID).get("ArtGallary");
//        String bookings = temp == null ? "" : temp;
//        temp = delegateMTL._participantBookings.get(participantID).get("Concerts");
//        bookings += temp == null ? "" : temp;
//        temp = delegateMTL._participantBookings.get(participantID).get("Theatre");
//        bookings += temp == null ? "" : temp;
//        //System.out.println(bookings);
//        String[] allBookings = bookings.split("-");
//        int bookingsInaWeek = 0;
//
//        for (int i = 0; i < allBookings.length; i++) {
//            if (allBookings[i].substring(0, allBookings[i].length() - 7) == delegateMTL._name) {
//                continue;
//            }
//
//            year = Integer
//                    .parseInt("20" + allBookings[i].substring(allBookings[i].length() - 2, allBookings[i].length()));
//            month = Integer
//                    .parseInt(allBookings[i].substring(allBookings[i].length() - 4, allBookings[i].length() - 2));
//            day = Integer
//                    .parseInt(allBookings[i].substring(allBookings[i].length() - 6, allBookings[i].length() - 4));
//            calendar.set(year, month, day);
//            if (weekNumber == calendar.get(Calendar.WEEK_OF_YEAR)) {
//                ++bookingsInaWeek;
//            }
//
//            if (bookingsInaWeek > 3)
//                return false;
//        }
//
//        return true;
    }
}
