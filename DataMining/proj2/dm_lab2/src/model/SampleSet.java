package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SampleSet {
	List<List<Sample>> data=new ArrayList<List<Sample>>();
	
	public static SampleSet generateSampleSet(List<Sample> samples)
	{
		SampleSet set=new SampleSet();
		for (Sample s:samples)
		{
			set.add(s);
		}
		return set;
	}
	
	public void add(Sample sample)
	{
		boolean exist=false;
		for (int i=0;i<data.size();i++)
		{
			if (sample.label==getLabel(i))
			{
				data.get(i).add(sample);
				exist=true;
				break;
			}
		}
		if (!exist)
		{
			List<Sample> newList=new ArrayList<Sample>();
			newList.add(sample);
			data.add(newList);
		}
	}
	
	public List<Sample> getData(int n)
	{
		if (n>=data.size() || n<0)
			return null;
		else
			return data.get(n);
	}
	
	public int getLabel(int n)
	{
		if (n>=data.size() || n<0)
			return 0;
		else
			return data.get(n).get(0).label;
	}
	
	public int getNumOfLabels()
	{
		return data.size();
	}
	
	public int getLabelOfMajority()
	{
		int maxSize=0;
		int label=0;
		for (int i=0;i<data.size();i++)
		{
			if (maxSize<data.get(i).size())
			{
				maxSize=data.get(i).size();
				label=getLabel(i);
			}
		}
		return label;
	}
	
	public int getTotalCount()
	{
		int total=0;
		for (int i=0;i<data.size();i++)
		{
			total+=data.get(i).size();
		}
		return total;
	}
	
	public int getCount(int n)
	{
		if (n>=data.size() || n<0)
			return 0;
		else
			return data.get(n).size();
	}
	
	public List<Sample> getSortedListByAttribute(final int attr)
	{
		List<Sample> array=new ArrayList<Sample>();
		for (List<Sample> l:data)
		{
			array.addAll(l);
		}
		Collections.sort(array, new Comparator<Sample>() {

			@Override
			public int compare(Sample s1, Sample s2) {
				if (s1.attributes[attr]>s2.attributes[attr])
					return 1;
				else if (s1.attributes[attr]==s2.attributes[attr])
					return 0;
				else
					return -1;
			}
		});
		return array;
	}
	
	public static void main(String[] args)
	{
		List<Integer> list=new ArrayList<Integer>();
		list.add(10);
		list.add(4);
		list.add(15);
		Collections.sort(list,new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return o1-o2;
			}
		});
		for (int i=0;i<list.size();i++)
		{
			System.out.print(list.get(i)+" ");
		}
	}
}
