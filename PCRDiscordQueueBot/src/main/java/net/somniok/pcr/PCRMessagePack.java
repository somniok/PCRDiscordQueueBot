package net.somniok.pcr;

import net.dv8tion.jda.api.entities.Message;

public class PCRMessagePack {
	private long channelId;
	private Message info;
	private Message info2;

	private Message controlA;
	private Message controlB;
	private Message controlC;
	private Message controlD;
	private Message listing;
	
	private long adminChannelId;
	
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof PCRMessagePack) {
			if(this.channelId == ((PCRMessagePack) o).getChannelId() ) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Long.hashCode(this.channelId);
	}

	
	public PCRMessagePack(long channelId) {
		this.channelId = channelId;
	}
	
	public long getChannelId() {
		return channelId;
	}
	public void setChannelId(long channelId) {
		this.channelId = channelId;
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

	public long getAdminChannelId() {
		return adminChannelId;
	}

	public void setAdminChannelId(long adminChannelId) {
		this.adminChannelId = adminChannelId;
	}

	public Message getListing() {
		return listing;
	}

	public void setListing(Message listing) {
		this.listing = listing;
	}

	@Override
	public String toString() {
		return "PCRMessagePack [channelId=" + channelId + ", info=" + info + ", info2=" + info2 + ", controlA="
				+ controlA + ", controlB=" + controlB + ", controlC=" + controlC + ", controlD=" + controlD
				+ ", listing=" + listing + ", adminChannelId=" + adminChannelId + "]";
	}
	
}
