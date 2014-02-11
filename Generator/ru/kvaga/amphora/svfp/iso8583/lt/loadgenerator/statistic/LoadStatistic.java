package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.statistic;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;

import java.util.logging.Level;

public class LoadStatistic {

	private int sendTxnsTotal  = 0,
			recTxnsTotal       = 0,
			recTxnsSuccess     = 0,
			recTxnsFailed      = 0,
			currentTPS         = 0, //текущее значение нагрузки в GUI (вычисляется по формуле: текущее кол-во транзакций минус предыдущее)
			recTxnsFraud       = 0,
			connectTotalFailed = 0; //количество отказов сервера ISO8583
	private static long sendTxnsThreads = 0; //количество открытых потоков на отправку сообщений
	private String timeStartScenario;
	private String timeStopScenario;
	@SuppressWarnings("unused")
	private long sumTxnResponceTimes = 0,
				 countTxnResponces = 0;

	private LoadStatistic() {
	}

	private static LoadStatistic loadStatistic = null;

	/**
	 * Получение Статистика нагрузки
	 *
	 * @return LoadStatistic
	 */
	public static LoadStatistic getInstance() {
		if (loadStatistic == null) loadStatistic = new LoadStatistic();
		return loadStatistic;
	}

	/**
	 * Счетчик времени Выполнения потока на отправку транзакции
	 * @param threadLiveTime
	 */
	public static void send1100ThreadFinished(long threadLiveTime) {
		try {
			sendTxnsThreads += threadLiveTime;
		} catch (Exception ex) {
			LoadGenerator.logCommon.log(Level.WARNING, "[LoadStatistic] Exception:" + ex.toString());
		}
	};

	/**
	 * Очистка счетчика потоков на отправку транзакции
	 */
	public void send1100ThreadTimeClean() {
		try {
			sendTxnsThreads = 0;
		} catch (Exception ex) {
			LoadGenerator.logCommon.log(Level.WARNING, "[LoadStatistic] Exception:" + ex.toString());
		}
	};

	/**
	 * Получение текущего количества отработавших потоков на отправку транзакций
	 * @return long
	 */
	public static long getSendTxnsThreadTime() {
		return sendTxnsThreads;
	}

	/**
	 * Получение количества отправленных транзакций
	 *
	 * @return int
	 */
	public int getSendTxnsTotal() {
		return sendTxnsTotal;
	}

	/**
	 * Получение текущего уровня нагрузки (TPS)
	 *
	 * @return int
	 */
	public int getCurrentTPS() {
		return currentTPS;
	}

	/**
	 * Получение количества полученных ответов транзакций
	 *
	 * @return int
	 */
	public int getRecTxnsTotal() {
		return recTxnsTotal;
	}

	/**
	 * Получение количества успешных отправленных транзакций
	 *
	 * @return int
	 */
	public int getRecTxnsSuccess() {
		return recTxnsSuccess;
	}

	/**
	 * Получение количества неуспешных отправленных транзакций
	 *
	 * @return int
	 */
	public int getRecTxnsFailed() {
		return recTxnsFailed;
	}

	/**
	 * Получение количества фродовых отправленных транзакций
	 *
	 * @return int
	 */
	public int getRecTxnsFraud() {
		return recTxnsFraud;
	}

	/**
	 * Получение количества неуспешных соединений с сервером
	 *
	 * @return int
	 */
	public int getConnectTotalFailed() {
		return connectTotalFailed;
	}

	/**
	 * Получение времени старта теста
	 *
	 * @return String
	 */
	public String getTimeStartScenario() {
		return timeStartScenario;
	}
	/**
	 * Получение времени финиша теста
	 *
	 * @return String
	 */
	public String getTimeStopScenario() {
		return timeStopScenario;
	}

	/**
	 * Увеличение количества успешных отправленных транзакций
	 */
	public synchronized void setIncrementSendTxnsTotal() {
		sendTxnsTotal++;
	}

	/**
	 * Увеличение количества полученных ответов транзакций
	 */
	public synchronized void setIncrementRecTxnsTotal() {
		recTxnsTotal++;
	}

	/**
	 * Увеличение количества успешных полученных ответов транзакций
	 */
	public synchronized void setIncrementRecTxnsSuccess() {
		recTxnsSuccess++;
	}

	/**
	 * Увеличение количества неуспешных полученных ответов транзакций
	 */
	public synchronized void setIncrementRecTxnsFailed() {
		recTxnsFailed++;
	}

	/**
	 * Увеличение количества полученных ответов фродовых транзакций
	 */
	public synchronized void setIncrementRecTxnsFraud() {
		recTxnsFraud++;
	}

	/**
	 * Увеличение количества полученных ответов фродовых транзакций
	 */
	public synchronized void setIncrementConnectTotalFailed() {
		connectTotalFailed++;
	}

	/**
	 * Установка значения текущей нагрузки (TPS)
	 */
	public synchronized void setCurrentTPS(int TPS) {
		currentTPS = TPS;
	}

	/**
	 * Установка времени начала подачи нагрузки
	 */
	public synchronized void setTimeStartScenario(String date) {
		this.timeStartScenario = date;
	}

	/**
	 * Установка времени финиша подачи нагрузки
	 */
	public synchronized void setTimeStopScenario(String date) {
		this.timeStopScenario = date;
	}
	/*public String getMaxTPSValue() {
		return null;
	}*/

}
