import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class PhaseTransitions {
	private static final int N_DIVISIONS=3; //6 // no. of states	
	private static final double[][] STATE_BNDRY = {{5.5, 0.5, 0}, {1.5, 2.5, 1}, {3.5, 4.5, 2} };
									//{{5.5, 0.5, 0}, {0.5, 1.5, 1}, {1.5, 2.5, 2}, {2.5, 3.5, 3}, {3.5, 4.5, 4}, {4.5, 5.5, 5}};
	private static final int TIMEpt_BEGIN=10000;
	private static final int TIMEpt_END=20000;
	
	ArrayList<Double> phaseDiff;
	//long[] bins;
	double[] binMeans;
	double[] binNs;
	
	long[][] transitionMatrix;
	
	public PhaseTransitions(String phaseDiffFile) {
		phaseDiff = new ArrayList<>();
		populatePhDiffs(phaseDiffFile);
		if(phaseDiff.size() < TIMEpt_END) {
			System.out.println("PhaseTransitions(String): Length of custom timepoints cannot exceed phaseDiff array!");
			System.exit(0);
		}
		//populateBins(nBins);
		populateTransitionMatrix();
	}
	
	private void populatePhDiffs(String fileName) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = null;
			
			str = br.readLine();
			
			StringTokenizer st = new StringTokenizer(str, ",");
			String token = null;
			while(st.hasMoreTokens()) {
				token = st.nextToken();
				double radians = Double.parseDouble(token);
				if(radians<0)
					radians=2*Math.PI - Math.abs(radians);
				phaseDiff.add(radians);
				
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void populateBins(int n) {		
		
		EmpiricalDistribution dist = new EmpiricalDistribution(n);
		dist.load(GeneralUtils.listToArrayDouble(phaseDiff));
		List<SummaryStatistics> binSummary = dist.getBinStats();
		binNs=new double[binSummary.size()];
		binMeans=new double[binSummary.size()];
		for(int i=0;i<binSummary.size();i++) {
			binNs[i]=binSummary.get(i).getN();
			binMeans[i]=binSummary.get(i).getMean();			
		}
	}
	
	private int indexOf(double phDiff) {
		int idx=-1;
		for(int i=0;i<STATE_BNDRY.length;i++) {
			if(i==0) {
				if(phDiff>STATE_BNDRY[i][0] || phDiff<=STATE_BNDRY[i][1]) {
					idx=(int)STATE_BNDRY[i][2];
					break;
				}
			}else {
				if(phDiff>STATE_BNDRY[i][0] && phDiff<=STATE_BNDRY[i][1]) {
					idx=(int)STATE_BNDRY[i][2];
					break;
				}
			}			
		}
		return idx;
	}
	
	private void populateTransitionMatrix() {
		transitionMatrix = new long[N_DIVISIONS][N_DIVISIONS];
		
		int previous_state=-1;
		int current_state=-1;		
		
		int startIdx=TIMEpt_BEGIN;
		for(int i=TIMEpt_BEGIN;i<TIMEpt_END;i++) {
			previous_state = indexOf(phaseDiff.get(i));
			startIdx++;
			if(previous_state!=-1)
				break;
		}
		
		for(int i=startIdx;i<TIMEpt_END;i++) {
			current_state=indexOf(phaseDiff.get(i));
			if(current_state==-1) {
				continue;
			}
			transitionMatrix[previous_state][current_state] = transitionMatrix[previous_state][current_state] +1;
			previous_state = current_state;
		}
	}
	
	public static void main(String[] args) {
		//int nBins=72;
		String fileName = "C:\\Users\\sivav\\Dropbox\\HCO\\Periods\\E4\\ph_diff_2_v2.dat";
		PhaseTransitions pt = new PhaseTransitions(fileName);
		//System.out.println(pt.phaseDiff);
		//GeneralUtils.displayListVertical(pt.phaseDiff);
		GeneralUtils.displayArray(pt.transitionMatrix);
		
		
		
		
		//GeneralUtils.display2DArrayVertical(new double[][] {pt.binMeans, pt.binNs});

	}

}
