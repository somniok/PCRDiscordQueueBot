package net.somniok.pcr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter{
	public static final char PREFIX_CHAR = '!';

	protected static Logger logger = LoggerFactory.getLogger(Bot.class);
		
	protected static PCRMessagePack msgPack;
	
	protected List<String>[] oneHitBossQueues;
	protected List<String>[] bossQueues;
	protected Map<Long, BossHit> goingSet;
	protected Map<Long, BossHit> sosSet;
	
	protected int currBoss = 0;

	protected static final int TIME_OUT = 300;
	protected static final ScheduledExecutorService queueUpdate = Executors.newScheduledThreadPool(1);
	protected static final ScheduledExecutorService hitting = Executors.newScheduledThreadPool(1);
	
	protected static long totalBossCount = 0; 
	
	@SuppressWarnings("unchecked")
	public Bot() {
		oneHitBossQueues = (List<String>[]) new List[5];
		bossQueues = (List<String>[]) new List[5];
		for(int i=0;i<5;i++) {
			oneHitBossQueues[i] = Collections.synchronizedList(new ArrayList<String>());
			bossQueues[i] = Collections.synchronizedList(new ArrayList<String>());
		}
		goingSet = new ConcurrentHashMap<Long, BossHit>();
		sosSet = new ConcurrentHashMap<Long, BossHit>();
		
	}
	
    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        Message msg = event.getMessage();
        if(PREFIX_CHAR != (msg.getContentRaw().charAt(0)) ||  msg.getAuthor().isBot()) {
        	return;
        }

        if (msg.getContentRaw().equals("!start")){
            MessageChannel channel = event.getChannel();
            msgPack = new PCRMessagePack(channel);
            channel.sendMessage("1️⃣2️⃣3️⃣4️⃣5️⃣分別代表1/2/3/4/5王").queue(r -> msgPack.setInfo(r));
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
        } else if(msg.getContentRaw().equals("!startAdmin")) {
        	
        }
    }

    protected synchronized void updateCurrBoss(int bossNo) {
    	if(bossNo > 0 && bossNo < 6) {
    		if(currBoss == bossNo) return;
    		
    		++totalBossCount;

    		currBoss = bossNo;
	    	if(msgPack.getControlD() != null) {
	    		msgPack.getControlD().editMessage("D: 出刀前點一下⚔️防撞刀, 目前打到" + currBoss + "王" ).queue(m->{
	    			m.delete().queueAfter(TIME_OUT, TimeUnit.SECONDS);
	    		});
	    	}
	    	
//	    	if(queueTimeout !== null){
//	    		clearTimeout(queueTimeout);
//	    		queueTimeout = null;
//	    	}
//	    	if(prevUser !== null){
//	    		if(userMap.has(prevUser)){
//	    			userMap.set(prevUser, userMap.get(prevUser)+1);
//	    		} else {
//	    			userMap.set(prevUser, 1);
//	    		}
//	    	}
//	    	// console.log(prevUser);
//	    	console.log(new Date().timeNow() + " now " + i + u.username);
//	    	currBoss = i;
//	    	var users = '';
//	    	if(oneHitQueues[i-1].length && curr1HitCounter < oneHitQueues[i-1].length) {
//	    		users += oneHitQueues[i-1][curr1HitCounter];
//	    		if(userMap.has(oneHitQueues[i-1][curr1HitCounter])){
//	    			users += '(已錯過' + userMap.get(oneHitQueues[i-1][curr1HitCounter]) + '次)';
//	    		}
//	    		queueTimeout = setTimeout(updateCurrBoss, TIMEOUT, i, "系統", curr1HitCounter + 1, oneHitQueues[i-1][curr1HitCounter]);
//	    		currChannel.send(users + "已到" + i + "王" + " by " + u + " "+ (TIMEOUT / 60000)+"分鐘後自動刪除").then(msg => msg.delete(TIMEOUT));
//	    	} else if (bossQueues[i-1].length) {
//	    		users += bossQueues[i-1].join(' ');
//	    		currChannel.send(users + "已到" + i + "王" + " by " + u + " "+ (TIMEOUT / 60000)+"分鐘後自動刪除").then(msg => msg.delete(TIMEOUT));
//	    	} else {
//	    		currChannel.send("已到" + i + "王, 但沒有人在排" + " by " + u + " "+ (TIMEOUT / 60000)+"分鐘後自動刪除").then(msg => msg.delete(TIMEOUT));
//	    	}
//	    	
    	}
    }
    
    protected synchronized String getQueue() {
    	String msg = "";
    	msg += "目前隊列\n";
    	for (int i=0;i<5;i++){
    		if(oneHitBossQueues[i].isEmpty() && bossQueues[i].isEmpty()){
    			msg += (i+1) + "王沒有人在排";
    		} else {
    			msg += (i+1) + "王";
    			if(!oneHitBossQueues[i].isEmpty()){
    				msg += " 一刀秒: " + oneHitBossQueues[i].stream().map(id->"<@" + id + ">").collect(Collectors.joining(" "));
    			}
    			if(!bossQueues[i].isEmpty()){
    				msg += " 普通刀: " + bossQueues[i].stream().map(id->"<@" + id + ">").collect(Collectors.joining(" "));
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
		if(event.getUser().isBot()) return;
		logger.debug(event.getUserId());
		logger.debug(event.getUser().getAsMention());
		logger.debug("Before: " + msgPack);
    	MessageChannel channel = event.getChannel();
    	logger.debug("Channel ID: " + channel.getIdLong());
    	if(channel.getIdLong() == msgPack.getPublicChannel().getIdLong()) {
    		
    		int emoji = parseEmoji(event.getReactionEmote().getAsReactionCode());

    		logger.debug("Emoji: " + emoji);
    		if (emoji == 0) return;

    		if (event.getMessageIdLong() == msgPack.getControlA().getIdLong()) {
    			if(emoji > 5) return;
    			if(!oneHitBossQueues[emoji-1].contains(event.getUserId())) {
    				oneHitBossQueues[emoji-1].add(event.getUserId());
        			listBossQueue();
    			}
    		} else if (event.getMessageIdLong() == msgPack.getControlB().getIdLong()) {
    			if(emoji > 5) return;
    			if(!bossQueues[emoji-1].contains(event.getUserId())) {
    				bossQueues[emoji-1].add(event.getUserId());
        			listBossQueue();
    			}
    		} else if (event.getMessageIdLong() == msgPack.getControlC().getIdLong()) {
    			if(emoji > 5) return;
    			updateCurrBoss(emoji);
    		} else if (event.getMessageIdLong() == msgPack.getControlD().getIdLong()) {
    			if(emoji != 6 || currBoss == 0) return;
    			channel.sendMessage(event.getUser().getAsMention() + "出" + (currBoss) + "王1刀, 要救點🆘").queue(r /* => Message */ -> {
    				r.addReaction("🆘").queue();;
    			});
    		} else if(goingSet.containsKey(event.getMessageIdLong())) {
    			channel.retrieveMessageById(event.getMessageIdLong()).queue((msg)->{
    				if(msg.getAuthor().getIdLong() == event.getUserIdLong() && emoji == 6) {
    					sos("");
    				}
    			}, (failure) -> {
    				
    			});
    		}
    		
    		
    		
    	} else if(channel.getIdLong() == msgPack.getAdminChannel().getIdLong()) {
    		
    	}
    	
    	logger.debug("After: " + msgPack);
    	
    	return;
    }
    
	@Override
	public void onMessageReactionRemove(final MessageReactionRemoveEvent event) {
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
    			listBossQueue();
    		} else if (event.getMessageIdLong() == msgPack.getControlB().getIdLong()) {
    			if(emoji > 5) return;
    			bossQueues[emoji-1].remove(event.getUserId());
    			listBossQueue();
			}
    	} else if(channel.getIdLong() == msgPack.getAdminChannel().getIdLong()) {
    		
    	}
    	logger.debug("After: " + msgPack);
//		jda.retrieveUserById(event.getUserId()).queue(user->{
//
//		});;
    	return;
    }
	
    private void sos(String str) {
        Thread t = new Thread(() -> someFunc(str));
        t.start();
		
	}

	private Object someFunc(String str) {
		// TODO Auto-generated method stub
		return null;
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
