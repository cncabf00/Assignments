package strategy;

import java.util.ArrayList;
import java.util.List;

import model.Sample;
import model.SampleSet;

public class Validator {
	int fold;
	int count=0;
	List<List<Sample>> sampleFragments;
	
	public Validator(List<Sample> samples,int fold)
	{
		sampleFragments=new ArrayList<List<Sample>>();
		int start=0;
		int end=samples.size();
		double step=(end-start)/fold;
		for (int i=0;i<fold;i++)
		{
			sampleFragments.add(samples.subList((int)(start+step*i), (int)(end-step*(fold-i-1))));
		}
		this.fold=fold;
	}
	
	public SampleSet nextSampleSet()
	{
		if (count>=fold)
			return null;
		SampleSet set=new SampleSet();
		for (int i=0;i<sampleFragments.size();i++)
		{
			if (i==count)
				continue;
			for (Sample s:sampleFragments.get(i))
			{
				set.add(s);
			}
		}
		count++;
		return set;
	}
	
	public List<Sample> getCurrentTestSet()
	{
		return sampleFragments.get(count-1);
	}
}
