package makeData;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class quickWekaScript {
	private static String IN_PATH = "/users/denizen/Desktop/UCI/";
	private static String	OUT_PATH	= "/users/denizen/Desktop/AfsResults/input/";
	private static String FILE_NAME = "waveform-5000";
	
	private static int DEPTH = 40;
	private static int MAX_CALC_DEPTH = 5;
	private static ArrayList<HashMap<HashSet<Integer>, Double>> adjustTables;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		adjustTables = new ArrayList<HashMap<HashSet<Integer>, Double>>();
		for(int i = 0; i < DEPTH; i++) {
			HashMap<HashSet<Integer>, Double> temp = new HashMap<HashSet<Integer>, Double>();
			adjustTables.add(temp);
		}
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(IN_PATH + FILE_NAME + ".arff"));
			Instances data = new Instances(reader);
			reader.close();
			data.setClassIndex(data.numAttributes() - 1);
			
			//System.out.println(eval(data));
			
			for(int i = 1; i < Math.pow(2, DEPTH); i++) {
				HashSet<Integer> key = new HashSet<Integer>();
				int num1s = 0;

				String binRep = Integer.toBinaryString(i);
				for(int j = 0; j < binRep.length(); j++) {
					//System.out.println("i=" + i + ", binRep = " + binRep + ", toAdd = " + toAdd.toString() + ", j = " + j + ", charAt = " + binRep.charAt(j));
					if(binRep.charAt(j) == '1') {
						//System.out.println("  charAt == '1' returned true!");
						key.add(binRep.length() - j);
						num1s++;
					}
				}
				
				if(num1s <= MAX_CALC_DEPTH) {
					double utility = eval(generateData(data, binRep));
					double utilityAdjustment = utility - existingUtil(binRep);
					adjustTables.get(num1s - 1).put(key, utilityAdjustment);
				}
				else {
					System.out.println("Skipped " + binRep + " since num1s = " + num1s + " <---------------------------------{SKIP");
					adjustTables.get(num1s - 1).put(key, 0d);
				}
				
				
			}
			dump();
			
			for(int i = 1; i <= DEPTH; i++) {
				File myFile = new File(OUT_PATH + FILE_NAME + i +".txt");
				if(!myFile.exists()) {
					myFile.createNewFile();
					BufferedWriter temp = new BufferedWriter(new FileWriter(myFile, true));
					
					Object[] temp2= adjustTables.get(i-1).keySet().toArray();
					for(int j = 0; j < temp2.length; j++) {
						//temp.write("  " + ((HashSet<Integer>)temp2[j]).toString() + " --> " + adjustTables.get(i-1).get(temp2[j]) + "\n");
						temp.write(adjustTables.get(i-1).get(temp2[j]).toString());
						 Object[] indexArr = ((HashSet<Integer>)temp2[j]).toArray();
						 for(int k = 0; k < indexArr.length; k++) {
							 temp.write("," + ((Integer)indexArr[k]).toString());
						 }
						 temp.write("\n");
					}
					
					temp.close();
				}
				else {
					System.out.println("File exists! NOT appending.");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static double existingUtil(String binRep) {
		ArrayList<Integer> toPass = new ArrayList<Integer>();
		for(int i = 0; i < binRep.length(); i++) {
			if(binRep.charAt(i) == '1') {
				//System.out.println("  charAt == '1' returned true!");
				toPass.add(binRep.length() - i);
			}
		}
		return getUtility(toPass);
	}
	
	public static double getUtility(ArrayList<Integer> featureSet) {
		double utility = 0;

		ArrayList<HashSet<Integer>> temp = generatePowerSet(featureSet, DEPTH);
		for(int j = 0; j < temp.size(); j++) {
			if(adjustTables.get(temp.get(j).size() - 1).containsKey(temp.get(j))) {
				utility += adjustTables.get(temp.get(j).size() - 1).get(temp.get(j));
			}
		}
		//System.out.println(featureSet.toString() + " --> " + utility);
		return utility;
	}
	
	public static ArrayList<HashSet<Integer>> generatePowerSet(ArrayList<Integer> featureSet, int size) {
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
	
	private static Instances generateData(Instances data, String toggle) {
		System.out.println("Toggling data based on: " + toggle);

		try {
			String toRemove = "";
			Boolean needComma = false;
			for(int i = 0; i < toggle.length(); i++) {
				if(toggle.charAt(i) == '0') {
					if(needComma) {
						toRemove += ",";
					}
					needComma = true;
					toRemove += (toggle.length() - i);
				}
			}
			
			for(int i = DEPTH; i > toggle.length(); i--) {
				if(needComma) {
					toRemove += ",";
				}
				needComma = true;
				toRemove += i;
			}
			System.out.println("  Passing toRemove as: " + toRemove);
			Remove remove = new Remove();
			remove.setAttributeIndices(toRemove);
			remove.setInvertSelection(false);
			remove.setInputFormat(data);
			Instances newData = Filter.useFilter(data, remove);
			System.out.println("  Now contains # attributes: " + newData.numAttributes());
			return newData;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	private static double eval(Instances data) {
		try {
			NaiveBayesUpdateable nb = new NaiveBayesUpdateable();
			 //nb.buildClassifier(data);
			
			 Evaluation eval = new Evaluation(data);
			 eval.crossValidateModel(nb, data, 2, new Random(1));
			 
			 System.out.println("  Percent correct: " + eval.pctCorrect());
			 
			 return eval.pctCorrect();
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void dump() {
		System.out.println("BEGIN DUMP");
		for(int i = 0; i < DEPTH; i++) {
			System.out.println("Dump for \"depth\"=" + (i+1));
			Object[] temp = adjustTables.get(i).keySet().toArray();
			for(int j = 0; j < temp.length; j++) {
				System.out.println("  " + ((HashSet<Integer>)temp[j]).toString() + " --> " + adjustTables.get(i).get(temp[j]));
			}
		}
		System.out.println("END DUMP");
	}

}
