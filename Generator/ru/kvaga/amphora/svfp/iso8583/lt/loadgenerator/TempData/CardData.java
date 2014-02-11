package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData;

/**
 * Created with IntelliJ IDEA.
 * User: u_m0llb
 * Date: 10.12.13
 * Time: 13:17
 * To change this template use File | Settings | File Templates.
 */
public class CardData {
	private String id_card = "";
	private CountryEnum country = CountryEnum.RU;
	private long timestamp = 0L;

	/**
	 * хранилище данных по последним транзакциям для слежки за фродами
	 * @param id_customer
	 * @param country
	 * @param timestamp
	 */
	public CardData(String id_customer, CountryEnum country, long timestamp) {
		this.id_card = id_customer;
		this.country = country;
		this.timestamp = timestamp;
	}

	/**
	 * Получить id карты
	 * @return id_card
	 */
	public String getCard() {
		//System.out.println("id_customer:"+this.id_customer);
		return this.id_card;
	}

	/**
	 * Получить страну
	 * @return country
	 */
	public CountryEnum getCountry() {
		//System.out.println("amount:"+this.amount);
		return this.country;
	}

	/**
	 * Получить время проведения транзакции
	 * @return timestamp
	 */
	public long getTimestamp() {
		//System.out.println("timestamp:"+this.timestamp);
		return this.timestamp;
	}
}
