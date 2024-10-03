package serverside;
/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Siddharth Benoy>
 * <sb62297>
 * <17195>
 * Spring 2023
 */
import java.io.*;
import java.util.*;
import java.net.Socket;

public class ClientHandler implements Observer, Runnable {
    private Server server;
    private Socket clientSocket;
    private BufferedReader fromClient;
    private PrintWriter toClient;
    private ObjectOutputStream objectOutputStream;
    private OutputStream outputStream;
    Catalog catalog = new Catalog();
    protected ClientHandler(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        try {
            fromClient = new BufferedReader(new
                    InputStreamReader(this.clientSocket.getInputStream()));
            toClient = new PrintWriter(this.clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputStream = clientSocket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
    }

    @Override
    public void run(){
        Object o1 = new Object();
        String input;
        try{
            while((input = fromClient.readLine()) != null) {
                System.out.println(input);
                List<String> request = new ArrayList<>(Arrays.asList(input.split(",")));
                System.out.println(request);
                synchronized (o1) {
                    if (request.size() == 1) {                            //if a user has logged in
                        sendToClient(catalog.getCatalog());
                        sendToClient(catalog.getReturningItems(input));
                    } else if(request.get(1).equals("REVIEW")){
                        String process = server.processRequest(request);
                        sendToClient(process);
                    }
                    else {                                             //user has input a return or checkout
                        String process = server.processRequest(request);
                        System.out.println(process);
                        sendToClient(process.substring(0, process.indexOf("@")));
                        sendToClient(process.substring(process.indexOf("@")));
                    }
                    sendToClient(null);
                    request.clear();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    protected void sendToClient(Object message) {
        System.out.println("Sending to client: " + message);
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        this.sendToClient(arg);
    }
}

