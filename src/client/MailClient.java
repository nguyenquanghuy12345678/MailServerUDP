package client;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MailClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 9999;

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        DatagramSocket socket = new DatagramSocket();

        System.out.println("=== MAIL CLIENT (UDP) ===");
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
            System.out.println("✅ Registration request sent!");
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
            
            if ("LOGIN_SUCCESS".equals(response)) {
                System.out.println("✅ Login successful!");
            } else {
                System.out.println("❌ Login failed! Invalid username or password.");
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
            
            if ("SEND_SUCCESS".equals(response)) {
                System.out.println("✅ Email sent successfully!");
            } else if ("AUTH_FAILED".equals(response)) {
                System.out.println("❌ Authentication failed! Invalid username or password.");
            } else {
                System.out.println("❌ Failed to send email.");
            }
        } 
        else {
            System.out.println("Invalid choice!");
        }

        socket.close();
        sc.close();
    }

    private static void sendMessage(DatagramSocket socket, String msg) throws Exception {
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, SERVER_PORT);
        socket.send(packet);
        System.out.println("📤 Sent: " + msg);
    }
}
