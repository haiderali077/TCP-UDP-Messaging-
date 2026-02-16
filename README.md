# 🌐 TCP/UDP Network Messaging System

[![Java](https://img.shields.io/badge/Java-18+-orange.svg)](https://www.oracle.com/java/)
[![Network Programming](https://img.shields.io/badge/Networking-TCP%20%7C%20UDP-blue.svg)]()
[![Multithreading](https://img.shields.io/badge/Concurrency-Multithreaded-green.svg)]()

A comprehensive network programming project demonstrating advanced socket programming, protocol design, and real-time messaging capabilities using both TCP and UDP protocols in Java.

---

## 🎯 Project Overview

This project showcases a complete instant messaging (IM) system built from scratch using low-level socket programming. It demonstrates proficiency in network protocols, concurrent programming, client-server architecture, and real-time communication systems.

### Key Features

- **Dual-Protocol Communication**: Seamlessly integrates TCP for reliable messaging and UDP for lightweight status updates
- **Multi-threaded Architecture**: Handles concurrent connections with multiple clients using thread-based design
- **Peer-to-Peer Messaging**: Direct client-to-client communication without server intermediation
- **Real-time Status Updates**: Live online/offline buddy status monitoring
- **Custom Protocol Implementation**: Designed and implemented a complete application-layer protocol
- **Scalable Server Design**: Thread-per-connection model supporting multiple simultaneous users

---

## 🏗️ Architecture & Design

### System Components

```
┌─────────────┐         TCP/UDP          ┌─────────────┐
│             │◄──────────────────────────►│             │
│  IM Client  │                            │  IM Server  │
│             │         Port 1234/1235     │             │
└──────┬──────┘                            └─────────────┘
       │                                          
       │ Direct TCP                              
       │ (Peer-to-Peer)                         
       ▼                                          
┌─────────────┐                                  
│  IM Client  │                                  
│   (Buddy)   │                                  
└─────────────┘                                  
```

### Protocol Design

| Operation | Protocol | Port | Description |
|-----------|----------|------|-------------|
| User Registration | TCP | 1234 | Register new user with server |
| Add/Delete Buddy | TCP | 1234 | Manage buddy list operations |
| Status Updates | UDP | 1235 | Broadcast online/offline status |
| Buddy Status Query | UDP | 1235 | Request all buddies' current status |
| Peer Messaging | TCP | Dynamic | Direct client-to-client messages |

### Response Codes

```
200 OK           - Request successful
201 INVALID      - Malformed request syntax
202 NO SUCH USER - User not found in database
203 DUPLICATE    - User already registered
```

---

## 💻 Technical Skills Demonstrated

### Network Programming
- **Socket Programming**: Low-level TCP and UDP socket implementation
- **Connection Management**: Establishing, maintaining, and closing network connections
- **Protocol Design**: Custom application-layer protocol with command parsing
- **Port Management**: Dynamic port allocation and multi-port communication

### Concurrent Programming
- **Multithreading**: Parallel execution for handling multiple connections
- **Thread Synchronization**: Safe access to shared resources (buddy lists, status)
- **Non-blocking I/O**: Simultaneous handling of multiple client requests
- **Welcome Socket Pattern**: Dedicated thread for accepting incoming connections

### Software Engineering
- **Object-Oriented Design**: Clean class hierarchies and encapsulation
- **Error Handling**: Robust exception management for network failures
- **State Management**: Maintaining client state (online/offline, buddy lists)
- **Code Organization**: Package-based structure with clear separation of concerns

---

## 🚀 Quick Start Guide

### Prerequisites

- **Java Development Kit (JDK)** 8 or higher
- **Terminal/Command Prompt** access
- Basic understanding of networking concepts

### Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone <your-repository-url>
   cd TCP-UDP-Messaging-
   ```

2. **Compile All Java Files**
   ```bash
   # Compile from the root directory
   javac src/IMServer/IMServer.java
   javac src/IMClient/IMClient.java
   javac src/tcp_client_server/*.java
   javac src/udp_client_server/*.java
   ```

---

## 📖 Usage Examples

### Running the Instant Messaging System

#### Step 1: Start the IM Server
```bash
cd src
java IMServer.IMServer
```
The server will start listening on:
- TCP Port **1234** for client registration and buddy management
- UDP Port **1235** for status updates and queries

#### Step 2: Launch First IM Client
```bash
cd src
java IMClient.IMClient
```

**Sample Interaction:**
```
Enter your user id: alice
Enter command (register, addbuddy, delbuddy, online, offline, status, msg, quit): register
Registration successful!

Enter command: online
Status updated to ONLINE

Enter command: addbuddy
Enter buddy user id: bob
Buddy added successfully!
```

#### Step 3: Launch Second IM Client (Different Terminal)
```bash
cd src
java IMClient.IMClient
```

**Configure Different Message Port** (Edit IMClient.java):
```java
public static int TCPMessagePort = 1248;  // Different from first client (1247)
```

**Sample Interaction:**
```
Enter your user id: bob
Enter command: register
Registration successful!

Enter command: online
Status updated to ONLINE

Enter command: status
Checking buddy status...
alice - ONLINE (192.168.1.100:1247)

Enter command: msg
Enter buddy user id to message: alice
Connected to alice. Type your messages:
> Hello Alice!
```

---

### Running Basic TCP Examples

#### TCP Echo Server & Client

**Terminal 1 - Start Server:**
```bash
cd src
java tcp_client_server.TCPServer
# Server listening on port 6789...
```

**Terminal 2 - Start Client:**
```bash
cd src
java tcp_client_server.TCPClient
# Type a message and press Enter
Hello World!
# FROM SERVER: HELLO WORLD!
```

**What This Demonstrates:**
- TCP connection establishment (3-way handshake)
- Reliable, ordered data transmission
- Request-response pattern
- Graceful connection closure

---

### Running Basic UDP Examples

#### UDP Echo Server & Client

**Terminal 1 - Start Server:**
```bash
cd src
java udp_client_server.UDPServer
# Server ready on port 9876...
```

**Terminal 2 - Start Client:**
```bash
cd src
java udp_client_server.UDPClient
# Type a message and press Enter
Hello UDP!
# FROM SERVER: HELLO UDP!
```

**What This Demonstrates:**
- Connectionless datagram communication
- Lower latency vs TCP
- Fire-and-forget messaging
- Best-effort delivery (no guaranteed order)

---

## 🧪 Testing Scenarios

### Scenario 1: Two-User Chat Session
1. Register two users (alice, bob) on separate clients
2. Both users go online
3. Alice adds Bob as a buddy
4. Bob adds Alice as a buddy
5. Check status to verify both are online
6. Initiate peer-to-peer chat from either client
7. Exchange messages in real-time

### Scenario 2: Status Monitoring
1. Start server and 3+ clients
2. Register all users and add each other as buddies
3. Have users go online/offline randomly
4. Use status command to verify buddy list updates correctly
5. Observe UDP status broadcasts

### Scenario 3: Connection Recovery
1. Start server and 2 clients
2. Establish chat connection
3. Abruptly close one client
4. Verify server handles disconnection gracefully
5. Reconnect client and resume operations

---

## 📂 Project Structure

```
TCP-UDP-Messaging-/
├── src/
│   ├── IMClient/
│   │   ├── IMClient.java              # Main IM client application
│   │   └── BuddyStatusRecord.java     # Buddy data structure
│   │
│   ├── IMServer/
│   │   ├── IMServer.java              # Main IM server application
│   │   ├── StatusRecord.java          # User status tracking
│   │   ├── UDPProcessor.java          # UDP request handler
│   │   └── TCPProcessor.java          # TCP request handler
│   │
│   ├── tcp_client_server/
│   │   ├── TCPServer.java             # Basic TCP echo server
│   │   ├── TCPClient.java             # Basic TCP client
│   │   ├── TCPServer2.java            # Enhanced TCP server
│   │   ├── TCPClient2.java            # Enhanced TCP client
│   │   └── TCPServerThread.java       # Multi-threaded server
│   │
│   └── udp_client_server/
│       ├── UDPServer.java             # Basic UDP echo server
│       ├── UDPClient.java             # Basic UDP client
│       ├── UDPServer2.java            # Enhanced UDP server
│       └── UDPClient2.java            # Enhanced UDP client
│
├── exampleOutput1.txt                 # Sample execution output
├── exampleOutput2.txt                 # Additional test output
└── README.md                          # Original assignment documentation
```

---

## 🔧 Configuration

### Modifying Ports and Addresses

**In IMClient.java:**
```java
public static String serverAddress = "localhost";  // Change for remote server
public static int TCPServerPort = 1234;            // TCP control port
public static int UDPServerPort = 1235;            // UDP status port
public static int TCPMessagePort = 1247;           // P2P messaging port (must be unique per client)
```

**In IMServer.java:**
```java
private static int TCPWelcomePort = 1234;          // TCP listening port
private static int UDPPort = 1235;                 // UDP listening port
```

### Running Multiple Clients Locally

To run multiple clients on the same machine:
1. **Option A**: Manually set different `TCPMessagePort` values in IMClient.java
   ```java
   public static int TCPMessagePort = 1247;  // Client 1
   public static int TCPMessagePort = 1248;  // Client 2
   public static int TCPMessagePort = 1249;  // Client 3
   ```

2. **Option B**: Use ephemeral ports (random available port)
   ```java
   welcomeSocket = new ServerSocket(0);  // 0 = auto-assign port
   ```

---

## 🎓 Learning Outcomes

This project demonstrates understanding of:

✅ **Transport Layer Protocols**
- TCP: connection-oriented, reliable, ordered delivery
- UDP: connectionless, fast, best-effort delivery
- When to use each protocol based on application requirements

✅ **Client-Server Architecture**
- Centralized server for coordination
- Decentralized peer-to-peer messaging
- Hybrid architecture combining both models

✅ **Concurrency & Threading**
- Handling multiple simultaneous connections
- Thread-safe data structures
- Blocking vs non-blocking I/O

✅ **Application Layer Design**
- Custom protocol specification
- Command parsing and validation
- Error handling and response codes

✅ **Real-world Networking Challenges**
- Port conflicts and management
- Network address translation (NAT) considerations
- Connection timeouts and error recovery

---

## 🐛 Troubleshooting

### Common Issues

**Problem:** `Address already in use` error
```
Solution: Port is occupied. Change the port number or kill the process:
  lsof -i :1234          # Find process using port
  kill -9 <PID>          # Terminate the process
```

**Problem:** `Connection refused`
```
Solution: Ensure server is running before starting clients
  1. Start IMServer first
  2. Wait for "Server started" message
  3. Then launch IMClient
```

**Problem:** Can't connect to buddy for messaging
```
Solution: Verify both clients are registered and online
  1. Both users must run "register" command
  2. Both users must run "online" command
  3. Each must add the other as a buddy
  4. Use "status" to confirm buddy is online
```

**Problem:** Clients on same machine can't message each other
```
Solution: Ensure different TCPMessagePort for each client
  Client 1: TCPMessagePort = 1247
  Client 2: TCPMessagePort = 1248
  Client 3: TCPMessagePort = 1249
```

---

## 🔬 Advanced Topics Explored

### Why Both TCP and UDP?

**TCP for Critical Operations:**
- User registration (must succeed)
- Buddy list management (requires acknowledgment)
- Peer-to-peer messaging (guaranteed delivery)

**UDP for Performance:**
- Status updates (frequency matters more than reliability)
- Buddy status queries (can tolerate occasional packet loss)
- Lower overhead for frequent, non-critical updates

### Thread Architecture

```
Main Thread
  ├── TCP Welcome Thread (accepts incoming peer connections)
  │
  └── UDP Listener Thread (receives status broadcasts)
```

---

## � Conclusion

This project demonstrates a comprehensive understanding of network programming fundamentals, showcasing the practical implementation of TCP/UDP protocols, multi-threaded server architecture, and real-time communication systems. Through building this instant messaging system from scratch, core concepts of client-server architecture, socket programming, and concurrent systems design have been thoroughly explored.

The implementation serves as both a learning resource for network programming concepts and a demonstration of practical software engineering skills in Java.

---

*Thank you for exploring this project!*
