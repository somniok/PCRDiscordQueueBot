package net.somniok.pcr.object;

import java.util.Vector;
import java.util.stream.Collectors;

import net.somniok.pcr.error.ChecksumError;
import net.somniok.pcr.util.PcrUtil;

public class BossList extends Vector<Report> {
	protected Report lastHit = null;
	protected int round;
	protected int boss;
	protected int totalHP;
	protected int currentHP;
	/**
	 * 
	 */
	private static final long serialVersionUID = 5869463386948806577L;
	
	
	public BossList(int round, int boss) {
		super();
		this.round = round;
		this.boss = boss;
		this.currentHP = this.totalHP = BossData.getBossHP(round, boss);
	}
	
	public void validate() {
		if(lastHit != null) {
			lastHit.actualDamage = currentHP > lastHit.damage ? lastHit.damage : currentHP;
		}
	}
	
	
	public synchronized void verifyAndAdd(Report r) throws ChecksumError{
		if(r.getDamageInt() > totalHP) {
			throw new ChecksumError("傷害" + r.getDamage() + "比"+r.getRound()+ "週目" + r.getBoss() + "王血量" + PcrUtil.addComma(totalHP) + "還高，請檢查是否填錯。");
		}
		if(r.getDamageInt() > currentHP && !r.isLastHit()) {
			throw new ChecksumError("傷害" + r.getDamage() + "比王剩余血量" + PcrUtil.addComma(currentHP) + "還高，請檢查是否填錯。本王已登記以下的刀(除尾刀): \r\n" 
									+ this.stream().filter(re->!re.isLastHit()).map(Report::getNameAndDamage).collect(Collectors.joining(", ")));
		}
		if(r.isLastHit()) {
			if(lastHit != null) {
				throw new ChecksumError(lastHit.getUserName() + "已收掉" + round + "週目" + boss + "王殘刀");
			}
			lastHit = r;
		} else {
			currentHP -= r.getDamageInt();
		}
		super.add(r);
	}
	
}
