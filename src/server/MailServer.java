package server;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MailServer extends JFrame {
    private JTextArea logArea;
    private DatagramSocket socket;
    private final int PORT = 9999;
    private final File MAIL_DIR = new File("MailServerData");
    private final File USERS_FILE = new File("MailServerData", "users.properties");
    private Map<String, String> users = new HashMap<>();
    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public MailServer() {
        setTitle("Mail Server (UDP)");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        if (!MAIL_DIR.exists()) MAIL_DIR.mkdirs();
        loadUsers();

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
                // Format: REGISTER:username:password
                String[] parts = message.split(":", 3);
                if (parts.length >= 3) {
                    String username = parts[1].trim();
                    String password = parts[2].trim();
                    createAccount(username, password);
                } else {
                    log("⚠️ Invalid REGISTER format.");
                }

            } else if (message.startsWith("LOGIN:")) {
                // Format: LOGIN:username:password
                String[] parts = message.split(":", 3);
                if (parts.length >= 3) {
                    String username = parts[1].trim();
                    String password = parts[2].trim();
                    boolean success = authenticateUser(username, password);
                    sendResponse(success ? "LOGIN_SUCCESS" : "LOGIN_FAILED", address, port);
                } else {
                    log("⚠️ Invalid LOGIN format.");
                }

            } else if (message.startsWith("SEND:")) {
                // Format: SEND:sender:password:receiver:subject:content
                String[] parts = message.split(":", 6);
                if (parts.length >= 6) {
                    String sender = parts[1].trim();
                    String password = parts[2].trim();
                    String receiver = parts[3].trim();
                    String subject = parts[4].trim();
                    String content = parts[5];
                    
                    if (authenticateUser(sender, password)) {
                        storeEmail(receiver, sender, subject, content);
                        sendResponse("SEND_SUCCESS", address, port);
                    } else {
                        log("⚠️ Authentication failed for sender: " + sender);
                        sendResponse("AUTH_FAILED", address, port);
                    }
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

    private void createAccount(String username, String password) throws IOException {
        if (users.containsKey(username)) {
            log("ℹ️ Account already exists: " + username);
            return;
        }

        File userDir = new File(MAIL_DIR, username);
        if (!userDir.exists()) {
            userDir.mkdirs();
            File welcomeFile = new File(userDir, "new_email.txt");
            try (FileWriter fw = new FileWriter(welcomeFile)) {
                fw.write("Thank you for using this service.\n");
                fw.write("We hope that you will feel comfortable using it.\n");
                fw.write("Enjoy your experience, " + username + "!\n");
            }
            
            // Store user credentials
            users.put(username, password);
            saveUsers();
            log("🟢 Account created: " + username);
        }
    }

    private void storeEmail(String receiver, String sender, String subject, String content) throws IOException {
        File receiverDir = new File(MAIL_DIR, receiver);
        if (!receiverDir.exists()) {
            log("⚠️ Receiver not found: " + receiver);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(timestampFormatter);
        String fileTimestamp = now.format(fileFormatter);
        String filename = "mail_from_" + sender + "_" + fileTimestamp + ".txt";
        File mailFile = new File(receiverDir, filename);

        try (FileWriter fw = new FileWriter(mailFile)) {
            fw.write("From: " + sender + "\n");
            fw.write("To: " + receiver + "\n");
            fw.write("Subject: " + subject + "\n");
            fw.write("Date: " + timestamp + "\n\n");
            fw.write("Message:\n" + content + "\n");
        }

        log("📨 New email saved to " + receiver + "/" + filename);
    }

    private void log(String msg) {
        String timestamp = LocalDateTime.now().format(timestampFormatter);
        String logMessage = "[" + timestamp + "] " + msg;
        SwingUtilities.invokeLater(() -> logArea.append(logMessage + "\n"));
    }

    private void loadUsers() {
        try {
            if (USERS_FILE.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(USERS_FILE)) {
                    props.load(fis);
                    for (String key : props.stringPropertyNames()) {
                        users.put(key, props.getProperty(key));
                    }
                }
                log("📂 Loaded " + users.size() + " user accounts");
            }
        } catch (Exception e) {
            log("⚠️ Error loading users: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try {
            Properties props = new Properties();
            for (Map.Entry<String, String> entry : users.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
            try (FileOutputStream fos = new FileOutputStream(USERS_FILE)) {
                props.store(fos, "Mail Server User Accounts");
            }
            log("💾 User accounts saved");
        } catch (Exception e) {
            log("⚠️ Error saving users: " + e.getMessage());
        }
    }

    private boolean authenticateUser(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

    private void sendResponse(String response, InetAddress address, int port) {
        try {
            byte[] data = response.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
            log("📤 Sent response: " + response);
        } catch (Exception e) {
            log("❌ Error sending response: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MailServer().setVisible(true));
    }
}
