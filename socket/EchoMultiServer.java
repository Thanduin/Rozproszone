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
                new EchoClientHandler(serverSocket.accept(), queue).start();
                

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
        int pozycja;
        public EchoClientHandler(Socket socket, BlockingQueue<String> queue) {
            this.clientSocket = socket;
            this.queue = queue;
        }


        public void run() {
            try {
                //queue.put(poisonPill);
                Thread obecny = currentThread();
                klienci.add(obecny);
                pozycja = klienci.size();
                obecny.setName("klient " + pozycja);
                System.out.println("Połączono z " + obecny.getName());
                obecny.setPriority(priority);
                priority++;
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


                String inputLine;
                String queueTake;
                
                while ((inputLine = in.readLine()) != null) {
                    if (".".equals(inputLine)) {
                        for(int i = 0; i < Thread.activeCount()-2; i++){
                            queue.put(obecny.getName() + " rozlaczyl sie");
                        }
                        System.out.println("Zakonczono polaczenie z klientem " + obecny.getName());
                        klienci.remove(obecny);
                        for(Thread watek : klienci){
                            System.out.println("Otrzymal wiadomosc: " + watek.getName());
                        }
                        out.println("bye");
                        obecny.interrupt();
                        break;
                    }
                    System.out.println(obecny.getId());
                    System.out.println("Pozycja: " + pozycja);
                    System.out.println("Aktywne: " + Thread.activeCount());
                    System.out.println("Otrzymano wiadomosc " + inputLine);
                    queueTake = queue.take();
                    if (queueTake.equals(obecny.getName())){
                        for(int i = 0; i < Thread.activeCount(); i++){
                            queue.put(obecny.getName() + "rozlaczyl sie");
                        }
                        System.out.println("Zakonczono polaczenie z klientem " + obecny.getName());
                        out.println(queueTake + " bye");
                        obecny.interrupt();
                        break;
                    }
                    if(queueTake.contains("rozlaczyl")){
                        if(connectionCounter == Thread.activeCount()-2){
                            synchronized(obecny){
                            obecny.notifyAll();
                            queue.put(obecny.getName() + " obudzil reszte");
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