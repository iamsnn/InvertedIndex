package Invert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SPIMI{

  // location of the output file
  private String outputFilePath = "";
  // words of each file
  private List<String> wordsInFile = new ArrayList<>();
  // store into the buffer
  // "term - docId"
  private List<String[]> buffers = new ArrayList<>();
  // store "term - docId"
  private Map<String, String> map = new HashMap<>();

  int docIndex = 1;


  public SPIMI(List<String> wordsInFile,String outputFilePath,int docIndex) {
    this.wordsInFile = wordsInFile;
    this.outputFilePath = outputFilePath;
    this.docIndex = docIndex;
  }

  // Read from the file
  // "term term term term"
  private List<String> readDataFile(String filePath) {
    File file = new File(filePath);
    List<String[]> dataArray = new ArrayList<>();
    List<String> words = new ArrayList<>();

    try {
      BufferedReader in = new BufferedReader(new FileReader(file));
      String str;
      String[] tempArray;
      // read by lines
      while ((str = in.readLine()) != null) {

        tempArray = str.split("@");
        dataArray.add(tempArray);
      }
      in.close();
    } catch (IOException e) {
      e.getStackTrace();
    }
    Set<String> set = new HashSet<>();
    // put all words into the list
    for (String[] array : dataArray) {
      for (String word : array) {
          words.add(word);

      }
    }

    return words;
  }

  // add a term to the buffer
  // word:term[0] docId:term[1]
  private void addTerm(String[] term) {
      String wordName = term[0];
      String docId = term[1];
      // No duplicate
      if (!map.containsKey(wordName) && wordName.length()!=0) {
      map.put(wordName, docId);
    } else if(map.containsKey(wordName)) {
      map.put(wordName, map.get(wordName) + "," + docId);
    }

  }

  // build Invert Index for a single file
  private void invertIndex(List<Document> docs){
    List<String> data;
    String[] recordData;

    for(Document tempDoc: docs){
      data = tempDoc.effectWords;

      for(String word: data){
        recordData = new String[2];
        recordData[0] = word;
        recordData[1] = tempDoc.docId;

        addTerm(recordData);
      }
    }

      writeToDisk(outputFilePath+(docIndex+""));
      map = new HashMap<>();
      buffers = new ArrayList<>();

  }

  //store to the disk
  private void writeToDisk(String filePath) {

    for(Map.Entry<String, String> entry:map.entrySet()){
      buffers.add(new String[]{entry.getKey(),entry.getValue()});
    }

    StringBuilder strBuilder = new StringBuilder();

    // add data in the buffer to the disk
    for(String[] array: buffers){

      strBuilder.append(array[0]);
      strBuilder.append("#");
      strBuilder.append(array[1]);
      strBuilder.append("@");
    }

    try {
      File file = new File(filePath);
      PrintStream ps = new PrintStream(new FileOutputStream(file));
      ps.println(strBuilder.toString());

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  //create the result
  public void createInvertIndex(int startDocId){

    //TODO : change docID to its index
    int docId = startDocId;

    Document tempDoc;
    List<Document> docs = new ArrayList<>();

    for(String path: wordsInFile){
      // get words by file
      List<String> words = readDataFile(path);
      tempDoc = new Document(words, path, docId+"");

      docId++;
      docs.add(tempDoc);
    }

    invertIndex(docs);
  }
}