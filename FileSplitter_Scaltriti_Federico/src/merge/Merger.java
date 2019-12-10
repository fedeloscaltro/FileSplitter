package merge;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import utils.*;

public class Merger {
	private String fileName;
	private String fileFormat;
	private SecretKeySpec secretKey;
	
	public Merger(String fileName, String fileFormat){
		this.fileName = fileName;
		this.fileFormat = fileFormat;
	}
	private void setKey(String passwd){
		byte[] hash;
        
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
        	
        	f = new File(fileName.substring(0, fileName.lastIndexOf("1"))+(++c)+letter+fileFormat);
        }
        
        bos.close();
    }
	
	
	public void decrypt(String fileName) throws IOException{
		byte[] cipherBytes = null;
		byte[] buf = new byte[(int) 8*1024];
		BufferedOutputStream bos = null;
		FileInputStream fis = null;
		Cipher cipher = null;
		// Generate the key first
        //Key key = keyGeneration();
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
        
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
        
        
        File f = new File(fileName+fileFormat); //Muse1D+.mp3
        System.out.println("fileName: "+fileName+"\nfileFormat: "+fileFormat);
        bos = new BufferedOutputStream(new FileOutputStream(fileName+fileFormat));
        int c = 1;
        char letter = 'C';
        CipherInputStream in = null;
        /*----------------------------------------------------*/
        
        while (f.exists()){	
        	fis = new FileInputStream(f.getName());
        	in = new CipherInputStream(fis, cipher);
        	if (f.length()>buf.length){
        		long numReads = f.length()/buf.length;
            	long bytesRemaining = f.length()%buf.length;
            	for(int i=0; i<numReads; i++) {
            		writeCrypt(in, bos, buf.length);
                }
                if(bytesRemaining > 0) {
                	writeCrypt(in, bos, bytesRemaining);
                }
        	}
        	
        	f = new File(fileName.substring(0, fileName.lastIndexOf("1"))+(++c)+letter+fileFormat);
        	bos = new BufferedOutputStream(new FileOutputStream(f.getName()));
        }
        /*----------------------------------------------------*/		
		bos.close();
	}
	
	public void writeCrypt(CipherInputStream cis, BufferedOutputStream bos, long numBytes) throws IOException{
        byte[] b = new byte[(int) numBytes];
        
        int val = cis.read(b);
        if(val != -1) {
            bos.write(b);
        }
        
//        int val = fis.read(buf);
//        if(val != -1) {
//            bos.write(buf);
//        }
//        while (cis.read(b) >= 0) {
//            baos.write(b, 0, numBytes);
//        }
	}
	
	public void unZip(){
		String srcFile = "C:/Users/stefano.scaltriti/Git/FileSplitter"
	    		+ "/FileSplitter_Scaltriti_Federico/Muse.zip";
		String unzippedFile = "C:/Users/stefano.scaltriti/Git/FileSplitter"
	    		+ "/FileSplitter_Scaltriti_Federico/Muse.mp3";
		
		ZipInputStream zis = null;
		
		byte[] buffer = new byte[8*1024]; // 8kB
		
        try {
			FileOutputStream fos = new FileOutputStream(unzippedFile);
			FileInputStream fis = new FileInputStream(srcFile);
	        zis = new ZipInputStream(fis);
	        //for (int i=0; i < srcFile.length(); i++) {
	        zis.getNextEntry();
	        
	        int length;
            while ((length = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            zis.closeEntry();
            // close the InputStream
            fis.close();
            fos.close();
	        //}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
        
	}
}
