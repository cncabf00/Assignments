package kmeans;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Validator {
    List<List<Item>> lists;
    List<List<Item>> trainingData;
    List<Item> testData;
    int cross=10;
    KMeans kMeans=new KMeans();
    int k1=10;
    int k2=10;
    double correctness=0;
//  double last=0.4043135912358781;
    double last=0;
    int correctCount=0;
    int lastCount=1;

    public void loadData(String filename)
    {
        Parser parser=new Parser();
        lists=parser.parseTrainingFile(filename);
        parser.readBounsWeight("bonus_weight_file.txt");
    }
    
    public void getData(int currentRound)
    {
        trainingData=new ArrayList<List<Item>>();
        testData=new ArrayList<Item>();
        for (int i=0;i<=1;i++)
        {
            int start=currentRound*lists.get(i).size()/cross;
            int end=(1+currentRound)*lists.get(i).size()/cross;
            List<Item> list=new ArrayList<Item>();
            if (currentRound!=0)
                list.addAll(lists.get(i).subList(0, start));
            if (currentRound!=cross-1)
                list.addAll(lists.get(i).subList(end, lists.get(i).size()));
            trainingData.add(list);
//          if(i==1)
                testData.addAll(lists.get(i).subList(start, end));
        }
    }
    
    
    
    public void doValidationByPercent(int currentRound)
    {
        getData(currentRound);
        kMeans.setItems(trainingData.get(0));
        kMeans.k=k1;
        kMeans.init();
        List<Cluster> unterminatedClusters=kMeans.doCluster();
        for (int i=0;i<unterminatedClusters.size();i++)
        {
            unterminatedClusters.get(i).label=0;
        }
        
        kMeans.setItems(trainingData.get(1));
        kMeans.k=k2;
        kMeans.init();
        List<Cluster> terminatedClusters=kMeans.doCluster();
        for (int i=0;i<terminatedClusters.size();i++)
        {
            terminatedClusters.get(i).label=1;
        }
        
        List<Cluster> clusters=new ArrayList<Cluster>();
        clusters.addAll(unterminatedClusters);
        clusters.addAll(terminatedClusters);
        kMeans.setClusters(clusters);
        for (Item item:testData)
        {
          kMeans.pointToDistanceToNearest(item,1);
        }
        int size=(int) (testData.size()*0.05);
        Collections.sort(testData);
        int count=0;
        for (int i=0;i<size;i++)
        {
          if (testData.get(i).type==1)
          {
            count++;
          }
        }
        correctCount+=count;
        correctness+=count*1.0/size;
//      for (int i=0;i<2;i++)
//        System.out.println("for type "+i+": " +corrects[i]+" correct out of "+counts[i]+" in total ("+corrects[i]*1.0/counts[i]+")");
//        System.out.println("correctness="+count*1.0/size+", correct count="+count);
    }
    
    public void doValidation(int currentRound)
    {
//      System.out.println(""+cross+"-cross validation, "+currentRound+" round");
        getData(currentRound);
//      System.out.println("training unterminated ones");
        kMeans.setItems(trainingData.get(0));
        kMeans.k=k1;
        kMeans.init();
        List<Cluster> unterminatedClusters=kMeans.doCluster();
        for (int i=0;i<unterminatedClusters.size();i++)
        {
            unterminatedClusters.get(i).label=0;
        }
        
//      System.out.println("training terminated ones");
        kMeans.setItems(trainingData.get(1));
        kMeans.k=k2;
        kMeans.init();
        List<Cluster> terminatedClusters=kMeans.doCluster();
        for (int i=0;i<terminatedClusters.size();i++)
        {
            terminatedClusters.get(i).label=1;
        }
        
        List<Cluster> clusters=new ArrayList<Cluster>();
        clusters.addAll(unterminatedClusters);
        clusters.addAll(terminatedClusters);
        kMeans.setClusters(clusters);
        int[] corrects=new int[2];
        int[] counts=new int[2];
        for (int i=0;i<2;i++)
        {
            corrects[i]=0;
            counts[i]=0;
        }
        for (Item item:testData)
        {
            int label=kMeans.getNearestCluster(item).label;
            counts[label]++;
            if (label==item.type)
            {
                corrects[item.type]++;
            }
        }
        correctness+=corrects[1]*1.0/counts[1];
        correctCount+=corrects[1];
//      for (int i=0;i<2;i++)
//        System.out.println("for type "+i+": " +corrects[i]+" correct out of "+counts[i]+" in total ("+corrects[i]*1.0/counts[i]+")");
    }
    
  public boolean adjustWeights(double k) {
    
    boolean modified=false;
    for (int n = 0; n < Item.bonusWeights.length; n++) {
      while (true)
      {
        double old=Item.bonusWeights[n];
        Item.bonusWeights[n]*=k;
        correctness = 0;
        correctCount=0;
        for (int i = 0; i < cross; i++) {
          doValidationByPercent(i);
        }
        correctness /= cross;
        if (correctness > last && (correctCount>lastCount || (lastCount-correctCount)*1.0/lastCount<0.1)) {
          last = correctness;
          lastCount=correctCount;
          System.out.println("higher correctness "+correctness+" correct count="+correctCount);
          for (int j=0;j<Item.bonusWeights.length;j++)
          {
            System.out.print(""+Item.bonusWeights[j]+" ");
          }
          System.out.println();
          modified=true;
        }
        else
        {
          Item.bonusWeights[n]=old;
          break;
        }
      }
    }
    return modified;
  }
  
  public void searchWeights(int maxRound)
  {
    last=0;
    for (int i=0;i<cross;i++)
    {
        doValidationByPercent(i);
    }
    correctness/=cross;
    last=correctness;
    File file=new File("bonus_weights.txt");
    try {
      FileWriter fw=new FileWriter(file);
      fw.write("bonus weights search\n");
      for (int i=1;i<=maxRound;i++)
      {
        fw.append("round "+i+"\n");
        double k=1.0/(i+1);
        if (adjustWeights(k))
        {
          for (int j=0;j<Item.bonusWeights.length;j++)
          {
            fw.append(""+Item.bonusWeights[j]+" ");
          }
          fw.append(""+last+"\n");
        }
        else
        {
          break;
        }
        k+=1;
        if (adjustWeights(k))
        {
          for (int j=0;j<Item.bonusWeights.length;j++)
          {
            fw.append(""+Item.bonusWeights[j]+" ");
          }
          fw.append(""+last+"\n");
        }
        else
        {
          break;
        }
        fw.flush();
      }
      fw.flush();
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
    
  public void searchK()
  {
    FileWriter fw;
    try {
      fw = new FileWriter(new File("search_k.txt"));
      fw.write("search for k\n");
      int maxK1=0;
      int maxK2=0;
      for (k1=1;k1<=40;k1++)
      {
          for (k2=1;k2<=40;k2++)
          {
              correctness=0;
              correctCount=0;
              for (int i = 0; i < cross; i++) {
                doValidationByPercent(i);
              }
              correctness /= cross;
              if (correctness > last && (correctCount>lastCount || (lastCount-correctCount)*1.0/lastCount<0.1))
              {
                  last=correctness;
                  lastCount=correctCount;
                  maxK1=k1;
                  maxK2=k2;
                  System.out.println("new k1="+k1+" k2="+k2+" correctness="+correctness+ " correct count="+correctCount);
                  fw.append("new k1="+k1+" k2="+k2+" correctness="+correctness+ " correct count="+correctCount+"\n");
                  fw.flush();
              }
          }
      }
      System.out.println(""+maxK1+" "+maxK2);
      fw.append(""+maxK1+" "+maxK2+"\n");
      fw.flush();
      fw.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
    public void run(String filename)
    {
        loadData(filename);   
//      for (int i = 0; i < cross; i++) {
//          doValidationByPercent(i);
//        }
//        searchK();
        searchWeights(10);
        
    }
    
    public static void main(String[] argv)
    {
        Validator validator=new Validator();
        validator.run("train_voice_bb.txt");
    }
}
