package agent.adversary;

import agent.selector.AbsSel;
import utilityFunc.*;

public class BinarySearch extends AbsAdv {
	protected double mLowerPrediction;
	protected double mCurPrediction;
	protected double mUpperPrediction;
	protected double mAccuracy;
	
	public BinarySearch(double newUpperPrediction, double newAccuracy, AbsUtF newFunction) {
		super(newFunction);
		mUpperPrediction = newUpperPrediction;
		mAccuracy = newAccuracy;
		mLowerPrediction = 0;
	}
	
	public int whichFeature(AbsSel target) {
		return mFeatures.get((int)(Math.random()*mFeatures.size()));
	}
	
	public void giveOffer(AbsSel target) {
		if(mFeatures.size() > 0) {
			if(mVerbose) System.out.println("Adv feature count: " + mFeatures.size());
			
			updatePrediction();
			int offer = whichFeature(target);
			
			
			if(mVerbose) System.out.println("Adv predicition: " + mCurPrediction);
			
			double cost = mUtF.getUtilityIncrease(offer, mSellHistory)/mCurPrediction;
			if(cost < 0) {
				//cost = 0;
				mFeatures.remove((Integer)offer);
			}
			else {
				boolean accepted = target.takeOffer(cost, offer);
				if(accepted) {
					processAccept(cost, offer);
				}
				else {
					processDecline(cost, offer);
				}
			}
		}
		else { 
			if(mVerbose) System.out.println("Adv cannot make another offer");
		}
		if(mVerbose) System.out.println("-------------");
	}
	
	public void updatePrediction() {
		if(mAccuracy*mUpperPrediction < mLowerPrediction) {
			mCurPrediction = mUpperPrediction;
		}
		else {
			mCurPrediction = (mUpperPrediction + mLowerPrediction) / 2;
		}
	}
	
	public void processAccept(double cost, int index) {
		super.processAccept(cost, index);
		if(mUpperPrediction > mCurPrediction) {
			mUpperPrediction = mCurPrediction;
		}
		updatePrediction();
	}
	
	public void processDecline(double cost, int index) {
		super.processDecline(cost, index);
		mLowerPrediction = mCurPrediction;
		updatePrediction();
	}

}
