package ru.kvaga.amphora.svfp.lt.persistence;

import ru.kvaga.amphora.svfp.loadGenerator.log.LogInit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage implements Runnable {
	private static Storage storage;
	private String KEY_CURRENT_TRACE_NUMBER = "lastTraceNumber";
	private String KEY_STORAGE_TIMEOUT      = "storageTimeout";
	private Logger logCommon                = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
	private Properties properties;
	private long currentTraceNumberValue = -1;
	private File storageFile;
	private long storageTimeout = -1;

	private Storage() {
	}

	public static int DEFAULT_CONF_TIMEOUT = 0; // брать значение из файла.

	public static Storage getInstance() {
		if (storage == null) {
			storage = new Storage();
		}
		return storage;
	}

	/**
	 * Инициализация сохранения номера транзакции
	 * @param storageFile
	 * @return 0
	 * @throws IOException
	 */
	public int init(File storageFile) throws IOException {
		this.storageFile = storageFile;
        properties = new Properties();
        properties.load(new FileInputStream(storageFile));
        currentTraceNumberValue = Long.parseLong(properties.getProperty(KEY_CURRENT_TRACE_NUMBER));
        return 0;
    }

	/**
	 * Инициализация сохранения номера транзакции по таймеру
	 * @param storageFile
	 * @param storageTimeout
	 * @return  0 - инициализация успешна <br>
	 *         -1 - некорректное время таймера <br>
	 * @throws IOException
	 */
    public int init(File storageFile, long storageTimeout) throws IOException {
        this.storageTimeout = storageTimeout;
        if (storageTimeout < 0) {
            return -1;
        }
        this.storageFile = storageFile;
        properties = new Properties();
        properties.load(new FileInputStream(storageFile));
        currentTraceNumberValue = Long.parseLong(properties.getProperty(KEY_CURRENT_TRACE_NUMBER));
        if (this.storageTimeout == DEFAULT_CONF_TIMEOUT) {
            this.storageTimeout = Integer.parseInt(properties.getProperty(KEY_STORAGE_TIMEOUT));
        }
        new Thread(this).start();
        return 0;
    }

	/**
	 * Получение следующего номера транзакции
	 * @return
	 */
    public synchronized long getNextTraceNumber() {
        return ++currentTraceNumberValue;
    }

	/**
	 * Сохранение текущего номера транзакции в файл
	 * @return 0 - сохранение успешно <br>
	 *         -1 - ошибка сохранения <br>
	 * @throws Exception
	 * @throws IOException
	 */
    public int storeTraceCurrentTraceNumber() throws Exception, IOException {
        if (properties != null) {
            properties.setProperty(KEY_CURRENT_TRACE_NUMBER, "" + currentTraceNumberValue);
            try {
                properties.store(new FileOutputStream(storageFile), "KEY_CURRENT_TRACE_NUMBER of ISO8583 load generator");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return -1;
        }
        return 0;
    }

	/**
	 * Запуск сохранения номера транзакции
	 */
    public void run() {
	    logCommon.log(Level.INFO, "[Storage] The task of persistence is started.");
        while (storage != null && properties != null) {
            sleep(storageTimeout);
            try {
                storeTraceCurrentTraceNumber();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
	    logCommon.log(Level.WARNING, "[Storage] The task of persistence is stoped.");
    }

	/**
	 * Остановка сохранения номера транзакции
	 */
    public int close() {
        if (properties != null) {
            properties.setProperty(KEY_CURRENT_TRACE_NUMBER, "" + currentTraceNumberValue);
            try {
                properties.store(new FileOutputStream(storageFile), "KEY_CURRENT_TRACE_NUMBER of ISO8583 load generator");
            } catch (Exception e) {
                e.printStackTrace();
                return -2;
            }
        } else {
            return -1;
        }
        properties = null;
        storage = null;
        return 0;
    }

    private void sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
