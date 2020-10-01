package net.somniok.pcr.object;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import net.somniok.pcr.error.ReportError;
import net.somniok.pcr.util.PcrUtil;

public class Report implements Comparable<Report>{
	public enum HitType {
		physical ("物理刀"),
		magical ("魔法刀"),
		sweetheart ("小小甜心刀");
		
		private final String name;
	    private HitType(String s) {
	        name = s;
	    }

	    public String toString() {
	       return this.name;
	    }
	}

	public enum HitStatus {
		normal ("普通刀"),
		last ("尾刀"),
		second ("補償刀"),
		secondLast ("補償刀尾刀");
		private final String name;
	    private HitStatus(String s) {
	        name = s;
	    }

	    public String toString() {
	       return this.name;
	    }
	}
	public static final String R_MESSAGE = "格式為: !r 週目 王 物/法 傷害 普/尾/補/補尾\r\n"
			+ "例如30週2王物刀打了7654321傷害, 補償刀, 要輸入 '!r 30 2 物 7654321 補'\r\n"
			+ "如果想輸入前幾天的刀(每日0500轉日)，請在最後加上日期(格式YYYY-MM-DD), 例如'!r 30 2 物 7654321 補 2020-08-28'";
	
	protected int round;
	protected int boss;
	protected HitType type;
	protected int damage;
	protected HitStatus status;
	protected String userName;
	protected LocalDate date;
	protected boolean isPersonalChecked;
	protected boolean isBossChecked;
	protected int actualDamage;
	
	
	public Report(int round, int boss, HitType type, int damage, HitStatus status, String userName, LocalDate date) {
		super();
		this.round = round;
		this.boss = boss;
		this.type = type;
		this.damage = damage;
		this.status = status;
		this.userName = userName;
		this.date = date;
		this.actualDamage = damage;
	}

	public Report(String[] command, String userName, LocalDateTime eventDateTime) throws ReportError {
		super();
		this.userName = userName;

    	if(command.length < 6 || command.length > 7) {
            throw new ReportError( "格式錯誤! " + R_MESSAGE);
    	}
    	int round, bossNo, damage;
    	try {
    		round = Integer.valueOf(command[1]);
    		bossNo = Integer.valueOf(command[2]);
    		if(command[4].length() > 8) {
    			throw new ReportError("傷害最多8位數，請檢查有沒有填錯");
    		}
    		damage = Integer.valueOf(command[4]);
    	} catch (NumberFormatException e) {
    		throw new ReportError("格式錯誤! " + R_MESSAGE);
    	}
    	if(round < 1 || round > 100) {
    		throw new ReportError("週目數字不太正常! 請填1~99");
    	}
    	if(bossNo < 1 || bossNo > 5) {
    		throw new ReportError("王數字不太正常! 請填1~5");
    	}

    	if(damage == 0) {
    		throw new ReportError("0傷害就不用報了，謝謝");
    	}
    	
    	if(damage > 20000000) {
    		throw new ReportError("傷害上限2千萬，請檢查有沒有填錯");
    	}
    	switch(command[3]) {
    	case "p":
    	case "物": 
    	case "物理":
    	case "物刀":
    	case "物理刀":
    		this.type = HitType.physical;
    		break;
    	case "m":
    	case "法":
    	case "魔":
    	case "魔法":
    	case "法刀":
    	case "魔法刀":
    		this.type = HitType.magical;
    		break;
    	case "s":
    	case "小":
    	case "甜心":
    	case "甜":
    	case "心":
    	case "小小甜心刀":
    		this.type = HitType.sweetheart;
    		break;
    	default:
    		throw new ReportError("魔刀/物刀輸入錯誤，請輸入'魔'/'物'");
    	}
		switch(command[5]) {
		case "l":
    	case "尾": 
    	case "收": 
    	case "尾刀":
    	case "收尾刀":
    	case "合尾刀":
    		this.status = HitStatus.last;
    		break;
    	case "2":
    	case "s":
    	case "殘":
    	case "殘刀":
    	case "補":
    	case "補償刀":
    	case "走":
    	case "走路":
    	case "走路刀":
    		this.status = HitStatus.second;
    		break;
    	case "1":
    	case "n":
    	case "普":
    	case "普刀":
    	case "普通刀":
    	case "正":
    	case "正刀":
    	case "正常刀":
    		this.status = HitStatus.normal;
    		break;
    	case "l2":
    	case "2l":
    	case "ls":
    	case "sl":
    	case "補尾":
    	case "補償刀尾刀":
    	case "補償尾刀":
    		this.status = HitStatus.secondLast;
    		break;
    	default:
    		throw new ReportError("普(普通刀)/尾(尾刀)/補(補償刀)/補尾(補償刀尾刀) 輸入錯誤，請輸入'普'/'尾'/'補'/'補尾'");
		}
    	this.round = round;
    	this.boss = bossNo;
    	this.actualDamage = damage;
    	this.damage = damage;
    	if(command.length == 6) {
    		date = PcrUtil.adjDate(eventDateTime);
    	} else {
    		try {
    			date = LocalDate.parse(command[6]);
    		} catch (DateTimeParseException e) {
    			throw new ReportError("日期格式錯誤, 應為'YYYY-MM-DD', 例如2020年8月31日請輸入'2020-08-31'");
    		}
    		
    	}

	}
	
	public Report(int round, int boss, HitType type, int damage, HitStatus status){
		
		this.round = round;
		this.boss = boss;
		this.type = type;
		this.damage = damage;
		this.status = status;
	}

	public Report(int round, int boss, HitType type, int damage) {
		super();
		this.round = round;
		this.boss = boss;
		this.type = type;
		this.damage = damage;
	}

	public int getRound() {
		return round;
	}

	public int getBoss() {
		return boss;
	}

	public HitType getType() {
		return type;
	}

	public String getTypeString() {
		return type.toString();
	}
	
	public String getDamage() {
		return PcrUtil.addComma(damage);
	}

	public HitStatus getStatus() {
		return status;
	}
	
	public String getStatusString() {
		return status.toString();
	}
	
	public String getUserName() {
		return userName;
	}
	
	public LocalDate getDate() {
		return date;
	}
	
	public boolean isPersonalChecked() {
		return isPersonalChecked;
	}

	public void setPersonalChecked(boolean isPersonalChecked) {
		this.isPersonalChecked = isPersonalChecked;
	}

	public boolean isBossChecked() {
		return isBossChecked;
	}

	public void setBossChecked(boolean isBossChecked) {
		this.isBossChecked = isBossChecked;
	}

	public boolean isReady() {
		return isPersonalChecked && isBossChecked;
	}
	
	public int getActualDamage() {
		return actualDamage;
	}

	public void setActualDamage(int actualDamage) {
		this.actualDamage = actualDamage;
	}

	public String getNameAndDamage() {
		return userName + " " + PcrUtil.addComma(damage);
	}
	
	@Override
	public String toString() {
		return round + "周目"+ boss + "王"
				+ type.toString() + "傷害" + getDamage() + status.toString()
				+ "(" + date.format(DateTimeFormatter.ISO_LOCAL_DATE) + ")" ;
	}

	public boolean isSecondStatus() {
		return status == HitStatus.second || status == HitStatus.secondLast;
	}
	
	public boolean isLastHit() {
		return status == HitStatus.last || status == HitStatus.secondLast;
	}

	public int getDamageInt() {
		return damage;
	}

	@Override
	public int compareTo(Report r) {
		return (round * 10 + boss) - (r.round * 10 + r.boss);
	}
	
}
