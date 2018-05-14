import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class Experiments {
	private static final String DIR = "/home/siyappan/NeuroProjects/Periods/E4/";
			//"C:\\Users\\sivav\\Google Drive\\NeuroProjects\\Periods\\E4\\";
		
	private static final String SIM_DAT_FILES_DIR = DIR+"External_Causal_Exp1/";
	private static final String REP_STATE_FILE =  DIR+"External_Causal_Exp1_rep_2.0";
	
	private static final int N_PERTURBS=100;
	private static final int N_PAIRS = 99;
	
	Perturbation[] perturbations;
	int mwLength;
	
	/*
	 *  construct moving window using wrapper
	 */
	public Experiments(int mw_startPt, int mw_endPt, int mw_length, boolean applyThresh){
		int mwLength = mw_length;
		System.out.println("Constructing Perturbation objects...");
		perturbations = NetworkStatesWrapper.ConstructPerturbations(SIM_DAT_FILES_DIR, N_PAIRS, mw_startPt, mw_endPt, mwLength, applyThresh);
	}
	
	/*
	 * Compare it with rep states
	 */
	public void plot_a(NetworkState[] repStates) throws IOException {
		String opFile=DIR+"plot_a/mw_"+mwLength;
		FileWriter fw = new FileWriter(opFile);
		
		boolean headernotwritten=true;
		for(int i=0;i<repStates.length;i++) {			
			System.out.println("perturbation.."+(i+1));
			
			Set<Integer> durations = perturbations[i].getAllDurations();
			//Collections.sort(durations);
			
			if(headernotwritten) { //header
				boolean first = true;
				for(int dur:durations) {
					if(!first) fw.write("\t");
					fw.write(""+dur);
					first = false;
				}
				fw.write("\n");
				headernotwritten=false;
			}
			boolean first = true;
			
			for(int dur:durations) {
				int count = 0;				
				count = perturbations[i].getNetworkState(dur).numberOfMatches(repStates[i]);
				if(!first) fw.write("\t");
				fw.write(""+count);
				first = false;
			}
			fw.write("\n");
			fw.flush();
		}
		fw.close();	
	}
	
	public static void main(String[] args) {
		int startpt = 0;
		int endpt = 15000;
		int mwlength = 100;
		boolean applyThresh = false;	
		
		NetworkState[] repStates = NetworkStatesWrapper.readRepStates(REP_STATE_FILE, N_PERTURBS, N_PAIRS);
		
		Experiments exps = new Experiments(startpt, endpt, mwlength, applyThresh);
		try {
			exps.plot_a(repStates);
		}catch(Exception io) {
			io.printStackTrace();
		}
		
	}

}
