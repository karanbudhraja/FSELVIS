package agent.adversary;

import java.util.ArrayList;
import java.util.HashMap;

import agent.selector.AbsSel;
import utilityFunc.*;

public class LearningWithBinarySearch extends BinarySearch {
	protected HashMap<String, Double> mPolicy;
	protected double mBaseQ;
	protected double mEpsilon;
	protected double mLearningRate;
	protected double mDiscountFactor;
	
	public LearningWithBinarySearch(double newUpperPrediction, double newAccuracy, 
			AbsUtF newFunction, double newBaseQ, double newEpsilon,
			double newLearningRate, double newDiscountFactor) {
		super(newUpperPrediction, newAccuracy, newFunction);
		mBaseQ = newBaseQ;
		mEpsilon = newEpsilon;
		mLearningRate = newLearningRate;
		mDiscountFactor = newDiscountFactor;
		mPolicy = new HashMap<String, Double>();
	}
	
	public void resetForLearning(double newUpperPrediction) {
		mUpperPrediction = newUpperPrediction;
		mLowerPrediction = 0;
		mUtility = 0;
		mCost = 0;
		mSellHistory = new ArrayList<Integer>();
		mFeatures = new ArrayList<Integer>();
	}
	
	public int whichFeature(AbsSel target) {
		if(Math.random() < mEpsilon) {
			return mFeatures.get((int)(Math.random()*mFeatures.size()));
		}
		else {
			return getOptimal();
		}
	}
	
	public int getOptimal() {
		String stateString = makeStateString();
		ArrayList<Integer> candidates = new ArrayList<Integer>();
		double curMaxQ = Integer.MIN_VALUE;
		if(mVerbose) System.out.println("stateString: " + stateString);
		for(int feature : mFeatures) {
			String stateActionString = makeStateActionString(stateString, feature);
			if(mVerbose) System.out.println(stateActionString);
			//if does not contain, add with base q-value
			if(!mPolicy.containsKey(stateActionString))
				mPolicy.put(stateActionString, (Double)mBaseQ);
			double qValue = mPolicy.get(stateActionString);
			if(qValue > curMaxQ) {
				candidates.clear();
				curMaxQ = mPolicy.get(stateActionString);
			}
			if(qValue == curMaxQ) {
				candidates.add(feature);
			}
		}
		return candidates.get((int)(Math.random()*candidates.size()));
	}
	
	public String makeStateActionString(String stateString, int feature) {
		return stateString + "O:" + feature;// + "/P:" + (int)mCurPrediction;
		//TODO: Remove cast to int; done only for debugging readability
	}
	
	public String makeStateString() {
		String stateString = "";
		Integer index = 1;
		while(mFeatures.contains(index) || mSellHistory.contains(index)) {
			if(mFeatures.contains(index))
				stateString += "A";//index + ":A/";
			else
				stateString += "S";//index + ":S/";
			index++;
		}
		return stateString;
	}
	
	public void processAccept(double cost, int index) {
		String stateActionString = makeStateActionString(makeStateString(), index);
		super.processAccept(cost, index);
		processLearning(cost, stateActionString);
	}
	
	public void processDecline(double cost, int index) {
		//String stateActionString = makeStateActionString(makeStateString(), index);
		super.processDecline(cost, index);
		//TODO: Possibly process even if declined
		//processLearning(cost, stateActionString);
	}
	
	public void processLearning(double cost, String oldStateActionString) {
		if(!mPolicy.containsKey(oldStateActionString))
			mPolicy.put(oldStateActionString, (Double)mBaseQ);
		double reward = cost;
		double nextOptimal;
		String newStateActionString;
		if(mFeatures.size() == 0) {
			nextOptimal = 0;
			newStateActionString = "'SSS'";
		}
		else {
			newStateActionString = makeStateActionString(makeStateString(), getOptimal());
			nextOptimal = mPolicy.get(newStateActionString);
		}
		//TODO: remove commented sections below
		double newQ = (1-mLearningRate)*mPolicy.get(oldStateActionString) + mLearningRate*(reward + 
				(mDiscountFactor*nextOptimal) /*- mPolicy.get(oldStateActionString)*/);
		/*System.out.println("Old = " + mPolicy.get(oldStateActionString) + 
				" for " + oldStateActionString + " ; New = " + newQ + " for " 
				+ newStateActionString);
		if(oldStateActionString.length() != 6)
			System.out.println("Ah!");
		if(newStateActionString.equals("SASO:2"))
			System.out.println("Gah!");*/
		mPolicy.put(oldStateActionString, (Double)newQ);
	}
}
