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


/**
 * Classe dedicata all'unione dei file divisi precedentemente
 * */
public class Merger{
	
	/**
	 * Stringhe rappresentanti rispettivamente: il nome del file in ingresso, il formato del file,
	 * il percorso complessivo che avrà il nuovo file e la cartella di destinazione in cui verrà salvato
	 * */
	private String fileName, fileFormat, destination, filePath;
	
	/**
	 * Chiave di cifratura costituita da un array di byte processati con un algoritmo specificato
	 * */
	private SecretKeySpec secretKey;


	/**
	 * Costruttore utilizzato al momento di attuare l'unione effettiva
	 * @param fileName il nome del file che è stato selezionato
	 * @param fileFormat il formato del file che è stato selezionato
	 * @param destination la cartella di destinazione in cui verrà salvato il nuovo file
	 * */
	public Merger(String fileName, String fileFormat, String destination){
		setFileName(fileName);
		setFileFormat(fileFormat);
		setDestination(destination);
	}
	
	/**
	 * Costruttore utilizzato al momento dell'aggiunta nella tabella
	 * @param fileName il nome del file che è stato selezionato
	 * @param filePath l'indirizzo finale in cui si andrà a salvare il nuovo file
	 * */
	public Merger(String fileName, String filePath){
		setFileName(fileName);
		setFilePath(filePath);
	}
	
	
	/**
	 * Metodo utilizzato per impostare la chiave di cifratura per decriptare i file separati
	 * @param passwd la password immessa da utente
	 * */
	private void setKey(String passwd){
		byte[] hash;
        
        MessageDigest sha = null; //Funzione di hash sicura che restituisce un valore di hash di lungezza fissata
		try {
			sha = MessageDigest.getInstance("SHA-1"); 	//imposto la funzione di hash desiderata
			hash = passwd.getBytes("UTF-8"); 		//Codifico la password in una serie di bytes usando il charset dato, 
													//salvando il risultato dentro un nuovo byte array
			hash = sha.digest(hash);				// aggiornamento della funzione di hash usando l'array di bytes dato
			hash = Arrays.copyOf(hash, 16);			//rendo la funzione di hash lunga 16 bytes
			secretKey = new SecretKeySpec(hash, "AES"); //imposto la chiave di cifratura data la funzione di hash e l'algoritmo scelto
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Metodo per la lettura da un InputStream nell'OutputStream desiderato per i file compressi
	 * @param zis ZipInputStream da cui leggere i dati
	 * @param fos FileOutputStream in cui scrivere i dati letti
	 * @param numBytes numeto di bytes da andare a leggere
	 * @throws IOException dovuta ad un'eventuale errore in lettura o scrittura dei dati
	 * */
	public void readWriteZip(ZipInputStream zis, FileOutputStream fos, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = zis.read(buf);
        if(val != -1) {
            fos.write(buf);
        }
	}
	
	/**
	 * Metodo per la lettura da un InputStream nell'OutputStream desiderato per un file diviso senza compressione
	 * @param fis FileInputStream da cui leggere i dati
	 * @param bos BufferedOutputStream in cui scrivere i dati letti
	 * @param numBytes numeto di bytes da andare a leggere
	 * @throws IOException dovuta ad un'eventuale errore in lettura o scrittura dei dati
	 * */
	public void readWrite(FileInputStream fis, BufferedOutputStream bos, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = fis.read(buf);
        if(val != -1) {
            bos.write(buf);
        }
	}
	
	/**
	 * Metodo usato per leggere i byte nel caso si tratti di un file criptato
	 * @param cis InputStream da cui leggere i bytes
	 * @param bos OutputStream in cui scrivere i bytes
	 * @param numBytes numeto di bytes da leggere
	 * */
	public void writeCrypt(CipherInputStream cis, BufferedOutputStream bos, long numBytes) throws IOException{
        byte[] b = new byte[(int) numBytes];
        int cont;


        cont = cis.read(b, 0, b.length); //i bytes letti veramente
        int totCont = b.length / cont; 	//quante letture dovrò fare
        
        for (int i = 1; i < totCont; i++) {
        	bos.write(b, 0, cont);
        	System.out.println("cont: "+cont);
        	cont = cis.read(b, 0, b.length);
		}
        
	}
	
	/**
	 * Metodo usato per decifrate la password data da utente
	 * @param originalDirectory cartella originale del file criptato
	 * @param passwd la password immessa da utente
	 * */
	public void decrypt(String originalDirectory, String passwd) throws IOException{
		byte[] buf = new byte[(int) BUFF];
		BufferedOutputStream bos = null;
		FileInputStream fis = null;
		Cipher cipher = null;
		
        setKey(passwd); //imposto la chiave da usare per decifrare
        
        File f = new File(originalDirectory+fileName+fileFormat); //Nome1Lettera+.formato
        bos = new BufferedOutputStream(new FileOutputStream(getDestination()+fileName.substring(0, fileName.lastIndexOf("1"))+fileFormat));
        int c = 1;
        char letter = getFileName().charAt(getFileName().length()-1);
        CipherInputStream in = null;
        
        byte[] iv = null;		//array di byte per l'IV (vettore di inizializzazione) 
        						//utilizzato per inizializzare lo stato di un cifrario
        
        while (f.exists()){	
        	fis = new FileInputStream(originalDirectory+f.getName());
        	
        	//se si è alla prima lettura, catturo l'IV e imposto il cipher e il CipherInputStream
        	if (c == 1){
        		iv = new byte[16];
        		fis.read(iv, 0, iv.length); //carico in iv i 16 byte
        		IvParameterSpec ivspec = new IvParameterSpec(iv);
        		try {
        			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //imposto il cifratore come in fase di criptazione
        			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec); 
        		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
        			e.printStackTrace();
        		}
        	}
        	in = new CipherInputStream(fis, cipher);
        		
        	if (f.length() > buf.length){ //se i byte complessivi da leggere sforano la dimensione del buffer
        		long numReads = f.length() / buf.length;
            	long bytesRemaining = f.length() % buf.length;
            	for(int i = 0; i<numReads; i++) {
            		writeCrypt(in, bos, buf.length);
                }
            	
                if(bytesRemaining > 0)
                	writeCrypt(in, bos, bytesRemaining);
        	}else
        		writeCrypt(in, bos, f.length());
            
        	in.close();
        	f = new File(originalDirectory+getFileName().substring(0, getFileName().lastIndexOf("1"))+(++c)+letter+getFileFormat());
        	fis.close();
        }
        //chiusura stream
		bos.close();
	}
	
	
	/**
	 * Metodo usato per l'unione, in caso di file divisi data una dimensione (lettera 'D') o in base
	 * a quanti file si ha voluto dividere il file originale (lettera 'T').
	 * @param originalDirectory cartella in cui sono contenuti i file da unire
	 * @throws IOException in caso di errore di inizializzazione degli stream o lettura di dati
	 * */
	public void merge(String originalDirectory) throws IOException{
        FileInputStream fis; 
        
        int c = 1;
        File f = new File(originalDirectory+getFileName()+getFileFormat()); //directory+Nome1Lettera+.formato				
        String realName = getFileName().substring(0, getFileName().lastIndexOf("1"));	//nome effettivo del file
        
        BufferedOutputStream bos = null;													
        bos = new BufferedOutputStream(new FileOutputStream(getDestination()+realName+getFileFormat())); 
        byte[] buf = new byte[BUFF]; 																
        
        char letter = getFileName().charAt(getFileName().length()-1);								
        
        //finché il file da leggere esiste
        while (f.exists()){																			
        	fis = new FileInputStream(originalDirectory+f.getName());	
        	
        	if (f.length()>buf.length){ 	//in caso la dimensione del file sia maggiore di quella del buffer
        		long numReads = f.length()/buf.length; 	//numero di letture di buf.length bytes
            	long bytesRemaining = f.length()%buf.length; //eventuali byte rimasti
            	for(int i=0; i<numReads; i++) {
            		readWrite(fis, bos, buf.length);													
                }
                if(bytesRemaining > 0) {
                	readWrite(fis, bos, bytesRemaining);
                }
        	}
        	f = new File(originalDirectory+getFileName().substring(0, getFileName().lastIndexOf("1"))+(++c)+letter+getFileFormat());
        	fis.close();
        }
        //chiusura del flusso
        bos.close();
    }
	
	/**
	 * Metodo per l'unione di file in caso di divisione compressa
	 * @param originalDirectory cartella in cui sono contenuti i file da unire
	 * @throws IOException in caso di errore di inizializzazione degli stream o lettura di dati
	 * */
	public void mergeZip(String originalDirectory) throws IOException{
        FileInputStream fis = null; 
        ZipInputStream zis = null;
        
        int c = 1;
        File f = new File(originalDirectory+fileName+fileFormat); //directory+NomeFile1Lettera+.formato				
        String realName = fileName.substring(0, fileName.lastIndexOf("1"));			
        FileOutputStream fos = null;													
        fos = new FileOutputStream(getDestination()+realName+fileFormat);
        byte[] buf = new byte[BUFF]; 														
        
        char letter = getFileName().charAt(getFileName().length()-1);								
        
        //finché il file da leggere esiste
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

	/**
	 * Metodo getter per il nome del file
	 * @return {@link #fileName}
	 * */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Metodo setter per il nome del file
	 * @param fileName il nome del file
	 * */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Metodo getter per il format del file
	 * @return {@link #fileFormat}
	 * */
	public String getFileFormat() {
		return fileFormat;
	}

	/**
	 * Metodo setter per il formato del file
	 * @param fileFormat il formato del file
	 * */
	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	/**
	 * Metodo getter per il percorso nel file system del file
	 * @return {@link #filePath}
	 * */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Metodo setter per il percorso del file
	 * @param filePath il percorso del file
	 * */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Metodo getter per la cartella di destinazione del file unito
	 * @return {@link #destination}
	 * */
	public String getDestination() {
		return destination;
	}

	/**
	 * Metodo setter per la cartella di destinazione del file unito
	 * @param destination la cartella di destinazione del file unito
	 * */
	public void setDestination(String destination) {
		this.destination = destination;
	}
}
