#Read Me
####This module is used to deploy on the edge of the net to provide notificationMessage functionality.
####It uses STOMP to maintain a persistent connection with the users to minimize the expense of connection.
####It is also a rocket mq consumer to get notifications.
This service is scalable and a number of services will perform best when they are in a consistent hashing ring.