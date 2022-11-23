

# Software Failure Tolerant and Highly Available Distributed Ticket Reservation System



## SOEN 423: Distributed Systems

#### Fall 2022


- Smit Desai (40120178)
- Adrien Tremblay (40108982)
- Ekamjot Singh (40106849)
- Efe Harmankaya (40077277)


# **Introduction**


## Distributed Ticket Reservation System

The goal of our past three assignments was to create a DTRS (Distributed Ticket Reservation System) using three different inter-process communication technologies. These technologies were Java RMI, CORBA, and Web Services. For the purpose of this project, three of the four members of our  group will combine their **web services** DTRS applications together, to create one **highly available** and **fault-tolerant **distributed system. In addition, the system will be designed to be tolerant to **simultaneous software failures and process crashes**.


## Desirable Characteristics

Being** highly available** means a distributed system is online and available to clients for a vast majority of the time. To demonstrate this characteristic, we will artificially crash one of the RMs (Replica Managers) and show that this is detected by the Front-End causing this replica manager to be restarted. We will also show that a specific replica can be crashed and that the RM will detect that this replica has crashed, and then restart it.

The related characteristic of **fault/failure-tolerance** means a distributed system is able to behave in a reasonably “correct” or well-defined manner despite software faults (bugs/glitches). To demonstrate this characteristic, we will artificially cause one of the RMs to return incorrect data. In our current design, the system will consider this replica manager as corrupted and kill it.


# **Technologies Used**


## Active Replication

Active replication is an architectural model where requests are sent to all Replica Managers, and replies come from all replica managers. This is contrast to Passive Replication where only a single ‘Master’ Replica Manager is used for all requests and replies until a software-fault or a crash is detected, at which point a backup ‘Slave’ Replica Manager is promoted to be the new ‘Master’. Active Replication helps provide high availability because even if one of the replica managers crashes, the other will still send and receive messages.


## Byzantine Failure Detection Algorithm

In Active Replication, messages are multicast to all Replica Managers, and responses are pooled from all Replica Managers. If we wanted the lowest latency possible, we could just return the response of the first RM to respond. In order to be tolerant of non-malicious Byzantine Failures, the Front End will collect the response of all the Replica Managers, compare them, and only send back the majority answer. So as long as a minority of replica managers are corrupted, the system behaviour will be correct.


## Reliable Multicast

Reliable multicast is used so that we are sure each Replica Manager receives every message. If a RM were not to receive a message, while others did receive it, this would cause different states of each RM. Using reliable multicast helps provide fault-tolerance.


## Replication

Replication is a key technique used in distributed systems to help support a number of desirable characteristics. Replication is when the exact same data (and sometimes processes) are replicated on multiple distributed nodes. 


## UDP

It stands for User Datagram Protocol and is used for network communication across the global internet. This communication is connectionless, i.e the client-server does not need a connection established to exchange data. The client only requires the server's IP address and port number to send a request. The server, on the other hand, listens for incoming requests on the registered port. The data transmitted is broken down into smaller datagrams which contain the source and destination information, headers and the actual data (body). UDP requires less overhead and is faster than TCP. However, 

- UDP is not reliable 

- UDP does-not provide ordered delivery 

- UDP has no flow and congestion control 

- UDP does-not provide error handling mechanisms


## SOAP

It stands for Simple Object Access Protocol and is used for exchanging data in the form of webservices in a distributed application. SOAP messages are written entirely in XML and are platform and language independent.


# System Architecture

![](System-Architecture-Images/System-Design.png)



## **Data-Flow Diagram**

![](System-Architecture-Images/Data-Flow-Diagram.png)



## Client

For this project, the client process will simply be a console application where users can specify the details of the operations they wish to perform. These requests are then relayed to the Front End using SOAP web services.


## Front End

The front-end is a middleware between the system and external agents which want to use the system's services. In the context of this project, the services provided are the various ticket reservation operations described in the assignment specifications. The Front End communicates with clients using SOAP web services. Upon receiving a Client request, the Front End forwards it to the sequencer to ensure total ordering. The Front End waits for the 3 responses. It then chooses the majority answer and forwards it to the client, if one of them is faulty, it informs the respective Replica Manger about it


## Sequencer

The sequencer attaches a unique sequence number to each request and then multicasts it to the Replica Managers. Requests are processed in order according to the sequence numbers by each individual RM. This prevents RMs from having different states due to requests arriving in different orders across the network. Using the sequences helps provide fault-tolerance.


## Replica Manager

The replica manager is the middleman between replicas and Front End. They are responsible for failure detection and recovery. The Replica Manager receives requests from the sequencer, checks for duplicate requests using the sequence number and then forwards it to the respective replica via UDP. Upon receiving the response from the replica, it sends it to the Front End using UDP. Ideally, replica managers should act as state machines meaning that their state (data content) is completely deterministic given the inputs it has been given. So in our design, since we send the exact same requests to all replica managers, theoretically their state should be identical. 


## Replicas

An individual replica represents an atomic replicated copy of a system component. In the context of this project, a replica represents each individual server process for Montreal, Toronto, or Vancouver. In the overall system there are multiple different replicas for each of the server city locations (amongst the different Replica Managers). This helps support high-availability and fault-tolerance because if there is a problem with one of the replicas, there are others that can theoretically be used in its place.


## **Sequence Diagram**



![](System-Architecture-Images/Activity-Diagram.png)

The above sequence diagram shows the flow for detecting faulty replicas and responding accordingly.


# Team and Individual Tasks


<table>
  <tr>
   <td><strong>Student ID</strong>
   </td>
   <td><strong>Name</strong>
   </td>
   <td><strong>Task</strong>
   </td>
  </tr>
  <tr>
   <td>40120178
   </td>
   <td>Smit Desai
   </td>
   <td>Front End
   </td>
  </tr>
  <tr>
   <td>40108982
   </td>
   <td>Adrien Tremblay
   </td>
   <td>Replica Manager + Replica
   </td>
  </tr>
  <tr>
   <td>40106849
   </td>
   <td>Ekamjot Singh
   </td>
   <td>Sequencer
   </td>
  </tr>
  <tr>
   <td>40077277
   </td>
   <td> Efe Harmankaya
   </td>
   <td>Client + Test Cases
   </td>
  </tr>
</table>



# **Test Scenarios**

    Test Case 1.  List available reservations 

    Result Expected: Should print all the available reservations successfully.

    Result Obtained: 

    Test Case 2. Add an new event slot 

    Result Expected: The new event slot should be added successfully. 

    Result Obtained: 

    Test Case 3. Add the same event again

    Result Expected: The event should not be added.

    Result Obtained: 

    Test Case 4. Book an event slot

    Result Expected: The user could book the slot successfully. 

    Result Obtained: 

    Test Case 5. Book an same event slot again

    Result Expected: The slot should not be booked again.

    Result Obtained: 

    Test Case 6. Cancel Ticket (with valid ticket)

    Result Expected: The tickets should be cancelled successfully.

    Result Obtained: 

    Test Case 7. Cancel Ticket (with invalid ticket)

    Result Expected: The tickets should not be cancelled and the proper message

    Should be either logged and/or displayed to the user.

    Result Obtained: 

    Test Case 8. Exchange tickets (with valid tickets)

    Result Expected: should successfully exchange tickets 

    Result Obtained: 

    Test Case 9. Exchange tickets (with invalid tickets)

    Result Expected: should not exchange tickets.

    Result Obtained: 

    Test Case 10: Software Failure Detection

    Result Expected: The software failure should be detected and the replica should be killed.

    Result Obtained: 

    Test Case 11: Software Crash Detection

    Result Expected: The Software Crash should be detected and the crashed replica should 

    be restarted.

    Result Obtained: 


# Sources



* [http://book.mixu.net/distsys/intro.html](http://book.mixu.net/distsys/intro.html)


## Work Distribution:

- Smit: 
    * Frontend

- Ekamjot:
    * Sequencer

- Adrien: 
    * Refactor Replica
        * Also use String instead of enums
    * Replica Manager

Other work:

* Client
* Setting up LAN


## Refactor Replica:



* Replace the SOAP interface with UDP server
* Note: There will be 2 UDP servers running concurrently on a replica
    * One for inter-server communication
    * Second for RM - Replica communication
* Standard for writing event types:
    * Event Type:
        * ArtGallery
        * Concerts
        * Theatre
* jsonFormat
```
    {
    	methodName: xxx,
    	paramName: value,
    	paramNam2: etc…,
    }
```


#### Replica Function Signatures
```
{
    “MethodName”: “addReservationSlot”,
    “adminID”: “String”,
    “eventType”: “String”,
    “eventID”: “String”,
    “capacity”: “int”,
},

{
    “MethodName”: “removeReservationSlot”,
    “adminID”: “String”,
    “eventType”: “String”,
    “eventID”: “String”,
},

{
    “MethodName”: “listReservationSlotAvailable”,
    “adminID”: “String”,
    “eventType”: “String”,
},

{
    “MethodName”: “reserveTicket”,
    “participantID”: “String”,
    “eventType”: “String”,
    “eventID”: “String”,
},

{
    “MethodName”: “getEventSchedule”,
    “participantID”: “String”,
},

{
    “MethodName”: “cancelTicket”,
    “participantID”: “String”,
    “eventType”: “String”,
    “eventID”: “String”,
},

{
    “MethodName”: “exchangeTicket”,
    “participantID”: “String”,
    “eventType”: “String”,
    “eventID”: “String”,
    “new_eventType”: “String”,
    “new_eventID”: “String”,
},

```
#### Replica Function Return Type:

```
{
    “Success” : “Boolean”,
    “Data” : “String”
}
```

## Replica Manager:

* Create a UDP Client and Server
* Integration with replicas
* Take in input from sequencer 
    * Determine which replica to send to
    * Send an ACK back to sequencer
* Send back results to FE
* Failure Recovery ???
    * Restart servers/replicas -> linux command
    * Data recovery
* Execute requests in order according to sequence


## Front-End:

* Create a UDP Client and Server
* Handle web-service requests from client
* Create a message to send to the sequencer
* Receive responses from RM
* Failure detection
    * Timeout
    * Incorrect result
* Send back the correct data to Client


## Sequencer:

* Add sequence # to request
* Multicast to RM
* If received incorrect ACK, then resend that packet


## Meeting notes:

* Use existing client implementation from one of the member’s codebase (just change the endpoint to the FE)
* Each person will have to refactor their replica code to work with UDP instead of web services
* Need to agree upon a common interface (or function signature and return type) for communicating with the FE
    * Return values need to be the same for comparison purpose for operations like getEventSchedule, getReservations
        * But we could also just use the other ops that return booleans for the byzantine fault detection stuff
* Each person need to implement a function in their copy of RM that maps the return type of their replica to the return type required by the FE