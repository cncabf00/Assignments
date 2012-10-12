package strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import model.Sample;
import model.SampleParser;
import model.SampleSet;

public class DecisionTreeStragtegyID3 implements DecisionTreeStrategy{
	
	
	@Override
	public SplittingCriterion selectAttributeForSplitting(SampleSet dataset,
			List<Integer> attributeList) {
		SplittingCriterion minResult=new SplittingCriterion();
		minResult.value=Double.MAX_VALUE;
		for (int i=0;i<attributeList.size();i++)
		{
			SplittingCriterion r=computeEntropy(dataset, attributeList.get(i));
			if (r.value<minResult.value)
			{
				minResult=r;
			}
		}
		return minResult;
	}

	@Override
	public void prune(TreeNode tree,DecisionTree decisionTree) {
		// ID3 don't prune, do nothing
	}
	
	double computeEntropy(SampleSet dataset)
	{
		double entropy = 0;
		for (int i=0;i<dataset.getNumOfLabels();i++)
		{
			double p=((double)dataset.getCount(i))/dataset.getTotalCount();
			entropy-=p*MyMath.log2(p);
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
			entropy-=p*MyMath.log2(p);
		}
		return entropy;
	}

	//currently only for attribute that is not discrete
	SplittingCriterion computeEntropy(SampleSet dataset, int attr)
	{
		List<Sample> sortedArray=dataset.getSortedListByAttribute(attr);
		
		SplittingCriterion minResult=new SplittingCriterion();
		minResult.value=Double.MAX_VALUE;
		if (sortedArray.size()==1)
			sortedArray.add(sortedArray.get(0));
		for (int i=1;i<sortedArray.size();i++)
		{
			SplittingCriterion r=new SplittingCriterion();
			r.attr=attr;
			r.splitPoints=new double[1];
			r.splitPoints[0]=(sortedArray.get(i).getAttributes()[attr]+sortedArray.get(i-1).getAttributes()[attr])/2;
			r.value= (((double)i)/sortedArray.size())*computeEntropy(sortedArray.subList(0, i))+(((double)(sortedArray.size()-i))/sortedArray.size())*computeEntropy(sortedArray.subList(i, sortedArray.size()));
			if (r.value<minResult.value)
			{
				minResult=r;
			}
		}
		return minResult;
	}
	
	
	public static void main(String[] args)
	{
		int fold=10;
		SampleParser sp=new SampleParser("train instance");
		Validator validator=new Validator(sp.parseForTest("train label"), fold);
		for (int k=0;k<fold;k++)
		{
			System.out.println("validation "+k);
			long currentTime=System.currentTimeMillis();
			System.out.print("start");
			
			ClassificationAlgorithm classification=new DecisionTree(new DecisionTreeStragtegyID3());
			SampleSet set=validator.nextSampleSet();
			List<Integer> attributeList=new ArrayList<Integer>();
			for (int i=0;i<set.getData(0).get(0).getAttributes().length;i++)
			{
				attributeList.add(i);
			}
			classification.train(set, attributeList);
			System.out.println("complete timeused="+(System.currentTimeMillis()-currentTime)+"ms");
			
			
			int faultCount=0;
			List<Sample> test=validator.getCurrentTestSet();
			for (int i=0;i<test.size();i++)
			{
				int r=classification.classify(test.get(i));
				if (r!=test.get(i).getLabel())
				{
					faultCount++;
				}
			}
			System.out.println("falut percent = "+(double)faultCount/test.size());
		}
		
		
	}
}
