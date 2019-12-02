package principal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.ZipOutputStream;

import javax.swing.*;

import split.Crypto;
import split.Split;
import split.SplitTimes;
import split.Zip;

public class Main {

	public static void main(String[] args) throws Exception{
		// TODO RIPRISTINA LA GRAFICA
		/*JFrame frame = new JFrame("File Splitter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabPane = new JTabbedPane();
		
		Panel splitPanel = new Panel();
		Panel mergePanel = new Panel();
		
		tabPane.setName("Split");
		tabPane.addTab("Split", splitPanel);
		tabPane.addTab("Merge", mergePanel);
		
		frame.setBounds(0, 0, 300, 150);
				
		frame.add(tabPane);
		
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);*/
		
		//TODO modifica filePath con i dati del file presi dal bottone
		String filePath = "Muse.mp3";
		String zipExtension = ".zip";
		String fileFormat = filePath.substring(filePath.indexOf("."), filePath.length());
        String fileName = filePath.substring(filePath.indexOf("/")+1, filePath.indexOf("."));
        
        int action = 2;
        
        RandomAccessFile raf = null;
        long sourceSize = 0;
        raf =  new RandomAccessFile(filePath, "r");
        sourceSize = raf.length();
        long bytesPerSplit, remainingBytes, numSplits;

        int maxReadBufferSize = 8 * 1024; //8KB
        
        
        
        switch(action){
        	case 1: //Divisione in più parti specificando la DIMENSIONE di ogni parte (default)
        		bytesPerSplit = 102400000; //100 mln B
        		numSplits = sourceSize/bytesPerSplit;
        		System.out.println("1 - numSplits: "+numSplits);
        		Split sd = new Split(fileName, fileFormat, raf);
                sd.action(numSplits, bytesPerSplit, maxReadBufferSize, sourceSize);
        		break;
        		
        	case 2: //Divisione in più parti specificando la dimensione di ogni parte e 
        		//crittografando il contenuto dei file generati tramite una chiave 
        		//(che può essere la stessa per tutti i file)
        		bytesPerSplit = 10240000; //100 mln B
        		numSplits = sourceSize/bytesPerSplit;
        		Crypto cp = new Crypto(fileName, fileFormat, raf);
        		cp.action(numSplits, bytesPerSplit, maxReadBufferSize, sourceSize);
        		break;
        		
        	case 3://Divisione in più parti specificando la DIMENSIONE di ogni parte e 
        		//COMPRIMENDO il contenuto dei file generati
        		
        		bytesPerSplit = 102400000; //100 kB
        		
        		Zip.compress();
        		
        		filePath = fileName+zipExtension;
        		RandomAccessFile raff = new RandomAccessFile(filePath, "r");
        		long sourceSizee = raff.length();
        		numSplits = sourceSizee/bytesPerSplit;
        		
        		Zip sz = new Zip(fileName, zipExtension, raff);
        		sz.action(numSplits, bytesPerSplit, maxReadBufferSize, sourceSizee);
        		
        		//deleting the main zipped file
        		File file = new File(fileName+zipExtension);
        		if(file.delete()){
        			System.out.println(file.getName() + " is deleted!");
        		}else{
        			System.err.println("Delete operation failed");
        		}
        		
        		break;
        		
        	case 4: //Divisione in più parti specificando il NUMERO DI PARTI
        		numSplits = 6; //from user input, extract it from args
        		bytesPerSplit = sourceSize/numSplits;
                remainingBytes = sourceSize % numSplits;
        		SplitTimes st = new SplitTimes(fileName, fileFormat, raf);
        		st.action(numSplits, bytesPerSplit, maxReadBufferSize, sourceSize);
        		break;
        }
    }

}
