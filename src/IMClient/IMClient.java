package IMClient;
/*
IMClient.java - Instant Message client using UDP and TCP communication.

Text-based communication of commands.
*/

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class IMClient {
	// Protocol and system constants
	public static String serverAddress = "localhost";
	public static int TCPServerPort = 1234;					// connection to server
	public static int UDPServerPort = 1235;					// UDP server port
	
	/* 	
	 * This value will need to be unique for each client you are running
	 */
	public static int TCPMessagePort = 1247;				// port for connection between 2 clients
	
	public static String onlineStatus = "100 ONLINE";
	public static String offlineStatus = "101 OFFLINE";

	private BufferedReader reader;							// Used for reading from standard input

	// Client state variables
	private String userId;
	private String status;
	
	// Store buddy list information
	private ArrayList<BuddyStatusRecord> buddyList;
	
	// Threads for UDP and TCP communication
	private Thread udpThread;
	private Thread tcpWelcomeThread;
	
	// UDP socket
	private DatagramSocket udpSocket;
	
	// TCP welcome socket
	private ServerSocket welcomeSocket;
	
	// Flag to control thread execution
	private boolean running;

	public static void main(String []argv) throws Exception
	{
		IMClient client = new IMClient();
		client.execute();
	}

	public IMClient()
	{
		// Initialize variables
		userId = null;
		status = null;
		buddyList = new ArrayList<BuddyStatusRecord>();
		running = true;
		
		try {
			// Create UDP socket for sending and receiving
			udpSocket = new DatagramSocket();
			// Set timeout so UDP receive doesn't block forever
			udpSocket.setSoTimeout(5000);
		} catch (Exception e) {
			System.out.println("Error creating UDP socket: " + e);
		}
	}


	public void execute()
	{
		initializeThreads();

		String choice;
		reader = new BufferedReader(new InputStreamReader(System.in));

		printMenu();
		choice = getLine().toUpperCase();

		while (!choice.equals("X"))
		{
			if (choice.equals("Y"))
			{	// Must have accepted an incoming connection
				acceptConnection();
			}
			else if (choice.equals("N"))
			{	// Must have rejected an incoming connection
				rejectConnection();
			}
			else if (choice.equals("R"))				// Register
			{	registerUser();
			}
			else if (choice.equals("L"))		// Login as user id
			{	loginUser();
			}
			else if (choice.equals("A"))		// Add buddy
			{	addBuddy();
			}
			else if (choice.equals("D"))		// Delete buddy
			{	deleteBuddy();
			}
			else if (choice.equals("S"))		// Buddy list status
			{	buddyStatus();
			}
			else if (choice.equals("M"))		// Start messaging with a buddy
			{	buddyMessage();
			}
			else
				System.out.println("Invalid input!");

			printMenu();
			choice = getLine().toUpperCase();
		}
		shutdown();
	}

	private void initializeThreads()
	{
		// Start UDP thread for sending status and receiving buddy list
		udpThread = new Thread(new UDPCommunicator(this));
		udpThread.start();
		
		// Start TCP welcome thread for accepting incoming connections
		tcpMessenger = new TCPMessenger(this);
		tcpWelcomeThread = new Thread(tcpMessenger);
		tcpWelcomeThread.start();
	}

	private void registerUser()
	{	
		// Register user id
		System.out.print("Enter user id: ");
		userId = getLine();
		System.out.println("Registering user id: " + userId);
		
		// Send TCP message to server using format: REG [userid]
		String message = "REG " + userId;
		String response = sendTCPMessage(message);
		System.out.println(response);
		
		// If registration successful (response starts with "200"), set status to online
		if (response != null && response.startsWith("200")) {
			status = onlineStatus;
		}
	}

	private void loginUser()
	{	// Login an existing user (no verification required - just set userId to input)
		System.out.print("Enter user id: ");
		userId = getLine();
		System.out.println("User id set to: "+userId);
		status = onlineStatus;
	}

	private void addBuddy()
	{	
		// Add buddy if have current user id
		// Check if user is logged in first
		if (userId == null) {
			System.out.println("Must register or login first!");
			return;
		}
		
		System.out.print("Enter buddy id: ");
		String buddyId = getLine();
		
		// Send TCP message to server using format: ADD [userid] [buddyid]
		String message = "ADD " + userId + " " + buddyId;
		String response = sendTCPMessage(message);
		System.out.println(response);
	}

	private void deleteBuddy()
	{	
		// Delete buddy if have current user id
		// Check if user is logged in first
		if (userId == null) {
			System.out.println("Must register or login first!");
			return;
		}
		
		System.out.print("Enter buddy id: ");
		String buddyId = getLine();
		
		// Send TCP message to server using format: DEL [userid] [buddyid]
		String message = "DEL " + userId + " " + buddyId;
		String response = sendTCPMessage(message);
		System.out.println(response);
	}

	private void buddyStatus()
	{	// Print out buddy status (need to store state in instance variable that received from previous UDP message)
		System.out.println("My buddy list:");
		for (BuddyStatusRecord buddy : buddyList) {
			System.out.println(buddy.toString());
		}
	}

	private void buddyMessage()
	{	// Make connection to a buddy that is online
		// Must verify that they are online and should prompt to see if they accept the connection
		if (userId == null) {
			System.out.println("Must register or login first!");
			return;
		}
		
		System.out.print("Enter buddy id: ");
		String buddyId = getLine();
		
		// Find buddy in list
		BuddyStatusRecord buddy = null;
		for (BuddyStatusRecord b : buddyList) {
			if (b.buddyId.equals(buddyId)) {
				buddy = b;
				break;
			}
		}
		
		if (buddy == null) {
			System.out.println("Buddy not found in list!");
			return;
		}
		
		if (!buddy.isOnline()) {
			System.out.println("Buddy is not online!");
			return;
		}
		
		// Try to connect to buddy
		try {
			System.out.println("Attempting to connect...");
			Socket socket = new Socket(buddy.IPaddress, Integer.parseInt(buddy.buddyPort));
			
			BufferedReader inFromBuddy = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			DataOutputStream outToBuddy = new DataOutputStream(socket.getOutputStream());
			
			// Wait for acceptance message
			String response = inFromBuddy.readLine();
			if (response != null && response.equals("ACCEPT")) {
				System.out.println("Buddy accepted connection.");
				System.out.println("Enter your text to send to buddy.  Enter q to quit.");
				
				// Start thread to receive messages
				Thread receiveThread = new Thread(() -> {
					try {
						String message;
						while ((message = inFromBuddy.readLine()) != null) {
							System.out.println("\nB: " + message);
							System.out.print("> ");
						}
					} catch (Exception e) {
						// Connection closed
					}
				});
				receiveThread.start();
				
				// Send messages
				String message = "";
				while (!message.equals("q")) {
					System.out.print("> ");
					message = getLine();
					if (!message.equals("q")) {
						outToBuddy.writeBytes(message + '\n');
					}
				}
				
				// Close connection
				socket.close();
				System.out.println("Buddy connection closed.");
			} else {
				System.out.println("Buddy rejected connection.");
				socket.close();
			}
		} catch (Exception e) {
			System.out.println("Error connecting to buddy: " + e.getMessage());
		}
	}

	private void shutdown()
	{	// Close down client and all threads
		System.out.println("Shutting down...");
		
		// Set status to offline before shutting down
		status = offlineStatus;
		
		// Stop threads
		running = false;
		
		// Close sockets
		try {
			if (udpSocket != null) {
				udpSocket.close();
			}
			if (welcomeSocket != null) {
				welcomeSocket.close();
			}
		} catch (Exception e) {
			System.out.println("Error closing sockets: " + e);
		}
		
		System.out.println("Goodbye!");
	}

	private void acceptConnection()
	{	// User pressed 'Y' on this side to accept connection from another user
		// Send confirmation to buddy over TCP socket
		// Enter messaging mode
		System.out.println("Connection accepted.");
		System.out.println("Enter your text to send to buddy.  Enter q to quit.");
		
		try {
			Socket socket = tcpMessenger.getCurrentConnection();
			if (socket == null) {
				System.out.println("No pending connection!");
				return;
			}
			
			BufferedReader inFromBuddy = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			DataOutputStream outToBuddy = new DataOutputStream(socket.getOutputStream());
			
			// Send acceptance message
			outToBuddy.writeBytes("ACCEPT\n");
			
			// Start thread to receive messages
			Thread receiveThread = new Thread(() -> {
				try {
					String message;
					while ((message = inFromBuddy.readLine()) != null) {
						System.out.println("\nB: " + message);
						System.out.print("> ");
					}
				} catch (Exception e) {
					// Connection closed
				}
			});
			receiveThread.start();
			
			// Send messages
			String message = "";
			while (!message.equals("q")) {
				System.out.print("> ");
				message = getLine();
				if (!message.equals("q")) {
					outToBuddy.writeBytes(message + '\n');
				}
			}
			
			// Close connection
			socket.close();
			System.out.println("Buddy connection closed.");
			
		} catch (Exception e) {
			System.out.println("Error in connection: " + e.getMessage());
		}
	}

	private void rejectConnection()
	{	// User pressed 'N' on this side to decline connection from another user
		// Send no message over TCP socket then close socket
		try {
			Socket socket = tcpMessenger.getCurrentConnection();
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {
			System.out.println("Error rejecting connection: " + e.getMessage());
		}
	}
	
	// Helper method to send TCP message to server
	// Returns the server's response as a string
	private String sendTCPMessage(String message) {
		try {
			// Create new TCP connection to server at port 1234
			Socket socket = new Socket(serverAddress, TCPServerPort);
			
			// Get output and input streams
			DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// Send message to server (add newline at end)
			outToServer.writeBytes(message + '\n');
			
			// Read response from server
			String response = inFromServer.readLine();
			
			// Close connection (each TCP request uses new connection)
			socket.close();
			
			return response;
		} catch (Exception e) {
			System.out.println("Error sending TCP message: " + e.getMessage());
			return null;
		}
	}
	
	// Getter methods for threads to access client state
	public String getUserId() {
		return userId;
	}
	
	public String getStatus() {
		return status;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void updateBuddyList(ArrayList<BuddyStatusRecord> newList) {
		buddyList = newList;
	}
	
	public DatagramSocket getUDPSocket() {
		return udpSocket;
	}
	
	// Store reference to TCP messenger for accept/reject
	private TCPMessenger tcpMessenger;

	private String getLine()
	{	// Read a line from standard input
		String inputLine = null;
		  try{
			  inputLine = reader.readLine();
		  }catch(IOException e){
			 System.out.println(e);
		  }
	 	 return inputLine;
	}

	private void printMenu()
	{	System.out.println("\n\nSelect one of these options: ");
		System.out.println("  R - Register user id");
		System.out.println("  L - Login as user id");
		System.out.println("  A - Add buddy");
		System.out.println("  D - Delete buddy");
		System.out.println("  M - Message buddy");
		System.out.println("  S - Buddy status");
		System.out.println("  X - Exit application");
		System.out.print("Your choice: ");
	}

}

// A record structure to keep track of each individual buddy's status
class BuddyStatusRecord
{	public String IPaddress;
	public String status;
	public String buddyId;
	public String buddyPort;

	public String toString()
	{	return buddyId+"\t"+status+"\t"+IPaddress+"\t"+buddyPort; }

	public boolean isOnline()
	{	return status.indexOf("100") >= 0; }
}

// This class implements the TCP welcome socket for other buddies to connect to.
// It runs in a separate thread and listens for incoming connections from other clients

class TCPMessenger implements Runnable
{
	private IMClient client;
	private ServerSocket welcomeSocket;
	private Socket currentConnection;  // Store the current pending connection

	public TCPMessenger(IMClient c)
	{	
		client = c;
		currentConnection = null;
		
		// Create welcome socket to listen for incoming buddy connections
		try {
			welcomeSocket = new ServerSocket(IMClient.TCPMessagePort);
		} catch (Exception e) {
			System.out.println("Error creating TCP welcome socket: " + e);
		}
	}

    public void run()
	{
		// This thread starts an infinite loop looking for TCP connection requests
		try
		{
			while (client.isRunning())
			{
		    	// Listen and wait for a TCP connection request from another buddy
		    	Socket connection = welcomeSocket.accept();
		    	
		    	// Store this connection so main thread can access it
		    	currentConnection = connection;

		    	// Prompt user to accept or reject the connection
		    	// Note: The actual input reading happens in the main menu thread
		    	System.out.print("\nDo you want to accept an incoming connection (y/n)? ");
			}
	    }
		catch (Exception e)
		{	
			// Socket closed or error - this is expected when shutting down
		}
	}
	
	// Method to get the current pending connection
	public Socket getCurrentConnection() {
		return currentConnection;
	}
}

// This class handles UDP communication with the server
// It runs in a separate thread and sends status updates every 10 seconds
class UDPCommunicator implements Runnable
{
	private IMClient client;
	
	public UDPCommunicator(IMClient c)
	{
		client = c;
	}
	
	public void run()
	{
		// Loop every 10 seconds to send status and get buddy list
		while (client.isRunning())
		{
			try {
				// Sleep for 10 seconds before next update
				Thread.sleep(10000);
				
				// Only send if user is logged in
				if (client.getUserId() != null && client.getStatus() != null) {
					// Send status update to server
					sendStatusUpdate();
					
					// Request updated buddy list from server
					requestBuddyList();
				}
			} catch (Exception e) {
				// Thread interrupted or error
			}
		}
	}
	
	private void sendStatusUpdate()
	{
		try {
			// Build SET message: SET [userid] [status] [msgport]
			// Example: SET scott1 100 ONLINE 1248
			String message = "SET " + client.getUserId() + " " + client.getStatus() + " " + IMClient.TCPMessagePort;
			
			// Convert message to bytes
			byte[] sendData = message.getBytes();
			
			// Get server address
			InetAddress serverAddress = InetAddress.getByName(IMClient.serverAddress);
			
			// Create UDP packet with data, length, destination address, and port
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, IMClient.UDPServerPort);
			
			// Send the packet through UDP socket
			client.getUDPSocket().send(sendPacket);
			
		} catch (Exception e) {
			System.out.println("Error sending status update: " + e.getMessage());
		}
	}
	
	private void requestBuddyList()
	{
		try {
			// Build GET message: GET [userid]
			// Example: GET scott1
			String message = "GET " + client.getUserId();
			
			// Convert message to bytes and send
			byte[] sendData = message.getBytes();
			InetAddress serverAddress = InetAddress.getByName(IMClient.serverAddress);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, IMClient.UDPServerPort);
			client.getUDPSocket().send(sendPacket);
			
			// Prepare to receive response
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			try {
				// Wait to receive response (will timeout after 5 seconds - set in constructor)
				client.getUDPSocket().receive(receivePacket);
				
				// Convert received bytes to string
				String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
				
				// Parse the buddy list from response
				parseBuddyList(response);
			} catch (SocketTimeoutException e) {
				// Timeout, no response - that's okay, try again in 10 seconds
			}
			
		} catch (Exception e) {
			System.out.println("Error requesting buddy list: " + e.getMessage());
		}
	}
	
	private void parseBuddyList(String response)
	{
		// Parse buddy list response
		// Format: [buddyId] [buddyStatus] [buddyIP] [buddyPort]
		// Example: scott2  100 ONLINE      127.0.0.1       1248
		ArrayList<BuddyStatusRecord> buddyList = new ArrayList<BuddyStatusRecord>();
		
		// Split response into lines (one buddy per line)
		String[] lines = response.split("\n");
		for (String line : lines) {
			// Split each line by whitespace
			String[] parts = line.trim().split("\\s+");
			
			// Need at least 5 parts: buddyId, status_code, status_text, IP, port
			if (parts.length >= 5) {
				BuddyStatusRecord buddy = new BuddyStatusRecord();
				buddy.buddyId = parts[0];
				buddy.status = parts[1] + " " + parts[2];  // e.g., "100 ONLINE"
				buddy.IPaddress = parts[3];
				buddy.buddyPort = parts[4];
				buddyList.add(buddy);
			}
		}
		
		// Update client's buddy list with new information
		client.updateBuddyList(buddyList);
	}
}