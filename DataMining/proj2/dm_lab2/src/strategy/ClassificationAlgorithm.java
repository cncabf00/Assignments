package strategy;

import java.util.List;

import model.Sample;
import model.SampleSet;

public interface ClassificationAlgorithm {
	public void train(SampleSet samples,List<Integer> attributeList);

	public int classify(Sample sample);
}
