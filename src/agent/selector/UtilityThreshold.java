package agent.selector;

import utilityFunc.AbsUtF;

public class UtilityThreshold extends AbsSel {
	protected double mThreshold;
	
	public UtilityThreshold(double newThreshold,  AbsUtF newFunction) {
		super(newFunction);
		mThreshold = newThreshold;
	}

	public boolean takeOffer(double cost, int featureIndex) {
		if(mVerbose) System.out.println("Sel feature count: " + mFeatures.size());
		double utilityIncrease = /*expectedUtility(featureIndex) - featureUtility() */
				mUtF.getUtilityIncrease(featureIndex, mFeatures);
		if(cost == 0 || utilityIncrease / cost >= mThreshold) {
			mCost += cost;
			mFeatures.add(featureIndex);
			return true;
		}
		else {
			return false;
		}
	}
}
