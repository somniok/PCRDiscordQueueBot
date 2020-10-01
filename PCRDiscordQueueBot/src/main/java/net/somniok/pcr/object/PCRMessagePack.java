package net.somniok.pcr.object;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class PCRMessagePack {
	private Message info;
	private Message info2;

	private Message controlA;
	private Message controlB;
	private Message controlC;
	private Message controlD;
	private Message queueList;

	private MessageChannel publicChannel;
	private MessageChannel adminChannel;
	
	public PCRMessagePack() {
	}
	
	public PCRMessagePack(MessageChannel channel) {
		this.publicChannel = channel;
	}

	public Message getInfo() {
		return info;
	}
	public void setInfo(Message info) {
		this.info = info;
	}
	public Message getInfo2() {
		return info2;
	}
	public void setInfo2(Message info2) {
		this.info2 = info2;
	}
	public Message getControlA() {
		return controlA;
	}
	public void setControlA(Message controlA) {
		this.controlA = controlA;
	}
	public Message getControlB() {
		return controlB;
	}
	public void setControlB(Message controlB) {
		this.controlB = controlB;
	}
	public Message getControlC() {
		return controlC;
	}
	public void setControlC(Message controlC) {
		this.controlC = controlC;
	}
	public Message getControlD() {
		return controlD;
	}
	public void setControlD(Message controlD) {
		this.controlD = controlD;
	}
	public Message getQueueList() {
		return queueList;
	}
	public void setQueueList(Message listing) {
		this.queueList = listing;
	}

	public MessageChannel getPublicChannel() {
		return publicChannel;
	}

	public void setPublicChannel(MessageChannel publicChannel) {
		this.publicChannel = publicChannel;
	}

	public MessageChannel getAdminChannel() {
		return adminChannel;
	}

	public void setAdminChannel(MessageChannel adminChannel) {
		this.adminChannel = adminChannel;
	}

}
