package net.somniok.pcr.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

public interface PcrUtil {
	static public String addComma(int n) {
		return NumberFormat.getNumberInstance(Locale.US).format(n);
	}
	static public LocalDate adjDate(LocalDateTime d) {
		LocalDate date = d.toLocalDate();
		if(d.getHour() < 5) {
			date = date.minusDays(1);
		}
		return date;
	}

    public static String userIdToTag(String userId) {
    	return "<@" + userId + ">";
    }
}
