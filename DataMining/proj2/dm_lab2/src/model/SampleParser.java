package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SampleParser {
	String instanceFilePath;

	public SampleParser(String instanceFilePath) {
		this.instanceFilePath = instanceFilePath;
	}
	
	/**
	 * parse the content of a file to a list of samples, samples in the list are all unlabeled
	 * @return the list of unlabeled samples
	 */
	public List<Sample> parse()
	{
		List<Sample> samples=new ArrayList<Sample>();
		BufferedReader instanceReader = null;
		try {
			instanceReader=new BufferedReader(new FileReader(new File(instanceFilePath)));
			String instanceLine=instanceReader.readLine();
			do
			{
				String[] attrStr=instanceLine.split("\\s+");
				double[] attrDouble=new double[attrStr.length];
				for (int i=0;i<attrDouble.length;i++)
				{
					attrDouble[i]=Double.parseDouble(attrStr[i]);
				}
				samples.add(new Sample(attrDouble));
				instanceLine=instanceReader.readLine();
			}while (instanceLine!=null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			if (instanceReader!=null)
			{
				try {
					instanceReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return samples;
	}
	
	public List<Sample> parseForTest(String labelFilePath) {
		List<Sample> samples=new ArrayList<Sample>();
		BufferedReader instanceReader = null;
		BufferedReader labelReader = null;
		try {
			instanceReader=new BufferedReader(new FileReader(new File(instanceFilePath)));
			labelReader=new BufferedReader(new FileReader(new File(labelFilePath)));
			String instanceLine=instanceReader.readLine();
			String labelLine=labelReader.readLine();
			do
			{
				int label=Integer.parseInt(labelLine);
				String[] attrStr=instanceLine.split("\\s+");
				double[] attrDouble=new double[attrStr.length];
				for (int i=0;i<attrDouble.length;i++)
				{
					attrDouble[i]=Double.parseDouble(attrStr[i]);
				}
				samples.add(new Sample(attrDouble, label));
				instanceLine=instanceReader.readLine();
				labelLine=labelReader.readLine();
			}while (instanceLine!=null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			if (instanceReader!=null)
			{
				try {
					instanceReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (labelReader!=null)
			{
				try {
					labelReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return samples;
	}

	/**
	 * parse the content of a file to an instance of SampleSet, samples in the set are all labeled
	 * @param labelFilePath path of the file contains the label info
	 * @return the SampleSet
	 */
	public SampleSet parse(String labelFilePath) {
		SampleSet samples=new SampleSet();
		BufferedReader instanceReader = null;
		BufferedReader labelReader = null;
		try {
			instanceReader=new BufferedReader(new FileReader(new File(instanceFilePath)));
			labelReader=new BufferedReader(new FileReader(new File(labelFilePath)));
			String instanceLine=instanceReader.readLine();
			String labelLine=labelReader.readLine();
			do
			{
				int label=Integer.parseInt(labelLine);
				String[] attrStr=instanceLine.split("\\s+");
				double[] attrDouble=new double[attrStr.length];
				for (int i=0;i<attrDouble.length;i++)
				{
					attrDouble[i]=Double.parseDouble(attrStr[i]);
				}
				samples.add(new Sample(attrDouble, label));
				instanceLine=instanceReader.readLine();
				labelLine=labelReader.readLine();
			}while (instanceLine!=null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			if (instanceReader!=null)
			{
				try {
					instanceReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (labelReader!=null)
			{
				try {
					labelReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return samples;
	}
	
	public static void main(String args[])
	{
//		SampleParser sp=new SampleParser("train instance", "train label");
//		SampleSet samples=sp.parse();
//		for(Sample s:samples)
//		{
//			for (int i=0;i<s.attributes.length;i++)
//				System.out.print(s.attributes[i]+" ");
//			System.out.println();
////			System.out.println(s.attributes);
//			System.out.println(s.label);
//		}
	}
}
