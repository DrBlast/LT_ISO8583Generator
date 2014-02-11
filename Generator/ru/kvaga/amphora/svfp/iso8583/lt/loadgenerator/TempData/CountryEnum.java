package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: u_m0llb
 * Date: 05.12.13
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public enum CountryEnum {
	BG("SOFIA     "),
	BR("BRAZILIA  "),
	BY("MINSK     "),
	CH("BERN      "),
	CZ("PRAGUE    "),
	DE("BERLIN    "),
	ES("MADRID    "),
	FR("PARIS     "),
	LK("COLOMBO   "),
	IL("JERUSALEM "),
	IT("ROME      "),
	GB("LONDON    "),
	RU("MOSCOW    "),
	TH("BANGKOK   "),
	TR("ANKARA    "),
	UA("KIEV      "),
	US("WASHINGTON");

	private static final List<CountryEnum> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
	private static final int SIZE = VALUES.size();
	private static final Random RANDOM = new Random();

	/**
	 * Получение случайной страны
	 * @return CountryEnum
	 */
	public static CountryEnum getRandomCountry() {
		return VALUES.get(RANDOM.nextInt(SIZE));
	}

	private String value;

	CountryEnum(String s) {
		this.value = s;
	}

	/**
	 * Получение списка стран
	 * @return List<CountryEnum>
	 */
	public static List<CountryEnum> getCountryEnum() {
		return VALUES;
	}

	public String getCapital(){
		return value;
	}


}
