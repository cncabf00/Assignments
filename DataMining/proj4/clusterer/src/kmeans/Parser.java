package kmeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

	public void readBounsWeight(String filename)
	{
	  File file=new File(filename);
  	  try {
        BufferedReader reader=new BufferedReader(new FileReader(file));
        String line=reader.readLine();
        String[] strs=line.split(" ");
        Item.bonusWeights=new double[Item.fieldNames.length];
        for (int i=0;i<Item.fieldNames.length;i++)
        {
          Item.bonusWeights[i]=1;
        }
        reader.close();
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
	}
  
	public List<List<Item>> parseTrainingFile(String filename)
	{
//		System.out.println("parsing training file "+filename);
		List<List<Item>> lists=new ArrayList<List<Item>>();
		List<Item> terminated=new ArrayList<Item>();
		List<Item> unterminated=new ArrayList<Item>();
		lists.add(terminated);
		lists.add(unterminated);
		File file=new File(filename);
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=reader.readLine();
			List<String> fields=new ArrayList<>();
			while (!line.toLowerCase().startsWith("@data"))
			{
			  String strs[]=line.split(" ");
			  if (strs[0].toLowerCase().equals("@attribute"))
			  {
			    fields.add(strs[1]);
			  }
			  line=reader.readLine();
			}
			Item.fieldNames=fields.subList(0, fields.size()-1).toArray(new String[0]);
//			String[] strs=line.split(" ");
//			line=reader.readLine();
//			strs=line.split(" ");
			Item.weights=new double[Item.fieldNames.length];
			Item.bonusWeights=new double[Item.fieldNames.length];
			for (int i=0;i<Item.fieldNames.length;i++)
			{
//				Item.weights[i]=Double.parseDouble(strs[i]);
			  Item.weights[i]=1;
			  Item.bonusWeights[i]=1;
			}
//			Item.bonusWights[10]=100;
			line=reader.readLine();
			while (line!=null)
			{
				if (line.startsWith("#"))
				{
					line=reader.readLine();
					continue;
				}
				String[] strs=line.split(" ");
				Item item=new Item();
//				item.id=strs[0];
				item.fields=new double[Item.fieldNames.length];
				for (int i=0;i<item.fields.length;i++)
				{
					item.fields[i]=Double.parseDouble(strs[i]);
					if (Double.isNaN(item.fields[i]))
					{
					  System.out.println(line);
				    }
				}
				if (strs[strs.length-1].equals("terminated"))
				  item.type=1;
				else
				  item.type=0;
				lists.get(item.type).add(item);
				line=reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lists;
	}
	
	public List<Item> parsePredictFile(String filename)
	{
		List<Item> items=new ArrayList<Item>();
		File file=new File(filename);
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=reader.readLine();
			String[] strs=line.split(" ");
			Item.fieldNames=strs;
			line=reader.readLine();
			strs=line.split(" ");
			for (int i=0;i<strs.length;i++)
			{
				Item.weights[i]=Double.parseDouble(strs[i]);
			}
			line=reader.readLine();
			while (line!=null)
			{
				strs=line.split(" ");
				Item item=new Item();
				item.id=strs[0];
				item.fields=new double[Item.fieldNames.length];
				for (int i=0;i<item.fields.length;i++)
				{
					item.fields[i]=Double.parseDouble(strs[i+1]);
				}
				line=reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return items;
	}
}
