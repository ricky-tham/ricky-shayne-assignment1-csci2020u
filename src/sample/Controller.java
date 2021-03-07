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
import java.util.*;

public class Controller {
    @FXML private TableView<TestFile> table;
    @FXML private TableColumn<TestFile, String> file;
    @FXML private TableColumn<TestFile, String> realClass;
    @FXML private TableColumn<TestFile, Double> probability;
    @FXML private TextField accuracyVal;
    @FXML private TextField precisionVal;
    @FXML private TextField trainDirectory;
    @FXML private TextField testDirectory;
    double truePositives = 0;
    double trueNegative = 0;
    double falsePositives = 0;
    double numTestingFiles = 0.0;
    double accuracy = 0.0;
    double precision = 0.0;

    // creating hashmaps for use in functions
    private HashMap<String,Double> hamFreq = new HashMap<String, Double>();
    private HashMap<String,Double> spamFreq = new HashMap<String, Double>();
    private HashMap<String,Integer> hamWordCount = new HashMap<String,Integer>();
    private HashMap<String,Integer> spamWordCount = new HashMap<String,Integer>();
    private HashMap<String,Double> spamWord = new HashMap<String, Double>();

    // checks in word is valid
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
        System.out.println("Number of files ham: " + files.length);

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
                double resultHam = (double)entry.getValue() / (double)files.length;
                hamFreq.put(entry.getKey(), resultHam);
            }
        }
    }

    // calculating the probability that the word W appears in a spam file. (Pr(W|S))
    public void prWS (File file) throws IOException {
        File[] files = file.listFiles();
        System.out.println("Number of files spam: " + files.length);

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
                if (spamWordCount.containsKey(entry.getKey())) {
                    // if word exists, adds to the word's count
                    int prevCount = spamWordCount.get(entry.getKey());
                    spamWordCount.put(entry.getKey(), prevCount + 1);
                } else {
                    // if word does not exist, add the word as a new entry
                    spamWordCount.put(entry.getKey(), 1);
                }
            }

            temp.clear();

            // calculates the frequency of word in spam file
            // puts it into a map
            for (Map.Entry<String,Integer> entry: spamWordCount.entrySet()){
                double resultSpam = (double)entry.getValue() / (double)files.length;
                spamFreq.put(entry.getKey(), resultSpam);
            }
        }
    }

    // calculating Pr(S|W)
    public void prSW(){
        for(Map.Entry<String, Double> entry: spamFreq.entrySet()){
            if(hamFreq.containsKey(entry.getKey())){
                double resultFinal = entry.getValue() / (entry.getValue() + hamFreq.get(entry.getKey()));
                spamWord.put(entry.getKey(), resultFinal);
            }
        }
    }

    public double prSF(File file) throws FileNotFoundException{
        double result = 0.0;
        double threshold = 0.5;
        double probSF;
        Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {
            String word = scanner.next();
            if (isValidWord(word) && spamWord.containsKey(word)) {
                result += Math.log( (1-spamWord.get(word) - Math.log(spamWord.get(word))));
            }
        }
        //System.out.println(result);


        probSF = 1 / (1 + Math.pow(Math.E, result));
        //System.out.println(probSF);

        // Accuracy and Precision
        if (file.getParent().contains("spam") && probSF > threshold){
            truePositives += 1;
        }
        if (file.getParent().contains("ham") && probSF > threshold){
            falsePositives += 1;
        }
        if (file.getParent().contains("ham") && probSF < threshold){
            trueNegative += 1;
        }
        numTestingFiles += 1;
        return probSF;
    }

    // calls the frequency calculator methods for the training directory
    public void runProcessTraining(File file){
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
            else if(file.getName().equals("ham2")){
                try{
                    prWH(file);
                }
                catch(IOException error){
                    error.printStackTrace();
                }
                System.out.println("Finished Ham2 Folder");
            }
            else if(file.getName().equals("spam")){
                try{
                    prWS(file);
                }
                catch(IOException error){
                    error.printStackTrace();
                }
                System.out.println("Finished Spam Folder");
            }
            else{
                File[] files = file.listFiles();
                for(int i = 0; i < files.length; i++){
                    runProcessTraining(files[i]);
                }
            }
        }
    }

    // button action for train
    public void trainAction(ActionEvent event){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        File a = directoryChooser.showDialog(null);
        if(a != null){
            String path = a.getAbsolutePath();
            trainDirectory.setText(path);
            runProcessTraining(a);
            prSW();
        }
        else{
            System.out.println("Invalid Directory");
        }
    }

    public void runProcessTesting(File file){
        if (file.isDirectory()){
            // goes through recursively if a directory of files
            File[] files = file.listFiles();
            for(int i = 0; i < files.length; i++){
                runProcessTesting(files[i]);
            }
        }
        else if(file.exists()){
            double spamProbability = 0.0;
            try{
                spamProbability = prSF(file);
            }
            catch(IOException error){
                error.printStackTrace();
            }
            // take probability of the test files and add to tableView
            //had to change decimal formatting to a string instead of double to make .format work
            DecimalFormat decimalFormat = new DecimalFormat("0.00000");
            if(file.getParent().contains("ham")){
                table.getItems().add(new TestFile(file.getName(), decimalFormat.format(spamProbability), "ham"));
            }
            else{
                table.getItems().add(new TestFile(file.getName(), decimalFormat.format(spamProbability), "spam"));
            }
        }
    }

    public void testAction(ActionEvent event){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        File a = directoryChooser.showDialog(null);
        if(a != null){
            String path = a.getAbsolutePath();
            testDirectory.setText(path);
            runProcessTesting(a);
            System.out.println(numTestingFiles);
            System.out.println("truePositives: " + truePositives);
            System.out.println("falsePositives: " + falsePositives);
            System.out.println("trueNegative: " + trueNegative);
            // Calculating output for accuracy and precision
            DecimalFormat decimalFormat = new DecimalFormat(("0.00000"));
            accuracy = (truePositives + falsePositives) / numTestingFiles;
            precision = truePositives / (falsePositives + trueNegative);
            accuracyVal.setText(decimalFormat.format(accuracy));
            precisionVal.setText(decimalFormat.format(precision));
            // add all the values to the tableView
            file.setCellValueFactory(new PropertyValueFactory<TestFile, String>("filename"));
            realClass.setCellValueFactory(new PropertyValueFactory<TestFile, String>("actualClass"));
            probability.setCellValueFactory(new PropertyValueFactory<TestFile, Double>("spamProbability"));
        }
        else{
            System.out.println("Invalid Directory");
        }
    }
}