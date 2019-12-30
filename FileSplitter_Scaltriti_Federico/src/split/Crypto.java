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
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import static utils.Const.BUFF;

public class Crypto extends Split implements Runnable{
	
	// Create Cipher instance and initialize it to encryption mode
    private Cipher cipher = null;
    private SecretKeySpec secretKey = null;
    IvParameterSpec ivspec;

	public Crypto(String fileName, String fullPath, RandomAccessFile raf, long numSplits, long bytesPerSplit, long sourceSize) {
		super(fileName, fullPath, raf, numSplits, bytesPerSplit, sourceSize);
	}
	
	private void setKey(String passwd){
		byte[] hash;
        MessageDigest sha = null;
        
		try {
			sha = MessageDigest.getInstance("SHA-1"); //imposto il metodo di cifratura
			hash = passwd.getBytes("UTF-8"); //imposto il tipo di encoding
			hash = sha.digest(hash); //aggiorno il MessageDigest da usare per la chiave
			hash = Arrays.copyOf(hash, 16); //per una questione di coerenza di dimensione del MessageDigest
			secretKey = new SecretKeySpec(hash, "AES"); //imposto la nuova chiave
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] setCipher(String name) throws FileNotFoundException{
		SecureRandom srandom = new SecureRandom(); //per generare l'IV
		byte[] iv = new byte[16];
		srandom.nextBytes(iv); //genero una serie random di interi che costiscono l'IV
		ivspec = new IvParameterSpec(iv); //per inizializzare il vettore
		
        try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //inizializzo il tipo di cifratore
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec); // transformazione dell'algoritmo
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
        return iv;
	}
	
	public void readWrite(CipherOutputStream cos, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        
        int cont;
        if((cont = getRaf().read(buf, 0, buf.length)) != -1)
        	cos.write(buf, 0, cont);
        System.out.println("cont: "+cont);
	}
	
	@Override
	public void mainDivision(long numSplits, long bytesPerSplit, long sourceSize) throws IOException{
	    String name = null;
	    CipherOutputStream cos = null;
	    FileOutputStream fos = null;
	    int read = 0;
	    byte[] iv = null;
	    String fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
	  
	    //imposto il chiper restituendomi l'IV impostato
	    iv = setCipher(name);
	    
		for(int destIx = 1; destIx <= numSplits; destIx++) {
            name = getFileName()+destIx+"C"+fileFormat;//Muse1C.mp3
            System.out.println("name: "+name);
			try {
				fos = new FileOutputStream(name);
				
				cos = new CipherOutputStream(fos, cipher);
				
			} catch (FileNotFoundException e) {
				System.err.println(getFileName()+fileFormat+" NOT FOUND !");
			}
			
			byte[] buf = new byte[(int) bytesPerSplit];
			int readTot = 0;
			
			//se è alla 1^ lettura, leggo come prima cosa l'IV
			if (destIx == 1)
				fos.write(iv, 0, 16);
			
			//while((readTot) < bytesPerSplit){
			read = getRaf().read(buf);
			System.out.println("read: "+read);
			readTot+=read;
            //}
			cos.write(buf, 0, readTot);	
			
            //
            //fos.flush();
            //cos.close();
//            if(bytesPerSplit > maxReadBufferSize) {
//                long numReads = bytesPerSplit/maxReadBufferSize;
//                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
//                for(int i=0; i<numReads; i++) {
//                	readWrite(cos, maxReadBufferSize);
//                }
//                if(numRemainingRead > 0) {
//                	readWrite(cos, numRemainingRead);
//                }
//            }else {
//            	readWrite(cos, bytesPerSplit);
//            }
            
        }
		//getRaf().close();
		
		//chiusura dei flussi
		cos.flush();
        cos.close();
        fos.close();
	}
	
	@Override
    public void checkFileRemaining(long remainingBytes, long numSplits) throws IOException{
		String fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
		String name = getFileName()+(numSplits+1)+"C"+fileFormat;
    	CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(name), cipher);
    	System.out.println("remainingBytes: "+remainingBytes);
    	int read;
    	byte[] buf = new byte[(int) remainingBytes];
    	
    	while((read = getRaf().read(new byte[(int) remainingBytes])) != -1){
  		  cos.write(buf, 0, read);
        }
    	cos.flush();
    	cos.close();
    }
	
	@Override
	public void run(){
		String passwd = null;
		
		//chiedo all'utente di inserire una password
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
	       System.out.print("Inserire la password: ");
	    try {
			passwd = buffer.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	 // imposto la chiave data
	    setKey(passwd);
	    
	    //effettuo le divisioni
    	try {
			mainDivision(getNumSplits(), getBytesPerSplit(), getSourceSize());
			
			long remainingBytes = getSourceSize() % getBytesPerSplit();
	    	
	    	//controllo se sono rimasti dei bytes da leggere
	    	if(remainingBytes > 0)
	    		checkFileRemaining(remainingBytes, getNumSplits());
	    	
	        getRaf().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	@Override
//	public void action(long numSplits, long bytesPerSplit, long sourceSize) throws IOException{
//		String passwd = null;
//		
//		//chiedo all'utente di inserire una password
//		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
//	       System.out.print("Inserire la password: ");
//	    try {
//			passwd = buffer.readLine();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	 // imposto la chiave data
//	    setKey(passwd);
//	    
//	    //effettuo le divisioni
//    	mainDivision(numSplits, bytesPerSplit, sourceSize);
//    	
//    	long remainingBytes = sourceSize % bytesPerSplit;
//    	
//    	//controllo se sono rimasti dei bytes da leggere
//    	if(remainingBytes > 0)
//    		checkFileRemaining(remainingBytes, numSplits);
//    	
//        getRaf().close();
//    }

}
