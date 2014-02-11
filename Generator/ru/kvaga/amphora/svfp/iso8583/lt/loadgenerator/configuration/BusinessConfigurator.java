package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class BusinessConfigurator extends Configurator {
	public String DATAPOOL = "dataPool/CardNumPool.txt";

	public         double FRAUD_CHANCE              = 0;
	public         int    TXN_FAIL_RESPONSE_TIMEOUT = 60000;
	private static double TPS                       = 1;

	/**
	 * Получение Бизнес конфигурации
	 *
	 * @return BusinessConfigurator
	 */
	static public BusinessConfigurator getInstance() {
		if (configurator == null) configurator = new BusinessConfigurator();
		return configurator;
	}

	public int loadProperties(String fileName) {
		//if (!countryProfile.equals(fileName) && !countryProfile.isEmpty() && !countryProfile.equals(""))
		//    countryProfile= fileName;
		System.out.println("[BusinessConfigurator] Business conf fileName: " + fileName);
		Properties p = new Properties();

		try {
			p.load(new FileInputStream(new File(fileName)));

			if (p.getProperty("DATAPOOL") != null)
				DATAPOOL = p.getProperty("DATAPOOL");
			if (p.getProperty("FRAUD_CHANCE") != null)
				FRAUD_CHANCE = Double.parseDouble(p.getProperty("FRAUD_CHANCE"));
			if (p.getProperty("TXN_FAIL_RESPONSE_TIMEOUT") != null)
				TXN_FAIL_RESPONSE_TIMEOUT = Integer.parseInt(p.getProperty("TXN_FAIL_RESPONSE_TIMEOUT"));

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

	/**
	 * Получение текущей нагрузки
	 *
	 * @return TPS
	 */
	public double getTPS() {
		return TPS;
	}

	/**
	 * Установка уровня нагрузки (TPS)
	 *
	 * @value
	 */
	public synchronized void setTPS(double TPS) {
		BusinessConfigurator.TPS = TPS;
	}

	private static BusinessConfigurator configurator = null;
}

