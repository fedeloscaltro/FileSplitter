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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static utils.Const.*;

public class Merger{
	private String fileName,fileFormat, destination;
	private SecretKeySpec secretKey;
	private String filePath;
	private long size;
	
	public Merger(String fileName, String fileFormat, String destination){ //TODO aggiungi il destinationFolder
		setFileName(fileName);
		setFileFormat(fileFormat);
		setDestination(destination);
	}
	
	public Merger(String fileName, String filePath){
		setFileName(fileName);
		setFilePath(filePath);
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
	public void readWriteZip(ZipInputStream zis, FileOutputStream bos, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = zis.read(buf);
        if(val != -1) {
            bos.write(buf);
        }
	}
	
	public void readWrite(FileInputStream fis, BufferedOutputStream bos, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = fis.read(buf);
        if(val != -1) {
            bos.write(buf);
        }
	}
	
	
	public void decrypt(String originalDirectory, String passwd) throws IOException{//String fileName
		byte[] buf = new byte[(int) BUFF];
		BufferedOutputStream bos = null;
		FileInputStream fis = null;
		Cipher cipher = null;
		
        setKey(passwd);
        
        File f = new File(originalDirectory+fileName+fileFormat); //Muse1D+.mp3
        System.out.println("fileName: "+fileName+"\nfileFormat: "+fileFormat);
        bos = new BufferedOutputStream(new FileOutputStream(getDestination()+fileName.substring(0, fileName.lastIndexOf("1"))+fileFormat));
        int c = 1;
        char letter = getFileName().charAt(getFileName().length()-1);
        CipherInputStream in = null;
        /*----------------------------------------------------*/
        byte[] iv = null;
        
        while (f.exists()){	
        	fis = new FileInputStream(originalDirectory+f.getName());
        	
        	//se si è alla prima lettura, catturo l'iv e setto il cipher e il CipherInputStream
        	if (c == 1){
        		iv = new byte[16];
        		fis.read(iv, 0, iv.length); //carico in iv i 16 byte
        		IvParameterSpec ivspec = new IvParameterSpec(iv);
        		try {
        			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
        		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
        			e.printStackTrace();
        		}
        	}
        	in = new CipherInputStream(fis, cipher);
        		
        	if (f.length() > buf.length){
        		long numReads = f.length() / buf.length;
            	long bytesRemaining = f.length() % buf.length;
            	System.out.println("numReads: "+numReads+" bytesRemaining: "+bytesRemaining);
            	for(int i = 0; i<numReads; i++) {
            		writeCrypt(in, bos, buf.length);
                }
            	System.out.println("bytesRemaining: "+bytesRemaining);
                if(bytesRemaining > 0)
                	writeCrypt(in, bos, bytesRemaining);
        	}else
        		writeCrypt(in, bos, f.length());
        	
//        	int cont;
//            
//            while((cont = in.read(buf)) >= 0) {
//                bos.write(buf, 0, cont);
//                System.out.println("contD: "+cont);
//            }
            
        	in.close();
        	f = new File(originalDirectory+fileName.substring(0, fileName.lastIndexOf("1"))+(++c)+letter+fileFormat);
        }
        fis.close();
		bos.close();
	}
	
	public void writeCrypt(CipherInputStream cis, BufferedOutputStream bos, long numBytes) throws IOException{
        byte[] b = new byte[(int) numBytes];
        System.out.println("b.length(): "+b.length);
        int cont, contTot = 0;
        
//        cont = cis.read(b, 0, b.length);
//        contTot+= cont;
        cont = cis.read(b, 0, b.length);
        int totCont = b.length / cont;
        
        for (int i = 1; i < totCont; i++) {
        	bos.write(b, 0, cont);
        	System.out.println("cont: "+cont);
        	cont = cis.read(b, 0, b.length);
		}
        	
        	
        	//contTot+= cont;
        //}
        
//        if((cont = cis.read(b, 0, b.length)) >= 0) {
//        		System.out.println("cont: "+cont);
//        		bos.write(b, 0, cont);
//        }
        
	}
	
	public void merge(String originalDirectory) throws IOException{
        FileInputStream fis; 
        
        int c = 1;
        File f = new File(originalDirectory+fileName+fileFormat); //directory+Muse1D+.mp3				
        String realName = fileName.substring(0, fileName.lastIndexOf("1"));							
        BufferedOutputStream bos = null;													
        bos = new BufferedOutputStream(new FileOutputStream(getDestination()+realName+fileFormat)); 
        byte[] buf = new byte[BUFF]; 																
        
        char letter = getFileName().charAt(getFileName().length()-1);								
        
        while (f.exists()){																			
        	fis = new FileInputStream(originalDirectory+f.getName());							
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
        	
        	f = new File(originalDirectory+fileName.substring(0, fileName.lastIndexOf("1"))+(++c)+letter+fileFormat);
        }
        
        bos.close();
    }
	
	public void mergeZip(String originalDirectory) throws IOException{
        FileInputStream fis = null; 
        ZipInputStream zis = null;
        
        int c = 1;
        File f = new File(originalDirectory+fileName+fileFormat); //directory+Muse1D+.mp3				
        String realName = fileName.substring(0, fileName.lastIndexOf("1"));			
        FileOutputStream fos = null;													
        fos = new FileOutputStream(getDestination()+realName+fileFormat); //same
        byte[] buf = new byte[BUFF]; 														
        
        char letter = getFileName().charAt(getFileName().length()-1);								
        
        while (f.exists()){																			
        	fis = new FileInputStream(originalDirectory+f.getName());
        	zis = new ZipInputStream(fis);
        	zis.getNextEntry();
        	
        	int length;
            while ((length = zis.read(buf)) > 0) {
                fos.write(buf, 0, length);
            }
            
            zis.closeEntry();
        	fis.close();
        	f = new File(originalDirectory+fileName.substring(0, fileName.lastIndexOf("1"))+(++c)+letter+fileFormat);
        }
        
        fos.close();
    }

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
}
