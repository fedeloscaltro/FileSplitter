package split;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Crypto extends Split{
	
	// Create Cipher instance and initialize it to encryption mode
    Cipher cipher = null;
    SecretKeySpec secretKey = null;

	public Crypto(String fileName, String fileFormat, RandomAccessFile raf) {
		super(fileName, fileFormat, raf);
	}
	
	private void setKey(String passwd){
		byte[] hash;
        
        //System.out.println(passwd.getBytes());
        
        MessageDigest sha = null;
        
		try {
			sha = MessageDigest.getInstance("SHA-1");
			hash = passwd.getBytes("UTF-8");
			hash = sha.digest(hash);
			hash = Arrays.copyOf(hash, 16);
			secretKey = new SecretKeySpec(hash, "AES");
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	}
	
	public void encrypt(String name, FileOutputStream fos, CipherOutputStream cos) throws FileNotFoundException{
		//ENCRYPTION
        //byte[] iv = new byte[16];
        
        try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			// Transformation of the algorithm
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
        
        
        
        //fos = new FileOutputStream(name); 
        //cos = new CipherOutputStream(fos, cipher); //<--PROBLEMA
	}
	
	public void readWrite(CipherOutputStream cos, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        
      //WRITING ON FILE
        if(getRaf().read(buf) != -1)
        	cos.write(buf);
	}
	
	@Override
	public void mainDivision(long numSplits, long bytesPerSplit, int maxReadBufferSize, long sourceSize) throws IOException{
	    String name = null;
	    CipherOutputStream cos = null;
	    
		for(int destIx=1; destIx <= numSplits; destIx++) {
            name = getFileName()+destIx+"C"+getFileFormat();
			try {
				FileOutputStream fos = new FileOutputStream(name);
				encrypt(name, fos, cos);
				cos = new CipherOutputStream(fos, cipher);
				
			} catch (FileNotFoundException e) {
				System.err.println(getFileName()+getFileFormat()+" NOT FOUND !");
			}
            if(bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit/maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for(int i=0; i<numReads; i++) {
                	readWrite(cos, maxReadBufferSize);
                }
                if(numRemainingRead > 0) {
                	readWrite(cos, numRemainingRead);
                }
            }else {
            	readWrite(cos, bytesPerSplit);
            }
            cos.flush();
            cos.close();
        }
	}
	
    public void checkFileRemaining(long remainingBytes, long numSplits) throws IOException{
		String name = getFileName()+(numSplits+1)+"C"+getFileFormat();
    	CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(name), cipher);
    	readWrite(cos, remainingBytes);
    	cos.flush();
    	cos.close();
    }
	
	@Override
	public void action(long numSplits, long bytesPerSplit, int maxReadBufferSize, long sourceSize) throws IOException{
		String passwd = null;
		
		// SET KEY
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
	       System.out.print("Inserire la password: ");
	    try {
			passwd = buffer.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    setKey(passwd);
		
    	mainDivision(numSplits, bytesPerSplit, maxReadBufferSize, sourceSize);
    	
    	long remainingBytes = sourceSize % bytesPerSplit;
    	
    	if(remainingBytes > 0){
    		checkFileRemaining(remainingBytes, numSplits);
    	}
        getRaf().close();
    }

}
