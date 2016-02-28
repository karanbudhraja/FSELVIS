package agent;

import java.util.ArrayList;
import utilityFunc.*;

public abstract class AbsAgent {
	protected AbsUtF mUtF;
	protected ArrayList<Integer> mFeatures;
	protected double mUtility;
	protected double mCost;
	protected boolean mVerbose;
	
	public AbsAgent() {
		mVerbose = false;
		mUtility = 0;
		mCost = 0;
		mFeatures = new ArrayList<Integer>();
	}

	public AbsAgent(AbsUtF newUtF) {
		this();
		mUtF = newUtF;
	}
	
	public AbsAgent(AbsUtF newUtF, ArrayList<Integer> featureSet) {
		this(newUtF);
		addFeatureSet(featureSet);
	}
	
	public void addFeatureSet(ArrayList<Integer> featureSet) {
		for(int i = 0; i < featureSet.size(); i++) {
			addFeature(featureSet.get(i));
		}
	}

	public void addFeature(int featureIndex) {
		if(!mFeatures.contains(featureIndex)) {
			mFeatures.add(featureIndex);
		}
	}
	
	public double getUtility() {
		return mUtility;
	}
	
	public double getCost() {
		return mCost;
	}
	
	public void setVerbose(boolean toSet) {
		mVerbose = toSet;
	}
	/*
	public double evaluateUtility() {
		return (mUtF.getUtility(mFeatures))/100.00;
	}*/
}
