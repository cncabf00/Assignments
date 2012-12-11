package customer;

public class BillingAccount {
	String billingID;
	double[] realPayments=new double[12];
	double[] lastBills=new double[12];
	double[] payments=new double[12];
	double[] credit=new double[12];
	double[] outstandings=new double[12];
	double[] currentBills=new double[12];
	double[] roundings=new double[12];
	VoiceUsage[] voiceUsages=new VoiceUsage[12];
}
