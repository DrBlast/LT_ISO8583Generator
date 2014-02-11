package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SystemConfigurator extends Configurator {
	public String ISO_SERVER_HOST             = "10.1.14.56",//"192.168.203.16",
			LOG_FILE_COMMON                   = "ISOServerLogCommon.log",
			LOG_FILE_SENDER                   = "ISOServerLogSender.log",
			LOG_FILE_RECEIVER                 = "ISOServerLogCommon.log",
			LOG_FILE_TPS                      = "ISOServerLogTPS.log",
			LOG_FILE_PATH                     = "log",
			LOG_LEVEL_ON_LOAD                 = "WARN",
			FILE_PERSISTENCE_TXN_TRACE_NUMBER = "system/persistence.txn.trace.number.sys";

	public static String SendLogFullPath = "",
			TPSLogFullPath               = "",
			RecLogFullPath               = "";

	public int ISO_SERVER_PORT                 = 2223,//7488,
			SYS_ISO_SERVER_CONNECTION_TIME_OUT = 5000,
			MAX_THREADS_COUNT                  = 50,
			THREAD_SLEEP_TIME_MILLIS           = 100,
			RESTART_GENERATOR_THREADS_COUNT    = 0,
			WAIT_REC_TXN_AFTER_STOP_LOAD       = 10,
			MAX_TPS                            = 0;//default

	public static String  timeFormat                              = "yyyy-MM-dd HH:mm:ss,SSS";
	public        double  LOAD_STEP_SIZE                          = 0;
	public        boolean LOG_FILE_USE_DEFAULT_LOG_PATH_AND_NAMES = true;

	public long SYS_SLEEP_IF_LOAD_SUSPENDED_MILLIS   = 1000,
			UPDATE_TPS_LOG_FILE_TASK_SLEEP_IN_MILLIS = 1000;

	public boolean IS_THREADS_COUNT_MONITOR = false,
			DISCONNECT_ISO_CHANNEL          = true;
	//STOP_LOAD_AFTER_GENERATOR_START=false;

	public boolean IS_THREADS_LIVETIME_MONITOR = false;

	/**
	 * Получение системной конфигурации
	 *
	 * @return SystemConfigurator
	 */
	static public SystemConfigurator getInstance() {
		if (configurator == null) configurator = new SystemConfigurator();
		return configurator;
	}

	/**
	 * Загрузка параметров
	 *
	 * @param - файл с параметрами
	 * @return 0 - успех <br>
	 *         -1 - файл не найден <br>
	 *         -2 - ошибка ввода\вывода при чтении файла параметров<br>
	 *         -3 - другая ошибка<br>
	 */
	public int loadProperties(String fileName) {
        try {
            Properties p = new Properties();
			p.load(new FileInputStream(new File(fileName)));

			if (p.getProperty("ISO_SERVER_HOST") != null) ISO_SERVER_HOST = p.getProperty("ISO_SERVER_HOST");
			if (p.getProperty("ISO_SERVER_PORT") != null)
				ISO_SERVER_PORT = Integer.parseInt(p.getProperty("ISO_SERVER_PORT"));
			if (p.getProperty("LOG_FILE_COMMON") != null) LOG_FILE_COMMON = p.getProperty("LOG_FILE_COMMON");
			if (p.getProperty("LOG_FILE_SENDER") != null) LOG_FILE_SENDER = p.getProperty("LOG_FILE_SENDER");
			if (p.getProperty("LOG_FILE_RECEIVER") != null) LOG_FILE_RECEIVER = p.getProperty("LOG_FILE_RECEIVER");
			if (p.getProperty("LOG_FILE_TPS") != null) LOG_FILE_TPS = p.getProperty("LOG_FILE_TPS");
			if (p.getProperty("LOG_FILE_PATH") != null) LOG_FILE_PATH = p.getProperty("LOG_FILE_PATH");
			if (p.getProperty("LOG_FILE_USE_DEFAULT_LOG_PATH_AND_NAMES") != null)
				LOG_FILE_USE_DEFAULT_LOG_PATH_AND_NAMES = Boolean.parseBoolean(p.getProperty("LOG_FILE_USE_DEFAULT_LOG_PATH_AND_NAMES"));
			if (p.getProperty("LOG_LEVEL_ON_LOAD") != null && (p.getProperty("LOG_LEVEL_ON_LOAD").equals("INFO") || p
					.getProperty("LOG_LEVEL_ON_LOAD").equals("WARN")))
				LOG_LEVEL_ON_LOAD = p.getProperty("LOG_LEVEL_ON_LOAD");
			if (p.getProperty("SYS_SLEEP_IF_LOAD_SUSPENDED_MILLIS") != null)
				SYS_SLEEP_IF_LOAD_SUSPENDED_MILLIS = Long.parseLong(p.getProperty("SYS_SLEEP_IF_LOAD_SUSPENDED_MILLIS"));
			if (p.getProperty("SYS_ISO_SERVER_CONNECTION_TIME_OUT") != null)
				SYS_ISO_SERVER_CONNECTION_TIME_OUT = Integer.parseInt(p.getProperty("SYS_ISO_SERVER_CONNECTION_TIME_OUT"));
			if (p.getProperty("UPDATE_TPS_LOG_FILE_TASK_SLEEP_IN_MILLIS") != null)
				UPDATE_TPS_LOG_FILE_TASK_SLEEP_IN_MILLIS = Long.parseLong(p.getProperty("UPDATE_TPS_LOG_FILE_TASK_SLEEP_IN_MILLIS"));

			if (p.getProperty("FILE_PERSISTENCE_TXN_TRACE_NUMBER") != null)
				FILE_PERSISTENCE_TXN_TRACE_NUMBER = p.getProperty("FILE_PERSISTENCE_TXN_TRACE_NUMBER");
			if (p.getProperty("IS_THREADS_LIVETIME_MONITOR") != null)
				IS_THREADS_LIVETIME_MONITOR = Boolean.parseBoolean(p.getProperty("IS_THREADS_LIVETIME_MONITOR"));
			if (p.getProperty("IS_THREADS_COUNT_MONITOR") != null)
				IS_THREADS_COUNT_MONITOR = Boolean.parseBoolean(p.getProperty("IS_THREADS_COUNT_MONITOR"));
			if (p.getProperty("MAX_THREADS_COUNT") != null)
				MAX_THREADS_COUNT = Integer.parseInt(p.getProperty("MAX_THREADS_COUNT"));
			if (p.getProperty("THREAD_SLEEP_TIME_MILLIS") != null)
				THREAD_SLEEP_TIME_MILLIS = Integer.parseInt(p.getProperty("THREAD_SLEEP_TIME_MILLIS"));
			if (p.getProperty("RESTART_GENERATOR_THREADS_COUNT") != null)
				RESTART_GENERATOR_THREADS_COUNT = Integer.parseInt(p.getProperty("RESTART_GENERATOR_THREADS_COUNT"));
			if (p.getProperty("MAX_TPS") != null) MAX_TPS = Integer.parseInt(p.getProperty("MAX_TPS"));
			if (p.getProperty("LOAD_STEP_SIZE") != null)
				LOAD_STEP_SIZE = Double.parseDouble(p.getProperty("LOAD_STEP_SIZE"));
			//	if(p.getProperty("STOP_LOAD_AFTER_GENERATOR_START")!=null) STOP_LOAD_AFTER_GENERATOR_START = Boolean.parseBoolean(p.getProperty("STOP_LOAD_AFTER_GENERATOR_START"));
			if (p.getProperty("DISCONNECT_ISO_CHANNEL") != null)
				DISCONNECT_ISO_CHANNEL = Boolean.parseBoolean(p.getProperty("DISCONNECT_ISO_CHANNEL"));
			if (p.getProperty("WAIT_REC_TXN_AFTER_STOP_LOAD") != null)
				WAIT_REC_TXN_AFTER_STOP_LOAD = Integer.parseInt(p.getProperty("WAIT_REC_TXN_AFTER_STOP_LOAD"));

			p.clear();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		} catch (Exception e) {
			e.printStackTrace();
			return -3;
		}
		return 0;
	}

	private static SystemConfigurator configurator = null;
}
