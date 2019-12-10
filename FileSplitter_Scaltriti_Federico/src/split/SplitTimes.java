package split;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SplitTimes extends Split{

	public SplitTimes(String fileName, String fileFormat, RandomAccessFile raf) {
		super(fileName, fileFormat, raf);
	}
	
	@Override
	public void mainDivision(long numSplits, long bytesPerSplit, int maxReadBufferSize, long sourceSize) throws IOException{
		long remainingBytes = sourceSize % bytesPerSplit;
		String name = null;
		
		for(int destIx=1; destIx <= numSplits; destIx++) {	
            BufferedOutputStream bw = null;
			try {
				name = getFileName()+destIx+"T"+getFileFormat();
				bw = new BufferedOutputStream(new FileOutputStream(name));
			} catch (FileNotFoundException e) {
				System.err.println(getFileName()+getFileFormat()+" NOT FOUND !");
			}
            if(bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit/maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for(int i=0; i<numReads; i++) {
                	readWrite(name, bw, maxReadBufferSize);
                }
                if(numRemainingRead > 0)
                	readWrite(name, bw, numRemainingRead);
                
                //TODO se nel merge non lo ricompone perfettamente, ins. questo controllo
                if (destIx == numSplits && remainingBytes > 0)
                	readWrite(name, bw, remainingBytes);
                
            }else {
            	readWrite(name, bw, bytesPerSplit);	
            }
            bw.close();
        }
	}
    
	@Override
    public void action(long numSplits, long bytesPerSplit, int maxReadBufferSize, long sourceSize) throws IOException{
    	
    	mainDivision(numSplits, bytesPerSplit, maxReadBufferSize, sourceSize);
        getRaf().close();
    }
}
