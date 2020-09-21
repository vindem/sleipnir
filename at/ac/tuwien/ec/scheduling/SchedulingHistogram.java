package at.ac.tuwien.ec.scheduling;

import java.util.HashMap;

import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadSchedulingStatistics;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.SchedulingStatistics;

public class SchedulingHistogram extends HashMap<Scheduling, SchedulingStatistics>{

	public boolean containsKey(Scheduling d) {
		// TODO Auto-generated method stub
		return super.containsKey(d);
	}
	
	public SchedulingStatistics put(Scheduling d, SchedulingStatistics stats) {
		return super.put(d, stats);
	}

	public SchedulingStatistics replace(Scheduling d, SchedulingStatistics tmp) {
		return super.replace(d, tmp);
	}

	public void update(Scheduling d, double scoreUpdate) {
		if(super.containsKey(d))
		{
			SchedulingStatistics ss = super.get(d);
			ss.setFrequency(ss.getFrequency() + 1.0);
			ss.setScore(scoreUpdate);
			super.put(d,ss);
		}
		
	}

	public void add(Scheduling d, double e) {
		SchedulingStatistics ss = new SchedulingStatistics();
		ss.setFrequency(1.0);
		ss.setScore(e);
		super.put(d,ss);
	}

}
