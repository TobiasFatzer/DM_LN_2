package ch.zhaw;

import ch.zhaw.exceptions.NotEnoughDatabaseEntriesException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.sql.Timestamp;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;


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

    public static Document getCorrectPropertyBasedResult(MongoCollection<Document> col, String selection) {
        Bson filter = and(not(eq(selection, null)), not(eq(selection, 0)));
        return shuffle(col.find(filter).into(new ArrayList<Document>())).get(1);
    }

    public static List<Document> getFalsePropertyBasedResult(MongoCollection<Document> col, String selection, Document trueResult) throws NotEnoughDatabaseEntriesException {
        Bson filter = and(
                not(eq(selection, null)),
                not(eq(selection, 0)),
                lt(selection, Double.parseDouble(trueResult.get(selection).toString()) / 2),
                eq("Bezugseinheit", trueResult.get("Bezugseinheit")));
        ArrayList<Document> shuffledReturnList = shuffle(col.find(filter).into(new ArrayList<Document>()));
        if (shuffledReturnList.size() <= 2) {
            throw new NotEnoughDatabaseEntriesException(
                    "Could not find enough entries for " + selection + " to launch this Game"
            );
        }
        return shuffledReturnList.subList(0, 2);
    }


    private static ArrayList<Document> shuffle(ArrayList<Document> ArrayListInput) {
        Collections.shuffle(ArrayListInput);
        return ArrayListInput;
    }

    public static UUID insertQuizRun(String userName, long duration, int points, String selectedProperty) {
        UUID gameUUID = UUID.randomUUID();
        Document document = new Document();
        document.append("userName", userName);
        document.append("duration", duration);
        document.append("points", points);
        document.append("category", selectedProperty);
        document.append("timeStamp", new Timestamp(System.currentTimeMillis()).getTime());
        document.append("gameRunUUID", gameUUID);
        ConnectionHandler.getDatabase("LN2").getCollection("scoreboard").insertOne(document);
        return gameUUID;
    }

    /**
     * Returns the whole Scoreboard sorted after points and duration
     *
     * @return
     */
    public static ArrayList<Document> getScoreBoard() {
        return ConnectionHandler.getDatabase("LN2").getCollection("scoreboard").find().sort(descending("points", "duration")).into(new ArrayList<Document>());
    }

    /**
     * Returns the whole Scoreboard of the selected property sorted after points and duration
     *
     * @param property
     * @return
     */
    public static ArrayList<Document> getScoreBoard(String property) {
        return ConnectionHandler.getDatabase("LN2").getCollection("scoreboard").find(eq("category", property)).sort(descending("points", "duration")).into(new ArrayList<Document>());
    }

    public static boolean checkIfUserAlreadyHasEntries(String userName) {
        return ConnectionHandler.getDatabase("LN2").getCollection("scoreboard").find(eq("userName", userName)).into(new ArrayList<Document>()).size() > 0;
    }
}
