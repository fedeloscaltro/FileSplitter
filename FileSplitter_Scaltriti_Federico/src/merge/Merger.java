package merge;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Merger {
	private String fileName;
	private String fileFormat;
	
	public Merger(String fileName, String fileFormat){
		this.fileName = fileName;
		this.fileFormat = fileFormat;
	}
	
	public void readWrite(FileInputStream fis, BufferedOutputStream bos, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = fis.read(buf);
        if(val != -1) {
            bos.write(buf);
        }
	}
	
	public void merge(char letter) throws IOException{

        FileInputStream fis; 
        
        int c = 1;
        File f = new File(fileName+fileFormat); //Muse1D+.mp3
        String realName = fileName.substring(0, fileName.lastIndexOf("1"));
        BufferedOutputStream bos = null;
        bos = new BufferedOutputStream(new FileOutputStream(realName+fileFormat));
        byte[] buf = new byte[8*1024]; 
        
        while (f.exists()){	
        	fis = new FileInputStream(f.getName());
        	if (f.length()>buf.length){
        		long numReads = f.length()/buf.length;
            	long bytesRemaining = f.length()%buf.length;
            	for(int i=0; i<numReads; i++) {
            		readWrite(fis, bos, buf.length);
                }
                if(bytesRemaining > 0) {
                	readWrite(fis, bos, bytesRemaining);
                }
        	}
        	
        	
        	
//        	while(fis.read(buf) != -1)
//        		bos.write(buf);
        	
        	f = new File(fileName.substring(0, fileName.lastIndexOf("1"))+(++c)+letter+fileFormat);
        	System.out.println("f.getName(): "+f.getName());
        }
        
        bos.close();
    }	
}
