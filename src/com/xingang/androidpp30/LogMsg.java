package com.xingang.androidpp30;

public class LogMsg {
	public String msg;
	public enum TYPE { DEBUG, UA, CALL };
	public TYPE type = TYPE.DEBUG;
	public enum CAT { CRITICAL, MAJOR, WARN, INFO, POP, NONE};
	public CAT cat = CAT.NONE;

	public LogMsg( String s, TYPE t, CAT c ) {
		msg = s;
		cat = c;
		type = t;
	
	}

}
