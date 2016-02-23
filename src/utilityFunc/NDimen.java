package utilityFunc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import utilityFunc.discountFunc.AbsDiF;

public class NDimen extends AbsUtF {
	protected ArrayList<HashMap<HashSet<Integer>, Double>> mAdjustTables;
	protected int mDepth;
	protected HashMap<String, Double> mMemoizationTable;
	
	public NDimen(AbsDiF newDiscountFunction, String path, int depth) {
		super(newDiscountFunction);
		mMemoizationTable = new HashMap<String, Double>();
		mDepth = depth;
		mAdjustTables = new ArrayList<HashMap<HashSet<Integer>, Double>>();
		for(int i = 0; i < depth; i++) {
			HashMap<HashSet<Integer>, Double> temp = new HashMap<HashSet<Integer>, Double>();
			mAdjustTables.add(temp);
		}
		
		readInFeatures(path);
	}
	
	public void readInFeatures(String path) {
		for(int i = 1; i <= mDepth; i++) {
			try {
				FileReader tempReader = new FileReader(path + "letter" + i + ".txt");
				BufferedReader reader = new BufferedReader(tempReader);
				
				String line = reader.readLine();
				while(line != null){					
					String[] values = line.split(",");

					Double modification = Double.parseDouble(values[0]);
					HashSet<Integer> set = new HashSet<Integer>();

					for(int j = 1; j < values.length; j++) {
						set.add(Integer.parseInt(values[j]));
					}

					mAdjustTables.get(set.size() - 1).put(set, modification);
					line = reader.readLine();
				}

				tempReader.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void dump() {
		System.out.println("BEGIN DUMP");
		for(int i = 0; i < mDepth; i++) {
			System.out.println("Dump for \"depth\"=" + (i+1));
			Object[] temp = mAdjustTables.get(i).keySet().toArray();
			for(int j = 0; j < temp.length; j++) {
				System.out.println("  " + ((HashSet<Integer>)temp[j]).toString() + " --> " + mAdjustTables.get(i).get(temp[j]));
			}
		}
		System.out.println("END DUMP");
	}

	public double getUtility(ArrayList<Integer> featureSet) {
		String key = featureSet.toString();
		if(!mMemoizationTable.containsKey(key)) {
			double utility = 0;
			ArrayList<HashSet<Integer>> temp = generatePowerSet(featureSet, mDepth);
			for(int j = 0; j < temp.size(); j++) {
				if(mAdjustTables.get(temp.get(j).size() - 1).containsKey(temp.get(j))) {
					utility += mAdjustTables.get(temp.get(j).size() - 1).get(temp.get(j));
				}
			}
			if(utility > 0)
				mMemoizationTable.put(key, utility);
			else
				mMemoizationTable.put(key, 0d);
		}
		//System.out.println(featureSet.toString() + " --> " + utility);
		
		return mMemoizationTable.get(key);
		
	}
	
	public ArrayList<HashSet<Integer>> generatePowerSet(ArrayList<Integer> featureSet, int size) {
		ArrayList<HashSet<Integer>> toReturn = new ArrayList<HashSet<Integer>>();
		
		for(int i = 1; i < Math.pow(2, featureSet.size()); i++) {
			String binRep = Integer.toBinaryString(i);
			HashSet<Integer> toAdd = new HashSet<Integer>();
			for(int j = 0; j < binRep.length(); j++) {
				//System.out.println("i=" + i + ", binRep = " + binRep + ", toAdd = " + toAdd.toString() + ", j = " + j + ", charAt = " + binRep.charAt(j));
				if(binRep.charAt(j) == '1') {
					//System.out.println("  charAt == '1' returned true!");
					toAdd.add(featureSet.get(binRep.length() - j - 1));
				}
			}
			if(toAdd.size() <= size) {
				toReturn.add(toAdd);
			}
		}
		return toReturn;
	}
	
	public double getUtilityIncrease(int featureIndex, ArrayList<Integer> featureSet) {
		ArrayList<Integer> temp = new ArrayList<Integer>(featureSet);
		temp.add(featureIndex);
		return getUtility(temp) - getUtility(featureSet);
	}

}
