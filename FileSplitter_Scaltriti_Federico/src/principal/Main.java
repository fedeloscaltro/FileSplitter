package principal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.ZipOutputStream;

import javax.swing.*;

import merge.Merger;
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
		String fullPath = null, directory = null, fileFormat = null, fileName = null;
		String zipExtension = ".zip";
		RandomAccessFile raf = null;
        
        int decision, action = 2;
        decision = 1;
        char letter;
        long sourceSize = 0;
      
        
        long bytesPerSplit, remainingBytes, numSplits;

        int maxReadBufferSize = 8 * 1024; //8KB
        
        
        if (action==1){
        	fullPath = "C:/Users/stefano.scaltriti/Git/FileSplitter"
            		+ "/FileSplitter_Scaltriti_Federico/Muse.mp3";
        	directory = fullPath.substring(0, fullPath.lastIndexOf("/")+1);
        	fileFormat = fullPath.substring(fullPath.lastIndexOf("."), fullPath.length());
        	fileName = fullPath.substring(fullPath.lastIndexOf("/")+1, fullPath.lastIndexOf("."));
        	
            raf =  new RandomAccessFile(fullPath, "r");
            sourceSize = raf.length();
        	
        	switch(decision){
	        	case 1: //Divisione in più parti specificando la DIMENSIONE di ogni parte (default)
	        		bytesPerSplit = 1024000; //100 mln B
	        		numSplits = sourceSize/bytesPerSplit;

	        		Split sd = new Split(fileName, fileFormat, raf);
	                sd.action(numSplits, bytesPerSplit, maxReadBufferSize, sourceSize);
	        		break;
	        		
	        	case 2: //Divisione in più parti specificando la dimensione di ogni parte e 
	        		//crittografando il contenuto dei file generati tramite una chiave 
	        		//(che può essere la stessa per tutti i file)
	        		bytesPerSplit = 102400000; //100 mln B
	        		numSplits = sourceSize/bytesPerSplit;
	        		Crypto cp = new Crypto(fileName, fileFormat, raf);
	        		cp.action(numSplits, bytesPerSplit, maxReadBufferSize, sourceSize);
	        		break;
	        		
	        	case 3://Divisione in più parti specificando la DIMENSIONE di ogni parte e 
	        		//COMPRIMENDO il contenuto dei file generati
	        		
	        		bytesPerSplit = 102400000; //100 kB
	        		
	        		Zip.compress();
	        		
	        		fullPath = fileName+zipExtension;
	        		RandomAccessFile raff = new RandomAccessFile(fullPath, "r");
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
        } else{
        	Merger m = null;
        	switch(decision){
        		case 1:
        			letter = 'D';
        			fullPath = "C:/Users/stefano.scaltriti/Git/FileSplitter"
        		    		+ "/FileSplitter_Scaltriti_Federico/Muse1D.mp3";
        			directory = fullPath.substring(0, fullPath.lastIndexOf("/")+1);
                	fileFormat = fullPath.substring(fullPath.lastIndexOf("."), fullPath.length());
                	fileName = fullPath.substring(fullPath.lastIndexOf("/")+1, fullPath.lastIndexOf("."));
                	System.out.println("directory: "+directory+'\n'+"fileFormat: "+fileFormat+'\n'+"fileName: "+fileName);
        			
        			m = new Merger(fileName, fileFormat); //directory+fileName+1+letter+fileFormat
        			m.merge(letter);
        			break;
        		case 2:
        			break;
        		case 3:
        			break;
        		case 4:
        			letter = 'T';
        			fullPath = "C:/Users/stefano.scaltriti/Git/FileSplitter"
        		    		+ "/FileSplitter_Scaltriti_Federico/Muse1T.mp3";
        			directory = fullPath.substring(0, fullPath.lastIndexOf("/")+1);
                	fileFormat = fullPath.substring(fullPath.lastIndexOf("."), fullPath.length());
                	fileName = fullPath.substring(fullPath.lastIndexOf("/")+1, fullPath.lastIndexOf("."));
                	System.out.println("directory: "+directory+'\n'+"fileFormat: "+fileFormat+'\n'+"fileName: "+fileName);
        			
        			m = new Merger(fileName, fileFormat); //directory+fileName+1+letter+fileFormat
        			m.merge(letter);
        			break;
        	}
        }
        
    }

}
