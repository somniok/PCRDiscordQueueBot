package net.somniok.pcr.main;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.somniok.pcr.error.ChecksumError;
import net.somniok.pcr.error.ReportError;
import net.somniok.pcr.io.ExcelWriter;
import net.somniok.pcr.object.Backup;
import net.somniok.pcr.object.BossList;
import net.somniok.pcr.object.BossQueue;
import net.somniok.pcr.object.BossQueueCallUp;
import net.somniok.pcr.object.DailyUpdate;
import net.somniok.pcr.object.FightingDetail;
import net.somniok.pcr.object.PCRMessagePack;
import net.somniok.pcr.object.Report;
import net.somniok.pcr.object.ReportList;
import net.somniok.pcr.util.PcrUtil;

public class Bot extends ListenerAdapter{
	public static final char PREFIX_CHAR = '!';

	protected static Logger logger = LoggerFactory.getLogger(Bot.class);
		
	protected static PCRMessagePack msgPack;
	
	protected BossQueue[] oneHitBossQueues;
	protected BossQueue[] bossQueues;
	protected Map<String, FightingDetail> fightingSet;
	protected Map<String, FightingDetail> sosSet;
	protected List<BossQueueCallUp> bossQueueList;

	protected Map<String, Map<LocalDate, ReportList>> usersMap;
	protected Map<Integer, BossList> checksumMap;
	private Map<String, String> userNameMap;
	
	protected int currBoss = 0;

	public static final int TIME_OUT = 300;
	protected static final ScheduledExecutorService dailyUpdate = Executors.newScheduledThreadPool(1);
	protected static final ScheduledExecutorService backupSchedule = Executors.newScheduledThreadPool(1);
	protected static final ScheduledExecutorService queueSchedule = Executors.newScheduledThreadPool(1);
	protected static final ExecutorService queueThread = Executors.newFixedThreadPool(1);
	
	public static final String AUTO_DELETE_MESSAGE = (TIME_OUT/60) + "分鐘後自動刪除";
	
	protected static long totalBossCount = 0;
	
	public static LocalDate startDate = null;
	public static int totalDate = 6;
	
	protected boolean isReady = false;
	protected boolean isAdminReady = false;


	public Bot() {
		oneHitBossQueues = new BossQueue[5];
		bossQueues = new BossQueue[5];
		for(int i=0;i<5;i++) {
			oneHitBossQueues[i] = new BossQueue();
			bossQueues[i] = new BossQueue();
		}
		fightingSet = new ConcurrentSkipListMap<String, FightingDetail>();
		sosSet = new ConcurrentSkipListMap<String, FightingDetail>();
		bossQueueList = new ArrayList<BossQueueCallUp>();
		usersMap = new ConcurrentSkipListMap<String, Map<LocalDate, ReportList>>();
		checksumMap = new ConcurrentSkipListMap<Integer, BossList>();

		userNameMap = new ConcurrentSkipListMap<String, String>();
		
	}
	
	@Override
    public void onPrivateMessageReceived(final PrivateMessageReceivedEvent event) {
        Message msg = event.getMessage();
        LocalDateTime eventDateTime = LocalDateTime.now();
        if(msg.getAuthor().isBot()) {
        	return;
        }

	    String message = msg.getContentRaw();
	    
	    if(message.isEmpty() ) {
	    	return;
	    }
	    
	    if(!isReady) {
            sendPrivateMessage(event.getAuthor(), "準備中，請後再試");
            return;
	    }
	    
        String[] command = message.toLowerCase().split("\\s+");
        if(command.length == 0) {
        	return;
        }
        if(PREFIX_CHAR != (message.charAt(0))) {
            sendPrivateMessage(event.getAuthor(), "歡迎使用報刀系統, 請輸入'!h'以顯示幫助訊息");
            return;
        }
        userNameMap.putIfAbsent(msg.getAuthor().getId(), msg.getAuthor().getName());
	    if(message.equals("!h")||message.equals("!help")||message.equals("!救")||message.equals("!幫助")||message.equals("!幫")) {
            sendPrivateMessage(event.getAuthor(), "如想報傷害，請用!r指令。" + Report.R_MESSAGE 
            					+ "\r\n如想查看本日已報刀，請用!l指令。(可在後面加上日期)"
            					+ "\r\n如有填錯想修正，請用!d指今。例如'!d 1'會刪除!l列出的第1條。(可在後面加上日期)");
    		return;
	    }

        Map<LocalDate, ReportList> userReportMap = null;
        ReportList rl = null;
		LocalDate date;
        switch(command[0]) {
        case "!r":
        case "!report":
        case "!報":
        case "!回":
        case "!回報":
        case "!報傷害":
            if(command.length == 1) {
                sendPrivateMessage(event.getAuthor(), "指令不對，並且各項目需以半型空格' '分開\r\n");
        		return;
            }
        	Report r;
        	try {
        		r = new Report(command, msg.getAuthor().getName(), eventDateTime);
        	} catch (ReportError e) {
                sendPrivateMessage(event.getAuthor(), "出理錯誤：" + e.getMessage());
                return;
        	}

        	userReportMap = usersMap.get(event.getAuthor().getId());
        	if(userReportMap != null) {
        		rl = userReportMap.get(r.getDate());
        		if(rl != null && !rl.isEmpty()) {
        			if(rl.isOverLimit(r)) {
        				if(r.isSecondStatus()){
	        				sendPrivateMessage(event.getAuthor(), "尾刀數目不足，請先輸入尾刀再輸入補償刀。目前已尾"+rl.getLastCount()+"刀, 已出補償"+rl.getSecondCount()+"刀" );
	      					return;
        				} else {
	        				sendPrivateMessage(event.getAuthor(), "今日已輸入3刀普通刀/尾刀，不能再多了");
	      					return;
        				}
        			}
        		}
        	}

        	if(userReportMap == null || rl == null || rl.isEmpty()) {
        		if(r.isSecondStatus()) {
    				sendPrivateMessage(event.getAuthor(), "請先輸入尾刀再輸入補償刀" );
  					return;
        		}
        	}

        	BossList checksumRL = checksumMap.computeIfAbsent(r.getRound()*10+r.getBoss(), x->new BossList(r.getRound(), r.getBoss()));
        	
        	if(checksumRL != null) {
        		try {
					checksumRL.verifyAndAdd(r);
				} catch (ChecksumError e) {
    				sendPrivateMessage(event.getAuthor(), "出理錯誤﹕"+e.getMessage());
  					return;
				}
        	}
        	if(rl == null) {
        		rl = new ReportList(event.getAuthor());
        	}
    		rl.add(r);
        	if(userReportMap == null) {
        		userReportMap = new ConcurrentSkipListMap<LocalDate, ReportList>();
        	}
    		userReportMap.putIfAbsent(r.getDate(), rl);
        	usersMap.putIfAbsent(event.getAuthor().getId(), userReportMap);
            sendPrivateMessage(event.getAuthor(), "已收到報告，" + r.toString());
    		return;
        case "!l":
        case "!看":
        case "!看分數":
        case "!查":
        case "!查分數":
        	userReportMap = usersMap.get(event.getAuthor().getId());
        	if(userReportMap == null) {
         		sendPrivateMessage(event.getAuthor(), "本月尚未輸入出刀資料");
         		return;
        	}
			if(command.length == 1) {
				date = PcrUtil.adjDate(eventDateTime);
			} else {
				try {
	    			date = LocalDate.parse(command[1]);	
	    		} catch (DateTimeParseException e) {
	    			sendPrivateMessage(event.getAuthor(), "日期格式錯誤, 應為'YYYY-MM-DD', 例如2020年8月31日請輸入'2020-08-31'");
	    			return;
	    		}
			}
    		rl = userReportMap.get(date);
    		if(rl == null || rl.isEmpty()) {
    			sendPrivateMessage(event.getAuthor(), "目前未輸入出刀資料" );
    		} else {
    			sendPrivateMessage(event.getAuthor(), rl.listReport());
    		}
        	
        	return;
        case "!d":
        case "!刪":
        case "!刪除":
        	userReportMap = usersMap.get(event.getAuthor().getId());
        	if(userReportMap == null) {
         		sendPrivateMessage(event.getAuthor(), "本月尚未輸入出刀資料");
         		return;
        	}
        	if(command.length == 1) {
        		sendPrivateMessage(event.getAuthor(), "請輸入要刪除的項目，格式為 !d 項目，例如'!d 1'會刪除!l列出的第1條。(可在後面加上日期)");
            	return;
        	}
        	int index;
        	try {
        		index = Integer.valueOf(command[1]);
        	} catch (NumberFormatException e) {
                sendPrivateMessage(event.getAuthor(), "項目數字有誤! 請填1~6");
                return;
        	}
        	if(index < 1 || index > 6) {
                sendPrivateMessage(event.getAuthor(), "項目數字有誤! 請填1~6");
                return;
        	}
			if(command.length == 2) {
				date = PcrUtil.adjDate(eventDateTime);
			} else {
				try {
	    			date = LocalDate.parse(command[2]);	
	    		} catch (DateTimeParseException e) {
	    			sendPrivateMessage(event.getAuthor(), "日期格式錯誤, 應為'YYYY-MM-DD', 例如2020年8月31日請輸入'2020-08-31'");
	    			return;
	    		}
			}
    		rl = userReportMap.get(date);
    		if(rl == null || rl.isEmpty()) {
    			sendPrivateMessage(event.getAuthor(), "目前尚未輸入出刀資料" );
    		} else if(index > rl.size()){
    			sendPrivateMessage(event.getAuthor(), "第"+index+"項資不存，目前共有"+rl.size()+"項出刀資料" );
    		} else {
    			sendPrivateMessage(event.getAuthor(), "已刪除" + rl.remove(index-1).toString() );
    		}
        	return;
        }
        sendPrivateMessage(event.getAuthor(), "指令不對, 請輸入'!h'以顯示幫助訊息");
		return;
        
	}

    public void sendPrivateMessage(User user, String content) {
        // openPrivateChannel provides a RestAction<PrivateChannel>
        // which means it supplies you with the resulting channel
        user.openPrivateChannel().queue((channel) -> {
            channel.sendMessage(content).queue();
        });
    }
	
    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        Message msg = event.getMessage();
        if(msg.getContentRaw().isEmpty() || PREFIX_CHAR != (msg.getContentRaw().charAt(0)) ||  msg.getAuthor().isBot()) {
        	return;
        }
        if(msg.isFromType(ChannelType.PRIVATE)) {
        	return;
        }

        String[] command = msg.getContentRaw().split("\\s+");
        
        for(String sss : command) {
        	System.out.println(sss);
        }
        
        if (msg.getContentRaw().equals("!start")){
        	if(isReady) {
        		return;
        	}
        	isReady = true;
            MessageChannel channel = event.getChannel();
            if(msgPack == null)
            	msgPack = new PCRMessagePack();
            msgPack.setPublicChannel(channel);
            channel.sendMessage("1⃣2⃣3⃣4⃣5⃣分別代表1/2/3/4/5王").queue(r -> msgPack.setInfo(r));
            channel.sendMessage("想排隊點A/B, 出刀前請點D防撞, 王倒請在C點一下隻王").queue(r -> msgPack.setInfo2(r));
			channel.sendMessage("A: 1刀秒王排這邊").queue(m /* => Message */ -> {
				msgPack.setControlA(m);
				addOneToFiveEmoji(m);
			});
			channel.sendMessage("B: 普通刀排這邊").queue(m /* => Message */ -> {
				msgPack.setControlB(m);
				addOneToFiveEmoji(m);
			});
			channel.sendMessage("C: 報打到幾王").queue(m /* => Message */ -> {
				msgPack.setControlC(m);
				addOneToFiveEmoji(m);
			});
			channel.sendMessage("D: 出刀前點一下⚔️防撞刀").queue(m /* => Message */ -> {
				msgPack.setControlD(m);
				m.addReaction("⚔️").queue();
			});
			channel.sendMessage(getQueue()).queue(m->{
		    	msgPack.setQueueList(m);
		    });
			BossQueueCallUp.setMsgPack(msgPack);
			
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime nextRun = now.withHour(5).withMinute(15).withSecond(0);
			if(now.compareTo(nextRun) > 0)
			    nextRun = nextRun.plusDays(1);

			Duration duration = Duration.between(now, nextRun);
			long initalDelay = duration.getSeconds();
    
			dailyUpdate.scheduleAtFixedRate(new DailyUpdate(this),
			    initalDelay,
			    TimeUnit.DAYS.toSeconds(1),
			    TimeUnit.SECONDS);
			
			backupSchedule.scheduleAtFixedRate(new Backup(oneHitBossQueues,
					bossQueues, usersMap, userNameMap), 10, 10, TimeUnit.MINUTES);
			
        } else if(msg.getContentRaw().equals("!end")) {
        	msgPack.getInfo().delete().queue();
        	msgPack.getInfo2().delete().queue();
        	msgPack.getControlA().delete().queue();
        	msgPack.getControlB().delete().queue();
        	msgPack.getControlC().delete().queue();
        	msgPack.getControlD().delete().queue();
        	msgPack.getQueueList().delete().queue();
        	for(int i=0;i<5;i++) {
        		oneHitBossQueues[i].clear();
        		bossQueues[i].clear();
        	}
        	isReady = false;
        } else if(msg.getContentRaw().equals("!startAdmin")) {
        	if(isAdminReady) return;
        	isAdminReady = true;
            MessageChannel channel = event.getChannel();
            if(msgPack == null)
            	msgPack = new PCRMessagePack();
            msgPack.setAdminChannel(channel);
            if(startDate == null) {
            	startDate = LocalDate.now();
            }
            if(ReportList.currDate == null) {
            	ReportList.init(channel);
            }
            if(usersMap != null)
            usersMap.forEach((k,v)->{
            	ReportList rl = v.get(ReportList.currDate);
            	if(rl != null) {
            		rl.sendAdminMessage();
            	}
            });
		} else if (msgPack.getAdminChannel() != null
				&& msgPack.getAdminChannel().getIdLong() == msg.getChannel().getIdLong()
				&& msg.getContentRaw().equals("!excel")) {
			ExcelWriter.writeReport(usersMap, userNameMap);
		} else if (msgPack.getAdminChannel() != null
				&& msgPack.getAdminChannel().getIdLong() == msg.getChannel().getIdLong()
				&& msg.getContentRaw().equals("!nextDay")) {
			nextDay();
	    } else if (command.length > 1 && msgPack.getAdminChannel() != null
				&& msgPack.getAdminChannel().getIdLong() == msg.getChannel().getIdLong()
				&& command[0].equals("!setDate")) {
    		try {
    			startDate = LocalDate.parse(command[1]);
    			System.out.println("Start Date: " + startDate);
    		} catch (DateTimeParseException e) {
    			msgPack.getAdminChannel().sendMessage("日期格式錯誤, 應為'YYYY-MM-DD', 例如2020年8月31日請輸入'2020-08-31'").queue();
    		}
	    } else if (command.length > 1 && msgPack.getAdminChannel() != null
				&& msgPack.getAdminChannel().getIdLong() == msg.getChannel().getIdLong()
				&& command[0].equals("!loadBackup")) {
    		try {
    			ExcelWriter.readBackup(command[1]);
    		} catch (DateTimeParseException e) {
    			msgPack.getAdminChannel().sendMessage("日期格式錯誤, 應為'YYYY-MM-DD', 例如2020年8月31日請輸入'2020-08-31'").queue();
    		}
	    }
        
    }


    public void nextDay() {
    	for(BossList bl : checksumMap.values()) {
    		bl.validate();
    	}
		ExcelWriter.writeReport(usersMap, userNameMap);
    	for(int i=0;i<5;i++) {
    		oneHitBossQueues[i].clear();
    		bossQueues[i].clear();
    	}
		ReportList.nextDate();
		listBossQueue();
    }
    
    protected void updateCurrBoss(int bossNo, String userId){
    	logger.debug("updateCurrBoss start");
    	if(bossNo > 0 && bossNo < 6) {
    		if(currBoss == bossNo) return;
			logger.info(userId + " boss " + bossNo + " now");
			for (BossQueueCallUp callUp : bossQueueList) {
				if(callUp.getBossNo() == currBoss) {
					callUp.end();
				}
			}
			bossQueueList.clear();
			if(!sosSet.isEmpty()) {
				String sosMsg = "";
				for (FightingDetail detail : sosSet.values()) {
					logger.debug("Adding to SOS MSG: "+PcrUtil.userIdToTag(detail.getUserId()));
					sosMsg += PcrUtil.userIdToTag(detail.getUserId())+" ";
					detail.getMessage().delete().queue();
				}
				msgPack.getPublicChannel().sendMessage(sosMsg + "現在到" + bossNo + "王，可以出來了").queue(m->{
					m.delete().queueAfter(TIME_OUT, TimeUnit.SECONDS);
				});
				sosSet.clear();
			}
			fightingSet.clear();

    		++totalBossCount;

    		currBoss = bossNo;
	    	if(msgPack.getControlD() != null) {
	    		msgPack.getControlD().editMessage("D: 出刀前點一下⚔️防撞刀, 目前打到" + currBoss + "王" ).queue();
	    	}

	    	if(oneHitBossQueues[bossNo-1].isEmpty() && bossQueues[bossNo-1].isEmpty()) {
	    		msgPack.getPublicChannel()
	    		.sendMessage("已到" + bossNo + "王, 但沒有人在排 " + AUTO_DELETE_MESSAGE)
	    		.queue(msg -> msg.delete().queueAfter(TIME_OUT, TimeUnit.SECONDS));
	    	} else {
	    		logger.debug("scheduleAtFixedRate start");
	    		BossQueueCallUp callUp = new BossQueueCallUp(bossNo, oneHitBossQueues[bossNo-1], bossQueues[bossNo-1]);
	    		bossQueueList.add(callUp);
	    		queueThread.execute(()->{
		    		ScheduledFuture<?> schedule = queueSchedule.scheduleAtFixedRate(callUp, 0, TIME_OUT, TimeUnit.SECONDS);
		    		while(true) {
			    		logger.debug("CallUp position" + callUp.getCurrPosition());
			    		logger.debug("waiting for schedule result");
		    			try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
		    			if(callUp.isEnd()) {
		    				schedule.cancel(true);
		    				break;
		    			}
		    		}
	    		});
	    	}
    	}
    	logger.debug("updateCurrBoss end");
    }

    protected String getQueue() {
    	String msg = "";
    	msg += "目前隊列\n";
    	for (int i=0;i<5;i++){
    		if(oneHitBossQueues[i].isEmpty() && bossQueues[i].isEmpty()){
    			msg += (i+1) + "王沒有人在排";
    		} else {
    			msg += (i+1) + "王";
    			if(!oneHitBossQueues[i].isEmpty()){
    				msg += " 一刀秒: " + oneHitBossQueues[i].getTagString();
    			}
    			if(!bossQueues[i].isEmpty()){
    				msg += " 普通刀: " + bossQueues[i].getTagString();
    			}
    		}
    		msg+= "\n";
    	}
    	return msg;
    }
    
    protected void listBossQueue() {
    	String queueMsg = getQueue();
    	if(msgPack.getQueueList() != null) {
    		msgPack.getQueueList().editMessage(queueMsg).queue();
    	}
//    	if(listMessage !== null){
//    		listMessage.edit(m);
//    	} else {
//    		currChannel.send(m).then(msg => listMessage = msg);;
//    	}
		
	}
   
	@Override
	public void onMessageReactionAdd(final MessageReactionAddEvent event) {
    	if(!isReady) return;
		if(event.getUser().isBot()) return;
		logger.debug(event.getUserId());
		logger.debug(event.getUser().getAsMention());
		logger.debug("Before: " + msgPack);
    	MessageChannel channel = event.getChannel();
    	logger.debug("Channel ID: " + channel.getIdLong());
    	final String userId = event.getUserId();
    	if(channel.getIdLong() == msgPack.getPublicChannel().getIdLong()) {
    		int emoji = parseEmoji(event.getReactionEmote().getAsReactionCode());
    		logger.debug("Emoji: " + emoji);
    		if (emoji == 0) return;
    		if (event.getMessageIdLong() == msgPack.getControlA().getIdLong()) {
    			if(emoji > 5) return;
    			if(!oneHitBossQueues[emoji-1].contains(userId)) {
    				oneHitBossQueues[emoji-1].add(userId);
        			logger.info(event.getUserId() + " queue 1 hit " + emoji);
        			listBossQueue();
    			}
    		} else if (event.getMessageIdLong() == msgPack.getControlB().getIdLong()) {
    			if(emoji > 5) return;
    			if(!bossQueues[emoji-1].contains(userId)) {
    				bossQueues[emoji-1].add(userId);
        			logger.info(event.getUserId() + " queue normal " + emoji);
        			listBossQueue();
    			}
    		} else if (event.getMessageIdLong() == msgPack.getControlC().getIdLong()) {
    			if(emoji > 5) return;
				updateCurrBoss(emoji, userId);
	
    		} else if (event.getMessageIdLong() == msgPack.getControlD().getIdLong()) {
    			if(emoji != 6 || currBoss == 0) return;
    			channel.sendMessage(PcrUtil.userIdToTag(userId) + "出" + (currBoss) + "王1刀, 要救點🆘 "+AUTO_DELETE_MESSAGE).queue(r /* => Message */ -> {
    				final String id = r.getId();
    				for (BossQueueCallUp callUp : bossQueueList) {
    					if(callUp.getBossNo() == currBoss) {
    						callUp.end();
    					}
    				}
    				FightingDetail detail = new FightingDetail(id, userId, r, currBoss);
    				fightingSet.putIfAbsent(id, detail);
    				r.addReaction("🆘").queue();
    				r.delete().queueAfter(TIME_OUT, TimeUnit.SECONDS, (Void v)->{
    					if(!detail.isNeedSos()) {
    						oneHitBossQueues[detail.getBossNo()-1].remove(detail.getUserId());
    						bossQueues[detail.getBossNo()-1].remove(detail.getUserId());
    						listBossQueue();
    					}
    					fightingSet.remove(id);
    					
    				});
    			});
    			logger.info(event.getUserId() + " go " + currBoss + " boss");

    		} else if(emoji == 7 && fightingSet.containsKey(event.getMessageId())) {
				FightingDetail detail = fightingSet.get(event.getMessageId());
				if (detail.getUserId().equals(userId) && currBoss == detail.getBossNo()) {
	    			logger.info(event.getUserId() + " boss " + emoji + " need SOS");
					ArrayList<String> sosUserList = new ArrayList<String>();
					sosUserList.addAll(oneHitBossQueues[detail.getBossNo() - 1].getQueue());
					sosUserList.addAll(bossQueues[detail.getBossNo() - 1].getQueue());
					channel.sendMessage(PcrUtil.userIdToTag(userId) + "在" + (detail.getBossNo()) + "王掛樹，"
							+ sosUserList.stream().distinct().map(PcrUtil::userIdToTag).collect(Collectors.joining(" ")) + "有空幫救")
					.queue(r /* => Message */ -> {
						logger.debug("Add to sos list: " + r.getId());
						sosSet.putIfAbsent(r.getId(),new FightingDetail(r.getId(), userId, r, detail.getBossNo()));
					});
				}
    		}
    	//} else if(channel.getIdLong() == msgPack.getAdminChannel().getIdLong()) {
    		
    	}
    	
    	logger.debug("After: " + msgPack);
    	
    	return;
    }
    
	@Override
	public void onMessageReactionRemove(final MessageReactionRemoveEvent event) {
    	if(!isReady) return;
		if(event == null) return;
		logger.debug("User ID: " + event.getUserId());
		if(event.getUserId() == null) return;
		logger.debug("Before: " + msgPack);
    	MessageChannel channel = event.getChannel();
    	logger.debug("Channel ID: " + channel.getIdLong());
    	if(channel.getIdLong() == msgPack.getPublicChannel().getIdLong()) {
    		int emoji = parseEmoji(event.getReactionEmote().getAsReactionCode());
    		logger.debug("Emoji: " + emoji);
    		if (emoji == 0) return;
    		if (event.getMessageIdLong() == msgPack.getControlA().getIdLong()) {
    			if(emoji > 5) return;
    			oneHitBossQueues[emoji-1].remove(event.getUserId());
    			logger.info(event.getUserId() + " Leave 1 hit " + emoji);
    			listBossQueue();
    		} else if (event.getMessageIdLong() == msgPack.getControlB().getIdLong()) {
    			if(emoji > 5) return;
    			bossQueues[emoji-1].remove(event.getUserId());
    			logger.info(event.getUserId() + " Leave normal " + emoji);
    			listBossQueue();
			}
    	} else if(channel.getIdLong() == msgPack.getAdminChannel().getIdLong()) {
    		
    	}
    	logger.debug("After: " + msgPack);

    	return;
    }

	protected static void addOneToFiveEmoji(final Message m) {
 	   m.addReaction(EmojiParser.parseToUnicode(":one:")).queue();
 	   m.addReaction(EmojiParser.parseToUnicode(":two:")).queue();
 	   m.addReaction(EmojiParser.parseToUnicode(":three:")).queue();
 	   m.addReaction(EmojiParser.parseToUnicode(":four:")).queue();
 	   m.addReaction(EmojiParser.parseToUnicode(":five:")).queue();
    }
    
    protected static int parseEmoji(final String emoji) {
    	logger.debug("Emoji Raw:" + emoji);
		if(emoji.equals("1⃣")) {
			logger.debug("1⃣ equals " + emoji);
		}
    	switch(emoji) {
    	case "1⃣":
    		return 1;
    	case "2⃣":
    		return 2;
    	case "3⃣":
    		return 3;
    	case "4⃣":
    		return 4;
    	case "5⃣":
    		return 5;
    	case "⚔️":
    		return 6;
    	case "🆘":
    		return 7;
    	default:
    		return 0;
    	}
    }
//    public void 
}
