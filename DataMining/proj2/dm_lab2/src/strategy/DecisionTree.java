package strategy;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import model.Sample;
import model.SampleSet;

public class DecisionTree implements ClassificationAlgorithm {
	TreeModel tree;
	boolean discrete=false;
	DecisionTreeStrategy strategy;
	SampleSet trainingSet;
	
	public DecisionTree(DecisionTreeStrategy strategy)
	{
		this.strategy=strategy;
	}
	
	public DecisionTree(DecisionTreeStrategy strategy, boolean discrete)
	{
		this.strategy=strategy;
		this.discrete=discrete;
	}

	@Override
	public void train(SampleSet samples,List<Integer> attributeList) {
		this.trainingSet=samples;
		tree=new DefaultTreeModel(generateDecisionTree(samples, attributeList));
		strategy.prune((TreeNode) tree.getRoot(),this);
	}

	@Override
	public int classify(Sample sample) {
		return classifyStep((DefaultMutableTreeNode) tree.getRoot(), sample);
	}
	
	int classifyStep(DefaultMutableTreeNode node,Sample sample)
	{
		if (node.isLeaf())
		{
			SplittingCriterion criterion=(SplittingCriterion)node.getUserObject();
			return criterion.attr;
		}
		SplittingCriterion criterion=(SplittingCriterion)node.getUserObject();
		return classifyStep((DefaultMutableTreeNode) node.getChildAt(criterion.getPosition(sample)), sample);
	}
	
	DefaultMutableTreeNode generateDecisionTree(SampleSet dataset,List<Integer> attributeList)
	{
		DefaultMutableTreeNode node=new DefaultMutableTreeNode();
		if (dataset.getNumOfLabels()==1)
		{
			SplittingCriterion criterion=new SplittingCriterion();
			criterion.attr=dataset.getLabel(0);
			node.setUserObject(criterion);
			return node;
		}
		if (attributeList.size()==0)
		{
			SplittingCriterion criterion=new SplittingCriterion();
			criterion.attr=dataset.getLabelOfMajority();
			node.setUserObject(criterion);
			return node;
		}
		SplittingCriterion criterion=strategy.selectAttributeForSplitting(dataset, attributeList);
		criterion.value=0;
		criterion.value1=0;
		node.setUserObject(criterion);
//		System.out.println("new node, criterion attr="+criterion.attr+" split="+criterion.splitPoints[0]);
		if (discrete)
		{
			for (int i=0;i<attributeList.size();i++)
			{
				if (attributeList.get(i)==criterion.attr)
				{
					attributeList.remove(i);
					break;
				}
			}
		}
		SampleSet[] sampleSets=new SampleSet[criterion.splitPoints.length+1];
		for (int i=0;i<criterion.splitPoints.length+1;i++)
		{
			
			sampleSets[i]=null;
		}
		for (int i=0;i<dataset.getNumOfLabels();i++)
		{
			List<Sample> subset=dataset.getData(i);
			for (int j=0;j<subset.size();j++)
			{
				int p=criterion.getPosition(subset.get(j));
//				System.out.println(p);
				if(sampleSets[p]==null)
				{
					sampleSets[p]=new SampleSet();
				}
				sampleSets[p].add(subset.get(j));
			}
		}
		for (int i=0;i<sampleSets.length;i++)
		{
			if (sampleSets[i]==null)
			{
				DefaultMutableTreeNode child=new DefaultMutableTreeNode();
				SplittingCriterion c=new SplittingCriterion();
				c.attr=dataset.getLabelOfMajority();
				child.setUserObject(c);
				node.add(child);
			}
			else
			{
				node.add(generateDecisionTree(sampleSets[i], attributeList));
			}
			
		}
		return node;
	}
	
	

}


