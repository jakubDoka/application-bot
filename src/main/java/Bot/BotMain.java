package Bot;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.File;

import static java.lang.System.out;

public class BotMain {
    public static String dir = "applicationBot/";
    public static BotConfig config;
    public static Blacklist blacklist;
    public static Commands commands;
    public static Tester tester = new Tester();
    public static DiscordApi api = null;


    public BotMain(){
        out.println("Starting...");
        //make sure there is a directory
        new File(dir).mkdir();
        blacklist = new Blacklist();
        out.println("Use \"connect\" command to connect to your bot.");
        commands = new Commands();
    }

    public static void main(String[] args){
        new BotMain();
    }
}
