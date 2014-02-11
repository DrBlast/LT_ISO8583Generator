package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.profile;

import java.util.ArrayList;

public class PGOperations {
	private static ArrayList<PGOperations> known = new ArrayList<PGOperations>();
	
	public static final PGOperations CASH_OUT_ATM_1100_PC= new PGOperations("CASH_OUT_ATM_1100_PC",1);	
	public static final PGOperations CASH_OUT_POS_1100_PC= new PGOperations("CASH_OUT_POS_1100_PC",2);
	public static final PGOperations PURCHASE_ELECTRONIC_1100_PC= new PGOperations("PURCHASE_ELECTRONIC_1100_PC",3);
	public static final PGOperations PURCHASE_POS_1100_PC= new PGOperations("PURCHASE_POS_1100_PC",4);
	public static final PGOperations REFUND_ELECTRONIC_1100_PC= new PGOperations("REFUND_ELECTRONIC_1100_PC",5);
	public static final PGOperations REFUND_POS_1100_PC= new PGOperations("REFUND_POS_1100_PC",6);
	public static final PGOperations PRE_AUTH_1100_PC= new PGOperations("PRE_AUTH_1100_PC",7);
	public static final PGOperations PRE_AUTH_ELECTRONIC_1100_PC= new PGOperations("PRE_AUTH_ELECTRONIC_1100_PC",8);
	public static final PGOperations CASH_OUT_ATM_1420_PC= new PGOperations("CASH_OUT_ATM_1420_PC",9);
	public static final PGOperations CASH_OUT_POS_1420_PC= new PGOperations("CASH_OUT_POS_1420_PC",10);
	public static final PGOperations PURCHASE_ELECTRONIC_1420_PC= new PGOperations("PURCHASE_ELECTRONIC_1420_PC",11);
	public static final PGOperations PURCHASE_POS_1420_PC= new PGOperations("PURCHASE_POS_1420_PC",12);
	public static final PGOperations REFUND_ELECTRONIC_1420_PC= new PGOperations("REFUND_ELECTRONIC_1420_PC",13);
	public static final PGOperations REFUND_POS_1420_PC= new PGOperations("REFUND_POS_1420_PC",14);
	public static final PGOperations PRE_AUTH_1420_PC= new PGOperations("PRE_AUTH_1420_PC",15);
	public static final PGOperations PRE_AUTH_ELECTRONIC_1420_PC = new PGOperations("PRE_AUTH_ELECTRONIC_1420_PC",16);
	public static final PGOperations STAND_IN_AUTH_1120_PC = new PGOperations("STAND_IN_AUTH_1120_PC",17);
	public static final PGOperations CASH_OUT_ATM_1100_VC= new PGOperations("CASH_OUT_ATM_1100_VC",18);
	public static final PGOperations PURCHASE_ELECTRONIC_1100_VC= new PGOperations("PURCHASE_ELECTRONIC_1100_VC",19);
	public static final PGOperations REFUND_ELECTRONIC_1100_VC= new PGOperations("REFUND_ELECTRONIC_1100_VC",20);
	public static final PGOperations PRE_AUTH_ELECTRONIC_1100_VC= new PGOperations("PRE_AUTH_ELECTRONIC_1100_VC",21);
	public static final PGOperations CASH_OUT_ATM_1420_VC= new PGOperations("CASH_OUT_ATM_1420_VC",22);
	public static final PGOperations PURCHASE_ELECTRONIC_1420_VC= new PGOperations("PURCHASE_ELECTRONIC_1420_VC",23);
	public static final PGOperations REFUND_ELECTRONIC_1420_VC= new PGOperations("REFUND_ELECTRONIC_1420_VC",24);
	public static final PGOperations PRE_AUTH_ELECTRONIC_1420_VC= new PGOperations("PRE_AUTH_ELECTRONIC_1420_VC",25);
	public static final PGOperations STAND_IN_AUTH_1120_VC= new PGOperations("STAND_IN_AUTH_1120_VC",26);

	private final String name;
	private final int key;

	/**
	 * Получение операций генератора профиля
	 * @param paramName
	 * @param key
	 */
	protected PGOperations(String paramName, int key){
		if(paramName==null) throw new NullPointerException();
		this.name=paramName;
		this.key=key;
		synchronized(PGOperations.class){
			known.add(this);
		}
	}

	/**
	 * Получение имени
	 * @return name
	 */
	public String getName(){
		return this.name;
	}

	/**
	 * Получение номера
	 * @return key
	 */
	public int getKey(){
		return this.key;
	}
	
	public static void main(String args[]){
		for(PGOperations str:known)
			System.out.println("case "+str.getKey()+": "+str.getName()+"();break;");
	}
}
