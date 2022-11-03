package ch.zhaw;

import ch.zhaw.exceptions.NotEnoughDatabaseEntriesException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.*;

/**
 * Handles the whole Quiz Logic
 */
public class Quiz {

    private String userName;
    private MongoCollection<Document> foodCollection;

    private Scanner scanner;

    private int points = 0;

    /**
     * Constructor for Quiz Class. Will also Initiate a Scanner to be used in the Class
     *
     * @param user
     */
    public Quiz(String user) {
        this.setUserName(user);
        this.setFoodCollection(ConnectionHandler.getDatabase(Settings.databaseName).getCollection(Settings.foodCollection));
        this.setScanner(new Scanner(System.in));


    }

    /**
     * Initializes Game Launch and Handles Game Property Selection
     */
    public void launch() {
        System.out.println(DB.checkIfUserAlreadyHasEntries(this.getUserName()) ? "Hallo " + this.getUserName() + "! Schön dich wiederzusehen" : "Hallo " + this.getUserName() + "!"); //Es wird davon ausgegangen das ein userName uniqe ist (was normalerweise nicht der Fall wäre).
        System.out.println("Bitte wähle eine Kategorie aus:");
        List<Object> returnValue = this.printKeySet(DB.getFoodPropertiesAsResultList(this.getFoodCollection()).keySet());
        int selection = this.getUserSelection(Settings.firstPropertySelectionSize);
        this.startGameOnProperty(returnValue.get(selection).toString());

    }


    /**
     * Starts and Runs Main Game Loop on Selected Property for Settings.amountOfGameRuns Runs
     *
     * @param selectedProperty
     */
    private void startGameOnProperty(String selectedProperty) {
        System.out.println("Du hast " + selectedProperty + " ausgewählt.");
        long gameStart = System.nanoTime();
        for (int i = 0; i < Settings.amountOfGameRuns; i++) {
            System.out.println("\nRunde " + (i + 1) + " beginnt...");
            System.out.println("Welches dieser Essen hat am meisten " + selectedProperty + "?");
            Document rightDocument = DB.getCorrectPropertyBasedResult(this.getFoodCollection(), selectedProperty);
            List<Document> documents = null;
            try {
                documents = DB.getFalsePropertyBasedResult(this.getFoodCollection(), selectedProperty, rightDocument);
            } catch (NotEnoughDatabaseEntriesException e) {
                System.out.println(e);
                System.out.println("Resetting...");
                this.points = 0;
                System.out.println("Restarting...");
                System.out.print("\033[2J\033[1;1H");
                System.out.println("Restart Complete");
                this.launch();
                break;
            }
            documents.add(rightDocument);
            Collections.shuffle(documents);
            this.printAnswerOptions(documents);


            if (rightDocument == documents.get(this.getUserSelection(3))) {
                System.out.println("Korrekt. Die Produkte enthalten pro 100g essbarer Anteil die folgende Menge " + selectedProperty + ":");
                this.printResult(documents, selectedProperty);
                this.points = this.points + 1;
            } else {
                System.out.println("Leider falsch. Die Produkte enthalten pro 100g essbarer Anteil die folgende Menge " + selectedProperty + ":");
                this.printResult(documents, selectedProperty);
            }


        }
        long gameEnd = System.nanoTime();
        long duration = (gameEnd - gameStart) / 1000000;
        UUID gameID = DB.insertQuizRun(this.getUserName(), duration, this.points, selectedProperty);
        System.out.println(this.getUserName() + " du hast einen Score von " + this.points + " erreicht und dafür " + duration + "ms benötigt");
        System.out.println("Das ist die momentane TOP Drei Rangliste: \n");
        this.printScoreboard(DB.getScoreBoard().subList(0, 3));
        int overallPlaced = DB.getScoreBoard().stream().filter(x -> gameID.equals(x.get("gameRunUUID"))).map(x -> DB.getScoreBoard().indexOf(x)).toList().get(0) + 1;
        int propertyPlaced = DB.getScoreBoard(selectedProperty).stream().filter(x -> gameID.equals(x.get("gameRunUUID"))).map(x -> DB.getScoreBoard(selectedProperty).indexOf(x)).toList().get(0) + 1;

        System.out.println("Damit bist du " + propertyPlaced + ". in deiner Kategorie " + selectedProperty + " sowie " + overallPlaced + ". Überalles");

    }

    private void printResult(List<Document> documents, String selectedProperty) {
        for (int i = 0; i < documents.size(); i++) {
            System.out.println(i + 1 + ") " + documents.get(i).get("Name") + " \t " + documents.get(i).get(selectedProperty));
        }
    }

    /**
     * Prints Options for Answers
     *
     * @param documents
     */
    private void printAnswerOptions(List<Document> documents) {
        for (int i = 0; i < documents.size(); i++) {
            System.out.println(i + 1 + ") " + documents.get(i).get("Name"));
        }
    }

    private void printScoreboard(List<Document> documents) {
        for (int i = 0; i < documents.size(); i++) {
            System.out.println(i + 1 + ") \t" + documents.get(i).get("userName") + "\t" + documents.get(i).get("points") + "\t" + documents.get(i).get("duration") + "ms");
        }
    }

    /**
     * Gets user Selection
     *
     * @param max
     * @return userSelection
     */
    private int getUserSelection(int max) {
        System.out.println("Bitte wählen Sie einen Wert zwischen 1 und " + max);
        int result = this.getScanner().nextInt();
        if (result > max) {
            System.out.println("Bitte eine korrekte Eingabe tätigen!");
            this.getUserSelection(max);
        }
        return result - 1;

    }

    /**
     * Prints KeySet from first property Selection
     *
     * @param keySet
     * @return
     */
    private List<Object> printKeySet(Set<String> keySet) {
        List<Object> setAsList = Arrays.asList(keySet.toArray());
        Collections.shuffle(setAsList);
        for (int i = 0; i < Settings.firstPropertySelectionSize; i++) {
            System.out.println(i + 1 + ") " + setAsList.get(i));
        }

        return setAsList;

    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public MongoCollection<Document> getFoodCollection() {
        return foodCollection;
    }

    public void setFoodCollection(MongoCollection<Document> foodCollection) {
        this.foodCollection = foodCollection;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }
}
