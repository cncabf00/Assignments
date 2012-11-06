package edu.njucs.emalgorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import weka.clusterers.EM;
import weka.core.Instances;

public class EMClusterer {
	
	public static void main(String[] args)
	{
		EM em=new EM();
//		try {
//			FileWriter writer=new FileWriter(new File("head"));
//			writer.write("@relation cluster\n");
//			for (int i=0;i<856;i++)
//			{
//				writer.append("@attribute "+i+" real\n");
//			}
//			writer.append("@data");
//			writer.flush();
//			writer.close();
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		
		FileReader reader;
		try {
			reader = new FileReader(new File("clustering data"));
			Instances instances=new Instances(reader);
			em.buildClusterer(instances);
//			System.out.println(""+instances.get(0).classIndex());
			FileWriter writer=new FileWriter(new File("result"));
			for (int i=0;i<instances.size();i++)
			{
				writer.append(""+em.clusterInstance(instances.get(i))+"\n");
			}
			writer.flush();
			writer.close();
			writer=new FileWriter(new File("details"));
			writer.write(em.toString());
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
