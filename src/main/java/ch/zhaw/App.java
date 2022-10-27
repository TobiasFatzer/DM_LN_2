package ch.zhaw;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class App
{
    static Logger root = (Logger) LoggerFactory
            .getLogger(Logger.ROOT_LOGGER_NAME);

    static {
        root.setLevel(Level.INFO);
    }

    public static void main(String[] args )
    {
        Scanner keyScan = new Scanner(System.in);

        MongoCollection<Document> col = ConnectionHandler.getDatabase("LN2").getCollection("food");
        System.out.println(col.countDocuments());

        System.out.print("Please enter your name \n> ");
        //String name = keyScan.next();
        String name = "t"; //TODO XXX Delete
        Quiz quiz = new Quiz(name, col);
        quiz.launch();


    }



}
