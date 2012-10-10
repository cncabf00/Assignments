package strategy;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import model.Sample;
import model.SampleSet;

public class DecisionTree implements ClassificationAlgorithm {
	TreeModel tree;
	boolean discrete=false;
	DecisionTreeStrategy strategy;
	
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
		tree=new DefaultTreeModel(generateDecisionTree(samples, attributeList));
		strategy.prune();
	}

	@Override
	public int classify(Sample sample) {
		return classifyStep((DefaultMutableTreeNode) tree.getRoot(), sample);
	}
	
	int classifyStep(DefaultMutableTreeNode node,Sample sample)
	{
		if (node.isLeaf())
		{
//			System.out.println("leaf");
			return (Integer)node.getUserObject();
		}
		SplittingCriterion criterion=(SplittingCriterion)node.getUserObject();
//		System.out.println("by criterion [attr = "+criterion.attr+" split = "+criterion.splitPoints[0]+"]");
		return classifyStep((DefaultMutableTreeNode) node.getChildAt(criterion.getPosition(sample)), sample);
	}
	
	DefaultMutableTreeNode generateDecisionTree(SampleSet dataset,List<Integer> attributeList)
	{
		DefaultMutableTreeNode node=new DefaultMutableTreeNode();
		if (dataset.getNumOfLabels()==1)
		{
			node.setUserObject(dataset.getLabel(0));
			return node;
		}
		if (attributeList.size()==0)
		{
			node.setUserObject(dataset.getLabelOfMajority());
			return node;
		}
		SplittingCriterion criterion=strategy.selectAttributeForSplitting(dataset, attributeList);
		node.setUserObject(criterion);
		System.out.println("new node, criterion attr="+criterion.attr+" split="+criterion.splitPoints[0]);
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
				child.setUserObject(dataset.getLabelOfMajority());
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


