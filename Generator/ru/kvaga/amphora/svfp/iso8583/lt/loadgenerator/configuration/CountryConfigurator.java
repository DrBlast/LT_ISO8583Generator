package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.CountryEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class CountryConfigurator extends Configurator {
	public static String countryProfile = "conf/ISO8583.LT.LoadGenerator.Country.cfg";

	/**
	 * Получение инстанса Конфигурации Стран
	 *
	 * @return CountryConfigurator
	 */
	static public CountryConfigurator getInstance() {
		if (configurator == null) {
			configurator = new CountryConfigurator();
		}
		return configurator;
	}

	static ConcurrentHashMap<CountryEnum, Integer> CountryMap = new ConcurrentHashMap<CountryEnum, Integer>();

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
		//if (!countryProfile.equals(fileName) && !countryProfile.isEmpty() && !countryProfile.equals(""))
		//    countryProfile= fileName;
            System.out.println("[CountryConfigurator] CountryProfile conf fileName: " + countryProfile);
		Properties p = new Properties();

			p.load(new FileInputStream(new File(countryProfile)));
			Set<String> set = p.stringPropertyNames();
			Iterator<String> propertyIterator = set.iterator();

			String propertyKey;
            System.out.println("[CountryConfigurator] CountryProfile settings:");
			while (propertyIterator.hasNext()) {
				propertyKey = propertyIterator.next().toString();
				//	System.out.print(propertyKey + "=");
				//	System.out.println(p.getProperty(propertyKey));
				int value = Integer.parseInt(p.getProperty(propertyKey));
				CountryMap.put(CountryEnum.valueOf(propertyKey), value);
			}

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
	 * Получение Стран
	 *
	 * @return ConcurrentHashMap<CountryEnum, Integer>
	 */
	public ConcurrentHashMap<CountryEnum, Integer> getCountryMap() {
		return this.CountryMap;
	}

	private static CountryConfigurator configurator = null;
}

