package kmeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

	
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
			String[] strs=line.split(" ");
			Item.fieldNames=strs;
			line=reader.readLine();
			strs=line.split(" ");
			Item.weights=new double[Item.fieldNames.length];
			Item.bonusWights=new double[Item.fieldNames.length];
			for (int i=0;i<strs.length;i++)
			{
				Item.weights[i]=Double.parseDouble(strs[i]);
				Item.bonusWights[i]=1;
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
				strs=line.split(" ");
				Item item=new Item();
				item.id=strs[0];
				item.fields=new double[Item.fieldNames.length];
				for (int i=0;i<item.fields.length;i++)
				{
					item.fields[i]=Double.parseDouble(strs[i+1]);
				}
				item.type=Integer.parseInt(strs[strs.length-1]);
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
