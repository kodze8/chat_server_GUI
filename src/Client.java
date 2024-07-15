import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// Console version for client

public class Client {
    static final int PORT  = 9998;
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private volatile boolean online;

    public Client(){
        try {
            socket = new Socket("localhost", PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            online = true;

            System.out.println("Welcome to chat! " +  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            System.out.print("Username: ");
            Scanner scanner = new Scanner(System.in);
            String username = scanner.nextLine();
            sendMessage(username);
            System.out.println("Start chat");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(String msg) {
        try {
            this.writer.write(msg+"\n");
            this.writer.flush();
            if (msg.equalsIgnoreCase("exit")){
                this.online = false;
                this.close();
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void handleSending() {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        while (online) {
            try {
                String msg = consoleReader.readLine();
                sendMessage(msg);
            } catch (IOException e) {
                System.err.println("Error reading from console: " + e.getMessage());
                online = false;
                close();
            }
        }
    }

    private void receiveMessages(){
        while (online){
            try {
                String received = reader.readLine();
                if (received != null)
                    System.out.println(received);
            } catch (IOException e) {
                System.err.println("Error receiving message: " + e.getMessage());
                online = false;
                close();
            }
        }
    }
    public void startCommunicationThreads(){
        new Thread(this::handleSending).start();
        new Thread(this::receiveMessages).start();
    }

    private void close(){
        try {
            if (this.reader != null) this.reader.close();
            if (this.writer != null) this.writer.close();
            if (this.socket != null) this.socket.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client c = new Client();
        c.startCommunicationThreads();
    }
}