package split;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import static utils.Const.BUFF;

//Divisione in più parti specificando il numero di parti

public class Split implements Runnable{
	String fileName, fullPath;
	long numSplits, bytesPerSplit, remainingBytes, sourceSize;
	RandomAccessFile raf;
	
    public Split(String fileName, String fullPath, RandomAccessFile raf, long numSplits, long bytesPerSplit, long sourceSize) {
		super();
		this.fileName = fileName;
		this.fullPath = fullPath;
		this.raf = raf;
		this.numSplits = numSplits;
		this.bytesPerSplit = bytesPerSplit;
		this.sourceSize = sourceSize;
	}
    
    public Split(String fileName, String fullPath, long numSplits, long bytesPerSplit, long sourceSize) {
		super();
		this.fileName = fileName;
		this.fullPath = fullPath;
		this.numSplits = numSplits;
		this.bytesPerSplit = bytesPerSplit;
		this.sourceSize = sourceSize;
	}

	public void readWrite(String name, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = getRaf().read(buf);
        if(val != -1) {
            bw.write(buf);
        }
	}
	
	public void mainDivision(long numSplits, long bytesPerSplit, long sourceSize) throws IOException{
		String name = null;
		
		//operazioni da svolgere per ogni divisione risultante dal file iniziale
		for(int destIx=1; destIx <= numSplits; destIx++) {	
            BufferedOutputStream bw = null;
            String fileFormat = null;
            //instazio un nuovo BufferedOutputStream
			try {
				fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
				name = getFileName()+destIx+"D"+fileFormat;
				bw = new BufferedOutputStream(new FileOutputStream(name));
			} catch (FileNotFoundException e) {
				System.err.println(getFileName()+fileFormat+" NOT FOUND !");
			}
			//se il num di bytes per ogni file creato è più grande della dim. del buffer
            if(bytesPerSplit > BUFF) {
            	//calcolo quante letture dovrò fare per leggere tutto il file
                long numReads = bytesPerSplit/BUFF;
                long numRemainingRead = bytesPerSplit % BUFF;
                //leggo effettivamente i bytes
                for(int i=0; i<numReads; i++) {
                	readWrite(name, bw, BUFF);
                }
                //controllo se sono stati tralasciati bytes in più
                if(numRemainingRead > 0) {
                	readWrite(name, bw, numRemainingRead);
                }
                //se il buffer è più largo della dim. per ogni file diviso
            }else {
            	readWrite(name, bw, bytesPerSplit);
            }
            //chiudo lo stream
            bw.close();
        }
	}
	
    public void checkFileRemaining(long remainingBytes, long numSplits) throws IOException{
    	String fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
    	String name = getFileName()+(numSplits+1)+"D"+fileFormat;
    	BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(name));
    	readWrite(name, bw, remainingBytes);
    	bw.close();
    }
    
//    public void action(long numSplits, long bytesPerSplit, long sourceSize) throws IOException{
//
//    	//attuo la divisione principale
//    	mainDivision(numSplits, bytesPerSplit, sourceSize);
//    	
//    	//considero eventuali bytes restanti per una divisione finale
//    	long remainingBytes = sourceSize % bytesPerSplit;
//    	if(remainingBytes > 0){
//    		checkFileRemaining(remainingBytes, numSplits);
//    	}
//        getRaf().close();
//    }
    
	@Override
	public void run() {
		//considero eventuali bytes restanti per una divisione finale
    	long remainingBytes = getSourceSize() % getBytesPerSplit();
    	
    	try {
    		//attuo la divisione principale
			mainDivision(getNumSplits(), getBytesPerSplit(), getSourceSize());
			
			if(remainingBytes > 0)
				checkFileRemaining(remainingBytes, getNumSplits());
			
			getRaf().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
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

	public long getBytesPerSplit() {
		return bytesPerSplit;
	}

	public void setBytesPerSplit(long bytesPerSplit) {
		this.bytesPerSplit = bytesPerSplit;
	}

	public long getRemainingBytes() {
		return remainingBytes;
	}

	public void setRemainingBytes(long remainingBytes) {
		this.remainingBytes = remainingBytes;
	}

	public long getSourceSize() {
		return sourceSize;
	}

	public void setSourceSize(long sourceSize) {
		this.sourceSize = sourceSize;
	}
	
}
