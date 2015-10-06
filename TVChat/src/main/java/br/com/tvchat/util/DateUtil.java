package br.com.tvchat.util;

import java.util.Date;

public class DateUtil {
	public static String getTimeComment(long time){
		long date2 = new Date().getTime();
		
		int min = (int)((date2-time)/300000);
		if (min==0){
			return "Agora";
		} else if (min <= 12){
			return min*5 + " min";
		} else if (min <= 288){
			return min/12 + " horas";
		} else {
			return min/288 + " dias";
		}
	}
}
