package principal;

import java.io.File;
import java.io.RandomAccessFile;

import javax.swing.*;

import merge.Merger;
import split.Crypto;
import split.Split;
import split.SplitTimes;
import split.Zip;

public class Main {

	public static void main(String[] args) throws Exception{
	
		//imposto il frame contenitore
		JFrame frame = new JFrame("File Splitter");
		//imposto l'azione in caso di chiusura frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//elemento contenitore per i 2 Tab
		JTabbedPane tabPane = new JTabbedPane();
		
		//imposto i 2 pannelli
		PanelSplit splitPanel = new PanelSplit();
		PanelMerge mergePanel = new PanelMerge();
		
		tabPane.addTab("Split", splitPanel);
		tabPane.addTab("Merge", mergePanel);
		
		frame.setBounds(0, 0, 500, 300);
				
		frame.add(tabPane);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		
		//TODO modifica filePath con i dati del file presi dal bottone
		
		/*String fullPath = null, directory = null, fileFormat = null, fileName = null;
		String zipExtension = ".zip";
		RandomAccessFile raf = null;
        
        int decision = 3, action = 1;
        char letter;
        long bytesPerSplit, remainingBytes, numSplits, sourceSize = 0;

        Runnable sp = null;
        Thread th = null;
        
        //se scelgo di dividere un file
        if (action == 1){
        	fullPath = "C:/Users/stefano.scaltriti/Git/FileSplitter"
            		+ "/FileSplitter_Scaltriti_Federico/Muse.mp3";
        	directory = fullPath.substring(0, fullPath.lastIndexOf("/")+1);
        	fileFormat = fullPath.substring(fullPath.lastIndexOf("."), fullPath.length());
        	fileName = fullPath.substring(fullPath.lastIndexOf("/")+1, fullPath.lastIndexOf("."));
        	
            raf =  new RandomAccessFile(fullPath, "r");
            sourceSize = raf.length();
            
        	
            //in base al tipo di divisione, attuo una serie di operazioni
        	switch(decision){
	        	case 1: //Divisione in più parti specificando la DIMENSIONE di ogni parte (default)
	        		bytesPerSplit = 1024000; //100 mln B
	        		numSplits = sourceSize/bytesPerSplit;

	        		sp = new Split(fileName, fullPath, raf, numSplits, bytesPerSplit, sourceSize);
	                th = new Thread(sp);
	        		th.start();
	        		break;
	        		
	        	case 2: //Divisione in più parti specificando la dimensione di ogni parte e 
	        		//crittografando il contenuto dei file generati tramite una chiave
	        		bytesPerSplit = 1024000; //100 mln B
	        		numSplits = sourceSize/bytesPerSplit;
	        		sp = new Crypto(fileName, fileFormat, raf, numSplits, bytesPerSplit, sourceSize);
	        		th = new Thread(sp);
	        		th.start();
	        		//cp.action(numSplits, bytesPerSplit, sourceSize);
	        		break;
	        		
	        	case 3://Divisione in più parti specificando la DIMENSIONE di ogni parte e 
	        		//COMPRIMENDO il contenuto dei file generati
	        		
	        		bytesPerSplit = 1024000; //100 kB
	        		
	        		Zip.compress();
	        		
	        		fullPath = fileName+zipExtension;
	        		RandomAccessFile raff = new RandomAccessFile(fullPath, "r");
	        		long sourceSizee = raff.length();
	        		numSplits = sourceSizee/bytesPerSplit;
	        		File file = new File(fileName+zipExtension);
	        		
	        		sp = new Zip(fileName, zipExtension, raff, numSplits, bytesPerSplit, file.length());
	        		th = new Thread(sp);
	        		th.start();
	        		//sz.action(numSplits, bytesPerSplit, sourceSizee);
	        		
	        		//deleting the main zipped file
	        		
	        		if(file.delete())
	        			System.out.println(file.getName() + " is deleted!");
	        		else
	        			System.err.println("Delete operation failed");
	        		
	        		break;
	        		
	        	case 4: //Divisione in più parti specificando il NUMERO DI PARTI
	        		numSplits = 6; //from user input, extract it from args
	        		bytesPerSplit = sourceSize/numSplits;
	                remainingBytes = sourceSize % numSplits;
	        		sp = new SplitTimes(fileName, fileFormat, raf, numSplits, bytesPerSplit, sourceSize);
	        		th = new Thread(sp);
	        		th.start();
	        		
	        		break;
	        }
        	//in caso contrario, voglio unire più file
        }else{
        	Merger m = null;
        	//in base a che tipo di unione voglio fare, attuo determinate operazioni
        	switch(decision){
        		case 1://Unione date più parti di specificata DIMENSIONE (default)
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
        		case 2://Unione date più file crittografati tramite una chiave
        			letter = 'C';
        			fullPath = "C:/Users/stefano.scaltriti/Git/FileSplitter"
        		    		+ "/FileSplitter_Scaltriti_Federico/Muse1C.mp3";
        			directory = fullPath.substring(0, fullPath.lastIndexOf("/")+1);
                	fileFormat = fullPath.substring(fullPath.lastIndexOf("."), fullPath.length());
                	fileName = fullPath.substring(fullPath.lastIndexOf("/")+1, fullPath.lastIndexOf("."));
                	System.out.println("directory: "+directory+'\n'+"fileFormat: "+fileFormat+'\n'+"fileName: "+fileName);
        			
        			m = new Merger(fileName, fileFormat); //directory+fileName+1+letter+fileFormat
        			m.decrypt(); //fileName
        			break;
        		case 3://Unione dati più file compressi
        			letter = 'Z';
        			fullPath = "C:/Users/stefano.scaltriti/Git/FileSplitter"
        		    		+ "/FileSplitter_Scaltriti_Federico/Muse1Z.zip";
        			directory = fullPath.substring(0, fullPath.lastIndexOf("/")+1);
                	fileFormat = fullPath.substring(fullPath.lastIndexOf("."), fullPath.length());
                	fileName = fullPath.substring(fullPath.lastIndexOf("/")+1, fullPath.lastIndexOf("."));
                	System.out.println("directory: "+directory+'\n'+"fileFormat: "+fileFormat+'\n'+"fileName: "+fileName);
        			
                	m = new Merger(fileName, fileFormat); //directory+fileName+1+letter+fileFormat
        			m.merge(letter);
        			m.unZip();
        			File file = new File(fileName.substring(0, fileName.length()-2)+zipExtension);
	        		
        			//elimino il file unito compresso
        			if(file.delete())
	        			System.out.println(file.getName() + " is deleted!");
	        		else
	        			System.err.println("Delete operation failed");
                	break;
        		case 4://Unione di n file, data in partenza n
        			letter = 'T';
        			fullPath = "C:/Users/stefano.scaltriti/Git/FileSplitter"
        		    		+ "/FileSplitter_Scaltriti_Federico/Muse1T.mp3";
        			directory = fullPath.substring(0, fullPath.lastIndexOf("/")+1);
                	fileFormat = fullPath.substring(fullPath.lastIndexOf("."), fullPath.length());
                	fileName = fullPath.substring(fullPath.lastIndexOf("/")+1, fullPath.lastIndexOf("."));
        			
        			m = new Merger(fileName, fileFormat); //directory+fileName+1+letter+fileFormat
        			m.merge(letter);
        			break;
        	}
        }
        */
    }
}
