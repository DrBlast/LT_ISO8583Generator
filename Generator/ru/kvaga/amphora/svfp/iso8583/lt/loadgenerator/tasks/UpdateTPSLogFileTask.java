package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.tasks;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.SystemConfigurator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.statistic.LoadStatistic;
import ru.kvaga.amphora.svfp.loadGenerator.log.LogInit;

import static ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util.FilePrinter.printFileRecord;

public class UpdateTPSLogFileTask implements Runnable{
	private long howLongSleepBeforeUpdatesInMillis = 1000;

	/**
	 * Инициализация обновления лога TPS
	 * @param howLongSleepBeforeUpdatesInMillis
	 */
	public UpdateTPSLogFileTask(long howLongSleepBeforeUpdatesInMillis){
		this.howLongSleepBeforeUpdatesInMillis=howLongSleepBeforeUpdatesInMillis;
		Thread t = new Thread(this);
		t.setName("UpdateTPSLogFileTask");
		t.start();
	}

	/**
	 * Запуск потока обновления лога TPS
	 */
	public void run(){
		Logger loggerTps = LogInit.getLogger(LogInit.LOGGER_NAME_TPS);
		Logger loggerCommon = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
		
		LoadStatistic loadStatistic = LoadStatistic.getInstance();
		
		int countRecSuccess=loadStatistic.getRecTxnsSuccess();
		int countSend=loadStatistic.getSendTxnsTotal();
		int tempRecSuccess=0;
		int tempSend=0;


		//loggerTps.log(Level.ALL, "TPS_SEND;TPS_REC_SUCCESS");
		printFileRecord(SystemConfigurator.getInstance().TPSLogFullPath, "DATE_TIME;TPS_SEND;TPS_REC_SUCCESS");

		loggerCommon.log(Level.INFO, "[TPSlog] Задача UpdateTPSLogFileTask (обновление лога TPS) запущена");
		while(true){
			tempRecSuccess = loadStatistic.getRecTxnsSuccess();
			tempSend = loadStatistic.getSendTxnsTotal();
			//loggerTps.log(Level.ALL,""+(tempSend-countSend)+";"+(tempRecSuccess-countRecSuccess));
			printFileRecord(SystemConfigurator.getInstance().TPSLogFullPath, LoadGenerator.getFormattedDate(new Date(), SystemConfigurator.getInstance().timeFormat) + ";"+(tempSend-countSend)+";"+(tempRecSuccess-countRecSuccess));
			countRecSuccess=tempRecSuccess;
			countSend=tempSend;
			sleep(howLongSleepBeforeUpdatesInMillis);
		}
		
	}

	/**
	 * Пауза (миллисекунд)
	 * @param sleep
	 */
	private void sleep(long sleep){
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
