package model;

public class Sample {
	double[] attributes;
	int label;
	
	public Sample(double[] attributes)
	{
		this.attributes=attributes.clone();
	}
	
	public Sample(double[] attributes,int label)
	{
		this.attributes=attributes.clone();
		this.label=label;
	}
	
	public double[] getAttributes() {
		return attributes;
	}
	
	public int getLabel() {
		return label;
	}
	
	public void setLabel(int label)
	{
		this.label=label;
	}
	
	
}
