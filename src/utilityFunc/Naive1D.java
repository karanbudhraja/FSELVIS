package utilityFunc;

import java.util.ArrayList;
import java.util.HashMap;
import utilityFunc.discountFunc.AbsDiF;

public class Naive1D extends AbsUtF{
	protected HashMap<Integer, Double> mIndexToUtil;
	
	public Naive1D(AbsDiF newDiscountFunction) {
		super(newDiscountFunction);
		mIndexToUtil = new HashMap<Integer, Double>();
	}
	
	private double getRawUtility(ArrayList<Integer> featureSet) {
		double toReturn = 0;
		for(int i = 0; i < featureSet.size(); i++) {
			toReturn += mIndexToUtil.get(featureSet.get(i));
		}
		return toReturn;
	}
	
	public double getUtility(ArrayList<Integer> featureSet) {
		double toReturn = getRawUtility(featureSet);
		return mDiF.apply(toReturn);
	}
	
	public double getUtilityIncrease(int featureIndex, ArrayList<Integer> featureSet) {
		return mDiF.apply(mIndexToUtil.get(featureIndex) + getRawUtility(featureSet)) - 
				mDiF.apply(getRawUtility(featureSet));
	}
	
	public void addFeature(int index, double utility) {
		if(!mIndexToUtil.containsKey(index)) {
			mIndexToUtil.put(index, utility);
		}
	}
}
