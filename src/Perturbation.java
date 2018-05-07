import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Perturbation {
	Map<Integer, NetworkState> map;
	
	Perturbation(){
		map = new HashMap<>();
	}
	
	public void addData(int dur, NetworkState state) {
		map.put(dur, state);
	}
	public int calculateSinglePhaseLockedMode(int duration) {		
		NetworkState state = map.get(duration);
		return state.pairs.length  - state.numberOfUnSyncModes();
	}
	
	public NetworkState getNetworkState(int dur) {
		return map.get(dur);
	}
	public int durationOfMaxSyncModes() {
		Iterator it = map.entrySet().iterator();
		int max_syn_duration = 0;
		int maxSyncModes =0;
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			
			int duration = (Integer)pair.getKey();
			NetworkState state = (NetworkState)pair.getValue();
			int n_sync_modes = state.pairs.length-state.numberOfUnSyncModes();
			
			if(n_sync_modes > maxSyncModes) {
				maxSyncModes=n_sync_modes;
				max_syn_duration=duration;
			}
		}
		return max_syn_duration;
	}
	
	public Set<Integer> getAllDurations() {		
		return map.keySet();
	}
}
