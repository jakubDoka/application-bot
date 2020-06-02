package Bot;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.HashMap;

import static Bot.BotMain.config;
import static java.lang.System.out;

public class ConsoleCommandHandler {
    public HashMap<String,Command> commands = new HashMap<>();

    public void registerCommand(Command c){
        commands.put(c.name,c);
    }

    public boolean hasCommand(String command){
        return commands.containsKey(command);
    }

    public void onCommand(String content) {
        int nameLength = content.indexOf(" ");
        if(nameLength<0){
            runCommand(content,new CommandContext(new String[0]));
            return;
        }
        String theMessage = content.substring(nameLength+1);
        String[] args = theMessage.split(" ");
        String name = content.substring(0,nameLength);
        runCommand(name,new CommandContext(args));
    }

    public void runCommand(String name,CommandContext ctx){
        Command command = commands.get(name);
        if(command==null ){
            if(!name.equals("")){
                out.println("Nonexistent command \""+name+"\"");
            }
            return;
        }
        boolean tooFew = ctx.args.length<command.minArgs;
        if(tooFew || ctx.args.length>command.maxArgs){
            out.println("Too "+(tooFew ? "few" : "match")+" arguments.("+command.argStruct+")");
            return;
        }
        command.run(ctx);
    }
}
