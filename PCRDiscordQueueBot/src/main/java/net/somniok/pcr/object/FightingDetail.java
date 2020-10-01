package net.somniok.pcr.object;

import net.dv8tion.jda.api.entities.Message;

public class FightingDetail {
	protected String messageId;
	protected String userId;
	protected Message message;
	protected int bossNo;
	protected boolean isNeedSos = false;

	public FightingDetail(String messageId, String userId, Message message, int bossNo) {
		super();
		this.messageId = messageId;
		this.userId = userId;
		this.message = message;
		this.bossNo = bossNo;
	}
	
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}
	public int getBossNo() {
		return bossNo;
	}
	public void setBossNo(int bossNo) {
		this.bossNo = bossNo;
	}
	public void setSos() {
		this.isNeedSos = true;
	}
	public boolean isNeedSos() {
		return isNeedSos;
	}
}
