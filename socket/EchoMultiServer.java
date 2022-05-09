import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class EchoMultiServer {


    private ServerSocket serverSocket;
    private static int priority = 1;
    BlockingQueue<String> queue = new LinkedBlockingDeque<>();
    private final String poisonPill = "koniec";
    String[] odpowiedzi = {"1","2","3","4","5","6","7","8"};
    static int connectionCounter = 0;
    static ArrayList<Thread> klienci = new ArrayList<>();
    public void start(int port) throws InterruptedException {
        
        try {
            serverSocket = new ServerSocket(port);
            for(int i=0; i<8; i++){
                queue.put(odpowiedzi[i]);
            }
            while (true)
                new EchoClientHandler(serverSocket.accept(), queue, poisonPill).start();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }

    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private BlockingQueue<String> queue;
        private final String poisonPill;
        int pozycja;
        public EchoClientHandler(Socket socket, BlockingQueue<String> queue, String poisonPill) {
            this.clientSocket = socket;
            this.queue = queue;
            this.poisonPill = poisonPill;
        }

        public void sendToAll(String msg) throws IOException{
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        }

        public void run() {
            try {
                //queue.put(poisonPill);
                klienci.add(Thread.currentThread());
                pozycja = klienci.size();
                Thread obecny = currentThread();
                Thread.currentThread().setPriority(priority);
                priority++;
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                int nameCheck = 0;

                String inputLine;
                String queueTake;
                
                while ((inputLine = in.readLine()) != null) {
                    if (".".equals(inputLine)) {
                        for(int i = 0; i < Thread.activeCount()-2; i++){
                            queue.put(Thread.currentThread().getName() + " rozlaczyl sie");
                        }
                        System.out.println("Zakonczono polaczenie z klientem " + Thread.currentThread().getName());
                        klienci.remove(Thread.currentThread());
                        for(Thread watek : klienci){
                            System.out.println("Otrzymal wiadomosc: " + watek.getName());
                        }
                        out.println("bye");
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if(nameCheck == 0){
                    Thread.currentThread().setName(inputLine);
                    //queue.put(inputLine);
                    nameCheck++;
                    }
                    System.out.println(Thread.currentThread().getId());
                    System.out.println("Pozycja: " + pozycja);
                    Thread.currentThread();
                    System.out.println("Aktywne: " + Thread.activeCount());
                    System.out.println("Otrzymano wiadomosc " + inputLine);
                    queueTake = queue.take();
                    if (queueTake.equals(Thread.currentThread().getName())){
                        for(int i = 0; i < Thread.activeCount(); i++){
                            queue.put(Thread.currentThread().getName() + "rozlaczyl sie");
                        }
                        System.out.println("Zakonczono polaczenie z klientem " + Thread.currentThread().getName());
                        out.println(queueTake + " bye");
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if(queueTake.contains("rozlaczyl")){
                        if(connectionCounter == obecny.activeCount()-2){
                            synchronized(obecny){
                            obecny.notifyAll();
                            queue.put(Thread.currentThread().getName() + " obudzil reszte");
                            queue.put("testowe");
                            }
                        } else
                        synchronized(obecny){
                            out.println(queueTake + ". Czekam az inni sie dowiedza");
                            connectionCounter++;
                            obecny.wait();
                            
                            System.out.println(connectionCounter);
                        }
                        
                    } 
                    
                    // if(inputLine.equals(".")){

                    // }
                    out.println(queueTake);
                }
                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException e) {
                e.getMessage();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        EchoMultiServer server = new EchoMultiServer();
        server.start(5555);
    }

}