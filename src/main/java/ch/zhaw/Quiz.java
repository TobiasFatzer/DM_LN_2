package ch.zhaw;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.*;

/**
 * Handles the whole Quiz Logic
 */
public class Quiz {

    private String userName;
    private  MongoCollection<Document> collection;

    private Scanner scanner;

    private Integer points = 0;
    private long duration;

    /**
     * Constructor for Quiz Class. Will also Initiate a Scanner to be used in the Class
     * @param user
     * @param col
     */
    public Quiz(String user, MongoCollection<Document> col) {
        this.setUserName(user);
        this.setCollection(col);
        this.setScanner(new Scanner(System.in));


    }

    /**
     * Initializes Game Launch and Handles Game Property Selection
     */
    public void launch() {
        List<Object> returnValue = this.printKeySet(DB.getFoodPropertiesAsResultList(this.getCollection()).keySet()); //TOOD Check if vaue has enough entries
        Integer selection = this.getUserSelection(Settings.firstPropertySelectionSize);
        if(DB.checkIfSelectionHasEnoughValues(this.getCollection(), returnValue.get(selection).toString())) {
            this.startGameOnProperty(returnValue.get(selection).toString());
        }

         }


    /**
     * Starts and Runs Main Game Loop on Selected Property for 5 Runs
     * @param selectedProperty
     */
    private void startGameOnProperty(String selectedProperty) {
        long gameStart = System.nanoTime();
        for (int i = 0; i < Settings.amountOfGameRuns; i++) {
            Document rightDocument = DB.getCorrectPropertyBasedResult(this.getCollection(), selectedProperty);
            List<Document> documents = DB.getFalsePropertyBasedResult(this.getCollection(), selectedProperty, rightDocument);
            documents.add(rightDocument); //TODO Shuffle documents
            this.printAnswerOptions(documents);


            if(rightDocument == documents.get(this.getUserSelection(3))) {
                System.out.println("Korrekt. Die Produkte enthalten pro 100g essbarer Anteil die folgende Menge " + selectedProperty + ":");
                this.printResult(documents, selectedProperty);
                this.points = this.points + 1;
            } else {
                System.out.println("Leider falsch. Die Produkte enthalten pro 100g essbarer Anteil die folgende Menge " + selectedProperty + ":");
                this.printResult(documents, selectedProperty);
            }

        }
        long gameEnd = System.nanoTime();
        this.duration =  (gameEnd - gameStart) / 1000000;
        DB.insertQuizRun(this.getUserName(), this.duration, this.points, selectedProperty);
        DB.getTopThree();
        System.out.println(this.getUserName() + " du hast " + this.points + " erreicht und " + this.duration + "ms benötigt");
        System.out.println("Damit bist du xyz in deiner Kategorie " + selectedProperty + " sowie xyz überalles");
    }

    private void printResult(List<Document> documents, String selectedProperty) {
        for (int i = 0; i < documents.size(); i++) {
            System.out.println(i+1 + ") " + documents.get(i).get(selectedProperty));
        }
    }

    /**
     *Gets user Selection
     * @param max
     * @return userSelection
     */
    private Integer getUserSelection(int max) {
        System.out.println("Please select result between 1 and " + max);
        //return this.getScanner().nextInt(); TODO Uncomment
        return 1;

    }

    /**
     * Prints KeySet from first property Selection
     * @param keySet
     * @return
     */
    private List<Object> printKeySet(Set<String> keySet) {
        List<Object> setAsList = Arrays.asList(keySet.toArray());
        Collections.shuffle(setAsList);
        for (int i = 0; i < Settings.firstPropertySelectionSize; i++) {
            System.out.println(i + 1 + ") " + setAsList.get(i));
        }

        System.out.println("Please select a value \n>");
        return setAsList;

    }

    /**
     * Prints Options for Answers
     * @param documents
     */
    private void printAnswerOptions(List<Document> documents){
        for (int i = 0; i < documents.size(); i++) {
            System.out.println(i + ") " + documents.get(i).get("Name"));
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void setCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }
}
