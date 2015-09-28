package utilityFunc;

import java.util.ArrayList;
import utilityFunc.discountFunc.AbsDiF;

public abstract class AbsUtF {
	protected AbsDiF mDiF;
	protected boolean mVerbose;
	
	public AbsUtF(AbsDiF newDiscountFunction) {
		mDiF = newDiscountFunction;
		mVerbose = false;
	}
	
	public void setVerbose(boolean toSet) {
		mVerbose = toSet;
	}
	
	public abstract double getUtility(ArrayList<Integer> featureSet);
	public abstract double getUtilityIncrease(int featureIndex, ArrayList<Integer> featureSet);
}
