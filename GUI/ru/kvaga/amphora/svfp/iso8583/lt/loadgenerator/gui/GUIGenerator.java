package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.SystemConfigurator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.statistic.LoadStatistic;
import ru.kvaga.amphora.svfp.loadGenerator.log.LogInit;

public class GUIGenerator implements ActionListener {

	//	// Send transactions
//	private JLabel labelSendTxns;
//	private JButton buttonSendTxns;
//	// Receive transactions
//	private JLabel labelRecTxns;
//	private JButton buttonRecTxns;
//	// Current TPS
//	private JLabel labelFieldCurrentTPS;
//	private JTextField textFieldCurrentTPS;
//	private JButton buttonCurrentTPS;
//	private int ownCurrentTPS=0;
//	
//	// Full stop
//	
	Logger logCommon = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
	JLabel     labelSendTxnsTotal;
	JLabel     labelWaitTxnsTotal;
	JLabel     labelRecTxnsTotal;
	JLabel     labelRecTxnsSuccess;
	JLabel     labelRecTxnsFailed;
	JLabel     labelConnectTotalFailed; //количество отказов сервера ISO
	JLabel     labelRecTxnsFraud;
	JLabel     labelTimeStartScenario;
	JLabel     labelCurrentTPS;
	JSpinner   spinner;
	JSeparator separator;
	JSeparator settingsSeparator;
	JSeparator buttonSeparator;
	JTextField textFieldChangeTPS;
	JLabel     labelMaxTPS;
	JTextField textFieldChangeMaxTPS;
	JLabel     labelTicker;
	JTextField textFieldTicker;
	JLabel     labelMaxTPSTime;
	JButton    buttonStartStopLoad;
	LoadGenerator loadGenerator = null;

	/**
	 * Создание графического интерфейса
	 *
	 * @param loadGenerator
	 */
	public GUIGenerator(LoadGenerator loadGenerator) {
		this.loadGenerator = loadGenerator;
		// Frame
		final JFrame frame = new JFrame("LT ISO8583Generator");
		frame.getContentPane().setLayout(new FlowLayout());
		frame.setSize(new Dimension(255, 335));
		Image frameIcon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
		frame.setIconImage(frameIcon);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);

		//change close window logic
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			/**
			 * Событие на закрытие главной формы приложения
			 * @param e
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				int result = JOptionPane.showConfirmDialog(
						frame, "Are you sure you want to exit?\r\nPlease check that all txn responses were received.");
				if (result == JOptionPane.OK_OPTION) {
					// NOW we change it to dispose on close..
					frame.setDefaultCloseOperation(
							JFrame.DISPOSE_ON_CLOSE);
					logCommon.log(Level.WARNING, "[GUI] Application terminated. Waiting wait until next traceNumber value will be stored.");
					suspendScenario();
					sleep(SystemConfigurator.getInstance().UPDATE_TPS_LOG_FILE_TASK_SLEEP_IN_MILLIS); //wait until next traceNumber value will be stored
					frame.setVisible(false);
					frame.dispose();
					System.exit(0);
				}
			}
		});


		SpinnerModel intModel =
				new SpinnerNumberModel(0, //initial value
				                       0, //min
				                       999, //max
				                       0.1);                //step int or float

		labelSendTxnsTotal = new JLabel("Send transactions total: ");
		labelWaitTxnsTotal = new JLabel("Wait transactions total: ");
		labelWaitTxnsTotal.setToolTipText("Wait transactions total = Received - Send. If Wait transactions total more than Current TPS, then fields color= RED. ");
		labelRecTxnsTotal = new JLabel("Received transactions total: ");
		labelRecTxnsSuccess = new JLabel("Received transactions success: ");
		labelRecTxnsFailed = new JLabel("Received transaction failed: ");
		labelRecTxnsFraud = new JLabel("Received transaction fraud: ");
		labelConnectTotalFailed = new JLabel("Failed connections total: ");
		labelCurrentTPS = new JLabel("Current load (TPS): ");
		labelCurrentTPS.setToolTipText("Prev Send transactions total - Current Send transactions total");
		textFieldChangeTPS = new JTextField(3);
		textFieldChangeTPS.setToolTipText("Required Current load (TPS). Double value.");
		//spinner = new JSpinner(intModel);  //field only for digit values (int or float)
		labelMaxTPS = new JLabel("Max load (TPS): ");
		textFieldChangeMaxTPS = new JTextField(3);
		textFieldChangeMaxTPS.setToolTipText("Maximum load (TPS). Double value. If new MaxTPS value more than Current load, then Current Load will be decreased to MaxTPS value.");
		labelTicker = new JLabel("Inc. TPS every 1 sec by: ");
		textFieldTicker = new JTextField(3);
		textFieldTicker.setToolTipText("TPS wll be incremented every second by user defined step.");
		labelMaxTPSTime = new JLabel("Max TPS after (sec): 0");
		separator = new JSeparator();
		separator.setPreferredSize(new Dimension(frame.getWidth() - 10, 1));
		settingsSeparator = new JSeparator();
		settingsSeparator.setPreferredSize(new Dimension(frame.getWidth() - 10, 1));
		buttonSeparator = new JSeparator();
		buttonSeparator.setPreferredSize(new Dimension(frame.getWidth() - 10, 1));
		labelTimeStartScenario = new JLabel("Load start time:          ");
		buttonStartStopLoad = new JButton("Start");
		buttonStartStopLoad.setPreferredSize(new Dimension(100, 25));

		//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		//действие на изменение значения поля "Current load (TPS)"
		textFieldChangeTPS.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							logCommon.log(Level.INFO, "[GUI] Requirement TPS changed to : " + e.getActionCommand());
							setTPS(Double.parseDouble(e.getActionCommand()));
						} catch (Exception ex) {
							logCommon.log(Level.WARNING, "[GUI] Incorrect field value : " + e.getActionCommand());
						}
					}
				}
		);

		//действие на изменение значения поля "Max TPS"
		textFieldChangeMaxTPS.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							logCommon.log(Level.INFO, "[GUI] Max TPS changed to : " + e.getActionCommand());
							Double fieldValue = Double.parseDouble(e.getActionCommand());
							if (fieldValue == 0){
								logCommon.log(Level.WARNING, "[GUI] Incorrect field value : " + e.getActionCommand() + ". Max load Can should be more than 0. Please use the button \"Stop\" to stop the load.");
							} else {
								setMaxTPSValue(fieldValue);
								int currentTPS = (int) LoadGenerator.getReqTPS();
								//если значение MaxTPS выставлено меньше, чем currentTPS, то присвоить currentTPS значение MaxTPS
								if (currentTPS > fieldValue){
									setTPS(fieldValue);
									//textFieldChangeTPS.setText(fieldValue.toString());
								}
							}
						} catch (Exception ex) {
							logCommon.log(Level.WARNING, "[GUI] Incorrect field value : " + e.getActionCommand());
						}
					}
				}
		);

		//действие на изменение значения поля "Inc. TPS every 1 sec by"
		textFieldTicker.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							logCommon.log(Level.INFO, "[GUI] Ticker changed to : " + e.getActionCommand());
							setTickerValue(Double.parseDouble(e.getActionCommand()));
						} catch (Exception ex) {
							logCommon.log(Level.WARNING, "[GUI] Incorrect field value : " + e.getActionCommand());
						}
					}
				}
		);
		//textFieldChnageTPS.setActionCommand("commandChangeTPS");

		//действие на нажатие кнопки Start/Stop
		buttonStartStopLoad.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (buttonStartStopLoad.getText().equals("Start")) {
							buttonStartStopLoad.setText("Stop");
							resumeScenario();
						} else {
							buttonStartStopLoad.setText("Start");
							//start 2012/09/18
							Date scenarioStopTime = new Date();
							LoadStatistic.getInstance().setTimeStopScenario(LoadGenerator.getFormattedDate(scenarioStopTime, "yyyy/MM/dd HH:mm:ss"));
							logCommon.log(Level.WARNING, "[GUI] Scenario stop time: " + scenarioStopTime);
							suspendScenario();
							//end 2012/09/18
						}
					}
				}
		);

		new GUIUpdateWindowTask(this, 1000);

		// Сборка фрейма GUI
		frame.getContentPane().add(labelSendTxnsTotal);
		frame.getContentPane().add(labelWaitTxnsTotal);
		frame.getContentPane().add(labelRecTxnsTotal);
		frame.getContentPane().add(labelRecTxnsSuccess);
		frame.getContentPane().add(labelRecTxnsFailed);
		frame.getContentPane().add(labelRecTxnsFraud);
		frame.getContentPane().add(labelConnectTotalFailed);
		frame.getContentPane().add(settingsSeparator);  //----------- settings --------------
		frame.getContentPane().add(labelCurrentTPS);
		frame.getContentPane().add(textFieldChangeTPS);
		//frame.getContentPane().add(spinner);
		frame.getContentPane().add(labelMaxTPS);
		frame.getContentPane().add(textFieldChangeMaxTPS);
		frame.getContentPane().add(labelTicker);
		frame.getContentPane().add(textFieldTicker);
		frame.getContentPane().add(labelMaxTPSTime);
		frame.getContentPane().add(buttonSeparator);    //-------------------------
		frame.getContentPane().add(labelTimeStartScenario);
		frame.getContentPane().add(buttonStartStopLoad);

		frame.setVisible(true);

	}

	private void suspendScenario() {
		LoadGenerator.suspendScenario = true;
	}

	private void resumeScenario() {
		LoadGenerator.suspendScenario = false;
		LoadGenerator.resetTestTxnID();
	}

	private void setTPS(double TPS) {
		LoadGenerator.setTPS(TPS);
	}

	private void setMaxTPSValue(double TPS) {
		LoadGenerator.setMaxTPSValue(TPS);
	}

	private void setTickerValue(double TPS) {
		LoadGenerator.setTickerValue(TPS);
	}

	public void actionPerformed(ActionEvent arg0) {
	}

	private void sleep(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

//	public static void main(String args[]){
//		SwingUtilities.invokeLater(new Runnable(){
//			public void run(){
//				new LT_ISO8583GeneratorGUI(null);
//			}
//		});
//	}
}