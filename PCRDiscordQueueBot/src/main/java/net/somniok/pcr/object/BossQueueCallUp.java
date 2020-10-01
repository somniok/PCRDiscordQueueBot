package net.somniok.pcr.object;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import net.somniok.pcr.main.Bot;
import net.somniok.pcr.util.PcrUtil;

public class BossQueueCallUp implements Runnable{

	protected static PCRMessagePack msgPack;
	
	public static void setMsgPack(PCRMessagePack msgPack) {
		BossQueueCallUp.msgPack = msgPack;
	}
	
	protected ArrayList<String> messageList;
	protected int bossNo;
	protected int queuePosition = 0;
	protected boolean isEnd;
	
	public BossQueueCallUp(int bossNo, BossQueue oneHit, BossQueue normal) {
		this.bossNo = bossNo;
		messageList = new ArrayList<String>();
		for(String userId : oneHit.getQueue()) {
			messageList.add(PcrUtil.userIdToTag(userId)+"已到"+bossNo+"王(1刀秒) "+Bot.AUTO_DELETE_MESSAGE);
		}
		if(!normal.isEmpty()) {
			messageList.add(normal.getTagString()+"已到"+bossNo+"王(普通刀) "+Bot.AUTO_DELETE_MESSAGE);
		}
	}
	
	public int getBossNo() {
		return bossNo;
	}
	
	public int getCurrPosition() {
		return queuePosition;
	}
	
	public int getSize() {
		return messageList.size();
	}
	
	public boolean isEnd() {
		return isEnd || queuePosition >= messageList.size();
	}

	public void end() {
		this.isEnd = true;
	}
	
	@Override
	public void run() {
		if(isEnd()) return;
		msgPack.getPublicChannel().sendMessage(messageList.get(queuePosition++)).queue(m->{
			m.delete().queueAfter(Bot.TIME_OUT, TimeUnit.SECONDS);
		});
	}

}
