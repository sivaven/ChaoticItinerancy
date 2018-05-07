import java.util.ArrayList;
import java.util.List;

public class Entropy {
	public NetworkState[] states;
	public List<Double> probability;
	public double entropy;
	
	public Entropy(NetworkState[] states){
		this.states = states;
		this.probability = new ArrayList<>();
		populateStateDuplicates();
		calculateProbabilities();
		calculateEntropy();
	}
	
	/*
	 * maybe this is round about logic instead of just counts -- so that it can be verified.
	 */
	public void populateStateDuplicates() {		
		List<Integer> tempList = new ArrayList<>();
		
		for(int i=0;i<states.length;i++) {
			states[i].duplicates = new ArrayList<>(); //make an empty list for all states
			if(tempList.contains(i)) {  // skip states that were already matched with previous states -- 
				continue;				// these states will have empty duplicates list. 	
			}
			for(int j=0;j<states.length;j++) {	
				if(tempList.contains(j)) {  
					continue;				
				}
				if(states[i].matches(states[j])) {
					states[i].duplicates.add(j);
					tempList.add(j);
				}
			}
		}
	}
	
	public void calculateProbabilities() {
		probability = new ArrayList<>();
		for(int i=0;i<states.length;i++) {
			if(!states[i].duplicates.isEmpty()) {
				double p = (states[i].duplicates.size()*1.0d)/(states.length*1.0d);
				probability.add(p);
			}
		}
	}
	
	public void calculateEntropy() {
		double exp=0;
		for(int i=0;i<probability.size();i++) {
			double P =  probability.get(i);
			exp+= P * Math.log(P);
		}
		entropy = -exp;
	}
	public void displayStats(boolean duplicates) {
		if(duplicates)
		for(int i=0;i<this.states.length;i++) {
			GeneralUtils.displayList(states[i].duplicates);
		}
		
		for(double d:this.probability)
			System.out.println(d);
	}
}
