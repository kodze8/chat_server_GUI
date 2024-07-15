import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class client_server extends Thread{
    static final int PORT = 9999;
    static List<client_server> servers = new ArrayList<>();
    static List<String> active_users = new ArrayList<>();
    static final Object  lock = new Object();

    String username;
    Socket server;
    BufferedReader reader;
    BufferedWriter writer;


    public client_server(Socket server){
        this.server = server;
        try {
            reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send_message(String msg) throws IOException {
        for (client_server s : servers) {
            try {
                if (this!=s) {
                    s.writer.write(msg + "\n");
                    s.writer.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run() {
        try {
            boolean name_received = false;

            while (true) {
                String msg = reader.readLine();
                if (msg!=null) {
                    if (!name_received){
                        name_received=true;
                        this.username = msg;

                        synchronized (lock) {
                            active_users.add(msg);
                            servers.add(this);
                        }
                        send_message(msg+" joined!");
                    }
                    else if (msg.equalsIgnoreCase("active")){
                        String info = "";
                        for (String names: active_users)
                            if (!names.equals(this.username))
                                info += names + ", ";
                        send_client("Active users: " + info);
                    }
                    else if (msg.equalsIgnoreCase("exit")){
                        send_message(this.username + " left the chat");
                        synchronized (lock) {
                            active_users.remove(this.username);
                            servers.remove(this);
                        }
                        break;
                    }
                    else {
                        send_message(this.username + ": " + msg);
                        send_client("you: " + msg);
                    }
                }
            }
            reader.close();
            writer.close();
            server.close();
        } catch (IOException e) {
            System.err.println("Problem with server: " + e.getMessage());
        }
    }

    public void send_client(String msg){
        try {
            this.writer.write(msg + "\n");
            this.writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void run_server(){
        try {
            ServerSocket serverSocket = new ServerSocket(client_server.PORT);
            while (true){
                Socket server_soc = serverSocket.accept();
                client_server s = new client_server(server_soc);
                s.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args)  {
        run_server();
    }
}



