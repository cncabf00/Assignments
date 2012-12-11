package kmeans;

import java.util.ArrayList;
import java.util.List;

public class Validator {
	List<List<Item>> lists;
	List<List<Item>> trainingData;
	List<Item> testData;
 	int cross=2;
 	KMeans kMeans=new KMeans();
 	int k1=10;
 	int k2=10;
 	double correctnes=0;

	public void loadData(String filename)
	{
		Parser parser=new Parser();
		lists=parser.parseTrainingFile(filename);
	}
	
	public void getData(int currentRound)
	{
		trainingData=new ArrayList<List<Item>>();
		testData=new ArrayList<Item>();
		for (int i=0;i<=1;i++)
		{
			int start=currentRound*lists.get(i).size()/10;
			int end=(1+currentRound)*lists.get(i).size()/10;
			List<Item> list=new ArrayList<Item>();
			if (currentRound!=0)
				list.addAll(lists.get(i).subList(0, start));
			if (currentRound!=cross-1)
				list.addAll(lists.get(i).subList(end, lists.get(i).size()));
			trainingData.add(list);
//			if(i==1)
				testData.addAll(lists.get(i).subList(start, end));
		}
	}
	
	public void doValidation(int currentRound)
	{
//		System.out.println(""+cross+"-cross validation, "+currentRound+" round");
		getData(currentRound);
//		System.out.println("training unterminated ones");
		kMeans.setItems(trainingData.get(0));
		kMeans.k=k1;
		kMeans.init();
		List<Cluster> unterminatedClusters=kMeans.doCluster();
		for (int i=0;i<unterminatedClusters.size();i++)
		{
			unterminatedClusters.get(i).label=0;
		}
		
//		System.out.println("training terminated ones");
		kMeans.setItems(trainingData.get(1));
		kMeans.k=k2;
		kMeans.init();
		List<Cluster> terminatedClusters=kMeans.doCluster();
		for (int i=0;i<terminatedClusters.size();i++)
		{
			terminatedClusters.get(i).label=1;
		}
		
		List<Cluster> clusters=new ArrayList<Cluster>();
		clusters.addAll(unterminatedClusters);
		clusters.addAll(terminatedClusters);
		kMeans.setClusters(clusters);
		int[] corrects=new int[2];
		int[] counts=new int[2];
		for (int i=0;i<2;i++)
		{
			corrects[i]=0;
			counts[i]=0;
		}
		for (Item item:testData)
		{
			int label=kMeans.getNearestCluster(item).label;
			counts[label]++;
			if (label==item.type)
			{
				corrects[item.type]++;
			}
		}
		correctnes+=corrects[1]*1.0/counts[1];
//		for (int i=0;i<2;i++)
//			System.out.println("for type "+i+": " +corrects[i]+" correct out of "+counts[i]+" in total ("+corrects[i]*1.0/counts[i]+")");

	}
	
	public void run(String filename)
	{
		loadData(filename);
		double max=0;
		int maxK1=0;
		int maxK2=0;
		for (k1=5;k1<40;k1++)
		{
			for (k2=5;k2<40;k2++)
			{
				correctnes=0;
				for (int i=0;i<cross;i++)
				{
					doValidation(i);
				}
				correctnes/=cross;
				if (correctnes>max)
				{
					max=correctnes;
					maxK1=k1;
					maxK2=k2;
					System.out.println("new k1="+k1+" k2="+k2+" correctness="+correctnes);
				}
			}
		}
		System.out.println(""+maxK1+" "+maxK2);
		
	}
	
	public static void main(String[] argv)
	{
		Validator validator=new Validator();
		validator.run("train_voice_bb.txt");
	}
}
