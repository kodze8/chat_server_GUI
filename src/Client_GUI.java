import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// GUI version for client

public class Client_GUI {
    static final int PORT  = 9999;
    private final Socket socket;
    private String username;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private volatile boolean online;



    String msg_history = "";
    JFrame main_frame = new JFrame();
    JTextArea  chat = new JTextArea();
    JTextField msg_input = new JTextField();
    JButton send_msg = new JButton("Send");

    public Client_GUI(){
        try {
            socket = new Socket("localhost", PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            online = true;

            initial_gui();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initial_gui(){
        JFrame frame_1 = new JFrame();
        frame_1.setLayout(null);
        frame_1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame_1.setSize(400, 300);
        frame_1.setResizable(false);

        JLabel label_user = new JLabel("Username");
        label_user.setBounds(80, 100, 100, 20);

        JTextField textField = new JTextField();
        textField.setBounds(180, 100, 150, 20);

        JButton send_button = new JButton("Send");
        send_button.setBounds(270, 130, 60, 20);

        JLabel  error_label =  new JLabel();
        error_label.setBounds(180, 130, 150, 20);
        error_label.setForeground(java.awt.Color.RED);

        send_button.addActionListener(e -> {
            String input = textField.getText().trim();
            if (input.isEmpty()) {
                if (error_label.getText().isEmpty()) {
                    error_label.setText("Enter your username!");
                    frame_1.revalidate();
                    frame_1.repaint();
                }
            } else {
                username = input;
                frame_1.setVisible(false);
                chat_frame();
                print("Welcome to chat! "+username + " "+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                sendMessage(username);
                print("Start chat");

            }
        });

        frame_1.add(label_user);
        frame_1.add(error_label);
        frame_1.add(textField);
        frame_1.add(send_button);
        frame_1.setVisible(true);
    }

    public void print(String msg){
        SwingUtilities.invokeLater(() -> {
            msg_history += msg + "\n";
            chat.setText(msg_history);
        });
    }
    public void chat_frame(){
        main_frame.setLayout(null);
        main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main_frame.setResizable(false);
        main_frame.setSize(400,450);

        chat.setBounds(20,20,350,300);

        msg_input.setBounds(20,350, 350,30);

        send_msg.setBounds(300,380,70,30);


        ActionListener sendAction = e -> {
                String message = msg_input.getText().trim();


                if (!message.isEmpty()) {
                    sendMessage(message);
                    msg_input.setText("");
                }};
        msg_input.addActionListener(sendAction);
        send_msg.addActionListener(sendAction);

        main_frame.add(chat);
        main_frame.add(msg_input);
        main_frame.add(send_msg);
        main_frame.setVisible(true);
    }



    private void sendMessage(String msg) {
        try {
            this.writer.write(msg+"\n");
            this.writer.flush();
            if (msg.equalsIgnoreCase("exit")){
                this.close();
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            this.close();
        }
    }



    private void receiveMessages(){
        while (online){
            try {
                String received = reader.readLine();
                if (received == null) {
                    System.out.println("Server closed the connection.");
                    this.close();
                    break;
                } else {
                    print(received);
                }
            } catch (IOException e) {
                if(online) {
                    System.err.println("Error receiving message: " + e.getMessage());
                }
                this.close();
            }
        }
    }

    public void startCommunicationThreads(){
        new Thread(this::receiveMessages).start();
    }

    private void close(){
        try {
            if (this.reader != null) this.reader.close();
            if (this.writer != null) this.writer.close();
            if (this.socket != null) this.socket.close();
            this.online = false;
            main_frame.dispose();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client_GUI c = new Client_GUI();
        c.startCommunicationThreads();
    }
}

