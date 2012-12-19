package customer;


public class ItemVoiceOnly {
	static String[] filedNames={
		"price","avgRealPayment6Months","avgLastBill6Months","avgPayment6Months",
		"avgCredit6Months","avgOutstanding6Months","avgCurrentBill6Months","avgRounding6Months",
		"avgLocalCall6Months","avgNationalCall6Months","avgInternationalCall6Months",
		"avgInternetCall6Months","avgComplaint6Months","avgInquiry6Months","avgTechnical6Months",
		"avgRequest6Months","avgMaintenance6Months","avgAdjustment6Months",
		"avgRealPayment2Months","avgLastBill2Months","avgPayment2Months",
		"avgCredit2Months","avgOutstanding2Months","avgCurrentBill2Months","avgRounding2Months",
		"avgLocalCall2Months","avgNationalCall2Months","avgInternationalCall2Months",
		"avgInternetCall2Months","avgComplaint2Months","avgInquiry2Months","avgTechnical2Months",
		"avgRequest2Months","avgMaintenance2Months","avgAdjustment2Months",
	};
	static double[] weights=new double[filedNames.length];
	static double[] max=new double[filedNames.length];
	static double[] min=new double[filedNames.length];
	String id;
	boolean terminated;
	double[] fields=new double[filedNames.length];
	
	static
	{
		for (int i=0;i<max.length;i++)
			max[i]=Double.MIN_VALUE;
		for (int i=0;i<min.length;i++)
			min[i]=Double.MAX_VALUE;
	}
	
	public ItemVoiceOnly(Customer customer,Service service,int lastMonth)
	{
		for (int i=0;i<fields.length;i++)
			fields[i]=0;
		id=customer.customerID;
		terminated=service.terminated;
		fields[0]=service.price;
//		int terminatedMonth=7;
		int terminatedMonth=lastMonth;
		if (terminatedMonth>13)
			terminatedMonth=13;
		int startMonth=lastMonth-7;
		if (startMonth<0)
			startMonth=0;
//		if (terminated)
//			terminatedMonth=service.terminatedMonth;
		int count1=0;
		int count2=0;
		for (int i=startMonth;i<terminatedMonth-1 && i<12;i++)
		{
			count1++;
			fields[1]+=service.billingAccount.realPayments[i];
			fields[2]+=service.billingAccount.lastBills[i];
			fields[3]+=service.billingAccount.payments[i];
			fields[4]+=service.billingAccount.credit[i];
			fields[5]+=service.billingAccount.outstandings[i];
			fields[6]+=service.billingAccount.currentBills[i];
			fields[7]+=service.billingAccount.roundings[i];
			if (service.billingAccount.voiceUsages[i]!=null)
			{
				fields[8]+=service.billingAccount.voiceUsages[i].local;
				fields[9]+=service.billingAccount.voiceUsages[i].national;
				fields[10]+=service.billingAccount.voiceUsages[i].international;
				fields[11]+=service.billingAccount.voiceUsages[i].internet;
			}			
			int temp=customer.complaints[i][0]==null?0:customer.complaints[i][0].count;
			fields[12]+=temp;
			temp=customer.complaints[i][1]==null?1:customer.complaints[i][1].count;
			fields[13]+=temp;
			temp=customer.complaints[i][2]==null?2:customer.complaints[i][2].count;
			fields[14]+=temp;
			temp=customer.complaints[i][3]==null?3:customer.complaints[i][3].count;
			fields[15]+=temp;
			temp=customer.complaints[i][4]==null?4:customer.complaints[i][4].count;
			fields[16]+=temp;
			temp=customer.complaints[i][5]==null?5:customer.complaints[i][5].count;
			fields[17]+=temp;
			if (lastMonth-i<=3 || i==terminatedMonth-2)
			{
				count2++;
				fields[18]+=service.billingAccount.realPayments[i];
				fields[19]+=service.billingAccount.lastBills[i];
				fields[20]+=service.billingAccount.payments[i];
				fields[21]+=service.billingAccount.credit[i];
				fields[22]+=service.billingAccount.outstandings[i];
				fields[23]+=service.billingAccount.currentBills[i];
				fields[24]+=service.billingAccount.roundings[i];
				if (service.billingAccount.voiceUsages[i]!=null)
				{
					fields[25]+=service.billingAccount.voiceUsages[i].local;
					fields[26]+=service.billingAccount.voiceUsages[i].national;
					fields[27]+=service.billingAccount.voiceUsages[i].international;
					fields[28]+=service.billingAccount.voiceUsages[i].internet;
				}
				temp=customer.complaints[i][0]==null?0:customer.complaints[i][0].count;
				fields[29]+=temp;
				temp=customer.complaints[i][1]==null?1:customer.complaints[i][1].count;
				fields[30]+=temp;
				temp=customer.complaints[i][2]==null?2:customer.complaints[i][2].count;
				fields[31]+=temp;
				temp=customer.complaints[i][3]==null?3:customer.complaints[i][3].count;
				fields[32]+=temp;
				temp=customer.complaints[i][4]==null?4:customer.complaints[i][4].count;
				fields[33]+=temp;
				temp=customer.complaints[i][5]==null?5:customer.complaints[i][5].count;
				fields[34]+=temp;
			}
		}
		for (int i=1;i<=17;i++)
			fields[i]/=count1;
		for (int i=18;i<=34;i++)
			fields[i]/=count2;
		for (int i=0;i<fields.length;i++)
		{
			if (Double.isNaN(fields[i]))
			{
				System.out.println("nan");
			}
			if (fields[i]<min[i])
				min[i]=fields[i];
			else if (fields[i]>max[i])
				max[i]=fields[i];
		}
	}
	
	public static void computeWeights()
	{
		for (int i=0;i<max.length;i++)
		{
			weights[i]=100/(max[i]-min[i]);
			if (Double.isInfinite(weights[i]))
			{
				System.out.println(filedNames[i]+" is infinite");
			}
		}
		
	}
}
