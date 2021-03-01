package sample;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Controller {
    @FXML private TableView<TestFile> table;
    @FXML private TableColumn<TestFile, String> file;
    @FXML private TableColumn<TestFile, String> realClass;
    @FXML private TableColumn<TestFile, Double> probability;
    @FXML private TextField accuracyVal;
    @FXML private TextField precisionVal;

    // creating hashmaps for use in functions
    private HashMap<String,Double> hamFreq = new HashMap<String, Double>();
    private HashMap<String,Integer> hamWordCount = new HashMap<String,Integer>();
    private HashMap<String,Integer> spamWordCount = new HashMap<String,Integer>();

    private boolean isValidWord(String word){
        String condition = "^[a-zA-Z]*$";
        if (word.matches(condition))
        {
            return true;
        } else {
            return false;
        }
    }

    // calculating the probability that the word W appears in a ham file. (Pr(W|H))
    public void prWH (File file) throws IOException {
        File[] files = file.listFiles();
        System.out.println("Number of files: " + files.length);

        for (int i = 0; i < files.length; i++) {
            HashMap<String, Integer> temp = new HashMap<String, Integer>();

            // put list of words in specific file to temporary map
            Scanner scanner = new Scanner(files[i]);
            while (scanner.hasNext()) {
                String word = scanner.next();
                if (isValidWord(word) && !temp.containsKey(word)) {
                    temp.put(word, 1);
                }
            }

            // iterate through temp hashmap and insert word list to a wordCount map
            for (Map.Entry<String, Integer> entry : temp.entrySet()) {
                // checks if hamWordCount contains the key
                if (hamWordCount.containsKey(entry.getKey())) {
                    // if word exists, adds to the word's count
                    int prevCount = hamWordCount.get(entry.getKey());
                    hamWordCount.put(entry.getKey(), prevCount + 1);
                } else {
                    // if word does not exist, add the word as a new entry
                    hamWordCount.put(entry.getKey(), 1);
                }
            }

            temp.clear();

            // calculates the frequency of word in ham file
            // puts it into a map
            for (Map.Entry<String,Integer> entry: hamWordCount.entrySet()){
                double result = (double)entry.getValue() / (double)files.length;
                hamFreq.put(entry.getKey(), result);
            }
        }
    }

    // calls the frequency calculator methods
    public void runProcess(File file){
        if(file.isDirectory()){
            if(file.getName().equals("ham")){
                try{
                    prWH(file);
                }
                catch(IOException error){
                    error.printStackTrace();
                }
                System.out.println("Finished Ham Folder");
            }
        }
    }
}