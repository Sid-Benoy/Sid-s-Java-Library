package serverside;
/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Siddharth Benoy>
 * <sb62297>
 * <17195>
 * Spring 2023
 */
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.gson.Gson;
public class Server extends Observable {
    Catalog catalog = new Catalog();
    public static void main(String[] args) throws IOException {
        new Server().runServer();

    }
    private void runServer() {
        try {
            catalog.setup();
            setUpNetworking();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
    private void setUpNetworking() throws Exception {
        @SuppressWarnings("resource")
        ServerSocket serverSock = new ServerSocket(4241);
        while (true) {
            Socket clientSocket = serverSock.accept();
            System.out.println("Connecting to... " + clientSocket);
            ClientHandler handler = new ClientHandler(this, clientSocket);
            this.addObserver(handler);
            Thread newThread = new Thread(handler);
            newThread.start();
        }

    }

    public String processRequest(List<String> input){
        System.out.println(input);
        String result = "Invalid";
        StringBuilder itemsCheckedOut = new StringBuilder("@");
        if(input.get(1).equals("RETURN")){
            catalog.returnItem(input);
            return catalog.getCatalog() + itemsCheckedOut;              //just to send to the server correctly adds a @ at the end of the string
        }
        else if(input.get(1).equals("RETURNALL")){
            catalog.returnAll(input);
            return catalog.getCatalog() + itemsCheckedOut;              //see above
        }
     //   else if(input.get(1).equals("REVIEW")){
       //     return "/" + catalog.review(input).toString();
      //  }
        else if(catalog.update(input)){
            for(int i = 1; i < input.size(); i++){
                itemsCheckedOut.append(input.get(i)).append(",");          //gets the items sent in the string input to update the returned items sent with the client
            }
            System.out.println(itemsCheckedOut);
            this.notifyObservers(catalog.getCatalog() + itemsCheckedOut);
            return catalog.getCatalog() + itemsCheckedOut;
        }
        else{
            return result + itemsCheckedOut;
        }

    }
}
