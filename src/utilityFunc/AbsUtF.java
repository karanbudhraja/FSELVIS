package utilityFunc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import utilityFunc.discountFunc.AbsDiF;

public abstract class AbsUtF {
	protected AbsDiF mDiF;
	protected boolean mVerbose;
	protected boolean mAllowMultiples;
	
	public AbsUtF(AbsDiF newDiscountFunction) {
		mDiF = newDiscountFunction;
		mVerbose = false;
		mAllowMultiples = false;
	}
	
	public AbsUtF(AbsDiF newDiscountFunction, Boolean allowMultiples) {
		mDiF = newDiscountFunction;
		mVerbose = false;
		mAllowMultiples = allowMultiples;
	}
	
	public void handleMultiples(ArrayList<Integer> featureSet) {
		if(!mAllowMultiples) {
			Set<Integer> tempSet = new HashSet<>();
			tempSet.addAll(featureSet);
			featureSet.clear();
			featureSet.addAll(tempSet);
		}
	}
	
	public void setVerbose(boolean toSet) {
		mVerbose = toSet;
	}
	
	public abstract double getUtility(ArrayList<Integer> featureSet);
	public abstract double getUtilityIncrease(int featureIndex, ArrayList<Integer> featureSet);
}
