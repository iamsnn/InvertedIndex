package Invert;

import java.util.List;

/**
 * Document
 */
public class Document {
  //document ID
  String docId;
  //document location in the server
  String filePath;
  //effectwords of such document
  List<String> effectWords;


  public Document(List<String> effectWords, String filePath){
    this.effectWords = effectWords;
    this.filePath = filePath;
  }

  public Document(List<String> effectWords, String filePath, String docId){
    this(effectWords, filePath);
    this.docId = docId;
  }
}