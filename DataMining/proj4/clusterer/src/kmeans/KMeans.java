package kmeans;

import java.util.ArrayList;
import java.util.List;

public class KMeans {
	List<Cluster> clusters;
	int currentRound=0;
	List<Item> items;
	int k=12;
	double disSum=1;
	int maxRound=10;
	
	public void setK(int k)
	{
		this.k=k;
	}
	
	public void setClusters(List<Cluster> clusters)
	{
		this.clusters=clusters;
	}
	
	public void setItems(List<Item> items)
	{
		this.items=items;
	}
	
	public void init()
	{
		clusters=new ArrayList<Cluster>();
		for (int i=0;i<k;i++)
		{
			Cluster cluster=new Cluster();
			cluster.add(items.get(i));
			clusters.add(cluster);
		}
	}
	
	public List<Cluster> doCluster()
	{
//		System.out.println("start clustering");
		currentRound=0;
		double newDisSum=0;
		while (true)
		{
			newDisSum=doRound();
			if (Math.abs(newDisSum-disSum)/disSum<0.001 || currentRound>=maxRound)
				break;
			disSum=newDisSum;
		}
//		System.out.println("end clustering");
		for (Cluster cluster:clusters)
        {
          cluster.clean();
        }
		return clusters;
	}
	
	public double doRound()
	{
//		System.out.println("in round "+currentRound);
		currentRound++;
		for (int i=0;i<clusters.size();i++)
		{
			clusters.get(i).newRound();
		}
		for (Item item:items)
		{
			Cluster cluster=getNearestCluster(item);
			cluster.add(item);
		}
		List<Cluster> oldClusters=clusters;
        clusters=new ArrayList<>();
        for (Cluster cluster:oldClusters)
        {
          if (cluster.items.size()>0)
            clusters.add(cluster);
        }
		double newDisSum=0;
		for (int i=0;i<clusters.size();i++)
		{
			newDisSum+=clusters.get(i).getDisSum();
		}
		return newDisSum;
	}
	
	public Cluster getNearestCluster(Item item)
	{
		double min=Double.MAX_VALUE;
		int pos=0;
		for (int i=0;i<clusters.size();i++)
		{
			double distance=clusters.get(i).getDistance(item);
			if (distance<min)
			{
				min=distance;
				pos=i;
			}
		}
		return clusters.get(pos);
	}
	
	public void pointToDistanceToNearest(Item item,int suggestLabel)
    {
      double distance=Double.MAX_VALUE;
      double min=Double.MAX_VALUE;
      for (int i=0;i<clusters.size();i++)
      {
        double d=clusters.get(i).getDistance(item);
        if (clusters.get(i).label==suggestLabel)
        {
          if (d<distance)
          {
            distance=d;
          }
        }
        if (d<min)
        {
          min=d;
          item.label=clusters.get(i).label;
        }
      }
      item.dis=distance;
    }
	
}
