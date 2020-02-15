package Invert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreTreatTool {

  private Set<String> FILTER_WORDS;
  private Set<String> noDuplicateWORDS = new HashSet<>();
  private String tempStore;

  private List<String> effectWordPaths = new ArrayList<>();


  private int part = 1;
  private int docId;
  private boolean isFirst = true;


  public PreTreatTool(String filePaths,Set<String> FILTER_WORDS,String tempStore) {
    this.FILTER_WORDS = FILTER_WORDS;
    this.tempStore = tempStore;
    readDataFile(filePaths);
  }

  public List<String> getEFWPaths() {
    return this.effectWordPaths;
  }
  public int currentDocId(){
    return docId;
  }


  private void readDataFile(String filePath) {

      File file = new File(filePath);
      List<List<String>> dataArray = new ArrayList<>();
      List<String> words = new ArrayList<>();

      try {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String str="";
        List<String> tempArray = new ArrayList<>();

        // read by line
        while ((str = in.readLine()) != null) {
          tempArray = dealWithAttr(str);

          if(tempArray.size()!=0){
            dataArray.add(tempArray);
          }
          else{

            //output to the txt
            for (List<String> array : dataArray) {
              for (String word : array) {
                if(word.trim().length()>1)
                  words.add(word.trim());
              }
            }

            String name = words.get(1);

            if(isFirst){
              docId = Integer.parseInt(name);
              isFirst = false;
            }

            List<String> tempWords = filterWords(words);
            writeOutOperation(tempWords,
                    tempStore+ name);
            effectWordPaths.add(tempStore+name);
            words = new ArrayList<>();
            dataArray = new ArrayList<>();
            noDuplicateWORDS = new HashSet<>();
          }

        }

        //last line
        //output to the txt
        for (List<String> array : dataArray) {
          for (String word : array) {
            if(word.trim().length()>1)
              words.add(word.trim());
          }
        }
        String name = words.get(1);


        List<String> tempWords = filterWords(words);

        writeOutOperation(tempWords,
                tempStore+ name);

        effectWordPaths.add(tempStore+name);


        words = new ArrayList<>();
        dataArray = new ArrayList<>();



        noDuplicateWORDS = new HashSet<>();

        in.close();
      }
      catch (IOException e) {
        e.getStackTrace();
      }

  }

  private List<String> filterWords(List<String> words) {

    // adj pattern
    Pattern adjPattern;
    // digit pattern
    Pattern numberPattern;

    Matcher adjMatcher;
    Matcher numberMatcher;

    adjPattern = Pattern.compile(".*(ly$|ful$|ing$)");
    numberPattern = Pattern.compile("[0-9]+(.[0-9]+)?");

    String w = "";

    for (int i = 0; i < words.size(); i++) {
      w = words.get(i).trim();
      if(w.length()==0){
        continue;
      }

      if(w.length()<2){
        continue;
      }


      char first = w.charAt(0);
      char last = w.charAt(w.length()-1);

      if(!(Character.isLetterOrDigit(first) && first != '\'' )){
        w = w.substring(1);
      }
      if(!(Character.isLetterOrDigit(last) && last != '\'')){
        w = w.substring(0,w.length()-1);
      }
      //replace ( ) to ""
      w = w.replace("(","");
      w = w.replace(")","");
      if(w.length()<2){
        continue;
      }

      w = w.toLowerCase();


      if(FILTER_WORDS.contains(w)){
        continue;
      }

      adjMatcher = adjPattern.matcher(w);
      numberMatcher = numberPattern.matcher(w);

      // adj OR adv OR digit-numbers
      if (adjMatcher.matches() || numberMatcher.matches()) {
        continue;
      }

      noDuplicateWORDS.add(w);

    }

    return new ArrayList<>(noDuplicateWORDS);
  }

  private void writeOutOperation(List<String> buffer, String filePath) {
    StringBuilder strBuilder = new StringBuilder();
    Set<String> set = new HashSet<>();
    for (String word : buffer) {
      if(!set.contains(word)){
        strBuilder.append(word);
        strBuilder.append("@");
        set.add(word);
      }
    }

    try {
      File file = new File(filePath);
      PrintStream ps = new PrintStream(new FileOutputStream(file));
      ps.print(strBuilder.toString());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private List<String> dealWithAttr(String str){

    List<String> tempArray = new ArrayList<>();
    if(str.length() == 0){
      // END OF A FILE
      part = 1;
      return tempArray;
    }
    // Only one word in a line
    if(str.length() == 1){
      tempArray.add("A");
      return tempArray;
    }

    char first = str.charAt(0);
    char second = str.charAt(1);

    //PN
    if(first=='P'&&second=='N'){
      part = 1;
      tempArray = Arrays.asList(str.split(" ")[1]);
    }
    //RN
    else if(first=='R'&&second=='N'){
      part = 2;
      // TODO
      tempArray = Arrays.asList(str.split(" ")[1]);
    }
    //AN
    else if(first=='A'&&second=='N'){
      part = 3;
      tempArray = Arrays.asList(str.split(" ")[1]);
    }
    //AU
    else if(first=='A'&&second=='U'){
      part = 4;
      tempArray = Arrays.asList(str.substring(3).split("\\s+"));
    }
    //TI
    else if(first=='T'&&second=='I'){
      part = 4;
      tempArray = Arrays.asList(str.substring(3).split("\\s+"));
    }
    //SO
    else if(first=='S'&&second=='O'){
      part = 6;
      tempArray = Arrays.asList(
              str.substring(3).split("\\s+")
    );
    }
    //MJ
    else if(first=='M'&&second=='J'){
      part = 7;
      tempArray = Arrays.asList(str.substring(3).split("[:\\s+]+"));
    }
    //MN
    else if(first=='M'&&second=='N'){
      part = 8;
      tempArray = Arrays.asList(str.substring(3).split("[:\\s+]+"));
    }
    //AB
    else if((first=='A'&&second=='B')||(first=='E'&&second=='X')){
      part = 9;
      tempArray = Arrays.asList(str.substring(3).split("\\s+"));
    }
    //RF
    else if(first=='R'&&second=='F'){
      part = 10;
      tempArray = Arrays.asList(str.substring(3).split("  "));

    }
    //CT
    else if(first=='C'&&second=='T'){
      part = 11;
      tempArray = Arrays.asList(str.substring(5).split("  "));
    }
    // Others
    else{
      //TI
      if(part == 4){
        tempArray = Arrays.asList(str.substring(3).split("\\s+"));
      }
      //SO
      if(part == 6){
        tempArray = Arrays.asList(str.substring(3).split("\\s+"));
      }
      //MJ
      else if( part == 7){
        tempArray = Arrays.asList(str.substring(3).split("[:\\s+]+"));
      }
      //MN
      else if(part == 8){
        tempArray = Arrays.asList(str.substring(3).split("[:\\s+]+"));
      }
      //AB
      else if(part == 9){
        tempArray = Arrays.asList(str.substring(3).split("\\s+"));
      }
      //RF
      else if(part == 10){
        tempArray = Arrays.asList(str.substring(3).split("  "));
      }
      //CT
      else if(part == 11){
        tempArray = Arrays.asList(str.substring(5).split("  "));
      }

    }
    return tempArray;
  }
}