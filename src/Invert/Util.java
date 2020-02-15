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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {

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
      // TODO Auto-generated catch block
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

  public static String searchWord(String src,String result){
    HashMap<String,String> map = new HashMap<>();
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
      map.putIfAbsent(key,value);
    }
    if(map.containsKey(src.trim())){
      return map.get(src.trim());
    }

    return "NOT EXISTS";
  }

  public static void Process(List<String> filePaths,String stopWordPath,
                             String tempStoreInvertIndex,String tempStore){

    List<String> efwFilePaths = new ArrayList<>();
    int id = 1;
    Map<String, BitSet> res= new HashMap<>();

    Set<String> stopWordList = Util.stopWords(stopWordPath);

    //Traverse each document
    for(int i=0;i<filePaths.size();i++){
      String filePath = filePaths.get(i);

      PreTreatTool preTool = new PreTreatTool(filePath,stopWordList, tempStore);
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
  }

  public static long getFileSize(String resultFilePath) throws IOException {
    long res = 0l;
    FileChannel fc= null;
    try {
      File f= new File(resultFilePath);
      if (f.exists() && f.isFile()){
        FileInputStream fis= new FileInputStream(f);
        fc= fis.getChannel();
        res = fc.size();
      }else{
        res = 0;
      }
    } catch (FileNotFoundException e) {
      res = 0;
    } catch (IOException e) {
      res = 0;
    } finally {
      if (null!=fc){
        fc.close();
      }
    }
    return res;
  }
}