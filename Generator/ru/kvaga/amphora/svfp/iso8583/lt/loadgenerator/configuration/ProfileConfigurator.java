package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileConfigurator extends Configurator {

	static ConcurrentHashMap<TrnTypeEnum, Integer> transactionMap = new ConcurrentHashMap<TrnTypeEnum, Integer>();


	/**
	 * Получение Бизнес конфигурации
	 *
	 * @return BusinessConfigurator
	 */
	static public ProfileConfigurator getInstance() {
		if (configurator == null) configurator = new ProfileConfigurator();
		return configurator;
	}


	public int loadProperties(String fileName) {
		//if (!countryProfile.equals(fileName) && !countryProfile.isEmpty() && !countryProfile.equals(""))
		//    countryProfile= fileName;
		System.out.println("[ProfileConfigurator] Profile conf fileName: " + fileName);
		Properties p = new Properties();

		try {
			p.load(new FileInputStream(new File(fileName)));
			Set<String> set = p.stringPropertyNames();
			Iterator<String> propertyIterator = set.iterator();

			String propertyKey;
			System.out.println("[ProfileConfigurator] Profile settings:");
			while (propertyIterator.hasNext()) {
				propertyKey = propertyIterator.next().toString();
				//	System.out.print(propertyKey + "=");
				//	System.out.println(p.getProperty(propertyKey));
				int value = Integer.parseInt(p.getProperty(propertyKey));
				transactionMap.put(TrnTypeEnum.valueOf(propertyKey), value);
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


	public ConcurrentHashMap<TrnTypeEnum, Integer> getTrnTypeMap() {
		return this.transactionMap;
	}

	private static ProfileConfigurator configurator = null;
}

