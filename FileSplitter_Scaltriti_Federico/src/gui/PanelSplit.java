package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import split.*;

/**
 * Classe per inizializzare il pannello per la gestione grafica della divisione dei file
 * @see JPanel
 * @see ActionListener
 * */

public class PanelSplit extends JPanel implements ActionListener{	
	/**
	 * Componenti grafici:
	 * TableModel che specifica i metodi per gestire la JTable; 
	 * Serie di JButton per: attivare la ricerca per la selezione del/dei file da dividere; 
	 * avviare la divisione dei file; permettere in un JDialog di scegliere la cartella di destinazione dei file divisi;
	 * confermare le impostazioni di divisione scelte per il singolo file; per cambiare, nel PanelSplit , le impostazioni del file scelto;
	 * per eliminare tutti i file selezionati.
	 * JTable per presentare graficamente i file selezionati;
	 * Serie di JTextField per: impostare la dimensione dei file da ottenere nel JDialog apposta; 
	 * indicare il numero di file che si vogliono ottenere dalla divisione; 
	 * JPasswordField per immettere la password in caso di criptazione;
	 * Serie di JLabel: relativa al JTextField della dimensione; per mostrare un messaggio di errore in inserimento dei dati nel JDialog;
	 * relativa al JTextField della password; relativa al JTextField del numero di parti; relativa al nome del file selezionato;
	 * indicante il progresso della divisione.
	 * Serie di JRadioButton per la scelta, all'interno del JDialog della divisione: 
	 * standard; cifrata; compressa; dato il numero di parti.
	 * JDialog usato per i messaggi d'errore;
	 * JDialog per le impostazioni di divisione;
	 * 
	 * */
	private MyTableModel dataModel;
	private JButton browse, split, browseDestination, settingsOk, change, delete;
	private JTable t;
	private JTextField dimField, partsField, destinationFolderField;
	private JPasswordField passwdField;
	private JLabel dimLabel, errorLabel, passwdLabel, partsLabel, fileNameLabel, progressLabel;
	private JRadioButton radioSplit, radioCrypto, radioZip, radioParts; 
	private JDialog messageDialog, choiceDialog;
	
	/**
	 * array contenente i file presi nel momento della singola selezione
	 * Serie di vettori: contenente gli elementi da aggiungere alla tabella; per le scelte di divisione fatte;
	 * per le dimensioni dei file da dividere; per memorizzare i valori del numero di parti in cui dividere i file;
	 * per le cartelle di destinazione; per le pasword inserite; con tutti i file selezionati fino a un istante di tempo.
	 * contatore di file selezionati che vengono processati 
	 */
	private File[] selectedFiles;
	private Vector<Split> v;
	private Vector<Integer> choices;
	private Vector<Long> dims, parts;
	private Vector<String> destinations;
	private Vector<char[]> passwords;
	private Vector<File> totalFiles;
	private int contFile = 0;
	
	/**
	 * Costruttore del pannello contenente i componenti grafici per la divisione
	 * */
	public PanelSplit(){
		super();
		v = new Vector<Split>();
		
		totalFiles = new Vector<File>();
		choices = new Vector<Integer>();
		dims = new Vector<Long>(); 
		parts = new Vector<Long>();
		passwords = new Vector<char[]>();
		destinations = new Vector<String>();
		
		//pannelli figli che contengono la tabella e e bottoni
		JPanel up = new JPanel();
		JPanel down = new JPanel();
		
		// crea il modello di dati a partire dal vettore
		dataModel = new MyTableModel(v);
		// crea la tabella
		t = new JTable(dataModel);
		t.getColumnModel().getColumn(0).setPreferredWidth(200);
		t.getColumnModel().getColumn(1).setPreferredWidth(200);
		
		//aggiunge la tabella al pannello
		add(t);		
		JScrollPane jsp = new JScrollPane(t); //pannello per contenere la tabella
		up.add(jsp);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		radioSplit = new JRadioButton("Split");
		radioCrypto = new JRadioButton("Crypto");
		radioZip = new JRadioButton("Zip");
		radioParts = new JRadioButton("Parts");
		buttonGroup.add(radioSplit);
		buttonGroup.add(radioCrypto);
		buttonGroup.add(radioZip);
		buttonGroup.add(radioParts);
		
		//insieme di listener per abilitare o no componenti grafici relativi alle scelte di divisione
		radioSplit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Split")){
					onSplit();
				}
			}
		});
		
		radioZip.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Zip")){
					onSplit();
				}
			}
		});
		
		radioCrypto.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Crypto")){
					onSplit();
				}
			}
		});
		
		radioParts.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("Parts")){
					onParts();
				}
			}
		});
		
		dimLabel = new JLabel("Dimensione (Byte)");
		passwdLabel = new JLabel("Password");
		partsLabel = new JLabel("N Parti");
		
		dimField = new JTextField("");
		passwdField = new JPasswordField("");
		partsField = new JTextField("");
		
		destinationFolderField = new JTextField("Destinazione");
		destinationFolderField.setEditable(false);
		destinationFolderField.setSize(80, 30);
		
		browseDestination = new JButton("Cerca");
		browseDestination.addActionListener(this);
		
		settingsOk = new JButton("OK");
		settingsOk.addActionListener(this);
		
		browse = new JButton("Naviga");
		browse.addActionListener(this);
		down.add(browse);
		
		change = new JButton("Modifica");
		change.addActionListener(this);
		down.add(change);
		
		delete = new JButton("Elimina");
		delete.addActionListener(this);
		down.add(delete);
		
		split = new JButton("Dividi");
		split.addActionListener(this);
		down.add(split);
		
		progressLabel = new JLabel("0"+"%");
		progressLabel.setBorder(new EmptyBorder(10,30,10,0));
		down.add(progressLabel);
		
		add(up);
		add(down);
	}
	
	/**
	 * Metodo per abilitare componenti grafici in caso si scelga la divisione
	 * di default, cifrata o compressa
	 * */
	public void onSplit(){
		partsField.setEnabled(false);
		dimField.setEnabled(true);
		if(!radioCrypto.isSelected())
			passwdField.setEnabled(false);
		else
			passwdField.setEnabled(true);
	}
	
	/**
	 * Metodo per abilitare componenti grafici in caso si scelga la divisione
	 * per parti
	 * */
	public void onParts(){
		partsField.setEnabled(true);
		dimField.setEnabled(false);
		passwdField.setEnabled(false);
	}
	
	/**
	 * Metodo per impostare il layout del JDialog per le impostazioni di divisione
	 * @param fileName JLabel contenente il nome del file selezionato
	 * @param contentPane il pannello padre del JDialog
	 * */
	public void setDialogLayout(JLabel fileName, JPanel contentPane){
		GroupLayout groupLayout= new GroupLayout(contentPane);
		contentPane.setLayout(groupLayout);
		groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
				.addGroup(
						groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(radioSplit)
						.addComponent(dimLabel)
						.addComponent(dimField)
						.addComponent(destinationFolderField))
				.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(fileName)
						.addComponent(radioCrypto)
						.addComponent(passwdLabel)
						.addComponent(passwdField)
						.addComponent(browseDestination))
				.addGroup(
						groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(radioParts)
						.addComponent(partsLabel)
						.addComponent(partsField)
						.addComponent(settingsOk))
				.addComponent(radioZip));
		
		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
				.addComponent(fileName)
				.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(radioSplit)
				.addComponent(radioCrypto)
				.addComponent(radioParts)
				.addComponent(radioZip))
			.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(dimLabel)
				.addComponent(passwdLabel)
				.addComponent(partsLabel))
			.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(dimField)
				.addComponent(passwdField)
				.addComponent(partsField))
			.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			.addComponent(destinationFolderField)
			.addComponent(browseDestination)
			.addComponent(settingsOk)));
		
		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
	}
	
	/**
	 * Metodo per l'impostazione del JDialog per le impostazioni di divisione
	 * @param currentFile file che si sta considerando per la divisione corrente
	 * @param flag valore booleano per abilitare o no i JRadioButton
	 * */
	public void setChoiceDialog(String currentFile, boolean flag){
		
		JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
		choiceDialog = new JDialog(topFrame);
		
		JPanel contentPane = new JPanel();
		contentPane.setOpaque(true);
		choiceDialog.setContentPane(contentPane);
		
		//listener per salvare, quando si chiude il JDialog, i valori impostati per la divisione
		choiceDialog.addWindowListener(new WindowAdapter (){
			@Override
			public void windowClosed(WindowEvent e){ //popolo i vettori con le impostazioni da salvare
					
					if (radioSplit.isSelected()){
						choices.add(1);
						dims.add(isNumber(dimField));
					}
					if (radioCrypto.isSelected()){
						choices.add(2);
						dims.add(isNumber(dimField));
						passwords.add(passwdField.getPassword());
					}
					if (radioZip.isSelected()){
						choices.add(3);
						dims.add(isNumber(dimField));
					}
					if (radioParts.isSelected()){
						choices.add(4);
						parts.add(isNumber(partsField));
					}
					destinations.add(destinationFolderField.getText());				
			}
		});
		
		choiceDialog.setTitle("Impostazioni");
		choiceDialog.setVisible(true);
		choiceDialog.setLocationRelativeTo(null);
		choiceDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		fileNameLabel = new JLabel(currentFile);
		setDialogLayout(fileNameLabel, contentPane);
		choiceDialog.pack();
		
		//in base al file che si sta considerando, si controlla se è già presente nell'elenco complessivo
		int result = checkLabel(fileNameLabel.getText());
		//in caso sia presente, vuol dire che lo sto modificando
		if (result != -1){
			int filesWithDim = 0, cryptoCount = 0, partsCount = 0;
			
			for (int i = 0; i < result; i++) {
				if (choices.get(i) == 1 || choices.get(i) == 2 || choices.get(i) == 3){
					filesWithDim++;
					if (choices.get(i) == 2)
						cryptoCount++;
				}else
					partsCount++;
			}
			
			//in base alla scelta precedentemente fatta, riprendo i valori impostati in quel caso
			switch(choices.get(result)){
				case 1:
					radioSplit.setSelected(true);
					dimField.setText(dims.get(filesWithDim).toString());
					onSplit();
					break;
				case 2:
					radioCrypto.setSelected(true);
					dimField.setText(dims.get(filesWithDim).toString());
					passwdField.setText(passwords.get(cryptoCount).toString());
					onSplit();
					break;
				case 3:
					radioZip.setSelected(true);
					dimField.setText(dims.get(filesWithDim).toString());
					onSplit();
					break;
				case 4:
					radioParts.setSelected(true);
					partsField.setText(parts.get(partsCount).toString());
					onParts();
					break;
			}
			destinationFolderField.setText(destinations.get(result));
		}
		
		radioSplit.setEnabled(flag);
		radioCrypto.setEnabled(flag);
		radioZip.setEnabled(flag);
		radioParts.setEnabled(flag);
		
	}
	
	/**
	 * Metodo per impostare il Dialog per i messaggi d'errore
	 * @param label JLabel contenente il messaggio da visualizzare
	 * */
	public void setMessageDialog(JLabel label){
		messageDialog = new JDialog();
		messageDialog.setVisible(true);
		messageDialog.setTitle("Errore");
		messageDialog.setSize(350, 70);
		messageDialog.add(label);
		messageDialog.setModal(true);
		messageDialog.setLocationRelativeTo(null);
	}
	
	
	/**
	 * Metodo di controllo dei dati inseriti
	 * @param textField contiene il valore inserito da utente da controllare
	 * @return il valore inserito se è valido, '10' come valore di default in caso contrario
	 * */
	public long isNumber(JTextField textField){
		long value;
		try{
			value = Long.parseLong(textField.getText());
			return value;
		}catch(NumberFormatException e){
			errorLabel = new JLabel("Valore non valido", SwingConstants.CENTER);
			setMessageDialog(errorLabel);
		}
		
		return 10;
	}
	
	/**
	 * Metodo per controllare se il valore inserito è numerico
	 * @return vero se è valido, falso se è nullo o se il formato è errato
	 * */
	public boolean isValid(String strNum) {
	    if (strNum == null) 
	        return false;
	    try {
	        @SuppressWarnings("unused")
			double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	/**
	 * Metodo per l'aggiunta di un file alla tabella
	 * */
	private void addToTable(){
		int decision = 0;
		if (radioSplit.isSelected())
			decision = 1;
		if (radioCrypto.isSelected())
			decision = 2;
		if (radioZip.isSelected())
			decision = 3;
		if (radioParts.isSelected())
			decision = 4;
		
		File f = new File(selectedFiles[contFile].getAbsolutePath());
		totalFiles.add(f);
		Split sp = new Split(selectedFiles[contFile].getName(), selectedFiles[contFile].getAbsolutePath(), selectedFiles[contFile].length(), decision);
		v.add(sp);
		int i = v.size();
		dataModel.addRow(v);
		v.setSize(i);
		contFile++;
		
		//se non ho ancora controllato tutti i file selezionati
		if (contFile<selectedFiles.length)
			setChoiceDialog(selectedFiles[contFile].getAbsolutePath(), true);
	}
	
	/**
	 * Metodo per pulire la tabella:
	 * rimuovo tutti gli elementi dal vettore, dalla tabella graficamente e comunico il cambiamento
	 * */
	private void clearTable(){
		v.removeAllElements();
		dataModel.getDataVector().removeAllElements();
		dataModel.fireTableDataChanged();
	}
	
	/**
	 * Metodo per verificare la presenza di un file all'interno del vettore
	 * @param il valore della label da controllare 
	 * @return la posizione dell'elemento nel vettore
	 * */
	public int checkLabel(String label){
		for (int i = 0; i < totalFiles.size(); i++) {
			if (totalFiles.get(i).toString().equals(label))
				return i;
		}
		return -1;
	}
	
	/**
	 * Metodo per aggiornare i valori relativi al file, selezionato dalla tabella
	 * @param row la riga della tabella in cui si trova il file 
	 */
	public void updateRow(int row){
		destinations.set(row, destinationFolderField.getText()); //aggiorno la destinazione
		int filesWithDim = 0, cryptoCount = 0, partsCount = 0;
		
		for (int i = 0; i < row; i++) {
			if (choices.get(i) == 1 || choices.get(i) == 2 || choices.get(i) == 3){
				filesWithDim++;
				if (choices.get(i) == 2)
					cryptoCount++;
			}else
				partsCount++;
		}
		if (radioSplit.isSelected() || radioCrypto.isSelected() || radioZip.isSelected()){
			dims.set(filesWithDim, isNumber(dimField));
			if (radioCrypto.isSelected())
				passwords.set(cryptoCount, passwdField.getPassword());
		}else
			parts.set(partsCount, isNumber(partsField));
	}
	
	/**
	 * Metodo sovrascritto per la gestione degli eventi scaturiti dal click sui JButton
	 * */
	@Override
	public void actionPerformed(ActionEvent e) {
		//se si è scelto di cercare nel file system i file da dividere
		if (e.getActionCommand().equals("Naviga")){ 
			
			JFileChooser fileChooser = new JFileChooser();
			//per abilitare la selezione multipla di file
			fileChooser.setMultiSelectionEnabled(true);
			//per impostare la directory iniziale in cui cercare
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			
			int result = fileChooser.showOpenDialog(this);
			
			// se l'utente seleziona uno o più file
			if (result == JFileChooser.APPROVE_OPTION) {
				selectedFiles = fileChooser.getSelectedFiles();
				contFile = 0;
				
				//se ho effettivamente selezionato dei file
				if(selectedFiles != null){
					progressLabel.setText("0%");
					setChoiceDialog(selectedFiles[0].getAbsolutePath(), true);
				}
							
			}
			
		//se si clicca sul bottone per selezionare la cartella di destinazione
		}else if (e.getActionCommand().equals("Cerca")){
			JFileChooser folderChooser = new JFileChooser();
			folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			folderChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			
			//disabilitata l'opzione 'Tutti i file'
			folderChooser.setAcceptAllFileFilterUsed(false);
			if (folderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				destinationFolderField.setText(folderChooser.getSelectedFile()+File.separator);
			
		//se si clicca sul bottone per confermare le impostazioni di divisione	
		}else if (e.getActionCommand().equals("OK")){ 
			String noDim = "Non hai inserito la dimensione ", 
					noParts = "Non hai definito quanti file creare ",
					noParam = "Uno o più parametri non sono stati inseriti correttamente",
					noSelection = "Non hai selezionato la modalità";
			
			//controllo che i campi siano inseriti correttamente
			boolean result = radioSplit.isSelected() || radioCrypto.isSelected() || 
								radioZip.isSelected() || radioParts.isSelected();
			
			//se nessuna modalità è stata selezionata
			if (!result){
				errorLabel = new JLabel(noSelection, SwingConstants.CENTER);
				setMessageDialog(errorLabel);
			}else{
				//se ho scelto di dividere in base alla dimensione, criptando o comprimendo
				if (dimField.isEnabled()){
					String dimFieldText = dimField.getText();
					if (dimFieldText.equals("") || !isValid(dimFieldText)){				
						errorLabel = new JLabel(noDim, SwingConstants.CENTER);
						setMessageDialog(errorLabel);
					}else if ((passwdField.isEnabled() && passwdField.getPassword().length == 0) 
							|| destinationFolderField.getText().equals("Destinazione")){
						errorLabel = new JLabel(noParam, SwingConstants.CENTER);
						setMessageDialog(errorLabel);
					}else{
						String label = fileNameLabel.getText();
						int row = checkLabel(label);
						choiceDialog.dispose();
						if(row == -1) //in caso il file non sia presente nella tabella, aggiungo la new entry
							addToTable();
						else //se invece esiste già, modifico i parametri relativi a quel file
							updateRow(row);
					}
				//altrimenti se è stato scelto di dividere per parti	
				}else if (partsField.isEnabled()){
					String partsFieldText = partsField.getText();
					if (partsFieldText.equals("") || !isValid(partsFieldText) 
							|| destinationFolderField.getText().equals("Destinazione")){
						errorLabel = new JLabel(noParts, SwingConstants.CENTER);
						setMessageDialog(errorLabel);
					}else{
						choiceDialog.dispose();
						String label = fileNameLabel.getText();
						int row = checkLabel(label);
						if(row == -1) //in caso il file non sia presente nella tabella, aggiungo la new entry
							addToTable();
						else //se invece esiste già, modifico i parametri relativi a quel file 
							updateRow(row);
					}
				}
			}
			
		//in caso si voglia eliminare i file e le selezioni scelti
		}else if (e.getActionCommand().equals("Elimina")){
			choices.clear();
			dims.clear();
			parts.clear();
			destinations.clear();
			passwords.clear();
			totalFiles.clear();
			clearTable();
			
			//se voglio modificare i valori inseriti nella tabella
		}else if (e.getActionCommand().equals("Modifica")){
			if (!t.getSelectionModel().isSelectionEmpty()){
				setChoiceDialog(t.getModel().getValueAt(t.getSelectedRow(), 1).toString(), false);
			}else{
				errorLabel = new JLabel("Non hai selezionato alcuna riga da modificare", SwingConstants.CENTER);
				setMessageDialog(errorLabel);
			}
			
		}else{ //l'unica azione rimanente è "Dividi"
			long totalBytes = 0, readBytes = 0;
			if (totalFiles.size() == 0){ //selectedFiles
				errorLabel = new JLabel("Non hai selezionato alcun file", SwingConstants.CENTER);
				setMessageDialog(errorLabel);
			}else{
				//attua la divisione effettiva
				int i = 0;
				//acquisizione totalBytes da leggere
				for (i = 0; i < totalFiles.size(); i++) {
					totalBytes+= Long.parseLong(t.getModel().getValueAt(i, 2).toString());
				}
				
				Split sp = null;
		        Thread th = null;
		        RandomAccessFile raf;
				for (File file : totalFiles) { //per ogni file selezionato
					try {
						raf = new RandomAccessFile(file.getAbsolutePath(), "r");
						long sourceSize = raf.length();
						long bytesPerSplit, numSplits, remainingBytes;
						String fullPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(file.getName()));
						
						switch(choices.get(0)){ //in base alla scelta fatta
							case 1:
								bytesPerSplit = dims.get(0);
				        		sp = new Split(file.getName(), fullPath+file.getName(), destinations.get(0), raf, bytesPerSplit, sourceSize);
								dims.remove(0);
				        		break;
							case 2:
								bytesPerSplit = dims.get(0);
				        		sp = new Crypto(file.getName(), fullPath+file.getName(), destinations.get(0), raf, bytesPerSplit, sourceSize, passwords.get(0));
								dims.remove(0);
				        		passwords.remove(0);
				        		break;
							case 3:
								bytesPerSplit = dims.get(0);
								String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
				        		sourceSize = raf.length();
				        		sp = new Zip(name, fullPath+file.getName(), destinations.get(0), raf, bytesPerSplit, file.length());		        	
								dims.remove(0);
				        		break;
							case 4:
								numSplits = parts.get(0);
				        		bytesPerSplit = sourceSize/numSplits;
				                remainingBytes = sourceSize % numSplits;
				        		sp = new SplitTimes(file.getName(), fullPath+file.getName(), destinations.get(0), raf, bytesPerSplit, sourceSize);
				        		parts.remove(0);
				        		break;
						}
						choices.remove(0);
						destinations.remove(0);
						
						//inizializzo ed inizio un nuovo thread relativo allo splitter istanziato
		                th = new Thread(sp);
		        		th.start();
						
						
						//avvio un thread per impostare l'avanzamento della progressLabel
		        		Progress p = new Progress(th, sp, progressLabel, totalBytes);
		        		p.execute();
		        				    
		        		//pulisco la tabella dai file scelti per essere processati
		        		clearTable();
		        		
					} catch (IOException  e1) {
						e1.printStackTrace();
					}
					
				}
				//finite tutte le divisioni, riabilito i bottoni ed elimino tutti i file selezionati 
				totalFiles.clear();
			}
					
		}	
	}
}