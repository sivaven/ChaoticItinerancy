
public enum PhaseLockMode {
	IN_phase(0), OUT_1_phase(1), OUT_2_phase(2), UNSYNC(9);
	
	private int value;
	PhaseLockMode(int val){
		value = val;
	}
	public int getValue(){
		return value;
	}
	
	public static PhaseLockMode getPhaseLockMode(int phasePartition) {
		if(phasePartition==0) {
			return PhaseLockMode.IN_phase;
		}
		if(phasePartition==2) {
			return PhaseLockMode.OUT_1_phase;
		}
		if(phasePartition==4) {
			return PhaseLockMode.OUT_2_phase;
		}
		return UNSYNC;
	}
}
