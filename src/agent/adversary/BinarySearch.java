package agent.adversary;

import java.util.Arrays;
import java.util.List;

import agent.selector.AbsSel;
import utilityFunc.*;

public class BinarySearch extends AbsAdv {
	protected double mLowerPrediction;
	protected double mCurPrediction;
	protected double mUpperPrediction;
	protected double mAccuracy;
	protected int mMostRecentOffer;
	protected int mInteractionCount;
	protected double mLowWeight;
	protected double mHighWeight;
	protected boolean mIsWeightedAverage;
	
	public int getMostRecentOffer(){
		return mMostRecentOffer;
	}
	
	public int getInteractionCount(){
		return mInteractionCount;
	}
	
	public List<Double> getModelEstimate(){
		return Arrays.asList(mLowerPrediction, mUpperPrediction);
	}
	
	public void setModelEstimate(List<Double> modelEstimate){
		if(modelEstimate.get(0) > mLowerPrediction) {
			mLowWeight +=modelEstimate.get(0) -  mLowerPrediction;
			mLowerPrediction = modelEstimate.get(0);
		}
		if(modelEstimate.get(1) < mUpperPrediction) {
			mHighWeight += mUpperPrediction - modelEstimate.get(1);
			mUpperPrediction = modelEstimate.get(1);
		}
	}
	
	public BinarySearch(double newUpperPrediction, double newAccuracy, AbsUtF newFunction, 
			boolean newIsWeightedAverage, double newExpectedRange) {
		super(newFunction);
		mUpperPrediction = newUpperPrediction;
		mAccuracy = newAccuracy;
		mLowerPrediction = 0;
		
		mMostRecentOffer = -1;
		mInteractionCount = 0;
		
		mLowWeight = newExpectedRange;
		mHighWeight = newExpectedRange;
		mIsWeightedAverage = newIsWeightedAverage;
	}
	
	public int whichFeature(AbsSel target) {
		return mFeatures.get((int)(Math.random()*mFeatures.size()));
	}
	
	public boolean giveOffer(AbsSel target) {
		//count an interaction
		mInteractionCount++;
		
		if(mFeatures.size() > 0) {
			if(mVerbose) System.out.println("Adv feature count: " + mFeatures.size());
			
			updatePrediction();
			int offer = whichFeature(target);
			mMostRecentOffer = offer;
			
			if(mVerbose) System.out.println("Adv predicition: " + mCurPrediction);
			
			double cost = mUtF.getUtilityIncrease(offer, mSellHistory)/mCurPrediction;
			if(cost <= 0) {
				//cost = 0;
				mFeatures.remove((Integer)offer);
				return false;
			}
			else {
				boolean accepted = target.takeOffer(cost, offer);
				if(accepted) {
					processAccept(cost, offer);
				}
				else {
					processDecline(cost, offer);
				}
				return accepted;
			}
		}
		else { 
			if(mVerbose) System.out.println("Adv cannot make another offer");
		}
		if(mVerbose) System.out.println("-------------");
		return false;
	}
	
	public void updatePrediction() {
		if(mAccuracy*mUpperPrediction < mLowerPrediction) {
			mCurPrediction = mUpperPrediction;
		}
		else {
			if(mIsWeightedAverage) {
				mCurPrediction = (mUpperPrediction*mHighWeight + mLowerPrediction*mLowWeight) / (mHighWeight+mLowWeight);
			}
			else {
				mCurPrediction = (mUpperPrediction + mLowerPrediction) / 2;
			}
		}
	}
	
	public void processAccept(double cost, int index) {
		super.processAccept(cost, index);
		if(mUpperPrediction > mCurPrediction) {
			mHighWeight += mUpperPrediction - mCurPrediction;
			mUpperPrediction = mCurPrediction;
		}
		updatePrediction();
	}
	
	public void processDecline(double cost, int index) {
		super.processDecline(cost, index);
		if(mLowerPrediction < mCurPrediction) {
			mLowWeight += mCurPrediction - mLowerPrediction;
			mLowerPrediction = mCurPrediction;
		}
		updatePrediction();
	}

}
