package agent.adversary;

import agent.selector.AbsSel;
import utilityFunc.*;

public class BinarySearch extends AbsAdv {
	protected double mLowerPrediction;
	protected double mUpperPrediction;
	protected double mAccuracy;
	
	public BinarySearch(double newUpperPrediction, double newAccuracy, AbsUtF newFunction) {
		super(newFunction);
		mUpperPrediction = newUpperPrediction;
		mAccuracy = newAccuracy;
		mLowerPrediction = 0;
	}
	
	public void giveOffer(AbsSel target) {
		if(mFeatures.size() > 0) {
			if(mVerbose) System.out.println("Adv feature count: " + mFeatures.size());
			int offer = mFeatures.get((int)(Math.random()*mFeatures.size()));
			
			double curPrediction;
			if(mAccuracy*mUpperPrediction < mLowerPrediction) {
				curPrediction = mUpperPrediction;
			}
			else {
				curPrediction = (mUpperPrediction + mLowerPrediction) / 2;
			}
			
			
			if(mVerbose) System.out.println("Adv predicition: " + curPrediction);
			
			double cost = mUtF.getUtilityIncrease(offer, mSellHistory)/curPrediction;
			if(cost < 0) {
				mFeatures.remove((Integer)offer);
			}
			else {
				boolean accepted = target.takeOffer(cost, offer);
				if(accepted) {
					processAccept(cost, offer);
					if(mUpperPrediction > curPrediction) {
						mUpperPrediction = curPrediction;
					}
				}
				else {
					mLowerPrediction = curPrediction;
				}
			}
		}
		else { 
			if(mVerbose) System.out.println("Adv cannot make another offer");
		}
		if(mVerbose) System.out.println("-------------");
	}

}
