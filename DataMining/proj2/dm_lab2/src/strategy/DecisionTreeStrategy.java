package strategy;

import java.util.List;

import model.Sample;
import model.SampleSet;

public interface DecisionTreeStrategy {
	boolean discrete=false;
	
	SplittingCriterion selectAttributeForSplitting(SampleSet dataset,List<Integer> attributeList);
	
	void prune();
}

class SplittingCriterion
{
	int attr;
	double[] splitPoints;

	public int getPosition(Sample sample)
	{
		for (int i=0;i<splitPoints.length;i++)
		{
//			System.out.println("sample attr="+sample.getAttributes()[attr]+" splitpoint="+splitPoints[i]);
			if (sample.getAttributes()[attr]<=splitPoints[i])
			{
//				System.out.println("split");
				return i;
			}
		}
		return splitPoints.length;
	}
}