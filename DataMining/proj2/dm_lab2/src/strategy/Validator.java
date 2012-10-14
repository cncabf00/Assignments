package strategy;

import java.util.ArrayList;
import java.util.List;

import logger.Logger;
import model.Sample;
import model.SampleSet;

public class Validator {
  int fold;
  int count = 0;
  List<List<Sample>> sampleFragments;

  public Validator(List<Sample> samples, int fold) {
    sampleFragments = new ArrayList<List<Sample>>();
    int start = 0;
    int end = samples.size();
    double step = (end - start) / fold;
    for (int i = 0; i < fold; i++) {
      sampleFragments.add(samples.subList((int) (start + step * i), (int) (end - step
          * (fold - i - 1))));
    }
    this.fold = fold;
  }

  private SampleSet nextSampleSet() {
    if (count >= fold) return null;
    SampleSet set = new SampleSet();
    for (int i = 0; i < sampleFragments.size(); i++) {
      if (i == count) continue;
      for (Sample s : sampleFragments.get(i)) {
        set.add(s);
      }
    }
    count++;
    return set;
  }

  private List<Sample> getCurrentTestSet() {
    return sampleFragments.get(count - 1);
  }

  public void validate(ClassificationAlgorithm classification, String filename) {
    Logger logger = Logger.getInstance();
    logger.setFile(filename);

    logger.log(classification.getDescription());
    logger.log("" + fold + " validation");

    int totalNum = 0;
    int totalFault = 0;

    for (int k = 0; k < fold; k++) {
      logger.log("validation " + k);
      long currentTime = System.currentTimeMillis();

      SampleSet set = nextSampleSet();
      set.setTestSamples(getCurrentTestSet());
      List<Integer> attributeList = new ArrayList<Integer>();
      for (int i = 0; i < set.getData(0).get(0).getAttributes().length; i++) {
        attributeList.add(i);
      }
      logger.log("start " + classification.getName() + " training using " + set.getTotalCount()
          + " samples");
      classification.train(set, attributeList);
      logger.log("complete, time used=" + (System.currentTimeMillis() - currentTime) + "ms");

      int faultCount = 0;
      List<Sample> test = getCurrentTestSet();
      for (int i = 0; i < test.size(); i++) {
        int r = classification.classify(test.get(i));
        if (r != test.get(i).getLabel()) {
          faultCount++;
        }
      }
      totalNum += test.size();
      totalFault += faultCount;
      logger.log("total samples = " + test.size() + ", fault num = " + faultCount
          + ", fault percent = " + (double) faultCount / test.size());
    }
    logger.log("validation finished\ntest " + totalNum + " samples in total, average fault rate = "
        + (double) totalFault / totalNum);
    count = 0; // reset
  }
}
