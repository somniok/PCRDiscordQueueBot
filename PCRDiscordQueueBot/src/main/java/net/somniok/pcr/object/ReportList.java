package net.somniok.pcr.object;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Vector;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.somniok.pcr.object.Report.HitStatus;
import net.somniok.pcr.util.PcrUtil;

public class ReportList extends Vector<Report> {
	
	public static LocalDate currDate = null;
	protected String userName;
	
	protected int secondCount = 0;
	protected int firstCount = 0;
	protected int lastCount = 0;

	protected Message adminMessage;
	
	protected static MessageChannel adminChannel = null;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3156529586112064419L;

	public ReportList(User user) {
		super();
		this.userName = user.getName();
//		if(adminChannel != null) {
//			sendAdminMessage();
//		}
	}

	public ReportList(String userName) {
		super();
		this.userName = userName;
//		if(adminChannel != null) {
//			sendAdminMessage();
//		}
	}
	
	
	public static void init(MessageChannel c) {
		adminChannel = c;
		currDate = PcrUtil.adjDate(LocalDateTime.now());
	}
	
	@Override
	public boolean add(Report r) {
		if(r.isSecondStatus()) {
			++secondCount;
		} else {
			++firstCount;
			
		}
		if(r.getStatus() == HitStatus.last){
			++lastCount;
		}
		boolean result = super.add(r);
		if(adminMessage == null) {
			sendAdminMessage();
		} else {
			updateAdminMessage();
		}
		return result;
	}

	@Override
	public Report remove(int i) {
		Report r = super.remove(i);
		if(r.isSecondStatus()) {
			--secondCount;
		} else {
			--firstCount;
			
		}
		if(r.getStatus() == HitStatus.last){
			--lastCount;
		}
		return r;
	}
	
	public boolean isOverLimit(Report r) {
		return r.isSecondStatus() ? secondCount >= lastCount : firstCount >= 3;
	}
	
	public int getSecondCount() {
		return secondCount;
	}
	public int getFirstCount() {
		return firstCount;
	}
	public int getLastCount() {
		return lastCount;
	}

	public void sendAdminMessage() {
		adminChannel.sendMessage(userName + ":" + this.toMessage()).queue(m->this.adminMessage = m);
	}
	
	public void  updateAdminMessage() {
		adminMessage.editMessage(userName + ":" + this.toMessage()).queue();
	}
	
	public String toMessage() {
		if(this.isEmpty()) {
			return "尚無資料";
		}
		String result = listReport(" ");
		return result;
	}
	
	public String listReport(String join) {
		String result = "";
		for(int i=0; i<this.size(); i++) {
			result += (i+1) + ". " + this.get(i).toString() + join;
		}
		return result;
	}
	
	public String listReport() {
		String result = "";
		for(int i=0; i<this.size(); i++) {
			result += (i+1) + ". " + this.get(i).toString() + "\r\n";
		}
		return result;
	}
	
	public static void nextDate() {
		if(currDate != null) {
			currDate = currDate.plusDays(1);
		}
	}
}
