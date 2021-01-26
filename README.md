# tcp-chat

Console TCP chat client and server with registration, login, broadcast and private messages, file transfer between users
and more  

Commands:

```
register <username> <password>
login <username> <password>
send-msg "<message>"
send-msg-to <username> "<message>"
send-file-to <username> "<path to file>"
```

Links that are sent in messages are shortened via cutt.ly REST API.

How does it work in a nutshell:
Each client uses two sockets - one for file trasfer and one for text trasfer. Each clients spawns up to 4 Threads:
One thread that takes console input and sends commands to server, one that handles incoming messages from the server and 2 threads for sending and receving files.

The server spawns a new ClientRequestHadler thread for each new client that connects to it. The ClientRequestHandler uses up to two threads. One for handling incoming messages and dispatching the messages to the users, and one for file transfer.
