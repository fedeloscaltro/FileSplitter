package split;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Classe che implementa la divisione di un file crittografandone il contenuto.
 * È sottoclasse di {@link Split}
 * @see Runnable
 * */
public class Crypto extends Split implements Runnable{
	
	/**
	 * Oggetto cifratore
	 * @see Cipher
	 * */
    private Cipher cipher = null;
    
    /**
     * Generatore della chiave segreta
     * @see SecretKeySpec
     * */
    private SecretKeySpec secretKey = null;
    
    /**
     * Initialization Vector (IV) necessario per crittografare in modalità CBC
     * @see IvParameterSpec
     * */
    private IvParameterSpec ivspec;
    
    /**
     * Array di char contenente la password scelta
     * */
    private char[] psswd;
    
    /**
	 * Costruttore dello splitter nel caso si voglia attuare la crittografia.
	 * Chiamato in fase di divisione una volta presa la decisione di dividere i file scelti.
	 * @param fileName nome del file.
	 * @param fullPath percorso in cui si trova il file selezionato.
	 * @param destinationFolder cartella di destinazione del file una volta diviso.
	 * @param raf file da cui iniziare.
	 * @param bytesPerSplit dimensione data da utente di ogni nuovo file.
	 * @param sourceSize dimensione del file iniziale.
	 * @param psswd password inserita da utente.
	 * */
	public Crypto(String fileName, String fullPath, String destinationFolder, RandomAccessFile raf, long bytesPerSplit, long sourceSize, char[] psswd) {
		super(fileName, fullPath, destinationFolder, raf, bytesPerSplit, sourceSize);
		this.psswd = psswd;
	}
	
	/**
	 * Metodo per impostare la chiave di cifratura
	 * @param passwd la password inserita da utente
	 * */
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
	
	/**
	 * Metodo per impostare l'oggetto cifratore
	 * @throws FileNotFoundException
	 * */
	public byte[] setCipher() throws FileNotFoundException{
		SecureRandom srandom = new SecureRandom(); 		//per generare l'IV
		byte[] iv = new byte[16];
		srandom.nextBytes(iv); 							//genero una serie random di interi che costiscono l'IV
		ivspec = new IvParameterSpec(iv); 				//per inizializzare il vettore
		
        try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); 	//inizializzo il tipo di cifratore
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec); 	// transformazione dell'algoritmo
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
        return iv;
	}
	
	/**
	 * Metodo per leggere un certo quantitativo di bytes
     * @param cos stream cifrato da cui leggere i byte
     * @param numBytes il totale di bytes da leggere
	 * */
	public void readWrite(CipherOutputStream cos, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        
        int cont;
        if((cont = getRaf().read(buf, 0, buf.length)) != -1)
        	cos.write(buf, 0, cont);
	}
	
	/**
	 * {@inheritDoc}
	 * */
	@Override
	public void mainDivision(long numSplits) throws IOException{
	    String newFullName = null;
	    CipherOutputStream cos = null;
	    FileOutputStream fos = null;
	    int read = 0;
	    byte[] iv = null;
	    String fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length()); //.Formato
	    String simpleName = getFullFileName().substring(0, getFullFileName().lastIndexOf('.')); //NomeFile
	    
	    //imposto il chiper restituendomi l'IV impostato
	    iv = setCipher();
	    
		for(int destIx = 1; destIx <= numSplits; destIx++) {
            newFullName = simpleName+destIx+"C"+fileFormat;		//NomeFileNumeroC.Formato
            
			try {
				fos = new FileOutputStream(getDestinationFolder()+newFullName);
				
				cos = new CipherOutputStream(fos, cipher);
				
			} catch (FileNotFoundException e) {
				System.err.println(getFullFileName()+" NOT FOUND !");
			}
			
			byte[] buf = new byte[(int) getBytesPerSplit()];
			int readTot = 0;
			
			if (destIx == 1) 			//se è alla 1^ lettura, leggo come prima cosa l'IV
				fos.write(iv, 0, 16);
			
			
			read = getRaf().read(buf);
			readTot+=read;
           
			setReadBytes(read);
			cos.write(buf, 0, readTot);	
            
        }
		//chiusura dei flussi
		cos.flush();
        cos.close();
        fos.close();
	}
	
	/**
	 * {@inheritDoc}
	 * */
	@Override
    public void checkFileRemaining(long remainingBytes, long numSplits) throws IOException{
		String fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
		String simpleName = getFullFileName().substring(0, getFullFileName().lastIndexOf('.')); //Muse
		String newFullName = simpleName+(numSplits+1)+"C"+fileFormat;
    	CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(getDestinationFolder()+newFullName), cipher);

    	int read;
    	byte[] buf = new byte[(int) remainingBytes];
    	
    	while((read = getRaf().read(new byte[(int) remainingBytes])) != -1){
  		  cos.write(buf, 0, read);
  		  setReadBytes(read);
        }
    	cos.flush();
    	cos.close();
    }
	
	/**
	 * {@inheritDoc}
	 * */
	@Override
	public void run(){
		String passwd = null;
		passwd = new String(psswd);
		
	    setKey(passwd); 				// imposto la chiave data
	    
    	try {							//effettuo le divisioni
    		long numSplits = getSourceSize()/getBytesPerSplit();
    		
			mainDivision(numSplits);
			
			long remainingBytes = getSourceSize() % getBytesPerSplit();
	    	
	    	if(remainingBytes > 0)		//controllo se sono rimasti dei bytes da leggere
	    		checkFileRemaining(remainingBytes, numSplits);
	    	
	        getRaf().close();			//chiusura del RandomAccessFile
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
