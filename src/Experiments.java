import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class Experiments {
	private static final String DIR = "/home/siyappan/NeuroProjects/Periods/E4/";
			//"C:\\Users\\sivav\\Google Drive\\NeuroProjects\\Periods\\E4\\";
		
	private static final String SIM_DAT_FILES_DIR = DIR+"External_Causal_Exp1/";
	private static final String REP_STATE_FILE =  DIR+"plot_0/rep_states";
	
	private static final int N_PERTURBS=100;
	private static final int N_PAIRS = 99;
	
	Perturbation[] perturbations;
	int mwLength; // for moving window pipeline
	int increments; // for accumulated duration pipeline
	/*
	 *  construct moving window using wrapper 
	 *  PL II
	 */
	public Experiments(int mw_startPt, int mw_endPt, int mw_length, boolean applyThresh){
		mwLength = mw_length;
		System.out.println("Constructing Perturbation objects...");
		perturbations = NetworkStatesWrapper.ConstructPerturbations(SIM_DAT_FILES_DIR, N_PAIRS, mw_startPt, mw_endPt, mwLength, applyThresh);
	}
	
	/*
	 *  construct Accu dur- perturbs using wrapper 
	 *  PL I
	 */
	public Experiments(int endPt, int incrmnts, boolean applyThresh){
		increments = incrmnts;
		System.out.println("Constructing Perturbation objects...");
		perturbations = NetworkStatesWrapper.ConstructPerturbations(SIM_DAT_FILES_DIR, N_PAIRS,  endPt, increments, applyThresh);
	}
	
	/*
	 * save representational states and plot-0: number of SYNC modes vs Duration
	 * PL I
	 */
	public void plot_0() throws IOException {
		NetworkStatesWrapper.writeRepStates(new FileWriter(REP_STATE_FILE), new FileWriter(REP_STATE_FILE+"_durs"), perturbations);
		
		String opFile=DIR+"plot_0/acc_0";
		FileWriter fw = new FileWriter(opFile);
		
		boolean headernotwritten=true;
		for(int i=0;i<perturbations.length;i++) {			
			System.out.println("perturbation.."+(i+1));
			
			Set<Integer> durations = perturbations[i].getAllDurations();
			
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
				count = perturbations[i].calculateSinglePhaseLockedMode(dur);
				if(!first) fw.write("\t");
				fw.write(""+count);
				first = false;
			}
			fw.write("\n");
			fw.flush();
		}
		fw.close();	
	}
	

	/*
	 * plot - 1: number of rep state modes vs Duration
	 * PL I
	 */
	public void plot_0(NetworkState[] repStates) throws IOException {
		String opFile=DIR+"plot_0/acc_1";
		FileWriter fw = new FileWriter(opFile);
		
		boolean headernotwritten=true;
		for(int i=0;i<perturbations.length;i++) {			
			System.out.println("perturbation.."+(i+1));
			
			Set<Integer> durations = perturbations[i].getAllDurations();
			
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
				count = perturbations[i].numberOfMatchingModes(dur, repStates[i]);
				if(!first) fw.write("\t");
				fw.write(""+count);
				first = false;
			}
			fw.write("\n");
			fw.flush();
		}
		fw.close();	
	}
	
	/*
	 * Compare moving window with rep. states
	 * PL II
	 */
	public void plot_a(NetworkState[] repStates) throws IOException {
		String opFile=DIR+"plot_a/nothresh_mw_"+mwLength;
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
	
	/*
	 * Compare moving window with previous window
	 */
	public void plot_b() throws IOException {
		String opFile=DIR+"plot_b/nothresh_mw_"+mwLength;
		FileWriter fw = new FileWriter(opFile);
		
		boolean headernotwritten=true;
		for(int i=0;i<perturbations.length;i++) {			
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
			
			NetworkState previousState = null;
			for(int dur:durations) {					
				int countofmatch = 0;
				if(!first) {
					countofmatch = perturbations[i].getNetworkState(dur).numberOfMatches(previousState);
				}
				if(!first) fw.write("\t");
				fw.write(""+countofmatch);
				first = false;
				
				previousState = perturbations[i].getNetworkState(dur);
			}
			
			fw.write("\n");
			fw.flush();
		}
		fw.close();	
	}
	
	/*
	 * plot_0 version of moving window: simply the no. of SYNC modes vs TIME
	 */
	public void plot_c() throws IOException {
		String opFile=DIR+"plot_c/nothresh_mw_"+mwLength;
		FileWriter fw = new FileWriter(opFile);
		
		boolean headernotwritten=true;
		for(int i=0;i<perturbations.length;i++) {			
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
				if(!first) {
					count = perturbations[i].calculateSinglePhaseLockedMode(dur);
				}
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
		boolean isMovingWindow = true;
		int mwlength = 100;
		
		int startpt = 0;
		int endpt = 15000;		
		int increments = 500;
		
		boolean applyThresh = false;		
		
		Experiments exps = null;
		
		if(isMovingWindow)
			exps = new Experiments(startpt, endpt, mwlength, applyThresh);
		else
			exps = new Experiments(endpt, increments, applyThresh);		
		
		try {
			//exps.plot_0();			//baseline - save rep and plot: number of SYNC MODES vs. Duration
			NetworkState[] repStates = NetworkStatesWrapper.readRepStates(REP_STATE_FILE, N_PERTURBS, N_PAIRS);
			//exps.plot_0(repStates);			//plot number of matching (to rep state) sync modes vs Duration
			
			/*
			 * moving window experiments below
			 */
			exps.plot_a(repStates);  // plot number of Matching (to rep state) sync modes vs. moving window
			exps.plot_b();  // plot number of Matching (to last window) sync modes vs. moving window
			exps.plot_c();  // baseline version (plot_0) of moving window. plot: number of SYNC modes vs TIME window
		}catch(Exception io) {
			io.printStackTrace();
		}
		
	}

}
