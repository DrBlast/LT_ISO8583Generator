package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration;

/**
 * Created with IntelliJ IDEA.
 * User: u_m0llb
 * Date: 02.12.13
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public enum TrnTypeEnum {
    CASHOUT_ATM("700"),
    POS("774"),
    EPOS("680"),
    PURCHASE_ATM("508"),
    BALANCE("702");

    private String value;

	/**
	 * Присвоение типа транзакции
	 * @param s
	 */
    TrnTypeEnum(String s) {
        this.value = s;
    }

	/**
	 * Получение значения типа транзакции
	 * @return
	 */
    public String getValue(){
        return value;
    }

    private static final int size = TrnTypeEnum.values().length;

	/**
	 * Получение размера справочника по типам транзакций
	 * @return
	 */
    public int getSize() {
        return size;
    }


}
