
import java.net.Socket;
import java.net.SocketException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;


public class Client2 {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

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

    public static void main(String[] args) throws IOException {
        Client2 client = new Client2();
        client.startConnection("172.24.198.35", 5555);
        String msg = "test";
        String response;
        Scanner scan = new Scanner(System.in);
        System.out.println("Pierwsza wiadomosc okresla nazwe klienta!!!!");
        while(!msg.equals(".")){
            try{
                System.out.println("Podaj wiadomosc: ");
                msg = scan.nextLine();
                response = client.sendMessage(msg);
                System.out.println("Wiadomosc zwrotna: " + response);
            } catch(SocketException e){
                System.out.println("Connection lost");
                break;
            }
        }     
}
}