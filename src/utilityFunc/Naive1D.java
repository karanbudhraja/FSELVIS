package utilityFunc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import utilityFunc.discountFunc.AbsDiF;

public class Naive1D extends AbsUtF{
	protected HashMap<Integer, Double> mIndexToUtil;
	
	public Naive1D(AbsDiF newDiscountFunction, Boolean allowMultiples, String path) {
		super(newDiscountFunction, allowMultiples);
		mIndexToUtil = new HashMap<Integer, Double>();
		
		readInFeatures(path);
	}
	
	public void readInFeatures(String path) {
		try {
			FileReader tempReader = new FileReader(path + 1 + ".txt");
			BufferedReader reader = new BufferedReader(tempReader);

			String line = reader.readLine();
			while(line != null){					
				String[] values = line.split(",");

				Double modification = Double.parseDouble(values[0]);
				mIndexToUtil.put(Integer.parseInt(values[1]), modification);
				line = reader.readLine();
			}

			tempReader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
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
		if(mVerbose) System.out.println(featureSet + " = " + mDiF.apply(toReturn));
		return mDiF.apply(toReturn);
	}
	
	public double getUtilityIncrease(int featureIndex, ArrayList<Integer> featureSet) {
		if(mAllowMultiples == false && featureSet.contains(featureIndex))
			return 0;
		
		ArrayList<Integer> temp = new ArrayList<Integer>(featureSet);
	//	for(Integer i : featureSet)
	//		temp.add(i);
		temp.add(featureIndex);
		double toReturn = getUtility(temp) - getUtility(featureSet);

		
		if(mVerbose) System.out.println(featureSet + " + " + featureIndex + " -> " + toReturn);
		return toReturn;
	}
	
	public void addFeature(int index, double utility) {
		if(!mIndexToUtil.containsKey(index)) {
			mIndexToUtil.put(index, utility);
		}
	}
}
