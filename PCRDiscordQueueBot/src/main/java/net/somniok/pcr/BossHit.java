package net.somniok.pcr;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class BossHit implements Runnable{

	protected static PCRMessagePack msgPack;
	
	protected Message msg;
	protected boolean isSOS;
	protected User user;
	protected int boss;
//	protected 

	public Message getMsg() {
		return msg;
	}

	public void setMsg(Message msg) {
		this.msg = msg;
	}

	public boolean isSOS() {
		return isSOS;
	}

	public void setSOS(boolean isSOS) {
		this.isSOS = isSOS;
	}

	public static void setMsgPack(PCRMessagePack msgPack) {
		BossHit.msgPack = msgPack;
	}
	
	@Override
	public void run(){
		msgPack.getPublicChannel();
	}

}
