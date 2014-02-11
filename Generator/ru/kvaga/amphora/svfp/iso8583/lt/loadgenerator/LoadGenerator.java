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
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util.YetAnotherSleep;
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
	public static ScheduleConfigurator  shConf          = ScheduleConfigurator.getInstance();
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
	public int configuration(String configFileSystem, String configFileBusiness, String configFileProfile, String configFileCountry, String configFileScheduler) {
        try{
			int res = sConf.loadProperties(configFileSystem);
			logFileIOError(res, configFileSystem);
			res = res + bConf.loadProperties(configFileBusiness);
			logFileIOError(res, configFileBusiness);
			res = res + pConf.loadProperties(configFileProfile);
			logFileIOError(res, configFileProfile);
			res = res + cConf.loadProperties(configFileCountry);
			logFileIOError(res, configFileCountry);
			res = res + shConf.loadProperties(configFileScheduler);
			logFileIOError(res, configFileScheduler);
			res = res + dConf.loadProperties(dConf.DEFAULT_FILE_NAME_FOR_LOAD_PROPERTIES);
			return res;
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
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
	 * Установка текста с используемыми, приложением ключами запуска
	 * @return String
	 */
	public static String usage() {
		return "Usage:\n" +
				"java LoadGenerator [-cfs|-cfb|-cfc|-help]\n" +
				"-cfs=configFileSystem - uses if need change default system configFile. " +
				"Default configFileSystem=conf/ISO8583.LT.LoadGenerator.System.cfg\n" +
				"-cfb=configFileBusiness - uses if need change default business configFile. " +
				"Default configFileBusiness=conf/ISO8583.LT.LoadGenerator.Business.cfg\n" +
				"-cfp=configFileProfile - uses if need change default profile configFile. " +
				"Default configFileProfile=conf/ISO8583.LT.LoadGenerator.Profile.cfg\n" +
				"-cfc=configFileCountry - uses if need change default country configFile. " +
				"Default configFileCountry=conf/ISO8583.LT.LoadGenerator.Country.cfg\n" +
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
    try{
		String configFileSystem = "conf/ISO8583.LT.LoadGenerator.System.cfg";
		String configFileProfile = "conf/ISO8583.LT.LoadGenerator.Profile.cfg";
		String configFileBusiness = "conf/ISO8583.LT.LoadGenerator.Business.cfg";
		String configFileCountry = "conf/ISO8583.LT.LoadGenerator.Country.cfg";
		String configFileScheduler = "conf/ISO8583.LT.LoadGenerator.Schedule.cfg";
		// Убирает warning "Initialize log4j", но, будучи ненастроенной, выкидывает в лог лишний мусор
		//org.apache.log4j.BasicConfigurator.configure();
		for (String arg : args) {
			if (arg.startsWith("-cfs=")) {
				configFileSystem = arg.replaceFirst("-cfs=", "");
			} else if (arg.startsWith("-cfb=")) {
				configFileBusiness = arg.replaceFirst("-cfb=", "");
			} else if (arg.startsWith("-cfp=")) {
				configFileProfile = arg.replaceFirst("-cfp=", "");
			} else if (arg.startsWith("-cfc=")) {
				configFileCountry = arg.replaceFirst("-cfc=", "");
			} else if (arg.startsWith("-cfsh=")) {
				configFileScheduler = arg.replaceFirst("-cfsh=", "");
			} else if (arg.startsWith("-help") || arg.equals("--help")) {
				System.out.println(LoadGenerator.usage());
				return;
			} else {
				System.out.println(LoadGenerator.usage());
			}
		}

		LoadGenerator loadGenerator = new LoadGenerator();

		// Инизиализация конфигураторов
		if (loadGenerator.configuration(configFileSystem, configFileBusiness, configFileProfile, configFileCountry, configFileScheduler) < 0) {
			System.err.println("[ERROR][CONFIGURATOR] The problem was encountered during set configurator.");
			return;
		}
		
		//Инициализация логирования
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
		logCommon.log(Level.INFO, "[MAIN] Reading FILE_PERSISTENCE_TXN_TRACE_NUMBER completed.");


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
		
		//Инициализация лог файлов
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

		logCommon.log(Level.INFO, "[MAIN] Connection to ISO8583 server opened with params: " +
				"ISO_SERVER_HOST:" + sConf.ISO_SERVER_HOST + " ISO_SERVER_PORT:" + sConf.ISO_SERVER_PORT + " SYS_ISO_SERVER_CONNECTION_TIME_OUT:" + sConf.SYS_ISO_SERVER_CONNECTION_TIME_OUT);


		//24.08.2012 try to add restart functionality
		setMaxTPSValue(sConf.MAX_TPS);
		setTickerValue(sConf.LOAD_STEP_SIZE);
		//loadGenerator.stopScenario=sConf.STOP_LOAD_AFTER_GENERATOR_START;
		//end of 24.08.2012


		// Создание профиля
		ProfileGenerator profileGenerator = ProfileGenerator.getInstance(isoClient);
		// Запуск задачи получения сообщений от сервера ISO
		//new RecTxnsTask(isoClient);
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
		//stopScenario=false;
		reqTPS=0;

		long scenarioStartTime = 0; //clear timer
        int CurrentCountOfWait_sec = 0;
        //exclude pause to prepare txn
        long lastTxnDateCreation =new Date().getTime(); //time before pause between transactions
        long startTxnDateCreation =new Date().getTime(); //time before prepare txn fields
        long txnPrepareTime =0; //pause to prepare txn

		//START LOAD HERE!!!
		while (!stopScenario) {
				// Поддержание требуемого TPS
				if (reqTPS == 0) {
					long sleepTimeIfTPSZero = 1000;
					logCommon.log(Level.WARNING, "[MAIN] Load stopped... TPS=0. Wait " + Long.toString(sleepTimeIfTPSZero) + " milliseconds.");
					sleep(sleepTimeIfTPSZero);
				} else {
	                Thread.sleep((long) (1000 / reqTPS)); //YetAnotherSleep.sleep(1000/reqTPS); //pause between create new transaction
				}
				while (suspendScenario){
					sleep(1000);
				}
				loadStatistic.setIncrementSendTxnsTotal();
				loadStatistic.setIncrementRecTxnsTotal();
		} 
      }catch(Exception ex){
            ex.printStackTrace();
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

    private static void checkConnectionResult(int connectResult){
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

	/**
	 * Установка значения текущего значения TPS
	 * @param reqTPSValue
	 */
	public static void setTPS(double reqTPSValue) {
		reqTPS = reqTPSValue;
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
