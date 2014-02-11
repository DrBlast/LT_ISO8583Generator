package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.gui;

import java.awt.Color;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;

import java.util.Date;
import java.util.logging.Level;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.SystemConfigurator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.statistic.LoadStatistic;

public class GUIUpdateWindowTask implements Runnable {
	private GUIGenerator  guiGenerator                         = null;
	private int           frequencyUpdateWindowInTimesInSecond = 1000;
	private LoadStatistic loadStatistic                        = LoadStatistic.getInstance();
	private int           lastTotalNumberSendTransaction       = 0;

	public GUIUpdateWindowTask(GUIGenerator guiGenerator, int frequencyUpdateWindowInTimesInSecond) {
		this.guiGenerator = guiGenerator;
		this.frequencyUpdateWindowInTimesInSecond = frequencyUpdateWindowInTimesInSecond;
		new Thread(this).start();
	}

	/**
	 * Запуск обновления интерфейса
	 */
	public void run() {
		try {
			while (true) {
				int TPSvalue = loadStatistic.getSendTxnsTotal() - lastTotalNumberSendTransaction;
				LoadStatistic.getInstance().setCurrentTPS(TPSvalue);
				int waitTxnsCount = loadStatistic.getSendTxnsTotal() - loadStatistic.getRecTxnsTotal();

				guiGenerator.labelSendTxnsTotal.setText("Send transactions total: " + loadStatistic.getSendTxnsTotal());
				//если значение нагрузки превышает подаваемую нагрузку в 3 раза, то закрасить текст красным цветом
				if (waitTxnsCount > (LoadGenerator.getReqTPS() + 1) * 3)
					guiGenerator.labelWaitTxnsTotal.setForeground(Color.RED);
				else
					guiGenerator.labelWaitTxnsTotal
					            .setForeground(guiGenerator.labelSendTxnsTotal.getForeground());//setBackground(Color.BLACK);
				guiGenerator.labelWaitTxnsTotal.setText("Wait transactions total: " + waitTxnsCount);
				guiGenerator.labelRecTxnsTotal.setText("Received transactions total: " + loadStatistic.getRecTxnsTotal());
				guiGenerator.labelRecTxnsSuccess.setText("Received transactions success: " + loadStatistic.getRecTxnsSuccess());
				if (loadStatistic.getRecTxnsFailed()>0){
					guiGenerator.labelRecTxnsFailed.setForeground(Color.RED);
				}
				guiGenerator.labelRecTxnsFailed.setText("Received transaction failed: " + loadStatistic.getRecTxnsFailed());
				guiGenerator.labelRecTxnsFraud.setText("Received transaction fraud: " + loadStatistic.getRecTxnsFraud());
				if (loadStatistic.getConnectTotalFailed()>0){
					guiGenerator.labelConnectTotalFailed.setForeground(Color.RED);
				}
				guiGenerator.labelConnectTotalFailed.setText("Failed connections total: " + loadStatistic.getConnectTotalFailed());
				guiGenerator.labelTimeStartScenario.setText("Load start time: " + loadStatistic.getTimeStartScenario() + "          ");
				if (alreadyElapsedSecond()) {
					guiGenerator.labelCurrentTPS.setText("Current load (TPS): " + TPSvalue*(1000/frequencyUpdateWindowInTimesInSecond));
					//18/08/2012 write new unc or increase load every 1 sec
					//if (LoadGenerator.suspendScenario == false) {

						if (LoadGenerator.getReqTPS() < LoadGenerator.getMaxTPSValue()) {
							//отображение времени до наступления MaxTPS
							if (LoadGenerator.getTickerValue() == 0 || TPSvalue >= LoadGenerator.getMaxTPSValue()) {
								guiGenerator.labelMaxTPSTime.setText("Max TPS after (sec): 0");
							} else {
								guiGenerator.labelMaxTPSTime.setText("Max TPS after (sec): " +
										                                     (int) ((LoadGenerator.getMaxTPSValue() - LoadGenerator
												                                     .getReqTPS()) / LoadGenerator.getTickerValue()));
							}
							//вычисление следующего значения TPS  на основе заданного Inc. TPS every 1 sec by
							double nextTPS = LoadGenerator.getReqTPS() + LoadGenerator.getTickerValue();
							if (nextTPS <= LoadGenerator.getMaxTPSValue()){
								LoadGenerator.setTPS(nextTPS);
							}
							else {
								LoadGenerator.setTPS(LoadGenerator.getMaxTPSValue());
							}
						}
					//}
				}
				guiGenerator.labelMaxTPS.setText("Max load (TPS): " + LoadGenerator.getMaxTPSValue());
				guiGenerator.labelTicker.setText("Inc. TPS every 1 sec by: " + LoadGenerator.getTickerValue());
				if (LoadGenerator.suspendScenario == false) {
					if (SystemConfigurator.getInstance().IS_THREADS_LIVETIME_MONITOR) {
						LoadGenerator.logCommon.log(Level.WARNING, "Avg SendTxn live time =" + (TPSvalue == 0 ? 0 : LoadStatistic
								.getSendTxnsThreadCount() / TPSvalue) + " ms");
					}
				}
				//получение предыдущего значения уровня нагрузки (TPS)
				lastTotalNumberSendTransaction = loadStatistic.getSendTxnsTotal();
				//очистка счетчика отработавших потоков на отправку транзакций
				loadStatistic.send1100ThreadCountClean();

				if (frequencyUpdateWindowInTimesInSecond > 0)
					sleep(frequencyUpdateWindowInTimesInSecond);
				else
					sleep(1000);
			}
		} catch (Exception ex) {
			LoadGenerator.logCommon.log(Level.WARNING, "[GUIUpdateWindowTask] Exception:" + ex.toString());
		}
	}

	/**
	 * Пауза
	 *
	 * @param sleep
	 */
	private void sleep(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private long lastTimeLong = 0;

	/**
	 * Проверка необходимости увеличения TPS до максимально настроенного
	 *
	 * @return true - можно увеличить TPS
	 */
	private boolean alreadyElapsedSecond() {
		long curTimeLong = new Date().getTime();

		if (curTimeLong - lastTimeLong > 1000) {

			lastTimeLong = curTimeLong;
			return true;
		} else
			return false;
	}
}
