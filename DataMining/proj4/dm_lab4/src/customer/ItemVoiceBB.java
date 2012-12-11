package customer;


public class ItemVoiceBB {
	static String[] filedNames={
		"price","period","avgRealPayment6Months","avgLastBill6Months","avgPayment6Months",
		"avgCredit6Months","avgOutstanding6Months","avgCurrentBill6Months","avgRounding6Months",
		"avgLocalCall6Months","avgNationalCall6Months","avgInternationalCall6Months",
		"avgInternetCall6Months","avgComplaint6Months","avgInquiry6Months","avgTechnical6Months",
		"avgRequest6Months","avgMaintenance6Months","avgAdjustment6Months","avgRealPayment2Months","avgLastBill2Months","avgPayment2Months",
		"avgCredit2Months","avgOutstanding2Months","avgCurrentBill2Months","avgRounding2Months",
		"avgLocalCall2Months","avgNationalCall2Months","avgInternationalCall2Months",
		"avgInternetCall2Months","avgComplaint2Months","avgInquiry2Months","avgTechnical2Months",
		"avgRequest2Months","avgMaintenance2Months","avgAdjustment2Months",
		"avgUpload6Months","avgDownload6Months","avgUpload2Months","avgDownload2Months","speed"
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
	
	public ItemVoiceBB(Customer customer,Service service,int lastMonth)
	{
		for (int i=0;i<fields.length;i++)
			fields[i]=0;
		id=customer.customerID;
		terminated=service.terminated;
		fields[0]=service.price;
		fields[1]=service.period;
		fields[40]=service.speed;
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
			fields[2]+=service.billingAccount.realPayments[i];
			fields[3]+=service.billingAccount.lastBills[i];
			fields[4]+=service.billingAccount.payments[i];
			fields[5]+=service.billingAccount.credit[i];
			fields[6]+=service.billingAccount.outstandings[i];
			fields[7]+=service.billingAccount.currentBills[i];
			fields[8]+=service.billingAccount.roundings[i];
			if (service.billingAccount.voiceUsages[i]!=null)
			{
				fields[9]+=service.billingAccount.voiceUsages[i].local;
				fields[10]+=service.billingAccount.voiceUsages[i].national;
				fields[11]+=service.billingAccount.voiceUsages[i].international;
				fields[12]+=service.billingAccount.voiceUsages[i].internet;
			}
			int temp=customer.complaints[i][0]==null?0:customer.complaints[i][0].count;
			fields[13]+=temp;
			temp=customer.complaints[i][1]==null?1:customer.complaints[i][1].count;
			fields[14]+=temp;
			temp=customer.complaints[i][2]==null?2:customer.complaints[i][2].count;
			fields[15]+=temp;
			temp=customer.complaints[i][3]==null?3:customer.complaints[i][3].count;
			fields[16]+=temp;
			temp=customer.complaints[i][4]==null?4:customer.complaints[i][4].count;
			fields[17]+=temp;
			temp=customer.complaints[i][5]==null?5:customer.complaints[i][5].count;
			fields[18]+=temp;
			fields[36]+=service.uploadUsages[i];
			fields[37]+=service.downloadUsages[i];
			if (terminatedMonth-i<3)
			{
				count2++;
				fields[19]+=service.billingAccount.realPayments[i];
				fields[20]+=service.billingAccount.lastBills[i];
				fields[21]+=service.billingAccount.payments[i];
				fields[22]+=service.billingAccount.credit[i];
				fields[23]+=service.billingAccount.outstandings[i];
				fields[24]+=service.billingAccount.currentBills[i];
				fields[25]+=service.billingAccount.roundings[i];
				if (service.billingAccount.voiceUsages[i]!=null)
				{
					fields[26]+=service.billingAccount.voiceUsages[i].local;
					fields[27]+=service.billingAccount.voiceUsages[i].national;
					fields[28]+=service.billingAccount.voiceUsages[i].international;
					fields[29]+=service.billingAccount.voiceUsages[i].internet;
				}
				temp=customer.complaints[i][0]==null?0:customer.complaints[i][0].count;
				fields[30]+=temp;
				temp=customer.complaints[i][1]==null?1:customer.complaints[i][1].count;
				fields[31]+=temp;
				temp=customer.complaints[i][2]==null?2:customer.complaints[i][2].count;
				fields[32]+=temp;
				temp=customer.complaints[i][3]==null?3:customer.complaints[i][3].count;
				fields[33]+=temp;
				temp=customer.complaints[i][4]==null?4:customer.complaints[i][4].count;
				fields[34]+=temp;
				temp=customer.complaints[i][5]==null?5:customer.complaints[i][5].count;
				fields[35]+=temp;
				fields[38]+=service.uploadUsages[i];
				fields[39]+=service.downloadUsages[i];
			}
		}
		for (int i=2;i<=18;i++)
			fields[i]/=count1;
		fields[36]/=count1;
		fields[37]/=count1;
		for (int i=19;i<=35;i++)
			fields[i]/=count2;
		fields[38]/=count1;
		fields[39]/=count1;
		for (int i=0;i<fields.length;i++)
		{
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
