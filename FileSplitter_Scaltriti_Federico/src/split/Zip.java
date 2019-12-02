package split;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
//import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip extends Split{
    
	public Zip(String fileName, String fileFormat, RandomAccessFile raf) {
		super(fileName, fileFormat, raf);
	}

	
	public static void compress(){
	    String zipFile = "C:/Users/stefano.scaltriti/workspace/"
    		+ "Programmazione ad Oggetti/FileSplitter_Scaltriti_Federico/broad.zip";
	    
	    String[] srcFiles = { "C:/Users/stefano.scaltriti/workspace/"
    		+ "Programmazione ad Oggetti/FileSplitter_Scaltriti_Federico/broad.mp4"};
	     
	    ZipOutputStream zos = null;
	    try {
	        // create byte buffer
	        byte[] buffer = new byte[8*1024]; // 8kB
	        FileOutputStream fos = new FileOutputStream(zipFile);
	        zos = new ZipOutputStream(fos);
	         
	        for (int i=0; i < srcFiles.length; i++) {
	            File srcFile = new File(srcFiles[i]);
	            FileInputStream fis = new FileInputStream(srcFile);
	
	            // begin writing a new ZIP entry, positions the stream to the start of the entry data
	            zos.putNextEntry(new ZipEntry(srcFile.getName()));
	         
	            int length;
	            while ((length = fis.read(buffer)) > 0) {
	                zos.write(buffer, 0, length);
	            }
	            zos.closeEntry();
	            // close the InputStream
	            fis.close();
	        }
	        // close the ZipOutputStream
	        zos.close();
	         
	    }catch (IOException ioe) {
	        System.out.println("Error creating zip file: " + ioe);
	    }
	}
	
	@Override
	public void mainDivision(long numSplits, long bytesPerSplit, int maxReadBufferSize, long sourceSize) throws IOException{
	
		for(int destIx=1; destIx <= numSplits; destIx++) {	
            BufferedOutputStream bw = null;
			try {
				bw = new BufferedOutputStream(new FileOutputStream(getFileName()+destIx+".zip"));
			} catch (FileNotFoundException e) {
				System.err.println(getFileName()+".zip"+" NOT FOUND !");
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
}