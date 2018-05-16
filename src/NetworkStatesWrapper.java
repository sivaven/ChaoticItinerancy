import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class NetworkStatesWrapper {
	private static List<Integer> PertsToIgnore;
	double[][][] phdifs;
	
	public NetworkState[] states;
	public int[] validModes;
	
	public int nPairs; // n digits in each state
	public int duration; // duration using which the state is identified; affects the validModes
	
	/*public NetworkStatesWrapper(String dirOfCSVFiles, int npairs, int dur) {
		File f = new File(dirOfCSVFiles);
		File[] files = f.listFiles();
		
		states = new NetworkState[files.length];
		validModes = new int[files.length];
		nPairs = npairs;
		duration = dur;
		
		for(int i=0;i<files.length;i++) {
			String csvFile = files[i].getAbsolutePath();
			states[i]=new NetworkState(nPairs, csvFile, duration);
			validModes[i]= nPairs - states[i].numberOfUnSyncModes();
		}
	}*/
	public NetworkStatesWrapper(String dirOfCSVFiles, int npairs) {
		File f = new File(dirOfCSVFiles);
		if(!f.exists() || !f.isDirectory()) {
			System.out.println(f.getAbsolutePath() +"  doesnt exist OR not a directory");
		}
		File[] files = f.listFiles();
		
		nPairs = npairs;		
		phdifs = new double[files.length][][];
		
		for(int i=0;i<files.length;i++) {
			String csvFile = files[i].getAbsolutePath();
			phdifs[i] = NetworkState.populatePhDiffs(csvFile, npairs);			
		}
	}
	
	public NetworkState[] constructNetworkStates(int duration, boolean applyThresh) { 
		NetworkState[] states = new NetworkState[phdifs.length];	
		
		for(int i=0;i<phdifs.length;i++) {			
			
			double[][] phdiffsTrimmed = new double[nPairs][duration];		
			for(int j=0;j<nPairs;j++) {
				for(int k=0;k<duration;k++) {
					phdiffsTrimmed[j][k] = phdifs[i][j][k];
				}
			}
			
			states[i] = new NetworkState(nPairs,phdiffsTrimmed, applyThresh);
		}		
		return states;
	}
	
	public NetworkState[] constructNetworkStates(int startpt, int duration, boolean applyThresh) { 
		NetworkState[] states = new NetworkState[phdifs.length];	
		
		for(int i=0;i<phdifs.length;i++) {			
			
			double[][] phdiffsTrimmed = new double[nPairs][duration];		
			for(int j=0;j<nPairs;j++) {
				for(int k=startpt;k<startpt+duration;k++) {
					phdiffsTrimmed[j][k-startpt] = phdifs[i][j][k];
				}
			}
			
			states[i] = new NetworkState(nPairs,phdiffsTrimmed, applyThresh);
		}		
		return states;
	}
	public NetworkState constructNetworkState(int pertubid, int startpt, int duration, boolean applyThresh) { 
		NetworkState state = null;		
			
		double[][] phdiffsTrimmed = new double[nPairs][duration];		
		for(int j=0;j<nPairs;j++) {
			for(int k=startpt;k<startpt+duration;k++) {
				phdiffsTrimmed[j][k-startpt] = phdifs[pertubid][j][k];
			}
		}			
		state = new NetworkState(nPairs,phdiffsTrimmed, applyThresh);
			
		return state;
	}
	public int[] calculateSinglePhaseLockedModes(NetworkState[] states) {
		int[] nSinglePhaseLockedModes = new int[states.length];
		for(int i=0;i<states.length;i++) {
			nSinglePhaseLockedModes[i] = nPairs- states[i].numberOfUnSyncModes();
		}
		return nSinglePhaseLockedModes;
	}
	
	public void displayValidModes() {
		System.out.print(nPairs+"\t"+duration);
		for(int i=0;i<validModes.length;i++) {
			System.out.print("\t"+validModes[i]);
		}
	}
	
	public void displayFULLYSTABLEModes(NetworkStatesWrapper previousWrapper) {
		if(previousWrapper==null) {
			displayValidModes();
		}else {
			System.out.print(nPairs+"\t"+duration);
			
			for(int i=0;i<states.length;i++) {
				validModes[i]= nPairs - states[i].numberOfLostModes(previousWrapper.states[i]);
			}
			
			for(int i=0;i<validModes.length;i++) {
				System.out.print("\t"+validModes[i]);
			}
		}
		
	}
	
	public static void writeData(int duration, int[] Y, FileWriter fw) throws IOException {	
		double mean = 0;
			fw.write(""+duration);
			for(int i=0;i<Y.length;i++) {
				fw.write("\t"+Y[i]);
				mean += Y[i];
			}
			mean = mean/(Y.length*1.0d);
			fw.write("\t"+ mean);
			fw.write("\n");
			fw.flush();			
	}
	
	/*private static void forInfoDecay1() {
		int nPairs =99; // number of unique pairs = number of digits
		int[] durs = {1000, 1500, 2000, 2500, 
						3000, 3500, 4000, 4500, 5000, 
						6000, 7000, 8000, 9000, 10000,						
						12000, 14000, 16000, 18000, 20000};
		
		String csvfileDir = "C:\\Users\\sivav\\Google Drive\\NeuroProjects\\Periods\\E4\\External_Causal_v1";
		//"C:\\Users\\sivav\\Dropbox\\HCO\\Periods\\E4\\External_Causal_v0";
		
		NetworkStatesWrapper wrapper = null;
		NetworkStatesWrapper previous_wrapper = null;
		for(int i=0;i<durs.length;i++) {
			wrapper = new NetworkStatesWrapper(csvfileDir, nPairs, durs[i]);
		//	Entropy ent = new Entropy(wrapper.states);
			//ent.displayStats(false);
		//	System.out.println(durs[i]+"\t"+ent.entropy);
		
			wrapper.displayValidModes();
			//wrapper.displayFULLYSTABLEModes(previous_wrapper);
			System.out.println();
			previous_wrapper = wrapper;
		}		
	}*/
	
	/*
	 * accumulated durations
	 */
	public static Perturbation[] ConstructPerturbations(String ipFileDir, int nPairs, int endPt, int dur_increments, boolean applyThresh) {
				
		System.out.println("Reading data...");
		NetworkStatesWrapper wrapper = new NetworkStatesWrapper(ipFileDir, nPairs);	
		System.out.println("Reading complete...");
		
		Perturbation[] nperturbs = new Perturbation[wrapper.phdifs.length];
		for(int i=0;i<nperturbs.length;i++) {
			nperturbs[i] = new Perturbation();
		}		
			
		for(int dt=dur_increments;dt<=endPt;dt=dt+dur_increments) {
			System.out.println(dt + "completed: ");
			NetworkState[] _states = wrapper.constructNetworkStates(0, dt, applyThresh);
			
			for(int i=0;i<nperturbs.length;i++) {					
				nperturbs[i].addData(dt, _states[i]);
			}
			//_states= null;
		}				
		
		return nperturbs;
	}
	/*
	 * moving window 
	 */
	public static Perturbation[] ConstructPerturbations(String ipFileDir, int nPairs, int startPt, int endPt, int mwLength, boolean applyThresh) {				
		System.out.println("Reading data...");
		NetworkStatesWrapper wrapper = new NetworkStatesWrapper(ipFileDir, nPairs);	
		System.out.println("Reading complete...");
		
		Perturbation[] nperturbs = new Perturbation[wrapper.phdifs.length];
		for(int i=0;i<nperturbs.length;i++) {
			nperturbs[i] = new Perturbation();
		}		
			
		for(int dt=startPt;dt<=endPt;dt=dt+10) {
			System.out.println(dt + "completed: ");
			NetworkState[] _states = wrapper.constructNetworkStates(dt, mwLength, applyThresh);
			
			for(int i=0;i<nperturbs.length;i++) {					
				nperturbs[i].addData(dt+mwLength, _states[i]);
			}
			_states= null;
		}				
		
		return nperturbs;
	}
	/*
	 * threshold application dynamic! different perturbations should get threshold applied at different durations
	 */
	/*
	private static Perturbation[] forInfoDecayAccum2(String ipFileDir, int increments, int length, int[] durOfRep) {
		int nPairs =99; // number of unique pairs = number of digits
		
		System.out.println("Reading data...");
		NetworkStatesWrapper wrapper = new NetworkStatesWrapper(ipFileDir, nPairs);	
		System.out.println("Reading complete...");
		
		Perturbation[] nperturbs = new Perturbation[wrapper.phdifs.length];
		for(int i=0;i<nperturbs.length;i++) {
			nperturbs[i] = new Perturbation();
		}	
		
			for(int i=0;i<nperturbs.length;i++) {
				increments = 500;
				boolean applyThresh = true;
				int startpt = 0;	
				boolean addData = false;
				for(int dt=increments;dt<=length;) {									
					NetworkState state = wrapper.constructNetworkState(i, startpt, dt-startpt, applyThresh);// 0 to 500, 500 to 600, 
					if(addData)
						nperturbs[i].addData(dt,state);// 500, 
					if(dt>=durOfRep[i]) {
						increments = 100;
						applyThresh = false;
						startpt=durOfRep[i];
						addData = true;
					}
					dt=dt+increments;
				}
				
			}
			//_states= null;
						
		
		return nperturbs;
	}*/
	
	/*
	private static void singleDurStates() {
		int nPairs =99; // number of unique pairs = number of bits
		int dur = 2500;
		
		String csvfileDir = "C:\\Users\\sivav\\Google Drive\\NeuroProjects\\Periods\\E4\\External_Causal_v1";
		//"C:\\Users\\sivav\\Dropbox\\HCO\\Periods\\E4\\External_Causal_v0";
		
		NetworkStatesWrapper wrapper = new NetworkStatesWrapper(csvfileDir, nPairs, dur);
		//wrapper.displayValidModes();
		//System.out.println();
		for(int i=0;i<wrapper.states.length;i++) {
			System.out.print(wrapper.validModes[i]+"\t");
			wrapper.states[i].displayNetworkState(false, false);
			
			System.out.println();
		}
	}
	*/
	public static void writeRepStates(FileWriter fw_rep,  FileWriter fw_rep_dur, Perturbation[] perturbs) {
		try {
			for(int i=0;i<perturbs.length;i++) {
				int dur_of_max_sync = perturbs[i].durationOfMaxSyncModes();
				NetworkState repState = perturbs[i].getNetworkState(dur_of_max_sync);
				fw_rep.write(repState.getCsvString()+"\n");
				fw_rep_dur.write(""+dur_of_max_sync+"\n");
			}
			fw_rep.close();	
			fw_rep_dur.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static NetworkState[] readRepStates(String fileName, int nperturbs, int nPairs) {
		NetworkState[] repStates = new NetworkState[nperturbs];//100 perturbations
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			for(int i=0;i<repStates.length;i++) {			
				String str = br.readLine();			
				StringTokenizer st = new StringTokenizer(str, ",");
				
				OnePairPhaseTransitions[] pairs = new OnePairPhaseTransitions[nPairs];				
				for(int j=0;j<nPairs;j++) { // if no token exception, then written and read nPairs don't match!!!
					PhaseLockMode mode = PhaseLockMode.instantiatePhaseLockMode(Integer.parseInt(st.nextToken()));	
					pairs[j] = new OnePairPhaseTransitions(mode);
				}				
				repStates[i]=new NetworkState(pairs);
				
			}	
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return repStates;
	}
	
	public static int[] readRepStatesDur(String fileName) {
		int[] repStatesDur = new int[100];//100 perturbations
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			for(int i=0;i<repStatesDur.length;i++) {			
				String str = br.readLine();						
				repStatesDur[i]=Integer.parseInt(str);				
			}	
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return repStatesDur;
	}
	private static void writeForLEVEL1_Figure(FileWriter fw, Perturbation[] perturbs) throws IOException { //number of single phase locked mode
		boolean headernotwritten=true;
		for(int i=0;i<perturbs.length;i++) {
			System.out.println("perturbation.."+(i+1));
			//int dur_of_max_sync = perturbs[i].durationOfMaxSyncModes();
			//NetworkState repState = perturbs[i].getNetworkState(dur_of_max_sync);
			
			//if(repState.pairs.length  - repState.numberOfUnSyncModes() <80) continue;
			
			Set<Integer> durations = perturbs[i].getAllDurations();
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
				int splm = perturbs[i].calculateSinglePhaseLockedMode(dur);
				if(!first) fw.write("\t");
				fw.write(""+splm);
				first = false;
			}
			fw.write("\n");
			fw.flush();
		}
		fw.close();
		
		
	}
	
	private static void writeForLEVEL2_Figure(FileWriter fw, Perturbation[] perturbs, NetworkState[] repStates) throws IOException { //number of single phase locked mode
		boolean headernotwritten=true;
		for(int i=0;i<perturbs.length;i++) {
			System.out.println("perturbation.."+(i+1));
			//int dur_of_max_sync = perturbs[i].durationOfMaxSyncModes();
			//NetworkState repState = perturbs[i].getNetworkState(dur_of_max_sync);
			
			//if(repState.pairs.length  - repState.numberOfUnSyncModes() <80) continue;
			
			Set<Integer> durations = perturbs[i].getAllDurations();			
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
				int decay = perturbs[i].numberOfMatchingModes(dur, repStates[i]);
				if(!first) fw.write("\t");
				fw.write(""+decay);
				first = false;
			}
			fw.write("\n");
			fw.flush();
		}
		fw.close();
	}
	
	private static void writeForLEVEL2_Figure_Dynamic(FileWriter fw1, FileWriter fw2, Perturbation[] perturbs, NetworkState[] repStates) throws IOException { //number of single phase locked mode
		for(int i=0;i<perturbs.length;i++) {
			System.out.println("perturbation.."+(i+1));
			
			Set<Integer> durations = perturbs[i].getAllDurations();			
			
				boolean first = true;
				fw1.write(""+durations.size());
				for(int dur:durations) {
					fw1.write("\t");
					fw1.write(""+dur);
					
					int decay = perturbs[i].numberOfMatchingModes(dur, repStates[i]);
					if(!first) fw2.write("\t");
					fw2.write(""+decay);					
					first = false;
				}
				fw1.write("\n");
				fw2.write("\n");		
		}
		fw1.close();
		fw2.close();
	}
	/*
	 * moving window : total number of sync modes
	 */
	private static void writeFor_MW1(FileWriter fw, Perturbation[] perturbs) throws IOException { //number of single phase locked mode
		boolean headernotwritten=true;
		for(int i=0;i<perturbs.length;i++) {
			if(PertsToIgnore.contains(i)) continue;
			System.out.println("perturbation.."+(i+1));
			//int dur_of_max_sync = perturbs[i].durationOfMaxSyncModes();
			//NetworkState repState = perturbs[i].getNetworkState(dur_of_max_sync);
			
			//if(repState.pairs.length  - repState.numberOfUnSyncModes() <80) continue;
			
			Set<Integer> durations = perturbs[i].getAllDurations();
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
				int splm = perturbs[i].calculateSinglePhaseLockedMode(dur);
				if(!first) fw.write("\t");
				fw.write(""+splm);
				first = false;
			}
			fw.write("\n");
			fw.flush();
		}
		fw.close();
		
		
	}
	
	/*
	 * moving window : total number of sync modes that match PREVIOUS window
	 */
	private static void writeFor_MW2(FileWriter fw, Perturbation[] perturbs) throws IOException { //number of single phase locked mode
		boolean headernotwritten=true;
		for(int i=0;i<perturbs.length;i++) {
			if(PertsToIgnore.contains(i)) continue;
			System.out.println("perturbation.."+(i+1));
			//int dur_of_max_sync = perturbs[i].durationOfMaxSyncModes();
			//NetworkState repState = perturbs[i].getNetworkState(dur_of_max_sync);
			
			//if(repState.pairs.length  - repState.numberOfUnSyncModes() <80) continue;
			
			Set<Integer> durations = perturbs[i].getAllDurations();
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
					countofmatch = perturbs[i].getNetworkState(dur).numberOfMatches(previousState);
				}
				if(!first) fw.write("\t");
				fw.write(""+countofmatch);
				first = false;
				
				previousState = perturbs[i].getNetworkState(dur);
			}
			fw.write("\n");
			fw.flush();
		}
		fw.close();		
	}
	
	/*
	 * moving window : total number of sync modes that match first window
	 */
	private static void writeFor_MW3(FileWriter fw, Perturbation[] perturbs) throws IOException { //number of single phase locked mode
		
		boolean headernotwritten=true;
		for(int i=0;i<perturbs.length;i++) {
			if(PertsToIgnore.contains(i)) continue;
			System.out.println("perturbation.."+(i+1));
			//int dur_of_max_sync = perturbs[i].durationOfMaxSyncModes();
			//NetworkState repState = perturbs[i].getNetworkState(dur_of_max_sync);
			
			//if(repState.pairs.length  - repState.numberOfUnSyncModes() <80) continue;
			
			Set<Integer> durations = perturbs[i].getAllDurations();
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
			NetworkState state=null;
			for(int dur:durations) {
				int count = 0;
				if(first) {
					state = perturbs[i].getNetworkState(dur);
				}
				count = perturbs[i].getNetworkState(dur).numberOfMatches(state);
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
	 * moving window : total number of sync modes that match rep state
	 */
	private static void writeFor_MW4(FileWriter fw, Perturbation[] perturbs) throws IOException { //number of single phase locked mode
		
		boolean headernotwritten=true;
		for(int i=0;i<perturbs.length;i++) {
			if(PertsToIgnore.contains(i)) continue;
			System.out.println("perturbation.."+(i+1));
			int dur_of_max_sync = perturbs[i].durationOfMaxSyncModes();
			NetworkState repState = perturbs[i].getNetworkState(dur_of_max_sync);
			
			//if(repState.pairs.length  - repState.numberOfUnSyncModes() <80) continue;
			
			Set<Integer> durations = perturbs[i].getAllDurations();
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
				count = perturbs[i].getNetworkState(dur).numberOfMatches(repState);
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
		int[] ignoreidcs = {};/* {79,93,94,				26,				65,				0,				3,				4,				7,
				9,				13,				36,				41,				46,				64,				68,				72,
				75,				83,				84};*/
		PertsToIgnore=GeneralUtils.arrayToListInteger(ignoreidcs);
		
	
		int mwlength = 100;//Integer.parseInt(args[0]);
		
		//String csvfileDir = "/home/siyappan/NeuroProjects/Periods/E4/External_Causal_Exp1";
		String csvfileDir = "C:\\Users\\sivav\\Google Drive\\NeuroProjects\\Periods\\E4\\External_Causal_Exp1_rep_2.0_test";
	/*	String opFileL1 = csvfileDir+"_nsm_"+mwlength;
		String opFileL2 = csvfileDir+"_nsm_matchlast_"+mwlength;
		String opFileL3 = csvfileDir+"_nsm_matchfirst_"+mwlength;
		String opFileL4 = csvfileDir+"_nsm_matchrep_"+mwlength;
	*/	
		
		
		//int duration = 500;
		//int length = 15000;
		NetworkState.RATE_THRESH = 2;
	//	boolean applyThresh = true;
	//	String opFile_accum_1 = csvfileDir+"_nsm_acc_l1_"+NetworkState.RATE_THRESH;
		//String opFile_rep = csvfileDir+"_rep_"+NetworkState.RATE_THRESH;
		String opFile_rep_dur = csvfileDir+"_rep_dur_"+NetworkState.RATE_THRESH;
	//	String opFile_accum_2 = csvfileDir+"_nsm_acc_l2_"+NetworkState.RATE_THRESH;
		String opFile_mw = csvfileDir+"_nsm_mw_l2";
		String opFile_accum_dynamic = csvfileDir+"_nsm_acc_l2_dynamic";
		try {		
			//moving window - all
		/*	Perturbation[] perturbs = forInfoDecay2(csvfileDir, mwlength);
			writeFor_MW1(new FileWriter(opFileL1), perturbs);
			writeFor_MW2(new FileWriter(opFileL2), perturbs);
			writeFor_MW3(new FileWriter(opFileL3), perturbs);
			writeFor_MW4(new FileWriter(opFileL4), perturbs);
		*/	
			//accum time - all
		//	Perturbation[] perturbs = forInfoDecayAccum(csvfileDir, duration, length, true);
		//	writeRepStates(new FileWriter(opFile_rep), new FileWriter(opFile_rep_dur), perturbs);
			
			//writeForLEVEL1_Figure(new FileWriter(opFile_accum_1), perturbs);			
			//NetworkState[] repStates = readRepStates(opFile_rep, 99);
			//writeForLEVEL2_Figure(new FileWriter(opFile_accum_2), perturbs, repStates);
			
			//hybrid
		//	Perturbation[] perturbs = forInfoDecay2(csvfileDir, mwlength, applyThresh);
		//	NetworkState[] repStates = readRepStates(opFile_rep, 99);
		//	writeForLEVEL2_Figure(new FileWriter(opFile_mw), perturbs, repStates);
			
			//accum time - dynamic
			NetworkState[] repStates = readRepStates(csvfileDir, 82, 99);
			Entropy ent = new Entropy(repStates);
			ent.displayStats(false);
			//int[] repdurs = readRepStatesDur(opFile_rep_dur);
			//Perturbation[] perturbs = forInfoDecayAccum2(csvfileDir, 500, 8000, repdurs);
			//writeForLEVEL2_Figure_Dynamic(new FileWriter(opFile_accum_dynamic+"_x"), new FileWriter(opFile_accum_dynamic+"_y"), perturbs, repStates);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
