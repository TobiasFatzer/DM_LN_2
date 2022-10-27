package ch.zhaw;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;


public class DB {
    public static Document getFoodPropertiesAsResultList(MongoCollection<Document> col) {
        Document query = col.aggregate(Arrays.asList(new Document("$project",
                new Document("_id", 0L)
                        .append("Name", 0L)
                        .append("Kategorie", 0L)
                        .append("Bezugseinheit", 0L)
                        .append("Kalorien", 0L)))).first();

        return query;
    }
    public static boolean checkIfSelectionHasEnoughValues(MongoCollection<Document> col, String selection) {
        Bson filter = and(not(eq(selection, null)), not(eq(selection, 0)));
        return col.find(filter).into(new ArrayList<Document>()).stream().count() > 3;
    }
    public static Document getCorrectPropertyBasedResult(MongoCollection<Document> col, String selection) {
        Bson filter = and(not(eq(selection, null)), not(eq(selection, 0)));
        return shuffle(col.find(filter).into(new ArrayList<Document>())).get(1);
    }

    public static List<Document> getFalsePropertyBasedResult(MongoCollection<Document> col, String selection, Document trueResult) {
        Bson filter = and(
                not(eq(selection, null)),
                not(eq(selection, 0)),
                lt(selection, Double.parseDouble(trueResult.get(selection).toString()) / 2),
                eq("Bezugseinheit", trueResult.get("Bezugseinheit")));
        return shuffle(col.find(filter).into(new ArrayList<Document>())).subList(0,2);
    }


    private static ArrayList<Document> shuffle(ArrayList<Document> ArrayListInput) {
        Collections.shuffle(ArrayListInput);
        return ArrayListInput;
    }

    public static void insertQuizRun(String userName, long duration, Integer points, String selectedProperty) {
        Document document = new Document();
        document.append("userName", userName);
        document.append("duration", duration);
        document.append("point", points);
        document.append("category", selectedProperty);
        ConnectionHandler.getDatabase("LN2").getCollection("scoreboard").insertOne(document);
        System.out.println("Document inserted successfully");
    }

    public static void getTopThree() {
        ConnectionHandler.getDatabase("LN2").getCollection("scoreboard").find().sort(ascending("duration"));
    }
}
