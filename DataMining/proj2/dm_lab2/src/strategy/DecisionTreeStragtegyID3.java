package strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import strategy.DecisionTreeStrategyC45.ClassifyInfo;

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

//	@Override
//	public void prune(TreeNode tree,DecisionTree decisionTree) {
//		// ID3 don't prune, do nothing
//	}
	
	@Override
	public void prune(TreeNode tree, DecisionTree decisionTree) {
		// TODO
		// use cost-complexity pruning here
		// because the fault rate is zero if using the given training set
		List<Sample> testSet = decisionTree.trainingSet
				.getSortedListByAttribute(0);

//		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree;
		for (Sample s : testSet) {
			ClassifyInfo info = classifyStep((DefaultMutableTreeNode) tree, s);
			updateNode(info.node, info.label == s.getLabel(), 1);
		}
		pruneStep((DefaultMutableTreeNode) tree, decisionTree.trainingSet.getSortedListByAttribute(0));

	}
	
	List<Sample>[] split(SplittingCriterion criterion,List<Sample> samples)
	{
		@SuppressWarnings("unchecked")
		List<Sample>[] lists=new ArrayList[criterion.splitPoints.length+1];
		for (int i=0;i<lists.length;i++)
			lists[i]=new ArrayList<Sample>();
		for (Sample s:samples)
		{
			int p=criterion.getPosition(s);
			lists[p].add(s);
		}
		return lists;
	}

	void pruneStep(TreeNode tree, List<Sample> trainList) {
		 DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree;
		 double estimate = estimate(node) + 0.5;
		 SplittingCriterion criterion = (SplittingCriterion) node
		 .getUserObject();
		 double currentEstimate = criterion.value1
		 + node.getLeafCount()/2.0
		 + se(criterion.value1 + node.getLeafCount()/2.0,
				 trainList.size());
		 if (estimate < currentEstimate)
		 {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree;
			SplittingCriterion c = new SplittingCriterion();
			c.attr = majorityLabel(n);
			n.setUserObject(c);
			n.removeAllChildren();
		 }
		 else
		 {
			 List<Sample>[] lists=split(criterion, trainList);
			 for (int i=0;i<node.getChildCount();i++)
				 pruneStep(node.getChildAt(i), lists[i]);
		 }
	}
	
	int estimate(DefaultMutableTreeNode node) {
		DefaultMutableTreeNode leaf = node.getFirstLeaf();
		class Info
		{
			int count=1;
			int correctCount=0;
			int faultCount=0;
		}
		Map<Integer, Info> map = new HashMap<Integer, Info>();
		while (leaf != null && leaf.isNodeAncestor(node)) {
			SplittingCriterion criterion = (SplittingCriterion) leaf
					.getUserObject();
			int key = criterion.attr;
			if (map.containsKey(key)) {
				Info info = map.get(key);
				info.count++;
				info.correctCount+=criterion.value;
				info.faultCount+=criterion.value1;
			} else {
				Info info=new Info();
				info.correctCount+=criterion.value;
				info.faultCount+=criterion.value1;
				map.put(key, info);
			}
			leaf = leaf.getNextLeaf();
		}
		int label=0;
		int maxCount = 0;
		for (Map.Entry<Integer, Info> e : map.entrySet()) {
			if (e.getValue().count > maxCount) {
				maxCount = e.getValue().count;
				label = e.getKey();
			}
		}
		int fault=0;
		for (Map.Entry<Integer, Info> e : map.entrySet()) {
			if (e.getKey()==label)
			{
				fault+=e.getValue().faultCount;
			}
			else
				fault+=e.getValue().correctCount;
		}
		return fault;
	}

	double se(double e, int n) {
		return Math.sqrt((double) e * (n - e) / n);
	}
	
	int majorityLabel(DefaultMutableTreeNode node) {
		DefaultMutableTreeNode leaf = node.getFirstLeaf();
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		while (leaf != null && leaf.isNodeAncestor(node)) {
			SplittingCriterion criterion = (SplittingCriterion) leaf
					.getUserObject();
			int key = criterion.attr;
			if (map.containsKey(key)) {
				int count = map.get(key);
				map.put(key, count + 1);
			} else {
				map.put(key, 1);
			}
			leaf = leaf.getNextLeaf();
		}
		int label = 0;
		int maxCount = 0;
		for (Map.Entry<Integer, Integer> e : map.entrySet()) {
			if (e.getValue() > maxCount) {
				maxCount = e.getValue();
				label = e.getKey();
			}
		}
		return label;
	}

	void updateNode(TreeNode node, Boolean correct, int num) {
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) node;
		// if (!n.isLeaf()) {
		SplittingCriterion criterion = (SplittingCriterion) n.getUserObject();
		if (correct)
			criterion.value += num;
		else
			criterion.value1 += num;
		// }
		TreeNode parent = n.getParent();
		if (parent != null)
			updateNode(parent, correct, num);
	}

	class ClassifyInfo {
		int label;
		TreeNode node;
	}

	ClassifyInfo classifyStep(DefaultMutableTreeNode node, Sample sample) {
		if (node.isLeaf()) {
			ClassifyInfo ci = new ClassifyInfo();
			SplittingCriterion criterion=(SplittingCriterion) node.getUserObject();
			ci.label = criterion.attr;
			ci.node = node;
			return ci;
		}
		SplittingCriterion criterion = (SplittingCriterion) node
				.getUserObject();
		return classifyStep((DefaultMutableTreeNode) node.getChildAt(criterion
				.getPosition(sample)), sample);
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
