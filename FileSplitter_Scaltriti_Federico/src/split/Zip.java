package split;

import java.io.BufferedOutputStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static utils.Const.*;


/**
 * Classe che implementa la divisione di un file comprimendo il relativo contenuto.
 * È sottoclasse di {@link Split}
 * @see Runnable
 * */

public class Zip extends Split implements Runnable{
    
	
	/**
	 * Costruttore dello splitter nel caso si voglia attuare la crittografia.
	 * Chiamato in fase di divisione una volta presa la decisione di dividere i file scelti.
	 * @param fileName nome del file.
	 * @param fullPath percorso in cui si trova il file selezionato.
	 * @param destinationFolder cartella di destinazione del file una volta diviso.
	 * @param raf file da cui iniziare.
	 * @param bytesPerSplit dimensione data da utente di ogni nuovo file.
	 * @param sourceSize dimensione del file iniziale.
	 * */
	public Zip(String fileName, String fullPath, String destinationFolder, RandomAccessFile raf, long bytesPerSplit, long sourceSize) {
		super(fileName, fullPath, destinationFolder, raf, bytesPerSplit, sourceSize);
	}
	
	/**
	 * {@inheritDoc}
	 * */
	@Override
	public void mainDivision(long numSplits) throws IOException{
		long numReads, numRemainingRead;
		String newFullName = null;
		String fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
		
		for(int destIx=1; destIx <= numSplits; destIx++) {	
            ZipOutputStream zos = null;
			try {
				newFullName = getFullFileName()+destIx+"Z"+fileFormat;
				zos = new ZipOutputStream(new FileOutputStream(getDestinationFolder()+newFullName));
				
				// inizio scrivendo una nuova ZIP entry, posiziono lo stream all'inizio dell'entry data
		        zos.putNextEntry(new ZipEntry(getDestinationFolder()+newFullName));
			} catch (FileNotFoundException e) {
				System.err.println(newFullName+" NOT FOUND !");
			}
            if(getBytesPerSplit() > BUFF) {
                numReads = getBytesPerSplit()/BUFF;
                numRemainingRead = getBytesPerSplit() % BUFF;
                for(int i=0; i<numReads; i++) {
                	readWrite(zos, BUFF);
                	setReadBytes(BUFF);
                }
                if(numRemainingRead > 0) {
                	readWrite(zos, numRemainingRead);
                	setReadBytes(numRemainingRead);
                }
	            
            }else {
            	readWrite(zos, getBytesPerSplit());
            	setReadBytes(getBytesPerSplit());
            }
            zos.close();
        }
	}
	
	/**
	 * {@inheritDoc}
	 * */
	@Override
    public void checkFileRemaining(long remainingBytes, long numSplits) throws IOException{
		String fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
		String newFullName = getFullFileName()+(++numSplits)+"Z"+fileFormat;
		
		System.out.println("newFullName: "+newFullName);
		
		//imposto un nuovo BufferedOutputStream
    	ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(getDestinationFolder()+newFullName));
    	zos.putNextEntry(new ZipEntry(getDestinationFolder()+newFullName));
    	readWrite(zos, remainingBytes); 	//leggo i bytes richiesti
    	setReadBytes(remainingBytes);	//li aggiungo al totale dei bytes letti
    	zos.flush();
    	zos.close();						//chiudo lo stream
    }
	
	/**
	 * Metodo per leggere un certo quantitativo di bytes
     * @param zos stream compresso in cui scrivere i byte
     * @param numBytes il totale di bytes da leggere
	 * */
	public void readWrite(ZipOutputStream zos, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = getRaf().read(buf);
        if(val != -1) {
            zos.write(buf);
        }
	}
	
	/**
	 * {@inheritDoc}
	 * */
	@Override
	public void run() {
    	long remainingBytes = getSourceSize() % getBytesPerSplit(); //considero eventuali bytes restanti per una divisione finale
    	
    	try {
    		long numSplits = getSourceSize()/getBytesPerSplit();
			mainDivision(numSplits); 		//attuo la divisione principale
			
			if(remainingBytes > 0){ 		//controllo la presenza di bytes rimanenti
				System.err.println("Ci entra");
				checkFileRemaining(remainingBytes, numSplits);
			}			
			
			getRaf().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}