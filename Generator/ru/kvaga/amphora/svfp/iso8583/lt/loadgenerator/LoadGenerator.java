package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator;
/**
 * Генератор нагрузки на ISO8583 сервер.
 * 1. Входные данные: считывает идентификаторы абонентов (customerId) с из:
 * 	1.1. dataPool/VCCustomerPool.csv - виртаульные карты
 *  2.2. dataPool/PCCustomerPool.csv - пластиковые карты
 * 2. Настрока:
 * 	2.1. conf/system.cfg - системные настройки приложения
 * 	2.2. conf/scenario.cfg - настройки сценария нагрузки. Описание того, как будут развиваться события при нагрузки. 
 *  @version 2011-04-28 12:50
 */

import ru.kvaga.amphora.svfp.iso8583.lt.client.ISO8583Client;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.*;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.gui.GUIExecuter;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.isoutil.RandomISOFields;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.profile.ProfileGenerator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.statistic.LoadStatistic;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.tasks.RecTxnsTask;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.tasks.UpdateTPSLogFileTask;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util.ConsolePrint;
import ru.kvaga.amphora.svfp.loadGenerator.log.LogInit;
import ru.kvaga.amphora.svfp.lt.persistence.Storage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util.FilePrinter.printFileRecord;

public class LoadGenerator {
	// true-Прекращение выполнения сценария
	public static boolean              stopScenario    = false;
	// true-Приостановка/false-возобновление сценария
	public static boolean              suspendScenario = true;
	// Настройки
	public static CountryConfigurator  cConf           = CountryConfigurator.getInstance();
	public static BusinessConfigurator bConf           = BusinessConfigurator.getInstance();
	public static ProfileConfigurator  pConf           = ProfileConfigurator.getInstance();
	public static SystemConfigurator   sConf           = SystemConfigurator.getInstance();
	public static DefaultConfigurator  dConf           = DefaultConfigurator.getInstance();
	public static Logger logCommon;

//TODO прокомментировать все методы

	/**
	 * Get instance of Configuration
	 *
	 * @param configFileSystem - configuration file
	 * @return 0 - success <br>
	 *         -1 - error while initializing
	 */
	public int configuration(String configFileSystem, String configFileBusiness, String configFileProfile, String configFileCountry) {
		int res = sConf.loadProperties(configFileSystem);
		logFileIOError(res, configFileSystem);
		res = bConf.loadProperties(configFileBusiness);
		logFileIOError(res, configFileBusiness);
		res = pConf.loadProperties(configFileProfile);
		logFileIOError(res, configFileProfile);
		res = cConf.loadProperties(configFileCountry);
		logFileIOError(res, configFileCountry);
		res = dConf.loadProperties(dConf.DEFAULT_FILE_NAME_FOR_LOAD_PROPERTIES);

		return 0;
	}

	/**
	 * Вывод I/O ошибки
	 * @param res
	 * @param fileName
	 */
	private void logFileIOError(int res, String fileName) {
		if (res < 0) {
			switch (res) {
				case -1:
					System.err
					      .println("[ERROR][CONFIG] Конфигурационный файл <" + fileName + "> не найден.");
					break;
				case -2:
					System.err
					      .println("[ERROR][CONFIG] Ошибка ввода.вывода при работе с файлом <" + fileName + ">");
					break;
				default:
					System.err
					      .println("[ERROR][CONFIG] Другая ошибка при работе с файлом <" + fileName + ">");
			}
		}
	}

	/**
	 * Чтение данных из файлов по умолчанию: dataPool/VCCustomerPool.txt и dataPool/PCCustomerPool.txt
	 * Формат файлов: Один столбец. Столбец называется customerId. Каждая последующая строка является
	 * идентификатором абонента в Paybox: customerId.
	 *
	 * @return 0 - успешное чтение и обработка данных в файлах<br>
	 *         -1 - для файла VC: первый столбец не имеет названия customerId<br>
	 *         -2 - для файла VC: файл dataPool/VCCustomerPool.txt не найден<br>
	 *         -3 - для файла VC: ошибка ввода\вывода для файла dataPool/VCCustomerPool.txt<br>
	 *         -4 - для файла VC: после чтения файла dataPool/VCCustomerPool.txt datapool приложения не был наполнен. Возможно, файл пуст или заполнен некорректными данными.<br>
	 *         -5 - для файла PC: первый столбец не имеет названия customerId<br>
	 *         -6 - для файла PC: файл dataPool/PCCustomerPool.txt не найден<br>
	 *         -7 - для файла PC: ошибка ввода\вывода для файла dataPool/PCCustomerPool.txt<br>
	 *         -8 - для файла PC: после чтения файла dataPool/PCCustomerPool.txt datapool приложения не был наполнен. Возможно, файл пуст или заполнен некорректными данными.<br>
	 */
	public static int readDataPoolsSingleColumn(String dataPoolVC, String dataPoolPC) {

		// dataPoolVCCustomers
		if (dataPoolVCCustomers == null) dataPoolVCCustomers = new ArrayList<String>();
		try {
			BufferedReader inVC = new BufferedReader(
					new FileReader(new File(dataPoolVC).getAbsoluteFile()));
			String sVC = inVC.readLine();
			if (sVC != null) {
				if (!sVC.equals("customerId")) {
					inVC.close();
					return -1;
				}
			}
			sVC = inVC.readLine();
			while ((sVC != null) && (!sVC.equals(""))) {
				dataPoolVCCustomers.add(sVC);
				sVC = inVC.readLine();
			}
			if (dataPoolVCCustomers.size() == 0) {
				inVC.close();
				return -4;
			}
			inVC.close();
		} catch (FileNotFoundException e) {
			ConsolePrint.printErr(e.getMessage());
			return -2;
		} catch (IOException e) {
			ConsolePrint.printErr(e.getMessage());
			return -3;
		}


		// dataPoolPCCustomers
		if (dataPoolPCCustomers == null) {
			dataPoolPCCustomers = new ArrayList<String>();
		}
		try {
			BufferedReader inPC = new BufferedReader(
					new FileReader(new File(dataPoolPC).getAbsoluteFile()));
			String sPC = inPC.readLine();
			if (sPC != null) {
				if (!sPC.equals("customerId")) {
					inPC.close();
					return -5;
				}
			}
			sPC = inPC.readLine();
			while ((sPC != null) && (!sPC.equals(""))) {
				dataPoolPCCustomers.add(sPC);
				sPC = inPC.readLine();
			}
			if (dataPoolPCCustomers.size() == 0) {
				inPC.close();
				return -8;
			}
			inPC.close();
		} catch (FileNotFoundException e) {
			ConsolePrint.printErr(e.getMessage());
			return -6;
		} catch (IOException e) {
			ConsolePrint.printErr(e.getMessage());
			return -7;
		}

		return 0;
	}

	/**
	 * Установка текста с используемыми, приложением ключами запуска
	 * @return String
	 */
	public static String usage() {
		return "Usage:\n" +
				"java LoadGenerator [-cfs|-cfb|-cfc|-help]\n" +
				"-cfs=configFileSystem - uses if need change default system configFile. " +
				"Default configFileSystem=conf/ISO8583.LT.LoadGenerator.System.cfg\n" +
				"-cfb=configFileBusiness - uses if need change default business configFile. " +
				"Default configFileSystem=conf/ISO8583.LT.LoadGenerator.Business.cfg\n" +
				"-cfp=configFileProfile - uses if need change default profile configFile. " +
				"Default configFileSystem=conf/ISO8583.LT.LoadGenerator.Profile.cfg\n" +
				"-cfc=configFileCountry - uses if need change default country configFile. " +
				"Default configFileSystem=conf/countryConfiguration.cfg\n" +
				"-help or --help - shows description\n";
	}

	/**
	 * Установка сообщения об ошибке
	 * @param logCommon
	 * @param res
	 */
	private static void setErrorMessage(Logger logCommon, int res) {
		switch (res) {
			case -1:
				logCommon
						.log(Level.WARNING, "[ERROR][DATA POOL] 1 строка в файле datapool должна быть: customerId");
				break;
			case -2:
				logCommon
						.log(Level.WARNING, "[ERROR][DATA POOL] Файл не найден");
				break;
			case -3:
				logCommon
						.log(Level.WARNING, "[ERROR][DATA POOL] I/O error");
				break;
			case -4:
				logCommon
						.log(Level.WARNING, "[ERROR][DATA POOL] error afrer read file: datapool приложения не был наполнен. Возможно, файл пуст или заполнен некорректными данными.");
				break;
		}
	}

	/**
	 * Точка запуска приложения
	 * @param args
	 */
	public static void main(String[] args) {
		String configFileSystem = "conf/ISO8583.LT.LoadGenerator.System.cfg";
		String configFileProfile = "conf/ISO8583.LT.LoadGenerator.Profile.cfg";
		String configFileBusiness = "conf/ISO8583.LT.LoadGenerator.Business.cfg";
		String configFileCountry = "conf/countryConfiguration.cfg";
		// Убирает warning "Initialize log4j", но, будучи ненастроенной, выкидывает в лог лишний мусор
		//      org.apache.log4j.BasicConfigurator.configure();
		for (String arg : args) {
			if (arg.startsWith("-cfs=")) {
				configFileSystem = arg.replaceFirst("-cfs=", "");
			} else if (arg.startsWith("-cfb=")) {
				configFileBusiness = arg.replaceFirst("-cfb=", "");
			} else if (arg.startsWith("-cfp=")) {
				configFileProfile = arg.replaceFirst("-cfp=", "");
			} else if (arg.startsWith("-cfc=")) {
				configFileCountry = arg.replaceFirst("-cfc=", "");
			} else if (arg.startsWith("-help") || arg.equals("--help")) {
				System.out.println(LoadGenerator.usage());
				return;
			} else {
				System.out.println(LoadGenerator.usage());
			}
		}

		LoadGenerator loadGenerator = new LoadGenerator();

		// Инизиализация конфигураторов
		if (loadGenerator.configuration(configFileSystem, configFileBusiness, configFileProfile, configFileCountry) < 0) {
			System.err.print("[ERROR][CONFIGURATOR] The problem was encountered during set configurator.");
			return;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss");
		if (sConf.LOG_FILE_USE_DEFAULT_LOG_PATH_AND_NAMES)
			sConf.LOG_FILE_PATH = "log/" + sdf.format(new Date());

		if (!LogInit.setLoggerFile(LogInit.LOGGER_NAME_COMMON, sConf.LOG_FILE_PATH, sConf.LOG_FILE_COMMON)) {
			System.err.println("[ERROR][LOGGER COMMON] Ошибка во время инициализации COMMON LOGGER");
			return;
		}
		if (!LogInit.setLoggerFile(LogInit.LOGGER_NAME_RECEIVER, sConf.LOG_FILE_PATH, sConf.LOG_FILE_RECEIVER)) {
			System.err.println("[ERROR][LOGGER RECEIVER] Ошибка во время инициализации RECEIVER LOGGER");
			return;
		}
		if (!LogInit.setLoggerFile(LogInit.LOGGER_NAME_SENDER, sConf.LOG_FILE_PATH, sConf.LOG_FILE_SENDER)) {
			System.err.println("[ERROR][LOGGER SENDER] Ошибка во время инициализации SENDER LOGGER");
			return;
		}
		if (!LogInit.setLoggerFile(LogInit.LOGGER_NAME_TPS, sConf.LOG_FILE_PATH, sConf.LOG_FILE_TPS)) {
			System.err.println("[ERROR][LOGGER TPS] Ошибка во время инициализации TPS LOGGER");
			return;
		}

		Storage storage = Storage.getInstance();
		try {
			if (storage.init(new File(sConf.FILE_PERSISTENCE_TXN_TRACE_NUMBER), Storage.DEFAULT_CONF_TIMEOUT) < 0)
				return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		logCommon = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
		logCommon.setLevel(Level.INFO);
		logCommon.log(Level.INFO, "[MAIN] Reading FILE_PERSISTENCE_TXN_TRACE_NUMBER completeds.");


		logCommon.setLevel(Level.INFO);
		logCommon.log(Level.INFO, "[MAIN] Logging enabled.");

		//Logger logREC = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
		Logger logSender = LogInit.getLogger(LogInit.LOGGER_NAME_SENDER);
		Logger logTps = LogInit.getLogger(LogInit.LOGGER_NAME_TPS);

		// Чтение пула данных
		logCommon.log(Level.INFO, "[MAIN] Reading datapool...");

		int res = bConf.loadProperties(configFileBusiness);
		if (res < 0) {
			setErrorMessage(logCommon, res);
			return;
		}
		logCommon.log(Level.INFO, "[MAIN] Reading Datapools completed.");
		//logSender.log(Level.ALL, "TXN_TYPE;FE_TRN_ID;CUSTOMER_ID");
		SystemConfigurator.getInstance().RecLogFullPath = sConf.LOG_FILE_PATH + "/" + sConf.LOG_FILE_RECEIVER;
		SystemConfigurator.getInstance().SendLogFullPath = sConf.getInstance().LOG_FILE_PATH + "/" + sConf.getInstance().LOG_FILE_SENDER;
		SystemConfigurator.getInstance().TPSLogFullPath = sConf.getInstance().LOG_FILE_PATH + "/" + sConf.getInstance().LOG_FILE_TPS;
		printFileRecord(SystemConfigurator.getInstance().SendLogFullPath, "DATE_TIME;TXN_TYPE;FE_TRN_ID;CUSTOMER_ID");

		printFileRecord(SystemConfigurator.getInstance().RecLogFullPath, "DATE_TIME;TXN_TYPE;FE_TRN_ID;CUSTOMER_ID;PROCESSING_TIME;STATUS");

		// Создание клиента ISO8583 сервера
		ISO8583Client isoClient = null;
		try {
			isoClient = new ISO8583Client(sConf.ISO_SERVER_HOST, sConf.ISO_SERVER_PORT,
			                              sConf.SYS_ISO_SERVER_CONNECTION_TIME_OUT);
			if (isoClient == null) {
				throw new IOException("Can not create ISO8583 client with params:");
			}
			logCommon.log(Level.INFO, "[MAIN] ISO8583 client created with params:"
					+ " ISO_SERVER_HOST:" + sConf.ISO_SERVER_HOST
					+ " ISO_SERVER_PORT:" + sConf.ISO_SERVER_PORT
					+ " SYS_ISO_SERVER_CONNECTION_TIME_OUT:" + sConf.SYS_ISO_SERVER_CONNECTION_TIME_OUT);

		} catch (IOException e) {
			logCommon.log(Level.WARNING, "[MAIN] Can not connect to ISO8583 server with params: " +
					" ISO_SERVER_HOST:" + sConf.ISO_SERVER_HOST +
					" ISO_SERVER_PORT:" + sConf.ISO_SERVER_PORT +
					" SYS_ISO_SERVER_CONNECTION_TIME_OUT:" + sConf.SYS_ISO_SERVER_CONNECTION_TIME_OUT);
			e.printStackTrace();
			return;
		}
		int connectResult = isoClient.connect();

		if (connectResult < 0) {
			switch (connectResult) {
				case -1:
					logCommon.log(Level.WARNING, "[MAIN] Error during open connection");
					break;
				case -2:
					logCommon.log(Level.WARNING, "[MAIN] Object ISO8583Client'а предварительно не создавался");
					break;
			}
			return;
		} else if (connectResult > 0) {
			switch (connectResult) {
				case 1:
					logCommon.log(Level.INFO, "[MAIN] Connection opened");
					break;
			}
		}
		logCommon.log(Level.INFO, "[MAIN] Connection to ISO8583 server opened with params: " +
				"ISO_SERVER_HOST:" + sConf.ISO_SERVER_HOST + " ISO_SERVER_PORT:" + sConf.ISO_SERVER_PORT + " SYS_ISO_SERVER_CONNECTION_TIME_OUT:" + sConf.SYS_ISO_SERVER_CONNECTION_TIME_OUT);


		//24.08.2012 try to add restart functionality
		int RESTART_GENERATOR_THREADS_COUNT = sConf.RESTART_GENERATOR_THREADS_COUNT;
		setMaxTPSValue(sConf.MAX_TPS);
		setTickerValue(sConf.LOAD_STEP_SIZE);
		//loadGenerator.stopScenario=sConf.STOP_LOAD_AFTER_GENERATOR_START;
		//end of 24.08.2012


		// Создание профиля
		ProfileGenerator profileGenerator = ProfileGenerator.getInstance(isoClient);
		// Запуск задачи получения сообщений от сервера ISO
		new RecTxnsTask(isoClient);
		// Запуск задачи обновления лога TPS;
		new UpdateTPSLogFileTask(sConf.UPDATE_TPS_LOG_FILE_TASK_SLEEP_IN_MILLIS);
		// Запуск GUI, который руководит всем процессом тестирования.
		new GUIExecuter(loadGenerator);


		// Запуск самого генератора (основной цикл работы)
		LoadStatistic loadStatistic = LoadStatistic.getInstance();
		reqTPS = bConf.getTPS();

		// Проверка доступности сервера в диалекте ISO
		// ...

		// Устанавливаем уровень логирования перед тестом.
		logCommon.setLevel(sConf.LOG_LEVEL_ON_LOAD.equals("INFO") ? Level.INFO : Level.WARNING);


		long sleepT = 1000; //time to sleep until connect to server

		int threadSleepCount = 1000,
				maxThreadCount = 50,
				THREAD_SLEEP_TIME_MILLIS = 100;
		boolean isThreadSleep = false;
		if (sConf.IS_THREADS_COUNT_MONITOR) {
			if (sConf.MAX_THREADS_COUNT > 10) {
				isThreadSleep = true;
				maxThreadCount = sConf.MAX_THREADS_COUNT;
				THREAD_SLEEP_TIME_MILLIS = sConf.THREAD_SLEEP_TIME_MILLIS;
			}
		}
		boolean IS_DISCONNECT_ISO_CHANNEL = sConf.DISCONNECT_ISO_CHANNEL;
		int WAIT_REC_TXN_AFTER_STOP_LOAD = sConf.WAIT_REC_TXN_AFTER_STOP_LOAD;

		//sleep to 2000 millis before using Generator to avoid failed first txns
		sleep(1000);

		long scenarioStartTime = 0; //clear timer
		int CurrentCountOfWait_sec = 0;

		//START LOAD HERE!!!
		while (!stopScenario) {

			// Поддержание требуемого TPS
			if (reqTPS == 0) {
				long sleepTimeIfTPSZero = 1000;
				logCommon.log(Level.WARNING, "[MAIN] Load stopped... TPS=0. Wait " + Long.toString(sleepTimeIfTPSZero) + " milliseconds.");
				sleep(sleepTimeIfTPSZero);
			} else {

				//System.out.println("reqTPS="+reqTPS+"; sleep((long) ("+1000 / reqTPS+");");
				//sleep((long) (1000 / reqTPS));
				//long startTime = System.nanoTime();
				sleepTPS(reqTPS);
				//logCommon.warning("real sleep "+(double)(System.nanoTime()-startTime)/1000000+"ms; should sleep "+(double)(1000 / reqTPS)+ "ms");

				//if not connected, then try to reconnect
				//commented 24.08.2012
				if (!isoClient.isConnected()) {
					loadStatistic.setIncrementConnectTotalFailed();
					logCommon
							.log(Level.WARNING, "[MAIN] Not connected to ISO8583server. Sleep time: " + sleepT + " milliseconds and continue.");
					isoClient.connect();
					sleep(sleepT);
					//continue;
				}
				//logCommon.log(Level.WARNING,"TPS= "+reqTPS+" maxTPS="+MaxTPS+" Ticker="+Ticker);

				//threads monitoring (log WARNING and sleep THREAD_SLEEP_TIME_MILLIS milliseconds)
				if (isThreadSleep)
					if (java.lang.Thread.activeCount() > maxThreadCount) {
						logCommon.log(Level.WARNING, "[MAIN]  Active thread count:" + Integer
								.toString(java.lang.Thread.activeCount()) + " max=" + Integer
								.toString(maxThreadCount) + ". Sleep load for " + Integer
								.toString(THREAD_SLEEP_TIME_MILLIS) + " millis.");
						sleep(THREAD_SLEEP_TIME_MILLIS);
					   /* if(java.lang.Thread.activeCount()>=1000){
					        logCommon.log(Level.WARNING, "Printing stack trace:");
					    	  StackTraceElement[] elements = Thread.currentThread().getStackTrace();
					    	  for (int i = 1; i < elements.length; i++) {
					    	    StackTraceElement s = elements[i];
					    	    logCommon.log(Level.WARNING,"\tat " + s.getClassName() + "." + s.getMethodName()
					    	        + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
					    	  }
					    }*/
					}
				//24.08.2012 Restart load if RESTART_GENERATOR_THREADS_COUNT > 0
				if (RESTART_GENERATOR_THREADS_COUNT > 0) {
					if (java.lang.Thread.activeCount() >= RESTART_GENERATOR_THREADS_COUNT) {
						stopScenario = true;
						logCommon
								.log(Level.WARNING, "[MAIN] Stop scenario. Reason: too many active threads in Generator(" + Integer
										.toString(java.lang.Thread.activeCount()) + ")");
						setTPS(0);

						//2012/09/18 logCommon.log(Level.WARNING, "[MAIN] Wait 10 sec for REC last txns or  send txn count< rec txns count.");
						//2012/09/18 loadGenerator.sleep(10*1000);
						CurrentCountOfWait_sec = 0;
						logCommon.log(Level.WARNING, "[MAIN] Sent txns count(" + Integer
								.toString(loadStatistic.getSendTxnsTotal()) + ") > Rec txns count(" + Integer
								.toString(loadStatistic.getRecTxnsTotal()) + "). Wait " + Integer
								.toString(WAIT_REC_TXN_AFTER_STOP_LOAD) + " sec or " + Integer
								.toString(loadStatistic.getSendTxnsTotal() - loadStatistic
										.getRecTxnsTotal()) + " txn's responces");
						//wait until txns total still == send txns total or configured time
						if (loadStatistic.getSendTxnsTotal() > loadStatistic.getRecTxnsTotal()) {
							while ((loadStatistic.getSendTxnsTotal() > loadStatistic
									.getRecTxnsTotal()) && (CurrentCountOfWait_sec < WAIT_REC_TXN_AFTER_STOP_LOAD)) {
								sleep(1000);
								CurrentCountOfWait_sec = CurrentCountOfWait_sec + 1;
							}
						}
						//CurrentCountOfWait_sec = 0;
						//if rec txns total still < send txns total
			            /*2009/09/18
                        if(loadStatistic.getSendTxnsTotal()>loadStatistic.getRecTxnsTotal()) {
							logCommon.log(Level.WARNING, "[MAIN] Sent txns count("+Integer.toString(loadStatistic.getSendTxnsTotal())+") > Rec txns count("+Integer.toString(loadStatistic.getRecTxnsTotal())+"). Wait "+Integer.toString(WAIT_REC_TXN_AFTER_STOP_LOAD)+" sec or all txn rec. and restart connection and load.");
								loadGenerator.sleep(WAIT_REC_TXN_AFTER_STOP_LOAD*1000);	
						}
						*/
						logCommon.log(Level.WARNING, "[MAIN] Txns count in wait status=" + Integer
								.toString(loadStatistic.getSendTxnsTotal() - loadStatistic.getRecTxnsTotal()) + ".");

						if (IS_DISCONNECT_ISO_CHANNEL) {
							isoClient.disconnect();
							logCommon.log(Level.WARNING, "[MAIN] try to reconnect ISOChannels.");
							connectResult = isoClient.connect();
							if (connectResult < 0) {
								switch (connectResult) {
									case -1:
										logCommon.log(Level.WARNING, "[MAIN] Error during open connection");
										break;
									case -2:
										logCommon.log(Level.WARNING, "[MAIN] ISO8583Client object not exists");
										break;
								}
								return;
							} else if (connectResult > 0) {
								switch (connectResult) {
									case 1:
										logCommon.log(Level.WARNING, "[MAIN] Connected to ISO server.");
										break;
								}
							}
						}
						logCommon.log(Level.WARNING, "[MAIN] Set TPS =1 and restart load.");
						logCommon.log(Level.WARNING, "[MAIN] Active threads count in Generator=" + Integer
								.toString(java.lang.Thread.activeCount()) + ";");
						stopScenario = false;
						setTPS(1);

					}
				}
				//end of 24.08.2012

			}


			// Приостановка теста
			while (suspendScenario) {
				scenarioStartTime = 0;
				setTPS(0);
				logCommon.log(Level.ALL, "[MAIN] Load stoped.... waiting load");
				sleep(sConf.SYS_SLEEP_IF_LOAD_SUSPENDED_MILLIS);
				try {
					storage.storeTraceCurrentTraceNumber();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			//setting time start scenario
			if (scenarioStartTime <= 0) {
				Date date = new Date();
				scenarioStartTime = new Date().getTime();
				logCommon.log(Level.ALL, "[MAIN] Scenario start time: " + getFormattedDate(date, "yyyy/MM/dd HH:mm:ss"));
				loadStatistic.setTimeStartScenario(getFormattedDate(date, "yyyy/MM/dd HH:mm:ss"));
			}


			// Тело сценария
//			new Send1100TxnTask(isoClient,
//					loadGenerator.dataPoolVCCustomers.get(0), "333333", "000000", "007", "B8");
			//if(profileGenerator.sendNextMessageRandom()<0) logCommon.log(Level.WARNING, "Operation ProfileGenerator.sendNextMessageRandom() failed.");
			//if(profileGenerator.sendNextSequentially()<0) logCommon.log(Level.WARNING, "Operation ProfileGenerator.sendNextSequentially() failed.");

			int sendResult = profileGenerator.sendNextPercentProfile();
			if (sendResult == -1) {
				logCommon
						.log(Level.WARNING, "[MAIN] Operation ProfileGenerator.sendNextPercentProfile().getNextCountryProfile() failed. Please change country profile conf file.");
				return;
			}
			if (sendResult == -2) {
				logCommon
						.log(Level.WARNING, "[MAIN] Operation ProfileGenerator.sendNextPercentProfile().getNextPercentProfile() failed. Please change operations profile conf file.");
				return;
			}

//			if(profileGenerator.sendConcreteMessage(PGOperations.CASH_OUT_ATM_1420_PC)<0){
//				logCommon.log(Level.WARNING, "Operation ProfileGenerator.sendConcreteMessage() failed.");
//				return;
//			}
//			loadGenerator.sleep(10000);
		}
	}

	/**
	 * Задержка потока для поддержания уровня нагрузки (TPS)
	 * @param reqTPS
	 */
	private static void sleepTPS(double reqTPS) {
		try {
			long starttime = System.nanoTime();
			double sleepTime = 1000 / reqTPS;
			long goalTime=starttime+(long)(sleepTime*1000000);
			long currentSleepTime=starttime;
			long sleepTimerCount = 0;
			while(currentSleepTime<goalTime){
				// шумовые операции для снижения нагрузки на CPU, если возможно, необходимо заменить на какую-нибудь паузу
				// пауза Thread.sleep(0,50000) выполняется больше, чем за 0,3 миллисекунды!
				HashMap<Integer,Integer> hash = new HashMap();
				hash.put(1, 10);
				hash.remove(1);
				hash.clear();
				sleepTimerCount++;
				currentSleepTime=System.nanoTime();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Задержка потока для поддержания уровня нагрузки (TPS)
	 * @param reqTPS
	 */
	private static void sleepTPSold(double reqTPS) {
		//Thread.sleep ожидает больше, чем ожидается на примерно на 0,3 миллисекунды из-за таймеров Операционной системы
		try {
			double d = 1000 / reqTPS;
			long millis = (long) (d);
			int nanos = (int) ((d - Math.floor(d)) * 1000000);
//			String test = String.format("d = %10.6f, millis %d, nanos %d", d, millis, nanos);
//			System.out.println(test);

			//long startTime = System.nanoTime();
			Thread.sleep(millis, nanos);
			//logCommon.warning("real sleep "+(double)(System.nanoTime()-startTime)/1000000+"ms; should sleep "+(double)(1000 / reqTPS)+ "ms; "+"d = "+d+", millis "+(double)millis+", nanos "+nanos);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Форматирование даты
	 * @param date
	 * @param format
	 * @return
	 */
	public static String getFormattedDate(Date date, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}

	/**
	 * Задержка потока
	 * @param sleep
	 */
	private static void sleep(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Transaction's data pools
	 */
	private static ArrayList<String> dataPoolVCCustomers = null;
	private static ArrayList<String> dataPoolPCCustomers = null;

	private static double reqTPS    = 0;
	public static  double MaxTPS    = 0;
	public static  double Ticker    = 0;
	private static long   TestTxnID = 0;

	/**
	 * Установка значения текущего значения TPS
	 * @param reqTPSValue
	 */
	public static void setTPS(double reqTPSValue) {
		reqTPS = reqTPSValue;
	}

	/**
	 * Увеличение номера TestTxnID
	 */
	public static void incrementTestTxnID() {
		TestTxnID++;
	}

	/**
	 * Очистка номера TestTxnID
	 */
	public static void resetTestTxnID() {
		TestTxnID = 0;
	}

	/**
	 * Получение номера TestTxnIDtoString
	 * @return String
	 */
	public static String getTestTxnID() {
		String TestTxnIDtoString = "";
		for (int i = 0; i < Long.toString(TestTxnID).length(); i++) {
			TestTxnIDtoString = "0" + Long.toString(TestTxnID);
		}
		return TestTxnIDtoString;
	}

	/**
	 * Установка максимального значения TPS
	 * @param MaxTPSValue
	 */
	public static void setMaxTPSValue(double MaxTPSValue) {
		MaxTPS = MaxTPSValue;
	}

	/**
	 * Установка шага увеличения reqTPS до MaxTPS
	 * @param TickerValue
	 */
	public static void setTickerValue(double TickerValue) {
		Ticker = TickerValue;
	}

	/**
	 * Получение значения максимального значения TPS
	 * @return
	 */
	public static double getMaxTPSValue() {
		return MaxTPS;
	}

	/**
	 * Получение шага увеличения reqTPS до MaxTPS
	 * @return
	 */
	public static double getTickerValue() {
		return Ticker;
	}

	/**
	 * Получение значения текущего значения TPS
	 * @return
	 */
	public static double getReqTPS() {
		return reqTPS;
	}
}
