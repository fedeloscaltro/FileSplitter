package split;

import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static utils.Const.BUFF;

public class Zip extends Split implements Runnable{
    
	public Zip(String fileName, String fullPath, RandomAccessFile raf, long numSplits, long bytesPerSplit, long sourceSize) {
		super(fileName, fullPath, raf, numSplits, bytesPerSplit, sourceSize);
	}

	
	public static void compress(){
	    String zipFile = "C:/Users/stefano.scaltriti/Git/FileSplitter"
    		+ "/FileSplitter_Scaltriti_Federico/Muse.zip";
	    
	    String srcFiles[] = {"C:/Users/stefano.scaltriti/Git/FileSplitter"
	    		+ "/FileSplitter_Scaltriti_Federico/Muse.mp3"};
	     
	    ZipOutputStream zos = null;
	    try {
	        // creo un buffer di byte
	        byte[] buffer = new byte[8*1024]; // 8kB
	        //creo gli stream
	        FileOutputStream fos = new FileOutputStream(zipFile);
	        zos = new ZipOutputStream(fos);
	         
	        //per ogni file da zippare
	        for (int i=0; i < srcFiles.length; i++) {
	            File srcFile = new File(srcFiles[i]);
	            
	            FileInputStream fis = new FileInputStream(srcFile);
	
	            // begin writing a new ZIP entry, positions the stream to the start of the entry data
	            zos.putNextEntry(new ZipEntry(srcFile.getName()));
	         
	            int length;
	            
	            //lettura e scrittura direttamente nello stream specifico per la compressione
	            while ((length = fis.read(buffer)) > 0) {
	                zos.write(buffer, 0, length);
	            }
	            zos.closeEntry();
	            // chiusura dell'InputStream
	            fis.close();
	        }
	        // chiusura dello ZipOutputStream
	        zos.close();
	         
	    }catch (IOException ioe) {
	        System.out.println("Error creating zip file: " + ioe);
	    }
	}
	
	@Override
	public void mainDivision(long numSplits, long bytesPerSplit, long sourceSize) throws IOException{
		long numReads, numRemainingRead;
		String name = null;
		
		for(int destIx=1; destIx <= numSplits; destIx++) {	
            BufferedOutputStream bw = null;
			try {
				name = getFileName()+destIx+"Z"+".zip";
				bw = new BufferedOutputStream(new FileOutputStream(name));
			} catch (FileNotFoundException e) {
				System.err.println(getFileName()+".zip"+" NOT FOUND !");
			}
            if(bytesPerSplit > BUFF) {
                numReads = bytesPerSplit/BUFF;
                numRemainingRead = bytesPerSplit % BUFF;
                for(int i=0; i<numReads; i++) {
                	readWrite(name, bw, BUFF);
                }
                if(numRemainingRead > 0) {
                	readWrite(name, bw, numRemainingRead);
                }
	            
            }else {
            	readWrite(name, bw, bytesPerSplit);
            }
            bw.close();
        }
	}
	
	@Override
    public void checkFileRemaining(long remainingBytes, long numSplits) throws IOException{
		String fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
		String name = getFileName()+(numSplits+1)+"Z"+fileFormat;
    	BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(name));
    	readWrite(name, bw, remainingBytes);
    	bw.close();
    }
	
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
}