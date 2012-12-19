package customer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
	static final String PROFILE_FILE="SME/SME_Customer_Profile.txt";
	static final String VOICE_BB_SUBSCRIBERS_FILE="SME/SME_VoiceBB_Subscribers.txt";
	static final String VOICE_ONLY_SUBCRIBERS_FILE="SME/SME_VoiceOnly_Subscribers.txt";
	static final String COMPLAINT_FILE="SME/SME_Cus_SvcReq_Complaint.txt";
	static final String BILLING_FILE="SME/SME_Billing_Account.txt";
	static final String VOICE_USAGE_FILE="SME/SME_Voice_Usage.txt";
	static final String DOWNLOAD_FILE="SME/SME_Broadband_Download.txt";
	static final String UPLOAD_FILE="SME/SME_Broadband_Upload.txt";
	static final String TRAIN_VOICE_ONLY="train_voice_only.txt";
	static final String TRAIN_VOICE_BB="train_voice_bb.txt";
	static final String PREDICT_VOICE_ONLY="predict_voice_only.txt";
	static final String PREDICT_VOICE_BB="predict_voice_bb.txt";
	static final String PREDICT_VOICE_ONLY_ID="predict_voice_only_id.txt";
    static final String PREDICT_VOICE_BB_ID="predict_voice_bb_id.txt";
	
	Map<String,Service> serviceMap=new HashMap<String, Service>();
	Map<String,Customer> customerMap=new HashMap<String, Customer>();
	Map<String,BillingAccount> billingMap=new HashMap<String, BillingAccount>();
	List<ItemVoiceOnly> trainingItemsVoiceOnly=new ArrayList<ItemVoiceOnly>();
	List<ItemVoiceBB> trainingItemsVoiceBB=new ArrayList<ItemVoiceBB>();
	List<ItemVoiceOnly> predictItemsVoiceOnly=new ArrayList<ItemVoiceOnly>();
	List<ItemVoiceBB> predictItemsVoiceBB=new ArrayList<ItemVoiceBB>();
	
	int omitted=0;
	
	public void genTrainingItems()
	{
		System.out.println("generating items");
		File file=new File("alreade_terminated");
		try {
			FileWriter fw=new FileWriter(file);
			fw.write("customers alread churned in 2012?\n");
			int count=0;
			int lastPercent=0;
			for (Customer customer:customerMap.values())
			{
				count++;
				int percent=100*count/customerMap.size();
				if (percent%10==0 && percent!=lastPercent)
				{
					lastPercent=percent;
					System.out.println(""+percent+"%");
				}
				List<Service> services=customer.getConcernedService();
				if (services.size()==0)
					continue;
				else
				{
					for (Service service:services)
					{
						if(service.terminated && service.terminatedYear==2012 && service.terminatedMonth<=3)
						{
							try {
								fw.append(customer.customerID+" "+service.terminatedYear+"."+service.terminatedMonth+"\n");
							} catch (IOException e) {
								e.printStackTrace();
							}
							continue;
						}
						if (service.terminated && service.terminatedMonth<=2)
							continue;
						int lastMonth=7;
						if (service.terminated)
							lastMonth=service.terminatedMonth;
						if (service.type==Type.VoiceOnly)
						{
							trainingItemsVoiceOnly.add(new ItemVoiceOnly(customer,service,lastMonth));
						}
						else
						{
							trainingItemsVoiceBB.add(new ItemVoiceBB(customer,service,lastMonth));
						}
					}
				}
				
				
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void genPridictItems()
	{
		int count=0;
		int lastPercent=0;
		for (Customer customer:customerMap.values())
		{
			count++;
			int percent=100*count/customerMap.size();
			if (percent%10==0 && percent!=lastPercent)
			{
				lastPercent=percent;
				System.out.println(""+percent+"%");
			}
			List<Service> services=customer.getConcernedService();
			if (services.size()==0)
				continue;
			else
			{
				for (Service service:services)
				{
					if (service.terminated)
						continue;
					for (int lastMonth=13;lastMonth<=15;lastMonth++)
					{
						if (service.type==Type.VoiceOnly)
						{
							predictItemsVoiceOnly.add(new ItemVoiceOnly(customer,service,lastMonth));
						}
						else
						{
							predictItemsVoiceBB.add(new ItemVoiceBB(customer,service,lastMonth));
						}
					}
				}
			}
		}
	}
	
	public void printResults()
	{
		File file=new File(TRAIN_VOICE_ONLY);
		try {
			FileWriter fw=new FileWriter(file);
			StringBuilder str=new StringBuilder();
			fw.write("@RELATION SME\n");
			str.append("@ATTRIBUTE ID STRING\n");
			for (int i=0;i<ItemVoiceOnly.filedNames.length;i++)
			{
				str.append("@ATTRIBUTE "+ItemVoiceOnly.filedNames[i]+" REAL\n");
			}
			str.append("@ATTRIBUTE class {unterminated,terminated}\n");
			fw.append(str.toString());
//			ItemVoiceOnly.computeWeights();
//			str=new StringBuilder();
//			for (int i=0;i<ItemVoiceOnly.weights.length;i++)
//			{
//				str.append(ItemVoiceOnly.weights[i]);
//				if (i!=ItemVoiceOnly.weights.length-1)
//					str.append(" ");
//				else
//					str.append("\n");
//			}
//			fw.append(str.toString());
			fw.append("@data\n");
			for (ItemVoiceOnly item:trainingItemsVoiceOnly)
			{
				str=new StringBuilder();
				str.append(item.id+" ");
				for (int i=0;i<item.fields.length;i++)
				{
					str.append(item.fields[i]);
					str.append(" ");
				}
				String label="unterminated";
				if (item.terminated)
					label="terminated";
				str.append(label);
				str.append("\n");
				fw.append(str.toString());
			}
			
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		file=new File(TRAIN_VOICE_BB);
		try {
			FileWriter fw=new FileWriter(file);
			StringBuilder str=new StringBuilder();
			fw.write("@RELATION SME\n");
			str.append("@ATTRIBUTE ID STRING\n");
            for (int i=0;i<ItemVoiceBB.filedNames.length;i++)
            {
                str.append("@ATTRIBUTE "+ItemVoiceBB.filedNames[i]+" REAL\n");
            }
            str.append("@ATTRIBUTE class {unterminated,terminated}\n");
            fw.append(str.toString());
			
//			ItemVoiceBB.computeWeights();
//			str=new StringBuilder();
//			for (int i=0;i<ItemVoiceBB.weights.length;i++)
//			{
//				str.append(ItemVoiceBB.weights[i]);
//				if (i!=ItemVoiceBB.weights.length-1)
//					str.append(" ");
//				else
//					str.append("\n");
//			}
//			fw.append(str.toString());
			
            fw.append("@data\n");
			for (ItemVoiceBB item:trainingItemsVoiceBB)
			{
				str=new StringBuilder();
				str.append(item.id+" ");
				for (int i=0;i<item.fields.length;i++)
				{
				  if (Double.isNaN(item.fields[i]))
				  {
				    System.out.println(item.id);
				  }
					str.append(item.fields[i]);
					str.append(" ");
				}
				String label="unterminated";
                if (item.terminated)
                    label="terminated";
				str.append(label);
				str.append("\n");
				fw.append(str.toString());
			}
			
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		file=new File(PREDICT_VOICE_ONLY);
		File file1=new File(PREDICT_VOICE_ONLY_ID);
		try {
			FileWriter fw=new FileWriter(file);
			FileWriter fw1=new FileWriter(file1);
			StringBuilder str=new StringBuilder();
			fw.write("@RELATION SME\n");
			str.append("@ATTRIBUTE ID STRING\n");
            for (int i=0;i<ItemVoiceOnly.filedNames.length;i++)
            {
                str.append("@ATTRIBUTE "+ItemVoiceOnly.filedNames[i]+" REAL\n");
            }
            str.append("@ATTRIBUTE class {unterminated,terminated}\n");
			fw.write(str.toString());
			fw.append("@data\n");
			for (ItemVoiceOnly item:predictItemsVoiceOnly)
			{
				str=new StringBuilder();
				str.append(item.id+" ");
				for (int i=0;i<item.fields.length;i++)
				{
					str.append(item.fields[i]);
					str.append(" ");
				}
				str.append("unterminated");
				str.append("\n");
				fw.write(str.toString());
				fw1.write(item.id+"\n");
			}
			
			fw.flush();
			fw.close();
			fw1.flush();
			fw1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		file=new File(PREDICT_VOICE_BB);
		file1=new File(PREDICT_VOICE_BB_ID);
		try {
			FileWriter fw=new FileWriter(file);
			FileWriter fw1=new FileWriter(file1);
			StringBuilder str=new StringBuilder();
			fw.write("@RELATION SME\n");
			str.append("@ATTRIBUTE ID STRING\n");
            for (int i=0;i<ItemVoiceBB.filedNames.length;i++)
            {
                str.append("@ATTRIBUTE "+ItemVoiceBB.filedNames[i]+" REAL\n");
            }
            str.append("@ATTRIBUTE class {unterminated,terminated}\n");
			fw.write(str.toString());
			fw.append("@data\n");
			for (ItemVoiceBB item:predictItemsVoiceBB)
			{
				str=new StringBuilder();
				str.append(item.id+" ");
				for (int i=0;i<item.fields.length;i++)
				{
					str.append(item.fields[i]);
					str.append(" ");
				}
				str.append("unterminated");
				str.append("\n");
				fw.write(str.toString());
				fw1.write(item.id+"\n");
			}
			
			fw.flush();
			fw.close();
			fw1.flush();
            fw1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void parseProfile()
	{
		File file=new File(PROFILE_FILE);
		System.out.println("parsing file "+file.getName());
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=reader.readLine();
			line=reader.readLine();
			while (line!=null)
			{
				String[] strs=line.split(",",4);
				if (strs.length==1)
				{
					line=reader.readLine();
					omitted++;
					continue;
				}
				Customer customer=new Customer();
				customer.customerID=strs[0];
				customer.industory=strs[1];
				customer.since=strs[2];
				customerMap.put(customer.customerID, customer);
				line=reader.readLine();
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
	
	public void parseVoiceOnlySubscribers()
	{
		File file=new File(VOICE_ONLY_SUBCRIBERS_FILE);
		System.out.println("parsing file "+file.getName());
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=reader.readLine();
			line=reader.readLine();
			while (line!=null)
			{
				String[] strs=line.split(",",14);
				if (strs.length==1)
				{
					line=reader.readLine();
					omitted++;
					continue;
				}
				Service service=new Service();
				service.type=Type.VoiceOnly;
				String customerID=strs[0];
				BillingAccount billingAccount=new BillingAccount();
				billingAccount.billingID=strs[1];
				service.billingAccount=billingAccount;
				service.serviceID=strs[2];
				service.location=strs[3];
				service.hsbbArea=strs[4];
				try
				{
					service.price=Double.parseDouble(strs[5].replaceAll("\"",""));
				}
				catch (Exception e)
				{
					service.price=0;
				}
				service.terminated=!strs[7].equals("");
				if (service.terminated)
				{
					String[] dateStrs=strs[7].split(" ")[0].split("-");
					service.terminatedYear=Integer.parseInt(dateStrs[2]);
					service.terminatedMonth=Integer.parseInt(dateStrs[1]);
				}
				Customer customer=customerMap.get(customerID);
				customer.services.add(service);
				serviceMap.put(service.serviceID, service);
				billingMap.put(billingAccount.billingID, billingAccount);
				line=reader.readLine();
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
	
	public void parseVoiceBBSubscribers()
	{
		File file=new File(VOICE_BB_SUBSCRIBERS_FILE);
		System.out.println("parsing file "+file.getName());
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=reader.readLine();
			line=reader.readLine();
			while (line!=null)
			{
				String[] strs=line.split(",",14);
				if (strs.length==1)
				{
					line=reader.readLine();
					omitted++;
					continue;
				}
				Service service=new Service();
				service.type=Type.VoiceBB;
				String customerID=strs[0];
				BillingAccount billingAccount=new BillingAccount();
				billingAccount.billingID=strs[1];
				service.billingAccount=billingAccount;
				service.serviceID=strs[2];
				service.location=strs[3];
				service.hsbbArea=strs[4];
				String speedStr=strs[5].toLowerCase().replaceAll("\"", "");
				if (speedStr.length()==0)
					service.speed=0;
				else if (speedStr.charAt(speedStr.length()-1)=='m')
				{
					service.speed=1024*Double.parseDouble(speedStr.substring(0, speedStr.length()-1));
				}
				else if (speedStr.charAt(speedStr.length()-1)=='k')
				{
					service.speed=Double.parseDouble(speedStr.substring(0, speedStr.length()-1));
				}
				try
				{
					service.price=Integer.parseInt(strs[6].replaceAll("\"",""));
				}
				catch (Exception e)
				{
//					System.out.println(e.toString());
					service.price=0;
				}
				service.terminated=!strs[8].equals("");
				if (service.terminated)
				{
					String[] dateStrs=strs[8].split(" ")[0].split("-");
					service.terminatedYear=Integer.parseInt(dateStrs[2]);
					service.terminatedMonth=Integer.parseInt(dateStrs[1]);
				}
				try
				{
					service.period=Integer.parseInt(strs[9].split(" ")[0].replaceAll("\"", ""));
				}
				catch (Exception e)
				{
					service.period=0;
				}
				Customer customer=customerMap.get(customerID);
				customer.services.add(service);
				serviceMap.put(service.serviceID, service);
				billingMap.put(billingAccount.billingID, billingAccount);
				line=reader.readLine();
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
	
	public void parseComplaint()
	{
		File file=new File(COMPLAINT_FILE);
		System.out.println("parsing file "+file.getName());
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=reader.readLine();
			line=reader.readLine();
			while (line!=null)
			{
				String[] strs=line.split(",",6);
				if (strs.length==1)
				{
					line=reader.readLine();
					omitted++;
					continue;
				}
				String customerID=strs[0];
				Complaint complaint=new Complaint();
				String typeStr=strs[1].toLowerCase().replaceAll("\"","");
				int pos=0;
				if (typeStr.equals("complaint"))
				{
					pos=0;
					complaint.callType=ComplaintType.Complaint;
				}
				else if(typeStr.equals("inquiry") || typeStr.startsWith("inquiries"))
				{
					pos=1;
					complaint.callType=ComplaintType.Inquiry;
				}
				else if(typeStr.contains("request"))
				{
					pos=2;
					complaint.callType=ComplaintType.Request;
				}
				else if(typeStr.contains("maintenance"))
				{
					pos=3;
					complaint.callType=ComplaintType.Maintenance;
				}
				else if(typeStr.equals("technical"))
				{
					pos=4;
					complaint.callType=ComplaintType.Technical;
				}
				else if(typeStr.equals("adjustment"))
				{
					pos=5;
					complaint.callType=ComplaintType.Adjustment;
				}
				complaint.month=Integer.parseInt(strs[3].split("/")[1].replaceAll("\"", ""))-1;
				complaint.count=(int) Double.parseDouble(strs[4]);
				customerMap.get(customerID).complaints[complaint.month][pos]=complaint;
				line=reader.readLine();
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
	
	public void parseBillingAccount()
	{
		File file=new File(BILLING_FILE);
		System.out.println("parsing file "+file.getName());
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=reader.readLine();
			line=reader.readLine();
			while (line!=null)
			{
				String[] strs=line.split(",",10);
				if (strs.length==1)
				{
					line=reader.readLine();
					omitted++;
					continue;
				}
				String billingID=strs[0];
				BillingAccount billingAccount=billingMap.get(billingID);
				int month=Integer.parseInt(strs[1].substring(4,strs[1].length()))-1;
				billingAccount.lastBills[month]=Double.parseDouble(strs[2]);
				billingAccount.payments[month]=Double.parseDouble(strs[3]);
				if (strs[4].equals(""))
					billingAccount.credit[month]=0;
				else
					billingAccount.credit[month]=Double.parseDouble(strs[4]);
				billingAccount.outstandings[month]=Double.parseDouble(strs[5]);
				billingAccount.currentBills[month]=Double.parseDouble(strs[6]);
				billingAccount.roundings[month]=Double.parseDouble(strs[7]);
				billingAccount.realPayments[month]=Double.parseDouble(strs[8]);
				line=reader.readLine();
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
	
	public void parseVoiceUsage()
	{
		File file=new File(VOICE_USAGE_FILE);
		System.out.println("parsing file "+file.getName());
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=reader.readLine();
			line=reader.readLine();
			while (line!=null)
			{
				String[] strs=line.split(",",7);
				if (strs.length==1)
				{
					line=reader.readLine();
					omitted++;
					continue;
				}
				BillingAccount billingAccount=billingMap.get(strs[0]);
				int month=Integer.parseInt(strs[1].substring(4,strs[1].length()))-1;
				VoiceUsage voiceUsage=new VoiceUsage();
				voiceUsage.local=Double.parseDouble(strs[2]);
				voiceUsage.national=Double.parseDouble(strs[3]);
				voiceUsage.international=Double.parseDouble(strs[4]);
				voiceUsage.internet=Double.parseDouble(strs[5]);
				billingAccount.voiceUsages[month]=voiceUsage;
				line=reader.readLine();
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
	
	public void parseDownload()
	{
		File file=new File(DOWNLOAD_FILE);
		System.out.println("parsing file "+file.getName());
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=reader.readLine();
			line=reader.readLine();
			while (line!=null)
			{
				String[] strs=line.split(",",14);
				if (strs.length==1)
				{
					line=reader.readLine();
					omitted++;
					continue;
				}
				Service service=serviceMap.get(strs[0]);
				if (service==null)
				{
					System.out.println(line);
					line=reader.readLine();
					continue;
				}
				for (int i=0;i<12;i++)
				{
					if (strs[i+1].equals(""))
					{
						service.downloadUsages[i]=0;
					}
					else
					{
						service.downloadUsages[i]=Double.parseDouble(strs[i+1]);
					}
				}
				line=reader.readLine();
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
	
	public void parseUpload()
	{
		File file=new File(UPLOAD_FILE);
		System.out.println("parsing file "+file.getName());
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=reader.readLine();
			line=reader.readLine();
			while (line!=null)
			{
				String[] strs=line.split(",",14);
				if (strs.length==1)
				{
					line=reader.readLine();
					omitted++;
					continue;
				}
				Service service=serviceMap.get(strs[0]);
				if (service==null)
				{
					System.out.println(line);
					line=reader.readLine();
					continue;
				}
				for (int i=0;i<12;i++)
				{
					if (strs[i+1].equals(""))
					{
						service.uploadUsages[i]=0;
					}
					else
					{
						service.uploadUsages[i]=Double.parseDouble(strs[i+1]);
					}
				}
				line=reader.readLine();
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
	
	public void parseAll()
	{
		parseProfile();
		parseVoiceOnlySubscribers();
		parseVoiceBBSubscribers();
		parseComplaint();
		parseBillingAccount();
		parseVoiceUsage();
		parseDownload();
		parseUpload();
		System.out.println(""+omitted+" lines omitted");
	}
	
	public static void main(String[] argv)
	{
		Parser parser=new Parser();
		parser.parseAll();
		parser.genTrainingItems();
		parser.genPridictItems();
		parser.printResults();
	}
}
