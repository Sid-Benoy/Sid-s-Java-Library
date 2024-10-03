package clientside;
/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Siddharth Benoy>
 * <sb62297>
 * <17195>
 * Spring 2023
 */
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DashboardController {
    @FXML
    private Label user;
    @FXML
    private TableView<Item> table;
    @FXML
    private TableColumn<Item, String> itemType;
    @FXML
    private TableColumn<Item, String> title;
    @FXML
    private TableColumn<Item, String> author;
    @FXML
    private TableColumn<Item, Integer> storage;
    @FXML
    private TableColumn<Item, String> summary;
    @FXML
    private TableColumn<Item, String> userCurrentlyCheckedOut;
    @FXML
    private TableColumn<Item, String> membersListPrior;
    @FXML
    private TableColumn<Item, String> checkedOutLast;
    @FXML
    private TableColumn<Item, String> image;
    @FXML
    private Button logout;
    @FXML
    private Button checkout;
    @FXML
    private TextField selection;
    @FXML
    private ChoiceBox<String> returnItemDisplay;
    @FXML
    private Button returnItem;
    @FXML
    private Label invalid;
    @FXML
    private Button returnAllItems;
    @FXML
    private Button exit;
    @FXML
    private Button search;
    @FXML
    private TextField searchBar;
    @FXML
    private ImageView imageView;
    @FXML
    private TextArea reviewText;
    @FXML
    private TextArea reviewTextBox;
    String member;
    Client client;
    Stage stage;
    String[] args;
    ClientUI clientUI;
    Item[] itemsList;


    public void init(String member, Client client, Stage stage, ClientUI clientUI) {
        this.client = client;
        this.member = member;
        this.stage = stage;
        this.clientUI = clientUI;
        user.setText("Welcome, " + member + "!");
        try {
            client.serverCommunication(member);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public DashboardController getController(){
        return this;
    }
    public void display(String input){
        Gson gson = new Gson();
        System.out.println(input);
        Item[] items = gson.fromJson(input, Item[].class);
        this.itemsList = items;
        ObservableList<Item> itemList = FXCollections.observableArrayList(items);
        itemType.setCellValueFactory(new PropertyValueFactory<>("itemType"));
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        author.setCellValueFactory(new PropertyValueFactory<Item, String>("author"));
        storage.setCellValueFactory(new PropertyValueFactory<Item, Integer>("storage"));
        summary.setCellValueFactory(new PropertyValueFactory<Item, String>("summary"));
        userCurrentlyCheckedOut.setCellValueFactory(new PropertyValueFactory<Item, String>("userCurrentlyCheckedOut"));
        membersListPrior.setCellValueFactory(new PropertyValueFactory<Item, String>("membersListPrior"));
        checkedOutLast.setCellValueFactory(new PropertyValueFactory<Item, String>("checkedOutLast"));
        image.setCellValueFactory(new PropertyValueFactory<Item, String>("image"));
        table.setItems(itemList);
    }
    @FXML
    public void logout(ActionEvent event) throws IOException {
        FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("gui.fxml"));
        Parent dashboardRoot = dashboardLoader.load();
        Scene scene = new Scene(dashboardRoot, 639, 342);
        ClientUI guiController = dashboardLoader.getController();
        clientUI.restart(stage, guiController, scene);
    }
    @FXML
    public void exit(ActionEvent event){
        Platform.exit();
    }
    @FXML
    public void invalidRequest(){
        invalid.setText("ALERT: Invalid Request, Item has already been checked out.\nEnsure you have entered no characters after entering an item and a comma\nPlease try again.");
        selection.clear();
    }
    @FXML
    public void checkout(ActionEvent event){
        invalid.setText(null);
        imageView.setImage(null);
        String checkout = member + "," + selection.getText();
        System.out.println(checkout);
        List<String> yo = Arrays.asList(checkout.split(","));
        System.out.println(yo.get(1));
        try {
            client.serverCommunication(checkout);
        }catch (Exception e){
            e.printStackTrace();
        }
        selection.setText(null);
    }
    @FXML
    public void returnItem(ActionEvent event){
        int index = returnItemDisplay.getValue().indexOf(returnItemDisplay.getValue());
        String returnItem = member + "," + "RETURN" + "," + returnItemDisplay.getValue();
        try {
            client.serverCommunication(returnItem);
        }catch (Exception e){
            e.printStackTrace();
        }
        returnItemDisplay.setValue(null);
        returnItemDisplay.getItems().set(index, null);

    }
    @FXML
    public void returnAllItem(ActionEvent event){
        StringBuilder temp = new StringBuilder();
        for(int i = 0; i < returnItemDisplay.getItems().size(); i++){
            temp.append(",").append(returnItemDisplay.getItems().get(i));
        }
        String returnItem = member + "," + "RETURNALL" + temp;
        try {
            returnItemDisplay.getItems().clear();
            client.serverCommunication(returnItem);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @FXML
    public void search(ActionEvent event){
        String found = searchBar.getText();
        String url = null;
        System.out.println(found);
        for(int i = 0; i < itemsList.length; i++){
            if(itemsList[i].getTitle().equals(found)){
                System.out.println(itemsList[i].getImage());
                url = itemsList[i].getImage();
            }
        }
        Image image = new Image(url);
        imageView.setImage(image);

    }

    @FXML
    public void displayReturn(String input){
        if(input.equals("@")){
            return;
        }
        input = input.substring(1);
        List<String> display = Arrays.asList(input.split(","));
        returnItemDisplay.getItems().addAll(display);
    }
    @FXML
    public void displayReviews(String input){
        if(input.equals("/")){
            return;
        }
        reviewTextBox.setText(input);
    }

}
