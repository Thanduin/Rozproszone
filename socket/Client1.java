import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import java.awt.event.*;  
import javax.swing.*;

import java.awt.event.WindowEvent;   

public class Client1 {
    private Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    Client1(){
        JFrame f=new JFrame("Button Example");  
        JTextField tf=new JTextField();  
        JLabel label = new JLabel("Pierwsza wiadomość określa nazwę klienta!");
        tf.setBounds(50,50, 150,20);  
        JButton b=new JButton("Click Here");  
        label.setBounds(20, 20, 300, 20);
        b.setBounds(50,100,95,30);  
        b.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e){  
                try {
                    String msg2 = tf.getText().toString();
                    String response2 = sendMessage(msg2);
                    System.out.println("Wiadomosc zwrotna: " + response2);
                    tf.setText("");
                    if(msg2.equals(".")){
                        f.dispose();
                        System.exit(0);
                    }
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace(System.err);
                }
                
        }  
        });  
        f.add(label);
        f.add(b);
        f.add(tf);  
        f.setSize(400,400);  
        f.setLayout(null);  
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                String odpowiedz;
                try {
                    odpowiedz = sendMessage(".");
                    System.out.println("Wiadomosc zwrotna: " + odpowiedz);
                    f.dispose();
                    System.exit(0);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                
            }
        });
        
    }


    public static void main(String[] args) throws IOException {
        Client1 client = new Client1();
        client.startConnection("172.24.198.35", 5555);
        
}


}