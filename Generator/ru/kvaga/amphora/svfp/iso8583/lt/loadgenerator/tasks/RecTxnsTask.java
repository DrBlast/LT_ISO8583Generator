package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.tasks;
/**
 * Задача получения сообщений от сервера ISO
 */

import java.lang.Thread;
import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.ISO87PackagerExt;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.RespCode;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.TransactionMap;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.SystemConfigurator;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import ru.kvaga.amphora.svfp.iso8583.lt.client.ISO8583Client;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.statistic.LoadStatistic;

import ru.kvaga.amphora.svfp.loadGenerator.log.LogInit;

import static ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util.FilePrinter.printFileRecord;

public class RecTxnsTask implements Runnable {
	//private
	private ISO8583Client isoClient;
	private Logger             logCommon = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
	//private Logger             logRec    = LogInit.getLogger(LogInit.LOGGER_NAME_RECEIVER);
	private LoadStatistic      loadStat  = LoadStatistic.getInstance();
	//private int                countQuery = 100; //default query size
	private SystemConfigurator sConf     = null;
	//public

	/**
	 * Инициализация прослушки ответов сервера
	 *
	 * @param isoClient
	 */
	public RecTxnsTask(ISO8583Client isoClient) {
		this.isoClient = isoClient;
		Thread t = new Thread(this);
		t.setName("RecTxnsTask");
		//checking current threads count
		t.start();
	}

	/**
	 * Запуск потока прослушки ответов сервера
	 */
	public void run() {
		if (sConf == null)
			sConf = SystemConfigurator.getInstance();
		logCommon.log(Level.INFO, "[REC] Reciever message Task started.");

		//logRec.log(Level.ALL, "TXN_TYPE;FE_TRN_ID;CUSTOMER_ID;PROCESSING_TIME;STATUS");
		while (true) {
			if (!isoClient.isConnected()) {
			    /*long sleepT = 1000;
			    loadStat.setIncrementConnectTotalFailed();
				logCommon.log(Level.WARNING,"[REC] RecTxnsTask: Error during open message from server. Can not connect. Sleep time: " + sleepT + " milliseconds and continue.");
				isoClient.connect();
				sleep(sleepT);
				continue;*/
			} else
				try {
					ISOMsg msg = isoClient.receive();


					//fixme comment before load  ; What should we fix?
//					String msgPartStr = "";
//					logCommon.log(Level.INFO, "***** RECIEVING ISO MESSAGE: *******");
//					for (int i = 0; i < 199; i++) {
//						try {
//							for (int j = 0; j < msg.getBytes(i).length; j++) {
//								msgPartStr += (char) msg.getBytes(i)[j];
//							}
//							logCommon.log(Level.INFO, String.valueOf(i) + " : " + msgPartStr);
//							msgPartStr = "";
//						} catch (Exception e) {
//
//						}
//					}
//					logCommon.log(Level.INFO, "************");

					long TxnResponceTime = 0; //время отклика
					//отправка ответа на network connection
					if (msg.getMTI().matches("1804")) {
						printFileRecord(SystemConfigurator.getInstance().RecLogFullPath, LoadGenerator
								.getFormattedDate(new Date(), SystemConfigurator.getInstance().timeFormat) + ";1804;0;0;000");
						send1804NetConn(msg);
						loadStat.setIncrementSendTxnsTotal();
					} else {
						TxnResponceTime = new Date().getTime() - TransactionMap.getInstance()
						                                                       .getTxnMap(Long.parseLong(msg.getValue(37).toString()));
						printFileRecord(SystemConfigurator.getInstance().RecLogFullPath, LoadGenerator
								.getFormattedDate(new Date(), SystemConfigurator.getInstance().timeFormat) + ";" + msg.getMTI() + ";" + msg
								.getValue(37) + ";" + msg.getValue(102) + ";" + TxnResponceTime + ";" + msg.getValue(39));
					}

					loadStat.setIncrementRecTxnsTotal();
					//Q.

					if (msg.hasField(39)) { // added 1 line Isaev - NULL exceptions
						String respCodeValue = msg.getValue(39).toString();
						RespCode msgRespCode = RespCode.get(respCodeValue);
						//System.out.println(msg.getValue(39));
						//System.out.print("37:" + msg.getValue(37));
						//System.out.print("; MTI=" + msg.getMTI());
						//System.out.print("; Code=" + msgRespCode);
						//System.out.println("; CodeValue=" + msgRespCode.getCodeValue());
						//String TEST = ;
						String field37Value = (msg.getValue(37) == null) ? "" : (String) msg.getValue(37);
						logCommon.log(Level.INFO, "[REC] Recieved message: Response: "
								+ msgRespCode.getCodeValue()  /*+respCodeValue*/
								+ ", Type:" + msg.getMTI() + ", FETransactionId:0000" + field37Value);

						if (msgRespCode.equals(RespCode.POSSIBLE_FRAUD)) {  //только RespCode.POSSIBLE_FRAUD
							if (msg.getMTI().matches("1181")) {   //timeout
								loadStat.setIncrementRecTxnsFailed();
							} else {                           //RespCode.POSSIBLE_FRAUD
								//todo увеличить фрод счетчик ; Done
								loadStat.setIncrementRecTxnsFraud();
							}
						} else if (msg.getMTI().matches("1804") || msg.getMTI().matches("1814")) { //connect
							loadStat.setIncrementRecTxnsSuccess();
						} else if (RespCode.containsCode(respCodeValue)) { //другие коды ответа
							if (RespCode.containsFraudCode(respCodeValue)) {  //frauds
								//todo увеличить фрод счетчик ; Done
								loadStat.setIncrementRecTxnsFraud();
							}

							if (TxnResponceTime >= LoadGenerator.bConf.TXN_FAIL_RESPONSE_TIMEOUT) {
								loadStat.setIncrementRecTxnsFailed();
							} else {
								loadStat.setIncrementRecTxnsSuccess();
							}
						} else {
							logCommon.log(Level.WARNING, "[REC] Recieved with unknown response: " + respCodeValue);
							loadStat.setIncrementRecTxnsFailed();
						}
					}

				} catch (NullPointerException e) {
					e.printStackTrace();
					logCommon.log(Level.WARNING, "[REC] Null value was received from iso channel. INFO: " + e
							.getMessage());
					loadStat.setIncrementRecTxnsFailed();
					//System.exit(0);
				} catch (ISOException e) {
					logCommon
							.log(Level.WARNING, "[REC]ISOException was received from channel. INFO: " + e.getMessage());
					e.printStackTrace();
					loadStat.setIncrementRecTxnsFailed();
				} catch (IOException e) {
					logCommon.log(Level.WARNING, "[REC]IOException was received from channel. INFO: " + e.getMessage());
					loadStat.setIncrementRecTxnsFailed();
				} catch (Exception e) {
					logCommon.log(Level.WARNING, "[REC] Unknown Exception was received from channel. INFO: " + e.getMessage());
					e.printStackTrace();
					loadStat.setIncrementRecTxnsFailed();
				}
		}
	}

	/**
	 * Отправка сообщения 1804
	 *
	 * @param msg
	 * @return
	 */
	public int send1804NetConn(ISOMsg msg) {

		if (!isoClient.isConnected()) {
			logCommon.log(Level.WARNING, "[REC] There is no connection between ISO8583 client and server");
			return -8;
		}

		try {

			ISO87PackagerExt packager = new ISO87PackagerExt();
			msg.setPackager(packager);

			msg.setMTI("1814");
			msg.set(11, msg.getString(11));
			msg.set(12, msg.getString(12));
			msg.set(24, msg.getString(24));
			msg.set(39, RespCode.OK.getCodeValue());

		} catch (ISOException e) {
			logCommon.log(Level.WARNING, e.getMessage());
			return -6;
		}

		String msgPartStr = "";
		logCommon.log(Level.INFO, "[REC] ****** POSTING NET CONNECT MSG ******");
		for (int i = 0; i < 199; i++) {
			try {
				for (int j = 0; j < msg.getBytes(i).length; j++) {
					msgPartStr += (char) msg.getBytes(i)[j];
				}
				logCommon.log(Level.INFO, String.valueOf(i) + " : " + msgPartStr);
				msgPartStr = "";
			} catch (Exception e) {

			}
		}
		logCommon.log(Level.INFO, "[REC] ************");


		int statusSendTxn = isoClient.sendMsg(msg);

		if (statusSendTxn < 0) {

			switch (statusSendTxn) {
				case -1:
					logCommon.log(Level.WARNING, "[REC] Не установлен packager для упаковки сообщений. " +
							"Это необходимо делать в конструкторе");
					break;
				case -2:
					logCommon.log(Level.WARNING, "[REC] Socket Error");
					break;
				case -3:
					logCommon.log(Level.WARNING, "[REC] I/O ERROR");
					break;
				case -4:
					logCommon.log(Level.WARNING, "[REC] Ошибка во время подготовки к отправке сообщения");
					break;
				case -5:
					logCommon.log(Level.WARNING, "[REC] Connection lost");
					break;
				case -6:
					logCommon.log(Level.WARNING, "[REC] Error of calculating BitMap");
					break;
			}
			return -11;
		}

		logCommon.log(Level.INFO, "[REC] Connection approved");

		return 0;
	}


}
