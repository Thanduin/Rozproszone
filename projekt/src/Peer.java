
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/// peer is responsible for sending and receiving only
public class Peer {
    final int NO_RESPONSE_SPAN = 5000 ; /// in milliseconds, received any message in 3 seconds, alive or election
    private int port = 8090;
    private String host;
    private String myHost;/// default, all on local host
    private ServerSocket serverSocket = null;
    private boolean active = true ;
    Process process = null;
    Peer(int port, String host, String myHost){
        this.port= port;
        this.host = host;
        this.myHost = myHost;
    }
    String sendAndGetRespone(Peer peer , Message message , int timeOut){
        try{
            Socket s=new Socket(peer.getHost(),peer.getPort());
            System.out.println("Wysylam "+message.toString()+" do "+ peer.getPort());
            s.setSoTimeout(timeOut);
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            DataInputStream din = new DataInputStream(s.getInputStream());
            dout.writeUTF(message.toString());
            String response = din.readUTF(); /// we need to decode response
            process.decodeResponse(response);
            process.messageArea.append("Wysylam "+message.toString()+" do "+ peer.getPort()+"\n");
            dout.flush();
            dout.close();
            s.close();
            return response;
        }catch(Exception e){System.out.println(e +" " + peer.getPort());}
        return null;
    }
    Message receiveAndGiveResponse(int timeOut){
        try{
//          ServerSocket ss=new ServerSocket(this.getPort());
            if(timeOut> 0)
                serverSocket.setSoTimeout(timeOut);
            Socket s=serverSocket.accept();//establishes connection
            DataInputStream din=new DataInputStream(s.getInputStream());
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            String str=din.readUTF();
            System.out.println("Otrzymana wiadomosc:  "+str);
            Message message = process.encodeResponse(str);
            String response = message.toString();
            System.out.println("Odpowiedz: " +response);
            dout.writeUTF(response);
            process.messageArea.append("Otrzymana wiadomosc: "+str+"\n");
            process.messageArea.append("Odpowiedz: "+response+"\n");
            dout.flush();
            dout.close();
            din.close();
            return new Message(str);
        }catch(Exception e){
            /// if we timed out and !AMACoordinator, we send election
            if(!process.isAMA_COORDINATOR())
                active = false;

        }
        return null;
    }
    void Listen(){
        while(active){
            if(process.isAMA_COORDINATOR()){
                /// when timed out the socket is not closed so we don't need to open it again
                if(serverSocket == null||serverSocket.isClosed())
                    bindServerSocket();
                receiveAndGiveResponse(1000); ///listen for 2 second and send alive wait indefinitely
                process.sendAlive();
            }else {
                /// when timed out the socket is not closed so we don't need to open it again
                if(serverSocket == null||serverSocket.isClosed())
                    bindServerSocket();

                receiveAndGiveResponse(NO_RESPONSE_SPAN ); /// wait 3 seconds
            }
        }
        System.out.println(this.getPort()+" brak kordynatora");
        process.messageArea.append(this.getPort()+" brak kordynatora\n");
        process.frame.setTitle("Algorytm Tyrana - Kordynator");
        if(!process.isAMA_COORDINATOR())
            process.notifyElection();

    }



    boolean bindServerSocket(){
        try {
            serverSocket = new ServerSocket(this.getPort());
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return false;
    }
    public String getMyHost() {return myHost;}
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }


    public void setProcess(Process process) {
        this.process = process;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
