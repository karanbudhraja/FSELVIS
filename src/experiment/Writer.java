package experiment;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Stack;

public class Writer {
	private File mFile;
	private Stack<String> mBuffer;
	
	public Writer(String filePath) {
		mFile = new File(filePath);
		mBuffer = new Stack<String>();
		try {
			if(!mFile.exists()) {
				mFile.createNewFile();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void toBuffer(String message) {
		mBuffer.push(message);
		if(mBuffer.size() >= 100_000) {
			write();
		}
	}
	
	public void write() {
		try {
			BufferedWriter temp = new BufferedWriter(new FileWriter(mFile, true));
			while(mBuffer.isEmpty() == false) {
				temp.write(mBuffer.pop());		
			}
			temp.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
