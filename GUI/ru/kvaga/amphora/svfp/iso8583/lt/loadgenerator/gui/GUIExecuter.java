package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.gui;

import javax.swing.SwingUtilities;


import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;



public class GUIExecuter implements Runnable {
	private LoadGenerator loadGenerator;

	public GUIExecuter(LoadGenerator loadGenerator){
		this.loadGenerator=loadGenerator;
		SwingUtilities.invokeLater(this);
	}

	/**
	 * Запуск GUI
	 */
	public void run(){
		new GUIGenerator(loadGenerator);
	}
}
