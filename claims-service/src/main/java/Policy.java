
public class Policy {
	int policyId;
	String policyname;
	int premium;
	int coverage;
	public int getPolicyId() {
		return policyId;
	}
	public void setPolicyId(int policyId) {
		this.policyId = policyId;
	}
	public String getPolicyname() {
		return policyname;
	}
	public void setPolicyname(String policyname) {
		this.policyname = policyname;
	}
	public int getPremium() {
		return premium;
	}
	public void setPremium(int premium) {
		this.premium = premium;
	}
	public int getCoverage() {
		return coverage;
	}
	public void setCoverage(int coverage) {
		this.coverage = coverage;
	}
	public Policy(int policyId, String policyname, int premium, int coverage) {
		this.policyId = policyId;
		this.policyname = policyname;
		this.premium = premium;
		this.coverage = coverage;
	}
	
	public void claim(int claimAmount) {
		if(claimAmount>coverage) {
			System.out.println("claim Rejected");
		}else {
			System.out.println("Claim Approved");
		}
	}
	
	public static void main(String[] args) {
		
		Policy p1=new Policy(1,"healthCare",25000,5000000);
		
		
		p1.claim(500000);
	}
	
}


