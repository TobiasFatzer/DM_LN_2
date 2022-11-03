package ch.zhaw;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class App {
    static Logger root = (Logger) LoggerFactory
            .getLogger(Logger.ROOT_LOGGER_NAME);

    static {
        root.setLevel(Level.OFF);
    }

    public static void main(String[] args) {
        Scanner keyScan = new Scanner(System.in);
        System.out.print("Bitte Nutzernamen eingeben \n> "); //No Testing here since all values are allowed
        String name = keyScan.next();
        Quiz quiz = new Quiz(name);
        quiz.launch();


    }


}
