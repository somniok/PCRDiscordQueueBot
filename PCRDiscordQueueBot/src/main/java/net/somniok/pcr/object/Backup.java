package net.somniok.pcr.object;

import java.time.LocalDate;
import java.util.Map;

import net.somniok.pcr.io.ExcelWriter;

public class Backup implements Runnable {

	public BossQueue[] oneHitBossQueues;
	public BossQueue[] bossQueues;
	public Map<String, Map<LocalDate, ReportList>> usersMap;
	public Map<String, String> userNameMap;
	Map<Integer, BossList> checksumMap;
	
	public Backup(BossQueue[] oneHitBossQueues, BossQueue[] bossQueues,
			Map<String, Map<LocalDate, ReportList>> usersMap, Map<String, String> userNameMap) {
		super();
		this.oneHitBossQueues = oneHitBossQueues;
		this.bossQueues = bossQueues;
		this.usersMap = usersMap;
		this.userNameMap = userNameMap;
	}

	@Override
	public void run() {
		ExcelWriter.writeBackup(usersMap, userNameMap, oneHitBossQueues, bossQueues);
	}

}
