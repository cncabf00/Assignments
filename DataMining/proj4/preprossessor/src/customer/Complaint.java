package customer;

enum ComplaintType
{
	Complaint,
	Inquiry,
	Technical,
	Request,
	Maintenance,
	Adjustment,
}

public class Complaint {
	ComplaintType callType;
	int month;
	int count;
}
