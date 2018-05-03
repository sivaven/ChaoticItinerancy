import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

public class NetworkState {
	private static final double THRESHOLD_MIN=0.25;
	private static final double THRESHOLD_MAX=0.5;
	
	OnePairPhaseTransitions[] pairs;
	List<Integer> duplicates; // to hold a list of indices of duplicates of THIS state from wrapper classes
	
	public NetworkState(int nPairs, String csvfile, int dur) {
		this.pairs=new OnePairPhaseTransitions[nPairs];
		double[][] phdiffs = populatePhDiffs(csvfile, nPairs, dur);
		
		for(int i=0;i<nPairs;i++) {
			this.pairs[i]= new OnePairPhaseTransitions(phdiffs[i], THRESHOLD_MIN, THRESHOLD_MAX);
		}
	}
	
	private double[][] populatePhDiffs(String fileName, int nPairs, int dur) {
		double[][] phaseDiff = new double[nPairs][dur];
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = null;
			
			for(int i=0;i<nPairs;i++) {
				str = br.readLine();				
				StringTokenizer st = new StringTokenizer(str, ",");
				String token = null;
				
				for(int j=0;j<dur;j++) {
					token = st.nextToken();
					double radians = Double.parseDouble(token);
					if(radians<0)
						radians=2*Math.PI - Math.abs(radians);				
					phaseDiff[i][j]=radians;					
				}				
			}			
			br.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return phaseDiff;
	}

	public void displayNetworkState(boolean label, boolean stateTransitions) {
		if(label) {
			for(int i=0;i<this.pairs.length;i++) {
				System.out.print(pairs[i].mode.toString()+"\t");
			}
			System.out.print(";");
		}
		
		for(int i=0;i<this.pairs.length;i++) {
			System.out.print(pairs[i].mode.getValue()+"\t");
		}
		
		if(stateTransitions) {
			for(int i=0;i<this.pairs.length;i++) {
				System.out.println();
				GeneralUtils.displayArray(new long[] {0,1,2,3,4,5});
				GeneralUtils.displayArray(pairs[i].stateCount);
				GeneralUtils.displayArray(pairs[i].stateCountNormed);
			}
		}
	}
	
	public int numberOfUnSyncModes() {
		int n=0;
		for(int i=0;i<pairs.length;i++) {
			if(pairs[i].mode.equals(PhaseLockMode.UNSYNC))
				n++;
		}
		return n;
	}
	
	
	public boolean matches(NetworkState state) {
		boolean match=true;
		for(int i=0;i<this.pairs.length;i++) {
			if(this.pairs[i].mode.equals(PhaseLockMode.UNSYNC) || state.pairs[i].mode.equals(PhaseLockMode.UNSYNC)) {
				continue;
			}
			if(!this.pairs[i].mode.equals(state.pairs[i].mode)) {
				match = false;
				break;
			}
		}
		return match;
	}
	
	public static void main(String[] args) {
		int nPairs =99; // number of unique pairs = number of digits
		int dur = 9000;
		String csvfile = "C:\\Users\\sivav\\Dropbox\\HCO\\Periods\\E4\\External_Causal_v0\\spk_PSTUT_4.csv";
		
		NetworkState ns = new NetworkState(nPairs, csvfile, dur);
		ns.displayNetworkState(false, false);
		System.out.println("\n"+ns.numberOfUnSyncModes());
		//100 - 32 (9's)
		//150 - 22
		//200 - 34
		//300 - 38
		//500 - 52
		//1000 - 28
		//2000 - 15
		//2500 - 14
		//2550 - 14
		//2600 - 15
		//2625 - 15
		//2750 - 17
		//3000 - 18
		//4000 - 22
		//5000 - 28
		//9000 - 45
		//System.out.println();
		
		//GeneralUtils.displayArray(pt.transitionRateMatrix);
		//GeneralUtils.display2DArrayVertical(new double[][] {pt.binMeans, pt.binNs});

	}

}
