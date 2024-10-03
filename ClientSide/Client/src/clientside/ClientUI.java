package clientside;
/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Siddharth Benoy>
 * <sb62297>
 * <17195>
 * Spring 2023
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.xml.soap.Text;
import java.io.IOException;

public class ClientUI {
    @FXML
    private TextField userEnter;
    @FXML
    private TextField user;
    @FXML
    private TextField passwordEnter;
    @FXML
    private Button registerButton;
    String username;
    String password;

    Client client;
    Stage stage;
    Scene dashboard;
    FXMLLoader newController;
    DashboardController dashboardController;


    public void init(Stage stage, Client client) throws IOException {
        this.stage = stage;
        this.client = client;
        FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("checkout.fxml"));
        Parent dashboardRoot = dashboardLoader.load();
        dashboard = new Scene(dashboardRoot, 639, 342);
        newController = dashboardLoader;
    }
    public void restart(Stage stage, ClientUI clientUI, Scene scene) throws IOException {
        Client newClient = new Client();
        newClient.updateController(clientUI);
        this.stage = stage;
        this.stage.setScene(scene);
        try {
            newClient.setUpNetworking();
            clientUI.init(stage, newClient);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    public void setEntryConfirmation(ActionEvent event){
        String member = userEnter.getText();
        username = member;
        String password1 = passwordEnter.getText();
        password = password1;
        System.out.println(member);
        client.encryption(username, password);
        userEnter.clear();
        passwordEnter.clear();
        stage.setScene(dashboard);
        transition(member);
    }
    @FXML
    public void login(ActionEvent event){
 //       String member = userEnter.getText();
  //      String password1 = passwordEnter.getText();
   //     if(client.decrypt(member, password1)){
   //         stage.setScene(dashboard);
   //         transition(member);
    //    }
    }

    public void transition(String member){
        dashboardController = newController.getController();
        dashboardController.init(member,client, stage, this);
    }

    public DashboardController getNew(){
        return dashboardController.getController();
    }
}
