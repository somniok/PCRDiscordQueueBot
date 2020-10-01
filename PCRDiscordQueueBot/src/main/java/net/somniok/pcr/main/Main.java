package net.somniok.pcr.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.somniok.pcr.object.BossData;

public class Main {
	

    public static void main(String[] args) throws LoginException {
//      if (args.length < 1) {
//          System.out.println("You have to provide a token as first argument!");
//          System.exit(1);
//      }0
  	FileReader reader;
		try {
			reader = new FileReader("token.txt");
	        Properties p=new Properties();
	        p.load(reader);
	        Properties bossData = new Properties();
        	bossData.load(new FileReader("config.txt"));
        	BossData.init(bossData);

	        JDABuilder.createLight(p.getProperty("token"))
	                .addEventListeners(new Bot())
	                .setActivity(Activity.playing("公主連結戰隊戰")).build();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
      
  }
}
