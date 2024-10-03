package clientside;
/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Siddharth Benoy>
 * <sb62297>
 * <17195>
 * Spring 2023
 */
import java.io.*;
import java.security.*;
import java.util.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.Document;
import com.mongodb.MongoException;
import org.bson.conversions.Bson;
import org.bson.BsonInt64;


import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.File;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import javax.naming.event.ObjectChangeListener;

public class Client extends Application{
    private static final String URI = "mongodb+srv://user:yo@cluster0.pkmvsqw.mongodb.net/?retryWrites=true&w=majority";
    private static MongoClient mongo;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;
    private Document doc = new Document();
    private Cipher cipher;
    private KeyPair pair;
    private KeyPairGenerator keyPairGen;
    private byte[] cipherText;
    private static String host = "127.0.0.1";
    private BufferedReader fromServer;
    private PrintWriter toServer;
    private Scanner consoleInput = new Scanner(System.in);
    private String[] args;

    private Stage stage;
    private Scene scene;
    private Scene dashboard;
    private DashboardController controller;
    private ClientUI oldController;
    private ObjectInputStream objectInputStream;


    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUpNetworking() throws Exception {
        @SuppressWarnings("resource")
        Socket socket = new Socket(host, 4241);
        System.out.println("Connecting to... " + socket);
        fromServer = new BufferedReader(new
                InputStreamReader(socket.getInputStream()));
        toServer = new PrintWriter(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());

    }

    protected void processRequest(String input) {
        if(input.equals("Invalid")){
            controller.invalidRequest();
        }
        else if(input.charAt(0) == '@') {
            controller.displayReturn(input);
        }
        else if(input.charAt(0) == '/'){
            controller.displayReviews(input.substring(1));
        }
        else{
            controller.display(input);
        }
    }

    protected void serverCommunication(String string) throws IOException, ClassNotFoundException {
        controller = oldController.getNew();
        System.out.println("Sending to server: " + string);
        toServer.println(string);
        toServer.flush();
        Object input;

        try {
            while ((input = objectInputStream.readObject()) != null) {
                System.out.println(input);
                processRequest(input.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void updateController(ClientUI clientUI){
        oldController = clientUI;
    }

    public void encryption(String username, String password){
        mongo = MongoClients.create(URI);
        database = mongo.getDatabase("Cluster0");
        collection = database.getCollection("users");
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");

            //Creating KeyPair generator object
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

            this.keyPairGen = keyPairGen;
            //Initializing the key pair generator
            keyPairGen.initialize(2048);

            //Generating the pair of keys
            KeyPair pair = keyPairGen.generateKeyPair();
            this.pair = pair;
            //Creating a Cipher object


            //Initializing a Cipher object
            cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());
            this.cipher = cipher;
            //encrypting the data
            byte[] cipherText = cipher.doFinal();
            this.cipherText = cipherText;
            System.out.println( new String(cipherText, "UTF8"));

            Document query = new Document("username", username);
            Document existingDoc = collection.find(query).first();
            if(existingDoc == null) {
                doc.append("username", username);
                doc.append("password", password);
                doc.append("encrypted pass", cipherText);
                collection.insertOne(doc);
                doc.clear();
            }else
                System.out.println("already got this");

        }catch (Exception e){
            e.printStackTrace();
    }
    }

    public boolean decrypt(String username, String password1){
        try {
       //     cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
        ///    byte[] decipheredText = cipher.doFinal(cipherText);
        //    System.out.println(new String(decipheredText));
            Document query = new Document("username", username);
            Document doc = collection.find(query).first();
            if(doc.get("password").equals(password1)){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void start(Stage applicationStage) throws Exception {
        setUpNetworking();
        stage = applicationStage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);

        applicationStage.setScene(scene);
        applicationStage.show();

        ClientUI controller = loader.getController();
        oldController = controller;
        controller.init(applicationStage, this);

    }


}