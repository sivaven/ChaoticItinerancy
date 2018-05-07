import java.util.List;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class OnePairPhaseTransitions {
	private static final int N_DIVISIONS=6; //6 // no. of states	
	private static final double[][] STATE_BNDRY = //{{5.5, 0.5, 0}, {1.5, 2.5, 1}, {3.5, 4.5, 2} };
									{{5.5, 0.5, 0}, {0.5, 1.5, 1}, {1.5, 2.5, 2}, {2.5, 3.5, 3}, {3.5, 4.5, 4}, {4.5, 5.5, 5}};
	private static final int TIMEpt_BEGIN=0;
	private  int timePointEnd;
	
	double[] phaseDiff;
	//long[] bins;
	double[] binMeans;
	double[] binNs;
	
	long[][] transitionMatrix;
	double[][] transitionRateMatrix;
	
	long[] stateCount;
	double[] stateCountNormed;
	public PhaseLockMode mode; 
	
	public OnePairPhaseTransitions(double[] phase_diff, double thresholdMin, double thresholdMax) {
		phaseDiff = phase_diff;
		timePointEnd=phase_diff.length;
		
		//populateBins(nBins);
		//populateTransitionMatrix();
		//populateTransitionRateMatrix();
		populateStateCounts();
		populatePhaseLockMode(thresholdMin, thresholdMax);
	}
	public OnePairPhaseTransitions(double[] phase_diff, double rateThresh) {
		phaseDiff = phase_diff;
		timePointEnd=phase_diff.length;
		
		//populateBins(nBins);
		//populateTransitionMatrix();
		//populateTransitionRateMatrix();
		populateStateCounts();
		populatePhaseLockMode(rateThresh);
	}
	private void populateBins(int n) {		
		
		EmpiricalDistribution dist = new EmpiricalDistribution(n);
		dist.load(phaseDiff);
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
		for(int i=TIMEpt_BEGIN;i<timePointEnd;i++) {
			previous_state = indexOf(phaseDiff[i]);
			startIdx++;
			if(previous_state!=-1)
				break;
		}
		
		for(int i=startIdx;i<timePointEnd;i++) {
			current_state=indexOf(phaseDiff[i]);
			if(current_state==-1) {
				continue;
			}
			transitionMatrix[previous_state][current_state] = transitionMatrix[previous_state][current_state] +1;
			previous_state = current_state;
		}
	}
	
	private void populateStateCounts() {
		stateCount = new long[N_DIVISIONS];
		stateCountNormed = new double[N_DIVISIONS];
		for(int i=TIMEpt_BEGIN;i<timePointEnd;i++) {
			stateCount[indexOf(phaseDiff[i])] +=1; 
		}
		double sum=rowSum(stateCount);
		for(int i=0;i<N_DIVISIONS;i++) {
			stateCountNormed[i] = stateCount[i]/sum;
		}
	}
	private void populateTransitionRateMatrix() {
		transitionRateMatrix = new double[N_DIVISIONS][N_DIVISIONS];
		double row_sum=matSum(transitionMatrix);			
		for(int i=0;i<transitionMatrix.length;i++) {			
			for(int j=0;j<transitionMatrix[i].length;j++) {
				transitionRateMatrix[i][j]=transitionMatrix[i][j]/row_sum;
			}
		}		
	}
	
	private int matSum(long[][] mat) {
		int sum=0;
		for(int i=0;i<mat.length;i++) {
			for(int j=0;j<mat.length;j++) {
				sum += mat[i][j];
			}
		}
		return sum;
	}
	
	private int rowSum(long[] row) {
		int sum=0;
		for(int i=0;i<row.length;i++) {
				sum += row[i];
			}		
		return sum;
	}
	
    private void populatePhaseLockMode(double threshold_min, double threshold_max) {
    	this.mode=PhaseLockMode.UNSYNC;
    	boolean found = false;
    	for(int i=0;i<this.stateCountNormed.length;i++) {
    		
    		if(stateCountNormed[i]>threshold_max) {
    			found = true;
    			for(int j=0;j<this.stateCountNormed.length;j++) {    				
    				if(i==j) continue;
    				//min threshold crit. is not applied to the adjacent transient partitions 
    				//(cuz some of them are not transient, a fixed boundary partition doesnt capture this)
    				if(j==i+1 || j==i-1) continue;
    				if(i==0 && j==5) continue;
    				if(i==5 && j==0) continue;
    				
    				if(stateCountNormed[j]>=threshold_min) {found = false;}
    			}
    			if(found==true) {
    				this.mode=PhaseLockMode.getPhaseLockMode(i);
    				break;
    			}
    		}
    	}
    }

    private void populatePhaseLockMode(double thresh) {
    	this.mode=PhaseLockMode.UNSYNC;
    	boolean found = false;
    	for(int i=0;i<this.stateCountNormed.length;i++) {
    		
    	//	if(stateCountNormed[i]>threshold_max) 
    			found = true;
    			for(int j=0;j<this.stateCountNormed.length;j++) {    				
    				if(i==j) continue;
    				//min threshold crit. is not applied to the adjacent transient partitions 
    				//(cuz some of them are not transient, a fixed boundary partition doesnt capture this)
    				if(j==i+1 || j==i-1) continue;
    				if(i==0 && j==5) continue;
    				if(i==5 && j==0) continue;
    				
    				if(stateCountNormed[j]>=stateCountNormed[i]/thresh) {found = false;}
    			}
    			if(found==true) {
    				this.mode=PhaseLockMode.getPhaseLockMode(i);
    				break;
    			}
    		
    	}
    }
}
