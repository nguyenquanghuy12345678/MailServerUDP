package server;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class MailServer extends JFrame {
    private JTextArea logArea;
    private DatagramSocket socket;
    private final int PORT = 9999;
    private final File MAIL_DIR = new File("MailServerData");

    public MailServer() {
        setTitle("Mail Server (UDP)");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        if (!MAIL_DIR.exists()) MAIL_DIR.mkdirs();

        new Thread(this::startServer).start();
    }

    private void startServer() {
        try {
            socket = new DatagramSocket(PORT);
            log("✅ Server started on port " + PORT);

            byte[] buffer = new byte[4096];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                handleMessage(message, packet.getAddress(), packet.getPort());
            }

        } catch (Exception e) {
            log("❌ Error: " + e.getMessage());
        }
    }

    private void handleMessage(String message, InetAddress address, int port) {
        try {
            log("📩 Received: " + message);

            if (message.startsWith("REGISTER:")) {
                String username = message.substring(9).trim();
                createAccount(username);

            } else if (message.startsWith("SEND:")) {
                // Format: SEND:sender:receiver:subject:content
                String[] parts = message.split(":", 5);
                if (parts.length >= 5) {
                    String sender = parts[1];
                    String receiver = parts[2];
                    String subject = parts[3];
                    String content = parts[4];
                    storeEmail(receiver, sender, subject, content);
                } else {
                    log("⚠️ Invalid SEND format.");
                }
            } else {
                log("⚠️ Unknown command: " + message);
            }

        } catch (Exception e) {
            log("❌ Handle message error: " + e.getMessage());
        }
    }

    private void createAccount(String username) throws IOException {
        File userDir = new File(MAIL_DIR, username);
        if (!userDir.exists()) {
            userDir.mkdirs();
            File welcomeFile = new File(userDir, "new_email.txt");
            try (FileWriter fw = new FileWriter(welcomeFile)) {
                fw.write("Thank you for using this service.\n");
                fw.write("We hope that you will feel comfortable using it.\n");
                fw.write("Enjoy your experience, " + username + "!\n");
            }
            log("🟢 Account created: " + username);
        } else {
            log("ℹ️ Account already exists: " + username);
        }
    }

    private void storeEmail(String receiver, String sender, String subject, String content) throws IOException {
        File receiverDir = new File(MAIL_DIR, receiver);
        if (!receiverDir.exists()) {
            log("⚠️ Receiver not found: " + receiver);
            return;
        }

        String filename = "mail_from_" + sender + "_" + System.currentTimeMillis() + ".txt";
        File mailFile = new File(receiverDir, filename);

        try (FileWriter fw = new FileWriter(mailFile)) {
            fw.write("From: " + sender + "\n");
            fw.write("To: " + receiver + "\n");
            fw.write("Subject: " + subject + "\n\n");
            fw.write("Message:\n" + content + "\n");
        }

        log("📨 New email saved to " + receiver + "/" + filename);
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MailServer().setVisible(true));
    }
}
