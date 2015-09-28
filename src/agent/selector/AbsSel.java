package agent.selector;

//import java.util.ArrayList;

import utilityFunc.AbsUtF;
import agent.AbsAgent;

public abstract class AbsSel extends AbsAgent {
	
	public AbsSel(AbsUtF newFunction) {
		super(newFunction);
	}
	
	public abstract boolean takeOffer(double cost, int featureIndex);
	
	/*
	public double featureUtility() {
		return mUtF.getUtility(mFeatures);
	}
	*/
	
	/*
	public double expectedUtility(int newFeature) {
		ArrayList<Integer> futureSet = new ArrayList<Integer>();
		for(int i = 0; i < mFeatures.size(); i++) {
			futureSet.add(mFeatures.get(i));
		}
		futureSet.add(newFeature);
		return mUtF.getUtility(futureSet);
	}
	*/
}
