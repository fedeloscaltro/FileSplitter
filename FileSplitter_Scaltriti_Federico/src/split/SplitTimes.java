package split;

import java.io.BufferedOutputStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import static utils.Const.BUFF;

/**
 * Classe che implementa la divisione di un file specificando il numero di file da ottenere.
 * È sottoclasse di {@link Split #Split(String, String, String, RandomAccessFile, long, long)}
 * @see Runnable
 * */

public class SplitTimes extends Split implements Runnable{

	/**
	 * Costruttore dello splitter nel caso sia specificato il numero di file da ottenere.
	 * Chiamato in fase di divisione una volta presa la decisione di dividere i file scelti.
	 * */
	public SplitTimes(String fileName, String fullPath, String destinationFolder, RandomAccessFile raf, long bytesPerSplit, long sourceSize) {
		super(fileName, fullPath, destinationFolder, raf, bytesPerSplit, sourceSize);
	}
	
	/**
	 * {@inheritDoc}
	 * */
	@Override
	public void mainDivision(long numSplits) throws IOException{
		long remainingBytes = getSourceSize() % getBytesPerSplit();
		String newFullName = null, simpleName = null;
        String fileFormat = null;
        
		for(int destIx=1; destIx <= numSplits; destIx++) {	
            BufferedOutputStream bw = null;
			try {
				fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
				simpleName = getFullFileName().substring(0, getFullFileName().lastIndexOf('.')); //Muse
				newFullName = simpleName+destIx+"T"+fileFormat;
				bw = new BufferedOutputStream(new FileOutputStream(getDestinationFolder()+newFullName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
            if(getBytesPerSplit() > BUFF) {
                long numReads = getBytesPerSplit()/BUFF;
                long numRemainingRead = getBytesPerSplit() % BUFF;
                for(int i=0; i<numReads; i++) {
                	readWrite(bw, BUFF);
                	setReadBytes(BUFF);
                }
                if(numRemainingRead > 0){
                	readWrite(bw, numRemainingRead);
                	setReadBytes(numRemainingRead);
                }
                	
                
                //se vengono tralasciati dei bytes alla numSplits-esima divisione
                if (destIx == numSplits && remainingBytes > 0){
                	readWrite(bw, remainingBytes);
                	setReadBytes(remainingBytes);
                }
                	
                
            }else {
            	readWrite(bw, getBytesPerSplit());	
            	setReadBytes(getBytesPerSplit());
            }
            bw.close();
        }
	}
	
	/**
	 * {@inheritDoc}
	 * */
	@Override
	public void run(){
		try {
			long numSplits = getSourceSize()/getBytesPerSplit();
			mainDivision(numSplits);
			getRaf().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
