package kmeans;

public class Item implements Comparable<Item>{
	int type;
	static String[] fieldNames;
	static double[] weights;
	double[] fields;
	static double[] bonusWeights;
	Cluster cluster;
	String id;
	double dis=0;
	int label;
    @Override
    public int compareTo(Item o) {
      if (this.label!=o.label)
        return this.label-o.label;
      else
        return new Double(this.dis).compareTo(o.dis);
    }
	
	
}
