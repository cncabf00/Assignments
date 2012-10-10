package strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Sample;
import model.SampleParser;
import model.SampleSet;

public class DecisionTreeStragetyID3 implements DecisionTreeStrategy{
	static final double log2=Math.log(2);
	
	@Override
	public SplittingCriterion selectAttributeForSplitting(SampleSet dataset,
			List<Integer> attributeList) {
		Result minResult=new Result();
		minResult.entropy=Double.MAX_VALUE;
		for (int i=0;i<attributeList.size();i++)
		{
			Result r=computeEntropy(dataset, attributeList.get(i));
			if (r.entropy<minResult.entropy)
			{
				minResult=r;
			}
		}
		SplittingCriterion criterion=new SplittingCriterion();
		criterion.attr=minResult.attr;
		criterion.splitPoints=new double[1];
		criterion.splitPoints[0]=minResult.value;
		return criterion;
	}

	@Override
	public void prune() {
		// TODO Auto-generated method stub
		
	}
	
	double computeEntropy(SampleSet dataset)
	{
		double entropy = 0;
		for (int i=0;i<dataset.getNumOfLabels();i++)
		{
			double p=((double)dataset.getCount(i))/dataset.getTotalCount();
			entropy-=p*(Math.log(p)/log2);
		}
		return entropy;
	}
	
	double computeEntropy(List<Sample> array)
	{
		Map<Integer,Integer> countMap=new HashMap<Integer,Integer>();
		for (int i=0;i<array.size();i++)
		{
			int key=array.get(i).getLabel();
			if (countMap.containsKey(key))
			{
				int count=countMap.get(key);
				count++;
				countMap.put(key, count);
			}
			else
			{
				countMap.put(key, 1);
			}
		}
		
		double entropy=0;
		for (Map.Entry<Integer, Integer> e:countMap.entrySet())
		{
			double p=((double)e.getValue())/array.size();
			entropy-=p*(Math.log(p)/log2);
		}
		return entropy;
	}

	//currently only for attribute that is not discrete
	Result computeEntropy(SampleSet dataset, int attr)
	{
		List<Sample> sortedArray=dataset.getSortedListByAttribute(attr);
		
		Result minResult=new Result();
		minResult.entropy=Double.MAX_VALUE;
		if (sortedArray.size()==1)
			sortedArray.add(sortedArray.get(0));
		for (int i=1;i<sortedArray.size();i++)
		{
			Result r=new Result();
			r.attr=attr;
			r.value=(sortedArray.get(i).getAttributes()[attr]+sortedArray.get(i-1).getAttributes()[attr])/2;
			r.entropy= (((double)i)/sortedArray.size())*computeEntropy(sortedArray.subList(0, i))+(((double)(sortedArray.size()-i))/sortedArray.size())*computeEntropy(sortedArray.subList(i, sortedArray.size()));
			if (r.entropy<minResult.entropy)
			{
				minResult=r;
			}
		}
		return minResult;
	}
	
	class Result
	{
		int attr;
		double entropy;
		double value;
	}
	
	public static void main(String[] args)
	{
		long currentTime=System.currentTimeMillis();
		System.out.print("start");
		SampleParser sp=new SampleParser("train instance");
		ClassificationAlgorithm classification=new DecisionTree(new DecisionTreeStragetyID3());
		SampleSet set=sp.parse("train label");
		List<Integer> attributeList=new ArrayList<>();
		for (int i=0;i<set.getData(0).get(0).getAttributes().length;i++)
		{
			attributeList.add(i);
		}
		classification.train(set, attributeList);
		System.out.println("complete timeused="+(System.currentTimeMillis()-currentTime)+"ms");
		
		List<Sample> test=sp.parseForTest("train label");
		int faultCount=0;
		for (int i=0;i<test.size();i++)
		{
			int r=classification.classify(test.get(i));
			if (r!=test.get(i).getLabel())
			{
				faultCount++;
			}
			System.out.println("origin = "+ test.get(i).getLabel()+" classification = "+r);
		}
		System.out.println("falut percent = "+(double)faultCount/test.size());
	}
}
