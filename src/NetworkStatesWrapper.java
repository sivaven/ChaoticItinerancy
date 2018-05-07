import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class NetworkStatesWrapper {
	
	double[][][] phdifs;
	
	public NetworkState[] states;
	public int[] validModes;
	
	public int nPairs; // n digits in each state
	public int duration; // duration using which the state is identified; affects the validModes
	
	public NetworkStatesWrapper(String dirOfCSVFiles, int npairs, int dur) {
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
	}
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
	
	public NetworkState[] constructNetworkStates(int duration) { 
		NetworkState[] states = new NetworkState[phdifs.length];	
		
		for(int i=0;i<phdifs.length;i++) {			
			
			double[][] phdiffsTrimmed = new double[nPairs][duration];		
			for(int j=0;j<nPairs;j++) {
				for(int k=0;k<duration;k++) {
					phdiffsTrimmed[j][k] = phdifs[i][j][k];
				}
			}
			
			states[i] = new NetworkState(nPairs,phdiffsTrimmed);
		}		
		return states;
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
	
	private static void forInfoDecay1() {
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
	}
	private static Perturbation[] forInfoDecay2(String ipFileDir, int dt_start , int dt_plus) {
		int nPairs =99; // number of unique pairs = number of digits
		
		System.out.println("Reading data...");
		NetworkStatesWrapper wrapper = new NetworkStatesWrapper(ipFileDir, nPairs);	
		System.out.println("Reading complete...");
		
		Perturbation[] nperturbs = new Perturbation[wrapper.phdifs.length];
		for(int i=0;i<nperturbs.length;i++) {
			nperturbs[i] = new Perturbation();
		}		
			
		for(int dt=dt_start+500;dt<=dt_start+20000;dt=dt+dt_plus) {
			System.out.println(dt + "completed: ");
			NetworkState[] _states = wrapper.constructNetworkStates(dt);
			
			for(int i=0;i<nperturbs.length;i++) {					
				nperturbs[i].addData(dt, _states[i]);
			}
			_states= null;
		}				
		
		return nperturbs;
	}
	
	private static void singleDurStates() {
		int nPairs =99; // number of unique pairs = number of bits
		int dur = 2500;
		
		String csvfileDir = "C:\\Users\\sivav\\Google Drive\\NeuroProjects\\Periods\\E4\\External_Causal_v1";
		//"C:\\Users\\sivav\\Dropbox\\HCO\\Periods\\E4\\External_Causal_v0";
		
		NetworkStatesWrapper wrapper = new NetworkStatesWrapper(csvfileDir, nPairs, dur);
		//wrapper.displayValidModes();
		//System.out.println();
		/*for(int i=0;i<wrapper.states.length;i++) {
			System.out.print(wrapper.validModes[i]+"\t");
			wrapper.states[i].displayNetworkState(false, false);
			
			System.out.println();
		}*/
	}
	public static void main(String[] args) {		
		int start_dur=Integer.parseInt(args[0]);
		int dur_plus = Integer.parseInt(args[1]);
		String csvfileDir = "/home/siyappan/NeuroProjects/Periods/E4/External_Causal_v1";
		String opFile = csvfileDir+"_splm_"+start_dur;
		
		try {
			FileWriter fw = new FileWriter(opFile);		
		
			Perturbation[] perturbs = forInfoDecay2(csvfileDir, start_dur, dur_plus);
			for(int i=0;i<perturbs.length;i++) {
				System.out.println("perturbation.."+(i+1));
				Set<Integer> durations = perturbs[i].getAllDurations();
				//Collections.sort(durations);
				
				if(i==0) { //header
					boolean first = true;
					for(int dur:durations) {
						if(!first) fw.write("\t");
						fw.write(""+dur);
						first = false;
					}
					fw.write("\n");
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
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
}
