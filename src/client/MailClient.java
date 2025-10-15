package client;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MailClient {
    private static final String SERVER_IP = "192.168.56.101";
    private static final int SERVER_PORT = 9999;
    private static final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        DatagramSocket socket = new DatagramSocket();

        String startTime = LocalDateTime.now().format(timestampFormatter);
        System.out.println("=== MAIL CLIENT (UDP) ===");
        System.out.println("Started at: " + startTime);
        System.out.println("1. Register Account");
        System.out.println("2. Login");
        System.out.println("3. Send Email");
        System.out.print("Choose: ");
        int choice = sc.nextInt(); sc.nextLine();

        if (choice == 1) {
            // Register Account
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();
            System.out.print("Enter password: ");
            String password = sc.nextLine().trim();
            sendMessage(socket, "REGISTER:" + username + ":" + password);
            String timestamp = LocalDateTime.now().format(timestampFormatter);
            System.out.println("[" + timestamp + "] ✅ Registration request sent!");
        } 
        else if (choice == 2) {
            // Login
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();
            System.out.print("Enter password: ");
            String password = sc.nextLine().trim();
            sendMessage(socket, "LOGIN:" + username + ":" + password);
            
            // Wait for server response
            byte[] buffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            
            String loginTimestamp = LocalDateTime.now().format(timestampFormatter);
            if ("LOGIN_SUCCESS".equals(response)) {
                System.out.println("[" + loginTimestamp + "] ✅ Login successful!");
            } else {
                System.out.println("[" + loginTimestamp + "] ❌ Login failed! Invalid username or password.");
            }
        }
        else if (choice == 3) {
            // Send Email
            System.out.print("Your username: ");
            String sender = sc.nextLine().trim();
            System.out.print("Your password: ");
            String password = sc.nextLine().trim();
            System.out.print("Receiver username: ");
            String receiver = sc.nextLine().trim();
            System.out.print("Subject: ");
            String subject = sc.nextLine().trim();
            System.out.print("Message: ");
            String content = sc.nextLine().trim();

            String emailMsg = String.format("SEND:%s:%s:%s:%s:%s", sender, password, receiver, subject, content);
            sendMessage(socket, emailMsg);
            
            // Wait for server response
            byte[] buffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            
            String emailTimestamp = LocalDateTime.now().format(timestampFormatter);
            if ("SEND_SUCCESS".equals(response)) {
                System.out.println("[" + emailTimestamp + "] ✅ Email sent successfully!");
            } else if ("AUTH_FAILED".equals(response)) {
                System.out.println("[" + emailTimestamp + "] ❌ Authentication failed! Invalid username or password.");
            } else {
                System.out.println("[" + emailTimestamp + "] ❌ Failed to send email.");
            }
        } 
        else {
            System.out.println("Invalid choice!");
        }

        socket.close();
        sc.close();
    }

    private static void sendMessage(DatagramSocket socket, String msg) throws Exception {
        String timestamp = LocalDateTime.now().format(timestampFormatter);
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, SERVER_PORT);
        socket.send(packet);
        System.out.println("[" + timestamp + "] 📤 Sent: " + msg);
    }
}
