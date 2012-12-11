package customer;

enum Type
{
	VoiceOnly,
	VoiceBB
};

public class Service {
	Type type;
	BillingAccount billingAccount;
	String serviceID;
	String location;
	String hsbbArea;
	double speed; // kbps
	boolean terminated;
	int terminatedYear;
	int terminatedMonth;
	double price;
	int period;
	double[] downloadUsages=new double[12];
	double[] uploadUsages=new double[12];
}
