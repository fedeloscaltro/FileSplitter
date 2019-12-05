package split;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

//Divisione in più parti specificando il numero di parti

public class Split {
	String fileName, fileFormat;
	long numSplits, bytesPerSlit, remainingBytes;
	int maxReadBufferSize;
	RandomAccessFile raf;
	
    public Split(String fileName, String fileFormat, RandomAccessFile raf) {
		super();
		this.fileName = fileName;
		this.fileFormat = fileFormat;
		this.raf = raf;
	}

	public void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if(val != -1) {
            bw.write(buf);
        }
	}
	
	public void mainDivision(long numSplits, long bytesPerSplit, int maxReadBufferSize, long sourceSize) throws IOException{
		for(int destIx=1; destIx <= numSplits; destIx++) {	
            BufferedOutputStream bw = null;
			try {
				bw = new BufferedOutputStream(new FileOutputStream(getFileName()+destIx+"D"+getFileFormat()));
			} catch (FileNotFoundException e) {
				System.err.println(getFileName()+getFileFormat()+" NOT FOUND !");
			}
            if(bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit/maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for(int i=0; i<numReads; i++) {
                	readWrite(getRaf(), bw, maxReadBufferSize);
                }
                if(numRemainingRead > 0) {
                	readWrite(getRaf(), bw, numRemainingRead);
                }
            }else {
            	readWrite(getRaf(), bw, bytesPerSplit);
            }
            bw.close();
        }
	}
	
    public void checkFileRemaining(long remainingBytes, long numSplits) throws IOException{

    	BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(getFileName()+(numSplits+1)+"D"+getFileFormat()));
    	readWrite(getRaf(), bw, remainingBytes);
    	bw.close();
    }
    
    public void action(long numSplits, long bytesPerSplit, int maxReadBufferSize, long sourceSize) throws IOException{

    	mainDivision(numSplits, bytesPerSplit, maxReadBufferSize, sourceSize);
    	
    	long remainingBytes = sourceSize % bytesPerSplit;
    	if(remainingBytes > 0){
    		checkFileRemaining(remainingBytes, numSplits);
    	}
        getRaf().close();
    }

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	public RandomAccessFile getRaf() {
		return raf;
	}

	public void setRaf(RandomAccessFile raf) {
		this.raf = raf;
	}

	public long getNumSplits() {
		return numSplits;
	}

	public void setNumSplits(long numSplits) {
		this.numSplits = numSplits;
	}

}
