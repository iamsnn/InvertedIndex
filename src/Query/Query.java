package Query;

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

import Invert.Util;

//Graph
public class Query {

  private static String queryLoc = "D:\\NEUALL\\2020 information " +
          "retrieval\\proj1\\springMVC\\testdata\\";
  private static String UPLOAD_DIRECTORY = "D:\\NEUALL\\2020 information " +
          "retrieval\\proj1\\springMVC\\out\\artifacts\\springMVC_war_exploded\\sources\\";
  private static String result = UPLOAD_DIRECTORY + "IIndex\\Result";
  private static String stopWord = UPLOAD_DIRECTORY + "stopWords.txt";
  private static String tempStoreListAndMap = UPLOAD_DIRECTORY + "Rank\\";

  public static void main(String[] args) {
    String src = dealWithQuery(queryLoc + "cfquery",
            queryLoc);

    List<Double> YAxis = new ArrayList<>();

    List<Double> Precision = new ArrayList<>();
    List<Double> Recall = new ArrayList<>();

    int i =0 ;
    for (String s : src.split("#")) {

      String input = s.split("@")[0].substring(0, s.split("@")[0].length() - 1);
      String res = Util.searchWord(input, result,
              stopWord,
              tempStoreListAndMap + "list",
              tempStoreListAndMap + "map");

      //Calculate P@10
      //countAccuracy(s,res,YAxis);

      //Calculate Recall-Percision Curve
      precisionRecall(s, res, Precision, Recall);
      System.out.println("The "+i+"th Query is being processed");
      i++;
    }

    //Store P@10 result
    //writeP10ToDisk(YAxis);

    //Store Recall-Percision Curve store
    writePRToDisk(Precision, Recall);

  }

  // "query@id@id@id#"
  private static String dealWithQuery(String queryFilePath, String outputFilePath) {
    List<String> dataArray = new ArrayList<>();
    try {
      BufferedReader in = new BufferedReader(new FileReader(queryFilePath));
      String str;
      int start = 0;
      StringBuilder sb = new StringBuilder("");
      // read by lines
      while ((str = in.readLine()) != null) {
        //System.out.println(str.length());
        if (str.length() < 1) {
          continue;
        }

        if (str.length() == 1) {
          start = 0;
          sb.deleteCharAt(sb.length() - 1);
          sb.append("#");
          dataArray.add(sb.toString());
          sb = new StringBuilder();
          continue;
        }

        char signal = str.charAt(1);
        if (signal == 'U') {
          start = 1;
          sb.append(str.substring(3));
        } else if (signal == 'D') {
          start = 2;
          sb.append("@");
          for (String s : str.substring(4).trim().split("  ")) {
            sb.append(s.split(" ")[0]);
            sb.append("@");
          }
        } else if (signal == 'R') {
          start = 0;
        } else if (start == 1) {
          sb.append(str.substring(3));
        } else if (start == 2) {
          for (String s : str.trim().split("  ")) {
            sb.append(s.split(" ")[0]);
            sb.append("@");
          }
        }

      }

      in.close();
    } catch (IOException e) {
      e.getStackTrace();
    }

    StringBuilder strBuilder = new StringBuilder("");

    for (String s : dataArray) {
      strBuilder.append(s);
    }

    //output to disk
    try {
      File file = new File(outputFilePath + "query");
      PrintStream ps = new PrintStream(new FileOutputStream(file));
      ps.println(strBuilder.toString());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    return strBuilder.toString();
  }

  private static void countAccuracy(String s,
                                    String res, List<Double> YAxis) {
    String[] strings = s.split("@");
    Set<Integer> set = new HashSet<>();
    for (int i = 1; i < strings.length; i++) {
      if (strings[i].length() > 0) {
        set.add(Integer.parseInt(strings[i]));
      }
    }


    int count = 1;
    for (String num : res.split(",")) {
      if(num.length()==0){
        continue;
      }
      if (set.contains(Integer.parseInt(num))) {
        count++;
      }
    }
    double r = (count) / (double) 10;
    if(r>1){
      r = 1.0;
    }
    YAxis.add(r);

  }

  private static void precisionRecall(String s,
                                      String res, List<Double> Precision, List<Double> Recall) {

    int totalRelevant = 0;
    //s is all relevant documents
    String[] strings = s.split("@");
    Set<Integer> set = new HashSet<>();
    for (int i = 1; i < strings.length; i++) {
      if (strings[i].length() > 0) {
        totalRelevant++;
        set.add(Integer.parseInt(strings[i]));
      }
    }

    //res is all searched documents
    int searchedAndRelevant = 0;
    int searchedAndIrrelevant = 0;
    for (String num : res.split(",")) {
      if(num.length()==0){
        continue;
      }
      if (set.contains(Integer.parseInt(num))) {
        searchedAndRelevant++;
      } else {
        searchedAndIrrelevant++;
      }
    }
    int totalSearched = searchedAndRelevant + searchedAndIrrelevant;

    //System.out.println("totalRelevant:"+totalRelevant+" ,
    // "+"searchedAndRelevant:"+searchedAndRelevant+" , "+"searchedAndIrrelevant:"+searchedAndIrrelevant);

    Precision.add(searchedAndRelevant / (double) totalSearched);
    Recall.add(searchedAndRelevant / (double) totalRelevant);

  }

  private static void writeP10ToDisk(List<Double> YAxis) {
    StringBuilder strBuilder = new StringBuilder("");
    for (double s : YAxis) {
      strBuilder.append(s);
      strBuilder.append("\n");
    }

    //output to disk
    try {
      File file = new File(queryLoc + "YAxis");
      PrintStream ps = new PrintStream(new FileOutputStream(file));
      ps.println(strBuilder.toString());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void writePRToDisk(List<Double> Precision, List<Double> Recall) {
    StringBuilder strBuilder = new StringBuilder("");
    for (int i = 0; i < Precision.size(); i++) {
      strBuilder.append(Precision.get(i));
      strBuilder.append("\n");
    }

    //output to disk
    try {
      File file = new File(queryLoc + "PrecisionRatio");
      PrintStream ps = new PrintStream(new FileOutputStream(file));
      ps.println(strBuilder.toString());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    strBuilder = new StringBuilder("");
    for (int i = 0; i < Recall.size(); i++) {
      strBuilder.append(Recall.get(i));
      strBuilder.append("\n");
    }

    //output to disk
    try {
      File file = new File(queryLoc + "RecallRatio");
      PrintStream ps = new PrintStream(new FileOutputStream(file));
      ps.println(strBuilder.toString());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }
}
