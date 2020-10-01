package net.somniok.pcr.object;

import java.math.BigDecimal;
import java.util.Properties;

public class BossData {

	public static final int STAGE_2_START = 4;
	public static final int STAGE_3_START = 11;
	public static final int STAGE_4_START = 35;
	
	public static int bossHP[][];
	public static int bossRatio[][];
	
	public static int totalDays = 6;
	
	public static void init(Properties p) {
		bossHP = new int[4][5];
		bossRatio = new int[4][5];
		for(int i=0;i<4;i++) {
			for(int j=0;j<5;j++) {
				bossHP[i][j] = Integer.parseInt(p.getProperty("bossHP" + (i+1) + "-" + (j+1)));
				BigDecimal r = new BigDecimal(p.getProperty("bossRatio" + (i+1) + "-" + (i+1)));
				bossRatio[i][j] = r.multiply(BigDecimal.TEN).intValue();
			}
		}
		String totalDaysString = p.getProperty("totalDays");
		if(totalDaysString != null) {
			totalDays = Integer.parseInt(totalDaysString);
		}
	}
	
	public static int getStage(int round) {
		if(round < STAGE_2_START)
			return 1;
		else if(round < STAGE_3_START)
			return 2;
		else if(round < STAGE_4_START)
			return 3;
		else
			return 4;
	}
	
	public static int getBossHP(int round, int boss) {
		return bossHP[getStage(round)-1][boss-1];
	}
	
	public static int getBossRatio(int round, int boss) {
		return bossRatio[getStage(round)-1][boss-1];
	}
}
