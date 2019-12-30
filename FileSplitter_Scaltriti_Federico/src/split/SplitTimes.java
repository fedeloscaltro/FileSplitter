package split;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import static utils.Const.BUFF;
public class SplitTimes extends Split implements Runnable{

	public SplitTimes(String fileName, String fullPath, RandomAccessFile raf, long numSplits, long bytesPerSplit, long sourceSize) {
		super(fileName, fullPath, raf, numSplits, bytesPerSplit, sourceSize);
	}
	
	@Override
	public void mainDivision(long numSplits, long bytesPerSplit, long sourceSize) throws IOException{
		long remainingBytes = sourceSize % bytesPerSplit;
		String name = null;
		
		for(int destIx=1; destIx <= numSplits; destIx++) {	
            BufferedOutputStream bw = null;
            String fileFormat = null;
			try {
				fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
				name = getFileName()+destIx+"T"+fileFormat;
				bw = new BufferedOutputStream(new FileOutputStream(name));
			} catch (FileNotFoundException e) {
				System.err.println(getFileName()+fileFormat+" NOT FOUND !");
			}
            if(bytesPerSplit > BUFF) {
                long numReads = bytesPerSplit/BUFF;
                long numRemainingRead = bytesPerSplit % BUFF;
                for(int i=0; i<numReads; i++) {
                	readWrite(name, bw, BUFF);
                }
                if(numRemainingRead > 0)
                	readWrite(name, bw, numRemainingRead);
                
                //se vengono tralasciati dei bytes alla numSplits-esima divisione
                if (destIx == numSplits && remainingBytes > 0)
                	readWrite(name, bw, remainingBytes);
                
            }else {
            	readWrite(name, bw, bytesPerSplit);	
            }
            bw.close();
        }
	}
    
//	@Override
//    public void action(long numSplits, long bytesPerSplit, long sourceSize) throws IOException{
//    	
//    	mainDivision(numSplits, bytesPerSplit, sourceSize);
//        getRaf().close();
//    }
	
	@Override
	public void run(){
		try {
			mainDivision(getNumSplits(), getBytesPerSplit(), getSourceSize());
			getRaf().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
