package Invert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Rank.BM25;

public class Util {

  private static List<Integer> list = new ArrayList<>();
  private static Map<String,Map<Integer,Integer>> m = new HashMap<>();

  private static Map<String, BitSet> restoreFromFile(String filePath){

    Map<String, BitSet> map = new HashMap<>();
    List<String> dataArray = new ArrayList<>();
    try {
      BufferedReader in = new BufferedReader(new FileReader(filePath));
      String str;
      // read by lines
      while ((str = in.readLine()) != null) {
        dataArray.addAll(Arrays.asList(str.split("@"))) ;
      }
      in.close();
    } catch (IOException e) {
      e.getStackTrace();
    }

    for(String str:dataArray){
      String name = str.split("#")[0];
      String[] docIds = str.split("#")[1].split(",");
      map.putIfAbsent(name,new BitSet());
      for(String id:docIds){
        map.get(name).set(Integer.parseInt(id));
      }
    }
    return map;
  }

  //BitSet
  private static void mergeTwo(Map<String, BitSet> b1, Map<String, BitSet> b2){
    for(String name:b2.keySet()){
      if(!b1.containsKey(name)){
        b1.put(name,b2.get(name));
      }
      else{
        BitSet bs1 = b1.get(name);
        BitSet bs2 = b2.get(name);
        bs1.or(bs2);
        b1.put(name,bs1);
      }
    }

  }

  //store to the disk
  private static void writeToDisk(String outputFilePath, Map<String, BitSet> map) {

    List<String[]> buffers = new ArrayList<>();

    for(Map.Entry<String, BitSet> entry:map.entrySet()){
      BitSet b = entry.getValue();
      //trans b into string
      String temp = b.toString();
      temp = temp.substring(1,temp.length()-1).replaceAll(" ","");
      buffers.add(new String[]{entry.getKey(),temp});
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
      File file = new File(outputFilePath);
      PrintStream ps = new PrintStream(new FileOutputStream(file));
      ps.println(strBuilder.toString());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  //stop-Words
  private static Set<String> stopWords(String filePath){

    Set<String> res = new HashSet<>();

    try {
      BufferedReader in = new BufferedReader(new FileReader(filePath));
      String str;
      // read by lines
      while ((str = in.readLine()) != null) {
        res.add(str);
      }
      in.close();
    } catch (IOException e) {
      e.getStackTrace();
    }

    return res;
  }

  public static String searchWord(String input,String result,String stopWordPath,
                                  String listFilePath,String mapFilePath){

    Map<String,List<Integer>> map = new HashMap<>();

    List<String> dataArray = new ArrayList<>();
    try {
      BufferedReader in = new BufferedReader(new FileReader(result));
      String str;
      // read by lines
      while ((str = in.readLine()) != null) {
        dataArray.addAll(Arrays.asList(str.split("@"))) ;
      }
      in.close();
    } catch (IOException e) {
      e.getStackTrace();
    }
    for(String s:dataArray){
      String key = s.split("#")[0];
      String value = s.split("#")[1];
      map.putIfAbsent(key,new ArrayList<>());
      for(String number:value.split(",")){
        map.get(key).add(Integer.parseInt(number));
      }

    }
//List<String> Query, int totalDoc, Map<String,List<Integer>> src,double averLen,List<Integer> eachLen,Map<String,Map<Integer,Integer>> wordFreqInDoc

    List<Integer> myList = restoreList(listFilePath);
    Map<String,Map<Integer,Integer>> myMap = restoreMap(mapFilePath);


    int len = myList.size();
    long l = 0l;
    for(int i=0;i<len;i++){
      l+=myList.get(i);
    }
    double avrg = (double)(l/(double)len);

    List<String> src = filterWords(Arrays.asList(input.split(" ")),stopWordPath);

    BM25 bm25 = new BM25(src,len,map,avrg,myList,myMap);
    List<Integer> ll = bm25.getResult();

    if(ll.size() == 0){
      return "";
    }

    StringBuilder sb = new StringBuilder("");
    for(int page:ll){
      sb.append(page+1);
      sb.append(",");
    }

    return sb.toString().substring(0,sb.length()-1);
  }

  public static void Process(List<String> filePaths,String stopWordPath,
                             String tempStoreInvertIndex,String tempStore,
                             String tempStoreListAndMap){

    List<String> efwFilePaths = new ArrayList<>();
    int id = 1;
    Map<String, BitSet> res= new HashMap<>();

    Set<String> stopWordList = Util.stopWords(stopWordPath);

    //Traverse each document
    for(int i=0;i<filePaths.size();i++){
      String filePath = filePaths.get(i);

      PreTreatTool preTool = new PreTreatTool(filePath,stopWordList,tempStore);

      List<Integer> myList = preTool.getEachLen();
      Map<String,Map<Integer,Integer>> myMap = preTool.getWordFreqInDoc();

      list.addAll(myList);
      //merger myMap to m
      for(Map.Entry<String,Map<Integer,Integer>> entry: myMap.entrySet()){
        String key = entry.getKey();
        Map<Integer,Integer> map = entry.getValue();

        if(!m.containsKey(key)){
          m.put(key,new HashMap<>());
          for(Map.Entry<Integer,Integer> entry1:map.entrySet()){
            m.get(key).put(entry1.getKey(),entry1.getValue());
          }
        }
        else{
          for(Map.Entry<Integer,Integer> entry2:map.entrySet()){
            if(!m.get(key).containsKey(entry2.getKey())){
              m.get(key).put(entry2.getKey(),entry2.getValue());
            }
            else{
              m.get(key).put(entry2.getKey(),entry2.getValue()+m.get(key).get(entry2.getKey()));
            }
          }
        }
      }


      id = preTool.currentDocId();
      efwFilePaths = preTool.getEFWPaths();

      SPIMI sTool = new SPIMI(efwFilePaths,tempStoreInvertIndex,i);
      sTool.createInvertIndex(id);

      efwFilePaths = new ArrayList<>();

      if(i == 0){
        res = Util.restoreFromFile(tempStoreInvertIndex+(i+""));;
      }
      else{
        Map<String, BitSet> temp = Util.restoreFromFile(tempStoreInvertIndex+(i+""));
        mergeTwo(res,temp);
      }
    }
    writeToDisk(tempStoreInvertIndex+"Result",res);
    writeListAndMapToDisk(tempStoreListAndMap);
  }

  private static List<String> filterWords(List<String> words,String stopWordPath) {

    Set<String> FILTER_WORDS = Util.stopWords(stopWordPath);
    Set<String> noDuplicateWORDS = new HashSet<>();
    List<String> result = new ArrayList<>();

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
      if(w.length()<1){
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

      if(!noDuplicateWORDS.contains(w)){
        result.add(w);
        noDuplicateWORDS.add(w);
      }


    }
    return result;
  }

  //list and m
  private static void writeListAndMapToDisk(String outPutFilePath){

    List<String[]> buffers = new ArrayList<>();
    for(Map.Entry<String,Map<Integer,Integer>> entry:m.entrySet()){
      for(Map.Entry<Integer,Integer> entry2:entry.getValue().entrySet()){
        if(entry.getKey().length()>0){
          buffers.add(new String[]{entry.getKey(),entry2.getKey()+"",entry2.getValue()+""});
        }

      }
    }

    // add data in the buffer to the disk
    StringBuilder strBuilder = new StringBuilder("");
    for(String[] array: buffers) {
      strBuilder.append(array[0]);
      strBuilder.append("@");
      strBuilder.append(array[1]);
      strBuilder.append("@");
      strBuilder.append(array[2]);
      strBuilder.append("#");
    }


    try {
      File file = new File(outPutFilePath+"map");
      PrintStream ps = new PrintStream(new FileOutputStream(file));
      ps.println(strBuilder.toString());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }


    strBuilder = new StringBuilder("");
    for(int i:list){
      strBuilder.append(i);
      strBuilder.append("#");
    }

    try {
      File file = new File(outPutFilePath+"list");
      PrintStream ps = new PrintStream(new FileOutputStream(file));
      ps.println(strBuilder.toString());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }



  }

  private static List<Integer> restoreList(String listFilePath){
    List<Integer> list = new ArrayList<>();
    List<String> dataArray = new ArrayList<>();
    try {
      BufferedReader in = new BufferedReader(new FileReader(listFilePath));
      String str;
      // read by lines
      while ((str = in.readLine()) != null) {
        dataArray.addAll(Arrays.asList(str.split("#"))) ;
      }
      in.close();
    } catch (IOException e) {
      e.getStackTrace();
    }

    for(String str:dataArray){
      int count = Integer.parseInt(str);
      list.add(count);
    }

    return list;
  }

  private static Map<String,Map<Integer,Integer>> restoreMap(String mapFilePath){
    Map<String,Map<Integer,Integer>> map = new HashMap<>();
    List<String> dataArray = new ArrayList<>();
    try {
      BufferedReader in = new BufferedReader(new FileReader(mapFilePath));
      String str;
      // read by lines
      while ((str = in.readLine()) != null) {
        dataArray.addAll(Arrays.asList(str.split("#"))) ;
      }
      in.close();
    } catch (IOException e) {
      e.getStackTrace();
    }

    for(String str:dataArray){
      //System.out.println(str);
      String name = str.split("@")[0];
      int docId = Integer.parseInt(str.split("@")[1]);
      int count = Integer.parseInt(str.split("@")[2]);
      int[] docIds = new int[]{docId,count};

      map.putIfAbsent(name,new HashMap<>());
      for(int id:docIds){
        map.get(name).putIfAbsent(docId,count);
        int tempVal = map.get(name).get(docId);
        map.get(name).put(docId,count>tempVal?count:tempVal);
      }
    }
    return map;
  }
}
