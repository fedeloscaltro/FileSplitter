package gui;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import split.Split;


/**
 * Classe che avvia un processo GUI-interaction in un thread in background
 * @see SwingWorker
 * */
public class Progress extends SwingWorker<Void, Integer>{
	
	/**
	 * Thread in esecuzione per la divisione di un file
	 * */
	private Thread th;
	
	/**
	 * Oggetto Split su cui opera il thread {@link #th}
	 * */
	private Split sp;
	
	/**
	 * JLabel per visualizzare graficamente l'avanzamento delle operazioni di divisione
	 * */
	private JLabel progressLabel;
	
	/**
	 * Long indicante il totale dei bytes da leggere durante tutto il processo di divisione
	 * */
	private long totalBytes;
	
	
	/**
	 * Costruttore chiamato al momento dell'esecuzione del thread {@link #th}
	 * @param th {@link #th}
	 * @param sp {@link #sp}
	 * @param progressLabel {@link #progressLabel}
	 * @param totalBytes {@link #totalBytes}
	 * */
	public Progress(Thread th, Split sp, JLabel progressLabel, long totalBytes){
		this.th = th;
		this.sp = sp;
		this.progressLabel = progressLabel;
		this.totalBytes = totalBytes;
	}
	
	

	/**
	 * Metodo usato per i calcoli da eseguire in background
	 * @throws Exception nel caso sia inusabile
	 * */
	@Override
	protected Void doInBackground() throws Exception {
		long readBytes = 0;
		while(getTh().getState() != Thread.State.TERMINATED){ 	//finché il thread non è terminato
			readBytes = getSp().getReadBytes();					//ottengo il numero di bytes letti fino a quel momento
			int val =  Math.toIntExact((readBytes*100)/getTotalBytes()); //ottengo la percentuale relativa al totale
			publish(val); //Invia i dati intermedi, passati come argomenti, al metodo process()
		}
		return null;
	}
	
	/**
	 * Metodo per ricevere i dati dal metodo publish(). 
	 * Serve per eseguire aggiornamenti a livello di grafica 
	 * */
	@Override
	protected void process(List<Integer> chunks){
		int i = chunks.get(chunks.size()-1);
		getProgressLabel().setText(i+"%"); //aggiorno il valore della progressLabel
	}

	
	/**
	 * Metodo richiamato nel momento in cui doInBackground() termina l'esecuzione.
	 * */
	@Override
	protected void done() {
		try {
			get();
			if (getProgressLabel().getText().equals("98%") || 
					getProgressLabel().getText().equals("99%") || getProgressLabel().getText().equals("100%")){
				getProgressLabel().setText("100%");
				JOptionPane.showMessageDialog(getProgressLabel().getParent(), 
						"Divisione avvenuta con successo", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
				
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo getter per restituire il thread in esecuzione
	 * @return {@link #th} 
	 * */
	public Thread getTh() {
		return th;
	}

	/**
	 * Metodo setter per impostare  il thread in esecuzione
	 * @param {@link #th} 
	 * */
	public void setTh(Thread th) {
		this.th = th;
	}

	/**
	 * Metodo getter per restituire lo Splitter relativo al thread in esecuzione
	 * @return {@link #sp} 
	 * */
	public Split getSp() {
		return sp;
	}

	/**
	 * Metodo setter per impostare lo Splitter relativo al thread in esecuzione
	 * @param {@link #sp} 
	 * */
	public void setSp(Split sp) {
		this.sp = sp;
	}	
	
	/**
	 * Metodo getter per restituire la JLabel utilizzata per il monitorare l'avanzamento dell'esecuzione
	 * @return {@link #progressLabel} 
	 * */
	public JLabel getProgressLabel() {
		return progressLabel;
	}

	/**
	 * Metodo setter per impostare il thread in esecuzione
	 * @param {@link #progressLabel} 
	 * */
	public void setProgressLabel(JLabel progressLabel) {
		this.progressLabel = progressLabel;
	}

	/**
	 * Metodo getter per restituire il totale dei bytes da processare
	 * @return {@link #totalBytes} 
	 * */
	public long getTotalBytes() {
		return totalBytes;
	}

	/**
	 * Metodo setter per impostare il thread in esecuzione
	 * @param {@link #totalBytes} 
	 * */
	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}
}
