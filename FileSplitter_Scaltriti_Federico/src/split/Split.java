package split;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import static utils.Const.BUFF;

/**
 * Classe che implementa la divisione di un file specificando la dimensione di ogni file.
 * @see Runnable
 * */

public class Split implements Runnable{
	/**
	 * Stringhe rappresentanti rispettivamente: il nome del file (estensione compresa); 
	 * l'intero percorso in cui si trova il file desiderato; la cartella in cui verrà salvato
	 * */
	private String fullFileName, fullPath, destinationFolder;
	
	/**
	 * Long rappresentanti rispettivamente: la dimensione data da utente di ogni nuovo file;
	 * eventuali byte rimanenti da leggere; la dimensione del file iniziale;
	 * il numero di byte letti (posto inizialmente a zero).
	 * */
	private long bytesPerSplit, remainingBytes, sourceSize, readBytes = 0;
	
	/**
	 * RandomAccessFile per accedere al file iniziale
	 * */
	private RandomAccessFile raf;
	
	/**
	 * Intero utile per ricordare la scelta fatta nel momento della divisione.
	 * In base al valore che avrà verranno eseguite determinate operazioni.
	 * */
	private int decision;
	
	/**
	 * Costruttore dello splitter nel caso la dimensione massima di ogni file sia specificata.
	 * Chiamato in fase di divisione una volta presa la decisione di dividere i file scelti.
	 * @param fullFileName nome del file.
	 * @param fullPath percorso in cui si trova il file selezionato.
	 * @param destinationFolder cartella di destinazione del file una volta diviso.
	 * @param raf file da cui iniziare.
	 * @param bytesPerSplit dimensione data da utente di ogni nuovo file.
	 * @param sourceSize dimensione del file iniziale.
	 * */
    public Split(String fullFileName, String fullPath, String destinationFolder, RandomAccessFile raf, long bytesPerSplit, long sourceSize) {
		super();
		setFullFileName(fullFileName);
		setFullPath(fullPath);
		setDestinationFolder(destinationFolder);
		setRaf(raf);
		setBytesPerSplit(bytesPerSplit);
		setSourceSize(sourceSize);
	}
    
    /**
     * Secondo costruttore dello splitter nel caso la dimensione massima di ogni file sia specificata.
     * Chiamato nel momento in cui viene aggiunto un nuovo file, da processare, nella tabella
     * @param fullFileName nome del file.
     * @param fullPath percorso in cui si trova il file selezionato.
     * @param sourceSize dimensione del file iniziale.
     * @param decision scelta fatta nel momento della divisione.
     * */
    public Split(String fullFileName, String fullPath, long sourceSize, int decision) {
		super();
		setFullFileName(fullFileName);
		setFullPath(fullPath);
		setSourceSize(sourceSize);
		setDecision(decision);
	}

    /**
     * Metodo per leggere un certo quantitativo di bytes
     * @param bw stream da cui leggere i byte
     * @param numBytes il totale di bytes da leggere
     * */
	public void readWrite(BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = getRaf().read(buf);
        if(val != -1) {
            bw.write(buf);
        }
	}
	
	/**
	 * Metodo che implementa la divisione del file in dimensioni uguali (escluso l'ultimo).
	 * Durante la lettura, salva anche i bytes letti
	 * @param numSplits il numero di file che avranno la stessa dimensione
	 * */
	public void mainDivision(long numSplits) throws IOException{
		String newFullName = null;
		
		//operazioni da svolgere per ogni divisione risultante dal file iniziale
		for(int destIx = 1; destIx <= numSplits; destIx++) {	
            BufferedOutputStream bw = null;
            String fileFormat = null, simpleName = null;
           
			try { //instazio un nuovo BufferedOutputStream
				fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length()); //.formato
				simpleName = getFullFileName().substring(0, getFullFileName().lastIndexOf('.')); //Nome
				newFullName = simpleName+destIx+"D"+fileFormat; //NomeNumeroD.formato
				bw = new BufferedOutputStream(new FileOutputStream(getDestinationFolder()+newFullName));
			} catch (FileNotFoundException e) {
				System.err.println(getFullFileName()+" NOT FOUND !");
			}
			//se il numero di bytes per ogni file creato è più grande della dim. del buffer
            if(getBytesPerSplit() > BUFF) {
            	//calcolo quante letture dovrò fare per leggere tutto il file
                long numReads = getBytesPerSplit()/BUFF;
                long numRemainingRead = getBytesPerSplit() % BUFF;
                
                //leggo effettivamente i bytes
                for(int i=0; i<numReads; i++) {
                	readWrite(bw, BUFF);
                	setReadBytes(BUFF);
                }
                //controllo se sono stati tralasciati bytes in più
                if(numRemainingRead > 0) {
                	readWrite(bw, numRemainingRead);
                	setReadBytes(numRemainingRead);
                }
            //se il buffer è più largo della dimensione per ogni file diviso
            }else {
            	readWrite(bw, getBytesPerSplit());
            	setReadBytes(getBytesPerSplit());
            }
            //chiudo lo stream
            bw.close();
        }
	}
	
	
	/**
	 * Metodo invocato in caso siano rimasti dei bytes da leggere
	 * @param remainingBytes numero di bytes rimasti da leggere
	 * @param numSplits numero di file originati dalla divisione
	 * */
    public void checkFileRemaining(long remainingBytes, long numSplits) throws IOException{
    	String simpleName = null, fileFormat = getFullPath().substring(getFullPath().lastIndexOf("."), getFullPath().length());
    	simpleName = getFullFileName().substring(0, getFullFileName().lastIndexOf('.'));
		String newFullName = simpleName+(numSplits+1)+"D"+fileFormat;
		
		//inizializzo un nuovo BufferedOutputStream
    	BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(getDestinationFolder()+newFullName));
    	
    	//leggo i bytes rimasti e li aggiungo a quelli letti
    	readWrite(bw, remainingBytes);
    	setReadBytes(remainingBytes);
    	
    	//chiudo lo stream
    	bw.close();
    }
    
    /**
     * Metodo chiamato all'inizio della divisione.
     * Si occupa del calcolo di eventuali bytes per l'ultima divisione, 
     * del numero di file che avranno la stessa dimensione e di chiamare i metodi opportuni 
     * per effettuare le divisioni
     * */
	@Override
	public void run() {
		//considero eventuali bytes restanti per una divisione finale
    	long remainingBytes = getSourceSize() % getBytesPerSplit();
    	
    	long numSplits = getSourceSize()/getBytesPerSplit();
    	
    	try {
    		//attuo la divisione principale
			mainDivision(numSplits);
			
			//nel caso siano rimasti dei bytes da leggere, chiamo il metodo apposito
			if(remainingBytes > 0)
				checkFileRemaining(remainingBytes, numSplits);
			
			//chiudo il RandomAccessFile
			getRaf().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metodo getter per restituire il nome completo del file.
	 * @return {@link #getFullFileName()}
	 * */
	public String getFullFileName() {
		return fullFileName;
	}

	/**
	 * Metodo setter per impostare il nome completo del file.
	 * */
	public void setFullFileName(String fullFileName) {
		this.fullFileName = fullFileName;
	}

	/**
	 * Metodo getter per restituire il percorso completo del file.
	 * @return {@link #fullPath}
	 * */
	public String getFullPath() {
		return fullPath;
	}

	/**
	 * Metodo setter per impostare il percorso completo del file.
	 * */
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	/**
	 * Metodo getter per restituire il file iniziale.
	 * @return {@link #raf}
	 * */
	public RandomAccessFile getRaf() {
		return raf;
	}
	
	/**
	 * Metodo setter per impostare il file iniziale.
	 * */
	public void setRaf(RandomAccessFile raf) {
		this.raf = raf;
	}

	/**
	 * Metodo getter per restituire la dimensione di ogni file, data da utente.
	 * @return {@link #bytesPerSplit}
	 * */
	public long getBytesPerSplit() {
		return bytesPerSplit;
	}

	/**
	 * Metodo setter per impostare la dimensione di ogni file, data da utente.
	 * */
	public void setBytesPerSplit(long bytesPerSplit) {
		this.bytesPerSplit = bytesPerSplit;
	}

	/**
	 * Metodo getter per restituire eventuali bytes ancora da leggere.
	 * @return {@link #remainingBytes}
	 * */
	public long getRemainingBytes() {
		return remainingBytes;
	}

	/**
	 * Metodo setter per impostare il numero di eventuali bytes ancora da leggere.
	 * */
	public void setRemainingBytes(long remainingBytes) {
		this.remainingBytes = remainingBytes;
	}

	/**
	 * Metodo getter per restituire la dimensione del file iniziale.
	 * @return {@link #sourceSize}
	 * */
	public long getSourceSize() {
		return sourceSize;
	}

	/**
	 * Metodo setter per impostare la dimensione del file iniziale.
	 * */
	public void setSourceSize(long sourceSize) {
		this.sourceSize = sourceSize;
	}

	/**
	 * Metodo getter per restituire la cartella di destinazione di ogni file ottenuto dalla divisione.
	 * @return {@link #destinationFolder}
	 * */
	public String getDestinationFolder() {
		return destinationFolder;
	}

	/**
	 * Metodo setter per impostare la cartella di destinazione di ogni file ottenuto dalla divisione.
	 * */
	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	/**
	 * Metodo getter per restituire la decisione presa per la divisione.
	 * @return {@link #decision}
	 * */
	public int getDecision() {
		return decision;
	}

	/**
	 * Metodo setter per impostare la decisione presa per la divisione.
	 * */
	public void setDecision(int decision) {
		this.decision = decision;
	}

	/**
	 * Metodo getter per restituire quanti bytes sono stati letti.
	 * @return {@link #readBytes}
	 * */
	public long getReadBytes() {
		return readBytes;
	}

	/**
	 * Metodo setter per incrementare il numero di bytes letti.
	 * */
	public void setReadBytes(long readBytes) {
		this.readBytes += readBytes;
	}
}
