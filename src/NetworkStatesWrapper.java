import java.io.File;

public class NetworkStatesWrapper {
	
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
	
	public void displayValidModes() {
		System.out.print(nPairs+"\t"+duration);
		for(int i=0;i<validModes.length;i++) {
			System.out.print("\t"+validModes[i]);
		}
	}
	
	public static void main(String[] args) {
		int nPairs =99; // number of unique pairs = number of digits
		int[] durs = {500, 1000, 1500, 2000, 2500, 
						3000, 3500, 4000, 4500, 5000, 
						6000, 7000, 8000, 9000, 10000,						
						12000, 14000, 16000, 18000, 20000};
		
		String csvfileDir = "C:\\Users\\sivav\\Google Drive\\NeuroProjects\\Periods\\E4\\External_Causal_v1";
		//"C:\\Users\\sivav\\Dropbox\\HCO\\Periods\\E4\\External_Causal_v0";
		
		for(int i=0;i<durs.length;i++) {
			NetworkStatesWrapper wrapper = new NetworkStatesWrapper(csvfileDir, nPairs, durs[i]);
			wrapper.displayValidModes();
			System.out.println();
		}
		

	}

}
