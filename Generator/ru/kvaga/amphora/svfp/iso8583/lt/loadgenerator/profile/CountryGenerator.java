package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.profile;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.CountryConfigurator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.CountryEnum;
import ru.kvaga.amphora.svfp.loadGenerator.log.LogInit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

//import java.util.NoSuchElementException;

public class CountryGenerator {
	private ConcurrentHashMap<CountryEnum, Integer> countryGeneratorMap = new ConcurrentHashMap<CountryEnum, Integer>();
	private int                                     currentIteration    = 0;

	private CountryGenerator() {
		CountryConfigurator countryConfigurator = CountryConfigurator.getInstance();
		for (CountryEnum country : CountryEnum.values()) {
			countryGeneratorMap.put(country, 0);
		}
	}

	private CountryConfigurator cConf = null;
	private int                 j     = -1;  //default -1

	public CountryEnum getNextCountryProfile() {  //используется по-умолчанию
		CountryEnum resultCountryEnum = CountryEnum.RU;

		if (cConf == null)
			cConf = CountryConfigurator.getInstance();

		if (checkIsPercentProfileCorrect() < 0) {
			return null;
		}

		currentIteration++;
		int countryGeneratorSize = countryGeneratorMap.size();
		for (int i = 0; i < countryGeneratorSize; i++) {
			j = ((j + 1) % countryGeneratorSize);
			int iter = 0;
			for (Map.Entry<CountryEnum, Integer> entry : countryGeneratorMap.entrySet()) {
				int countryPercent = cConf.getCountryMap().get(entry.getKey());
				int currentValue = entry.getValue();
				if (((currentValue * 100 / currentIteration) < countryPercent)
						&& (countryPercent != 0) && (j == iter)) {
					currentValue++;
					entry.setValue(currentValue);
					return entry.getKey();
				}
				iter++;
			}

		}
		return resultCountryEnum;
	}

	/**
	 * Получить случайную страну
	 */
	public CountryEnum getNextCountryRandom() {   //не используется
		return CountryEnum.getRandomCountry();
	}

	private static CountryGenerator countryGenerator = new CountryGenerator();

	/**
	 * Получить генератор стран
	 *
	 * @return CountryGenerator
	 */
	public static CountryGenerator getInstance() {
		if (countryGenerator == null) {
			countryGenerator = new CountryGenerator();
		}
		return countryGenerator;
	}

	Logger logCommon = null;

	private int checkIsPercentProfileCorrect() {

		if (logCommon == null) logCommon = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
		double sumOfProfilePercents = 0;
		for (Map.Entry<CountryEnum, Integer> entry : cConf.getCountryMap().entrySet()) {
			sumOfProfilePercents += entry.getValue();
		}
		if (sumOfProfilePercents != 100) {
			logCommon.log(Level.WARNING, "[CountryGenerator] Incorrect sum of countries profile percents. Requirement: 100%, Actual: "
					+ sumOfProfilePercents + "%");
			return -1;
		}
		return 0;
	}

}
