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
    @FXML
    private TableView<TestFile> table;
    @FXML
    private TableColumn<TestFile, String> file;
    @FXML
    private TableColumn<TestFile, String> realClass;
    @FXML
    private TableColumn<TestFile, Double> probability;
    @FXML
    private TextField accuracyVal;
    @FXML
    private TextField precisionVal;
    @FXML
    private TextField trainDirectory;
    @FXML
    private TextField testDirectory;
    double truePositives = 0;
    double trueNegative = 0;
    double falsePositives = 0;
    double numTestingFiles = 0.0;
    double accuracy = 0.0;
    double precision = 0.0;

    // creating treemaps for use in functions
    private TreeMap<String, Double> trainHamFreq = new TreeMap<String, Double>();
    private TreeMap<String, Double> trainSpamFreq = new TreeMap<String, Double>();
    private TreeMap<String, Integer> hamWordCount = new TreeMap<String, Integer>();
    private TreeMap<String, Integer> spamWordCount = new TreeMap<String, Integer>();
    private TreeMap<String, Double> spamWord = new TreeMap<String, Double>();

    /*
     * Checks if a word is valid based on a condition
     * @param word   a word in one of the files
     * @return       true or false based on condition
     */
    private boolean isValidWord(String word) {
        String condition = "^[a-zA-Z]*$";
        if (word.matches(condition)) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Calculates the probability that the word W appears in a ham file (Pr(W|H))
     * Uses a treemap to store words and their counts
     * @param file   a file within the selected directory
     */
    public void prWH(File file) throws IOException {
        File[] files = file.listFiles();
        System.out.println("Number of files ham: " + files.length);

        for (int i = 0; i < files.length; i++) {
            TreeMap<String, Integer> temp = new TreeMap<String, Integer>();

            // put list of words in specific file to temporary map
            Scanner scanner = new Scanner(files[i]);
            while (scanner.hasNext()) {
                String word = scanner.next();
                if (isValidWord(word) && !temp.containsKey(word)) {
                    temp.put(word, 1);
                }
            }

            // iterate through temp treemap and insert word list to a wordCount map
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
            for (Map.Entry<String, Integer> entry : hamWordCount.entrySet()) {
                double resultHam = (double) entry.getValue() / (double) files.length;
                trainHamFreq.put(entry.getKey(), resultHam);
            }
        }
    }

    /*
     * Calculates the probability that the word W appears in a spam file (Pr(W|S))
     * Uses a treemap to store words and their counts
     * @param file   a file within the selected directory
     */
    public void prWS(File file) throws IOException {
        File[] files = file.listFiles();
        assert files != null;
        System.out.println("Number of files spam: " + files.length);

        for (int i = 0; i < files.length; i++) {
            TreeMap<String, Integer> temp = new TreeMap<String, Integer>();

            // put list of words in specific file to temporary map
            Scanner scanner = new Scanner(files[i]);
            while (scanner.hasNext()) {
                String word = scanner.next();
                if (isValidWord(word) && !temp.containsKey(word)) {
                    temp.put(word, 1);
                }
            }

            // iterate through temp treemap and insert word list to a wordCount map
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
            for (Map.Entry<String, Integer> entry : spamWordCount.entrySet()) {
                double resultSpam = (double) entry.getValue() / (double) files.length;
                trainSpamFreq.put(entry.getKey(), resultSpam);
            }
        }
    }

    /*
     * Calculates the probability a word is spam (Pr(S|W))
     * Uses a treemap to store words and their counts
     */
    public void prSW() {
        for (Map.Entry<String, Double> entry : trainSpamFreq.entrySet()) {
            if (trainHamFreq.containsKey(entry.getKey())) {
                double resultFinal = entry.getValue() / (entry.getValue() + trainHamFreq.get(entry.getKey()));
                spamWord.put(entry.getKey(), resultFinal);
            }
        }
    }

    /*
     * Calculates the final probability if the file is spam (Pr(S|F))
     * Keeps count of the number of each type of file
     * @param file   a file within the selected directory
     * @return       the final probability of a file being spam
     */
    public double prSF(File file) throws FileNotFoundException {
        double n = 0.0;
        double threshold = 0.5;
        double probSF;
        Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {
            String word = scanner.next();
            if (isValidWord(word) && spamWord.containsKey(word)) {
                n += Math.log((1 - spamWord.get(word) - Math.log(spamWord.get(word))));
            }
        }
        probSF = 1 / (1 + Math.pow(Math.E, n));
        System.out.println(file + " " + probSF);

        // Accuracy and Precision
        if (file.getParent().contains("spam") && probSF > threshold) {
            truePositives += 1;
        }
        if (file.getParent().contains("ham") && probSF > threshold) {
            falsePositives += 1;
        }
        if (file.getParent().contains("ham") && probSF < threshold) {
            trueNegative += 1;
        }
        numTestingFiles += 1;
        return probSF;
    }

    /*
     * Check what type of directory and calls the appropriate method to process the files
     * @param file   a file within the selected directory
     */
    public void runProcessTraining(File file) {
        if (file.isDirectory()) {
            if (file.getName().equals("ham")) {
                try {
                    prWH(file);
                } catch (IOException error) {
                    error.printStackTrace();
                }
                System.out.println("Finished Ham Folder");
            } else if (file.getName().equals("ham2")) {
                try {
                    prWH(file);
                } catch (IOException error) {
                    error.printStackTrace();
                }
                System.out.println("Finished Ham2 Folder");
            } else if (file.getName().equals("spam")) {
                try {
                    prWS(file);
                } catch (IOException error) {
                    error.printStackTrace();
                }
                System.out.println("Finished Spam Folder");
            } else {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    runProcessTraining(files[i]);
                }
            }
        }
    }

    /*
     * Check what type of directory and calls the appropriate method to process the files
     * Calls corresponding method for calculation of spam probability
     * @param event   the action of pressing a button on the screen
     */
    public void trainAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        File mainDirectory = directoryChooser.showDialog(null);
        if (mainDirectory != null) {
            String path = mainDirectory.getAbsolutePath();
            trainDirectory.setText(path);
            runProcessTraining(mainDirectory);
            prSW();
        } else {
            System.out.println("Invalid Directory");
        }
    }

    /*
     * Check what type of directory and calls the appropriate method to process the files
     * Adds the probability value to the table
     * @param file   a file within the selected directory or a directory itself
     */
    public void runProcessTesting(File file) {
        if (file.isDirectory()) {
            // goes through recursively if a directory of files
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                runProcessTesting(files[i]);
            }
        } else if (file.exists()) {
            double spamProbability = 0.0;
            try {
                spamProbability = prSF(file);
            } catch (IOException error) {
                error.printStackTrace();
            }
            // take probability of the test files and add to tableView
            //had to change decimal formatting to a string instead of double to make .format work
            DecimalFormat decimalFormat = new DecimalFormat("0.00000");
            if (file.getParent().contains("ham")) {
                table.getItems().add(new TestFile(file.getName(), decimalFormat.format(spamProbability), "ham"));
            } else {
                table.getItems().add(new TestFile(file.getName(), decimalFormat.format(spamProbability), "spam"));
            }
        }
    }

    /*
     * Check what type of directory and calls the appropriate method to process the files
     * Calls corresponding method for calculation of spam probability
     * Calculates the overall accuracy and precision and outputs them on the screen
     * Fills each row of the chart with the proper data
     * @param event   the action of pressing a button on the screen
     */
    public void testAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        File mainDirectory = directoryChooser.showDialog(null);
        if (mainDirectory != null) {
            String path = mainDirectory.getAbsolutePath();
            testDirectory.setText(path);
            runProcessTesting(mainDirectory);
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
            // add all the values to the table
            file.setCellValueFactory(new PropertyValueFactory<TestFile, String>("filename"));
            realClass.setCellValueFactory(new PropertyValueFactory<TestFile, String>("actualClass"));
            probability.setCellValueFactory(new PropertyValueFactory<TestFile, Double>("spamProbability"));
        } else {
            System.out.println("Invalid Directory");
        }
    }
}