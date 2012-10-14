package main;

import java.util.ArrayList;
import java.util.List;

import logger.Logger;
import model.Sample;
import model.SampleParser;
import model.SampleSet;
import strategy.ClassificationAlgorithm;
import strategy.DecisionTree;
import strategy.DecisionTreeStrategyC45;
import strategy.DecisionTreeStrategyID3;

public class Runner {
  public static void main(String[] args) {
    DecisionTreeStrategyID3.test(10);
    DecisionTreeStrategyC45.test(10);

    SampleParser sp = new SampleParser("train instance");
    ClassificationAlgorithm classifier = new DecisionTree(new DecisionTreeStrategyC45());
    SampleSet set = sp.parse("train label");
    List<Integer> attributeList = new ArrayList<Integer>();
    for (int i = 0; i < set.getData(0).get(0).getAttributes().length; i++) {
      attributeList.add(i);
    }
    classifier.train(set, attributeList);

    Logger logger = Logger.getInstance();
    logger.setFile("output.txt");
    sp = new SampleParser("test instance");
    List<Sample> testSamples = sp.parse();
    for (Sample s : testSamples) {
      int label = classifier.classify(s);
      logger.log("" + label);
    }
  }
}
