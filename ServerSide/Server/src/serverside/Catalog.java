/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Siddharth Benoy>
 * <sb62297>
 * <17195>
 * Spring 2023
 */
package serverside;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;


public class Catalog {
    private static final String URI = "mongodb+srv://user:yo@cluster0.pkmvsqw.mongodb.net/?retryWrites=true&w=majority";
    private static MongoClient mongo;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;
    private static MongoCollection<Document> collection2;
    Document doc = new Document();


    public void setup() {

        mongo = MongoClients.create(URI);
        database = mongo.getDatabase("Cluster0");
        collection = database.getCollection("myCollection");
        collection2 = database.getCollection("reviews");


        ObjectMapper objectMapper = new ObjectMapper();
        List<Item> item = null;
        try {
            item = objectMapper.readValue(new File("library.JSON"), new TypeReference<List<Item>>() {
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        Document query = new Document("storage", "142");                //makes sure i dont upload the same document multiple times to update mongoDB
        Document existingDoc = collection.find(query).first();
        if(existingDoc == null) {
            for (Item items : item) {
                doc.append("author", items.getAuthor());
                doc.append("itemType", items.getItemType());
                doc.append("title", items.getTitle());
                doc.append("storage", items.getStorage());
                doc.append("summary", items.getSummaryDescription());
                doc.append("userCurrentlyCheckedOut", items.getUserCurrentlyCheckedOut());
                doc.append("membersListPrior", items.getMembersListPrior());
                doc.append("image", items.getImage());
                doc.append("checkedOutLast", items.getCheckedOutLast());
                collection.insertOne(doc);
                doc.clear();
            }
        }else{
            System.out.println("already got this");
        }
        System.out.println(item);
        System.out.println(item.get(0).getItemType());
    }

    public String getCatalog(){
        MongoCollection<Document> collection = database.getCollection("myCollection");
        List<Document> documents = collection.find().into(new ArrayList<>());
        Gson gson = new Gson();
        String json = gson.toJson(documents);
        System.out.println(json);
        return gson.toJson(documents);
    }

    public void setUser(){
        MongoCollection<Document> users = database.getCollection("users");
        Document user = new Document();
    }

    public boolean update(List<String> input){
        String member = input.get(0);

        for(int i = 1; i < input.size(); i ++){
            Document temp = new Document("title", input.get(i));
            long count = collection.countDocuments(temp);
            if(count == 0)
                return false;
            Document doc = collection.find(temp).first();
            if(doc.getString("userCurrentlyCheckedOut") == null) {
                collection.updateOne(Filters.eq("title", input.get(i)), Updates.set("userCurrentlyCheckedOut", member));
            }else{
                return false;
            }
        }
        return true;
    }
    public void returnItem(List<String> input){
        String member = input.get(0);
        Document temp = new Document("title", input.get(2));
        Document doc = collection.find(temp).first();
        Object lastElement = null;
        List<Object> membersListPrior = doc.get("membersListPrior", List.class);            //makes sure not updating teh same perosn in members list prior field
        int lastIndex = membersListPrior.size() - 1;
        if(lastIndex > 0) {
            lastElement = membersListPrior.get(lastIndex);
        }

        if(doc.getString("userCurrentlyCheckedOut") != null){
            collection.updateOne(Filters.eq("title", input.get(2)), Updates.set("userCurrentlyCheckedOut", null));
            collection.updateOne(Filters.eq("title", input.get(2)), Updates.set("checkedOutLast", member));
            if(lastElement != input.get(2)){
                collection.updateOne(Filters.eq("title", input.get(2)), Updates.push("membersListPrior", member));
            }
        }
        System.out.println(collection);
    }
    public StringBuilder getReturningItems(String input){
        StringBuilder returnString = new StringBuilder("@");
        Document query = new Document("userCurrentlyCheckedOut", input);
        List<Document> documents = collection.find(query).into(new ArrayList<>());
        for(Document document : documents){
            String title = document.getString("title");
            returnString.append(title).append(",");
        }
        return returnString;
    }
    public String review(List<String> input){
        List<String> returnReviews = null;
        Document query = new Document("title", input.get(2));
        System.out.println(input.get(2));
        Document doc = collection2.find(query).first();
        if(doc != null){
            collection2.updateOne(Filters.eq("title", input.get(2)), Updates.push("reviews", input.get(3)));
        }else{
            Document doc1 = new Document("reviews", new ArrayList<String>());
            doc1.append("user", input.get(1));
            doc1.append("title", input.get(2));
            doc1.put("reviews", input.get(3));
            collection2.insertOne(doc1);
        }
        return "hi";
    }

    public void returnAll(List<String> input){
        String member = input.get(0);
        for(int i =2; i < input.size(); i++){
            Document temp = new Document("title", input.get(i));
            Document doc = collection.find(temp).first();
            if(doc.getString("userCurrentlyCheckedOut") != null){
                Object lastElement = null;
                List<Object> membersListPrior = temp.get("membersListPrior", List.class);
                if(membersListPrior!= null) {
                    int lastIndex = membersListPrior.size() - 1;
                    if (lastIndex >= 0) {
                        lastElement = membersListPrior.get(lastIndex);
                    }
                }
                System.out.println("about to return");
                collection.updateOne(Filters.eq("title", input.get(i)), Updates.set("userCurrentlyCheckedOut", null));
                collection.updateOne(Filters.eq("title", input.get(i)), Updates.set("checkedOutLast", member));
                if(lastElement != input.get(i)) {
                    collection.updateOne(Filters.eq("title", input.get(i)), Updates.push("membersListPrior", member));
                }
            }
        }
    }


}
