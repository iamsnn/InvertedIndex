package Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.management.Query;

public class BM25 {
//  List<Integer> f = new ArrayList<>();
//  List<Integer> df = new ArrayList<>();
  //how many Doc
  int totalDoc = 0;
  double k1 = 2.0;
  double b = 0.75;
  double averLen = 0.0;

  List<Integer> result = new ArrayList<>();
  //Map<String,List<Integer>>
  //List<String>
  //a pure input query
  //Map<String,List<Integer>> wordFreqInDoc: word in each document
  public BM25(List<String> Query, int totalDoc, Map<String,List<Integer>> src,double averLen,List<Integer> eachLen,Map<String,Map<Integer,Integer>> wordFreqInDoc) {
    this.totalDoc = totalDoc;
    this.averLen = averLen;

    dealWithInput(Query,src,eachLen,wordFreqInDoc);

  }
  private void dealWithInput(List<String> Query,Map<String,List<Integer>> src,
                             List<Integer> eachLen,Map<String,Map<Integer,Integer>> wordCountInDoc){

    //top-K problem
    PriorityQueue<String[]> pq = new PriorityQueue<>((a,b)->{
      return Double.valueOf(b[0])-Double.valueOf(a[0])>=0.0?1:-1;
    });

    //score to each document
    //wordFreqInDoc of doci
    for(int i=0;i<totalDoc;i++){
      String[] res = score(Query,src,eachLen.get(i), wordCountInDoc,eachLen,i);

      if(Double.parseDouble(res[0])>=0.0){
        pq.offer(res);
      }
    }
    int count = 9;
    while(count>=0&&!pq.isEmpty()){
      count--;
      this.result.add(Integer.valueOf(pq.poll()[1]));
    }

  }

  //
  private double[] getIdf(List<String> Query,Map<String,List<Integer>> src){
    if(Query.size()==0||src.size()==0 ){
      throw new IllegalArgumentException("Empty Input");
    }
    int len = Query.size();
    double[] res = new double[len];
    for(int i=0;i<len;i++){
      double up = this.totalDoc-src.get(Query.get(i)).size()+0.5;
      double down = src.get(Query.get(i)).size()+0.5;
      res[i] = Math.log(up/down);
    }

    return res;
  }

  //List<Double> eachLen
  //int curDoc
  private double[] getR(List<String> Query,Map<String,List<Integer>> src,int lenOfCurDoc,
                        Map<String,Map<Integer,Integer>> wordCountInDoc,
                        List<Integer> eachLen,int docId){
    if(Query.size()==0||src.size()==0 ){
      throw new IllegalArgumentException("Empty");
    }
    int len = Query.size();
    double[] res = new double[len];

    for(int i=0;i<len;i++){
      //fi为qi在d中的出现频率
      double freq = qInDFreq(Query.get(i),wordCountInDoc,
              eachLen,docId);
      double up = freq*(this.k1+1);
      double down = freq+getK(lenOfCurDoc);

      res[i] = up/down;
    }

    return res;
  }

  private double getK(int lenOfCurDoc){
    double res = 0.0;

    res = this.k1*(1-this.b+this.b*((double)lenOfCurDoc/this.averLen));
    return res;
  }

  //fi为qi在d中的出现频率
  //Map<String,Integer> : the frequency of a word in a document-ith
  //Map<String,List<Integer>>: how many words in eachDoc
  //List<Integer>: eachLen of eachDoc
  private double qInDFreq(String qi,Map<String,Map<Integer,Integer>> wordCountInDoc,
                          List<Integer> eachLen,int docId){
    double res = 0.0;

    if(!wordCountInDoc.containsKey(qi)){
      return res;
    }

    if(wordCountInDoc.get(qi).containsKey(docId)){
      res = wordCountInDoc.get(qi).get(docId);
    }
    return res;
  }

  private String[] score(List<String> Query,Map<String,List<Integer>> src,int lenOfCurDoc,Map<String,
          Map<Integer,Integer>> wordCountInDoc,
                       List<Integer> eachLen,int docId){

    double res = 0.0;
    double[] myIDFs = getIdf(Query,src);
    double[] myRs = getR(Query, src,lenOfCurDoc,wordCountInDoc,
            eachLen,docId);

    for(int i = 0; i< Query.size(); i++){
      double myIDF = myIDFs[i];
      double myR = myRs[i];

      res += myIDF*myR;
    }

   return new String[]{res+"",docId+""};
  }

  public List<Integer> getResult() {
    return result;
  }
}
