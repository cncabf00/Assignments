package customer;

import java.util.ArrayList;
import java.util.List;



public class Customer {
	String customerID;
	String industory;
	String since;
	Complaint[][] complaints=new Complaint[12][6];
	List<Service> services=new ArrayList<Service>();
	
	List<Service> getConcernedService()
	{
		List<Service> concerned=new ArrayList<Service>();
		for (int i=0;i<services.size();i++)
		{
			if (!services.get(i).terminated || services.get(i).terminatedYear>=2011)
			{
				concerned.add(services.get(i));
			}
		}
//		if (concerned.size()>1)
//		{
//			System.out.println("concerned service more than one!");
//			for (int i=0;i<concerned.size();i++)
//			{
//				System.out.print(concerned.get(i).serviceID+" ");
//			}
//			System.out.println();
//		}
//		if (concerned.size()>0)
		return concerned;
//		else
//			return null;
	}
}
