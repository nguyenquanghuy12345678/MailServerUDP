package client;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MailClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9999;

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        DatagramSocket socket = new DatagramSocket();

        System.out.println("=== MAIL CLIENT (UDP) ===");
        System.out.println("1. Register Account");
        System.out.println("2. Send Email");
        System.out.print("Choose: ");
        int choice = sc.nextInt(); sc.nextLine();

        if (choice == 1) {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();
            sendMessage(socket, "REGISTER:" + username);
        } 
        else if (choice == 2) {
            System.out.print("Sender username: ");
            String sender = sc.nextLine().trim();
            System.out.print("Receiver username: ");
            String receiver = sc.nextLine().trim();
            System.out.print("Subject: ");
            String subject = sc.nextLine().trim();
            System.out.print("Message: ");
            String content = sc.nextLine().trim();

            String emailMsg = String.format("SEND:%s:%s:%s:%s", sender, receiver, subject, content);
            sendMessage(socket, emailMsg);
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
