package split;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

public class Crypto extends Split{

	public Crypto(String fileName, String fileFormat, RandomAccessFile raf) {
		super(fileName, fileFormat, raf);
		// TODO Auto-generated constructor stub
	}
	
	public void crypt(){
		try{
            byte[] plainBytes = "HELLO JCE".getBytes();
             
            // Generate the key first
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);  // Key size
            Key key = keyGen.generateKey();
          
            // Create Cipher instance and initialize it to encrytion mode
            Cipher cipher = Cipher.getInstance("AES/OFB/PKCS5Padding");  // Transformation of the algorithm
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherBytes = cipher.doFinal(plainBytes);
             
            // Reinitialize the Cipher to decryption mode
            cipher.init(Cipher.DECRYPT_MODE, key, cipher.getParameters());
            byte[] plainBytesDecrypted = cipher.doFinal(cipherBytes);
             
            System.out.println("DECRUPTED DATA : "+new String(plainBytesDecrypted));    
        }catch(Exception ex){
            ex.printStackTrace();
        }
	}
	
	private Key keyGeneration(){
        KeyGenerator keyGen = null;
		try {
			keyGen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        keyGen.init(128);  // Key size
        Key key = keyGen.generateKey();
        
        return key;
	}
	
	@Override
	public void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        byte[] cipherBytes = null;
        
     // Generate the key first
        Key key = keyGeneration();
        
     // Create Cipher instance and initialize it to encryption mode
        Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			// Transformation of the algorithm
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}  catch (InvalidKeyException e) {
			e.printStackTrace();
		}    
        
        int val = raf.read(buf); //da "raf" leggo "buf" bytes
        
     
        byte[] plainBytesDecrypted = null; // Reinitialize the Cipher to decryption mode
        
        if(val != -1) { //se sono stati letti dei bytes
        	try {
    			cipherBytes = cipher.doFinal(buf);
    			//bw.write(cipherBytes); //scrive in "bw" i bytes salvati dentro "buf"
    			
    			
    			cipher.init(Cipher.DECRYPT_MODE, key, cipher.getParameters());
    			plainBytesDecrypted = cipher.doFinal(cipherBytes);
    			
    			bw.write(plainBytesDecrypted);
    			
    		} catch (IllegalBlockSizeException | BadPaddingException e) {
    			e.printStackTrace();
    		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
    			e.printStackTrace();
    		}      
        }
	}
	
	@Override
	public void mainDivision(long numSplits, long bytesPerSplit, int maxReadBufferSize, long sourceSize) throws IOException{
		
		for(int destIx=1; destIx <= numSplits; destIx++) {	
            BufferedOutputStream bw = null;
			try {
				bw = new BufferedOutputStream(new FileOutputStream(getFileName()+destIx+"C"+getFileFormat()));
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
	
	@Override
    public void checkFileRemaining(long remainingBytes, long numSplits) throws IOException{

    	BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(getFileName()+(numSplits+1)+"C"+getFileFormat()));
    	readWrite(getRaf(), bw, remainingBytes);
    	bw.close();
    }

}
