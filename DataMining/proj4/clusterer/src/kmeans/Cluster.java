package kmeans;

import java.util.HashSet;
import java.util.Set;

public class Cluster {
	Set<Item> items=new HashSet<Item>();
	double[] center=new double[Item.fieldNames.length];
	int label;
	double disSum=0;
	
	public double getDistance(Item item)
	{
		double distance=0;
		for (int i=0;i<item.fields.length;i++)
		{
			distance+=Math.sqrt((center[i]-item.fields[i])*(center[i]-item.fields[i]))*Item.weights[i]*Item.bonusWeights[i];
		}
		if (Double.isNaN(distance))
		{
		  System.out.println("nan");
		}
		return distance;
	}
	
	public void computeCenter()
	{
		center=new double[Item.fieldNames.length];
		for (int i=0;i<center.length;i++)
			center[i]=0;
		for (Item item:items)
		{
			for (int i=0;i<item.fields.length;i++)
				center[i]+=item.fields[i];
		}
		for (int i=0;i<center.length;i++)
		{
			center[i]/=items.size();
			if (Double.isNaN(center[i]))
			{
			  System.out.println("nan");
			}
		}
	}
	
	public double getDisSum()
	{
		return disSum;
	}
	
	public void add(Item item)
	{
		items.add(item);
		disSum+=getDistance(item);
	}
	
	public void newRound()
	{
		computeCenter();
		items=new HashSet<Item>();
		disSum=0;
	}
	
	public void clean()
	{
	  int k=0;
	  for (int i=0;i<k;i++)
	  {
	    double avgDis=disSum/items.size();
	    Set<Item> oldItems=items;
	    items=new HashSet<>();
	    disSum=0;
	    for (Item item:oldItems)
	    {
	      if (getDistance(item)<=avgDis)
	      {
	        add(item);
	      }
	    }
	    computeCenter();
	  }
	}
}
