package main;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class Runner {
  
  public void classify()
  {
    try {
      FileWriter fw1=new FileWriter(new File("output_tree_voice_only.txt"));
      FileWriter fw2=new FileWriter(new File("output_tree_voice_bb.txt"));
      
      ArffLoader loader = new ArffLoader();
      loader.setFile(new File("train_voice_only.txt"));
      Instances structure = loader.getStructure();
      structure.setClassIndex(structure.numAttributes() - 1);
      Instances data=new Instances(structure);
      Instance current;
      int count=0;
      while ((current = loader.getNextInstance(structure)) != null)
      {
        data.add(current);
        count++;
      }
      data.deleteStringAttributes();
//      Standardize filter = new Standardize();
     // initializing the filter once with training set
//     filter.setInputFormat(data);
     // configures the Filter based on train instances and returns
     // filtered instances
//     data = Filter.useFilter(data, filter);
     
      J48 model = new J48();
      model.buildClassifier(data);
      
      
//      loader = new ArffLoader();
//      loader.setFile(new File("predict_voice_only.txt"));
//      structure = loader.getStructure();
//      structure.setClassIndex(structure.numAttributes() - 1);
//      Instances unlabeled=new Instances(structure);
//      List<String> ids=new ArrayList<String>();
//      while ((current = loader.getNextInstance(structure)) != null)
//      {
//        unlabeled.add(current);
//        ids.add(current.stringValue(0));
//      }
//       // create copy
//       Instances labeled = new Instances(unlabeled);
//       unlabeled.deleteStringAttributes();
//       // label instances
//       for (int i = 0; i < unlabeled.numInstances(); i++) {
//         double clsLabel = model.classifyInstance(unlabeled.instance(i));
//         labeled.instance(i).setClassValue(clsLabel);
//         labeled.instance(i).setValue(0, ids.get(i));
//         }
//       // save newly labeled data
//       ArffSaver saver=new ArffSaver();
//       saver.setFile(new File("voice_only_labeled.txt"));
//       saver.setInstances(labeled);
//       saver.writeBatch();
       Evaluation eval=new Evaluation(data);
      eval.crossValidateModel(model, data, 10, new Random(1));
      System.out.println("precision of terminated="+eval.precision(1)+", count="+eval.numTruePositives(1));
      System.out.println(model.toString()+"\n\n");
      fw1.write(eval.toClassDetailsString("\nResults\n\n")+"\n");
      fw1.write(model.toString());
      fw1.flush();
      fw1.close();
//      System.out.println(eval.toClassDetailsString("\nResults\n\n"));
      
      
        loader = new ArffLoader();
        loader.setFile(new File("train_voice_bb.txt"));
        structure = loader.getStructure();
        structure.setClassIndex(structure.numAttributes() - 1);
        data=new Instances(structure);
        count=0;
        while ((current = loader.getNextInstance(structure)) != null)
        {
          data.add(current);
          count++;
        }
        data.deleteStringAttributes();
//         filter = new Standardize();
       // initializing the filter once with training set
//       filter.setInputFormat(data);
       // configures the Filter based on train instances and returns
       // filtered instances
//       data = Filter.useFilter(data, filter);
       
        model = new J48();
        model.buildClassifier(data);
        
        
//        loader = new ArffLoader();
//        loader.setFile(new File("predict_voice_bb.txt"));
//        structure = loader.getStructure();
//        structure.setClassIndex(structure.numAttributes() - 1);
//        unlabeled=new Instances(structure);
//        ids=new ArrayList<String>();
//        while ((current = loader.getNextInstance(structure)) != null)
//        {
//          unlabeled.add(current);
//          ids.add(current.stringValue(0));
//        }
//         // create copy
//         labeled = new Instances(unlabeled);
//         unlabeled.deleteStringAttributes();
//         // label instances
//         for (int i = 0; i < unlabeled.numInstances(); i++) {
//           double clsLabel = model.classifyInstance(unlabeled.instance(i));
//           labeled.instance(i).setClassValue(clsLabel);
//           labeled.instance(i).setValue(0, ids.get(i));
//           }
//         // save newly labeled data
//         saver=new ArffSaver();
//         saver.setFile(new File("voice_bb_labeled.txt"));
//         saver.setInstances(labeled);
//         saver.writeBatch();
        eval=new Evaluation(data);
        eval.crossValidateModel(model, data, 10, new Random(1));
        System.out.println("precision of terminated="+eval.precision(1)+", count="+eval.numTruePositives(1));
        System.out.println(model.toString()+"\n\n");
        
        fw2.write(eval.toClassDetailsString("\nResults\n\n")+"\n");
        fw2.write(model.toString());
        fw2.flush();
        fw2.close();
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } // build classifier
  }
  
  public void predict(String filename) throws IOException
  {
    Map<String,Integer> customers=new HashMap<String, Integer>();
    Set<String> s=new HashSet<String>();
    ArffLoader loader = new ArffLoader();
    loader.setFile(new File(filename));
    Instances structure = loader.getStructure();
    structure.setClassIndex(structure.numAttributes() - 1);
    Instance current;
    while ((current = loader.getNextInstance(structure)) != null)
    {
//      System.out.println(current.classValue());
      String key=current.stringValue(0);
      s.add(key);
      if (current.classValue()==1.0)
      {
        if (customers.containsKey(key))
        {
          customers.put(key,customers.get(key)+1);
        }
        else
        {
          customers.put(key, 1);
        }
      }
    }
    List<Map.Entry<String, Integer>> customerArray=new ArrayList<Map.Entry<String,Integer>>(customers.entrySet());
    Collections.sort(customerArray,new Comparator<Map.Entry<String, Integer>>() {

      @Override
      public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
        return -(o1.getValue()-o2.getValue());
      }});
    
    FileWriter fw=new FileWriter(new File("top5_"+filename));
    int top=(int) (s.size()*0.05);
    int count=0;
    for (int i=0;i<customerArray.size();i++)
    {
      count++;
      if (count>top)
        break;
      fw.write(customerArray.get(i).getKey()+"\n");
    }
    fw.flush();
    fw.close();
    
  }
  public static void main(String[] args) {
    Runner runner=new Runner();
//    runner.classify();
    try {
      runner.predict("voice_bb_labeled.txt");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      runner.predict("voice_only_labeled.txt");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
