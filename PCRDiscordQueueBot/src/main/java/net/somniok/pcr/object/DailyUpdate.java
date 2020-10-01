package net.somniok.pcr.object;

import net.somniok.pcr.main.Bot;

public class DailyUpdate implements Runnable {

	Bot bot;
	
	public DailyUpdate(Bot bot) {
		this.bot = bot;
	}
	
	@Override
	public void run() {
		bot.nextDay();
	}

}
