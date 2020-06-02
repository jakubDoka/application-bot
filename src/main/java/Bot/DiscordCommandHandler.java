package Bot;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

import static Bot.BotMain.*;

public class DiscordCommandHandler implements MessageCreateListener {

    public HashMap<String,Command> commands = new HashMap<>();

    public void registerCommand(Command c){
        commands.put(c.name,c);
    }

    public boolean hasCommand(String command){
        return commands.containsKey(command);
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Message message = event.getMessage();
        if(message.getAuthor().isBotUser()) return;
        if(!message.getContent().startsWith(config.prefix)) return;
        TextChannel commandChannel = config.getChannel("commands");
        //if there is channel for commands
        if(commandChannel!=null && !message.isPrivateMessage() && event.getChannel().getId()!=commandChannel.getId()) {
            event.getChannel().sendMessage("This is not channel for commands.");
            message.delete();
            return;
        }
        String content=message.getContent();
        int nameLength = content.indexOf(" ");
        if(nameLength<0){
            runCommand(content.replace(config.prefix,""),new CommandContext(event,new String[0],null));
            return;
        }
        String theMessage = content.substring(nameLength+1);
        String[] args = theMessage.split(" ");
        String name = content.substring(config.prefix.length(),nameLength);
        runCommand(name,new CommandContext(event,args,theMessage));
    }

    /**Validates command**/
    private void runCommand(String name, CommandContext ctx) {
        Command command=commands.get(name);
        if(command==null){
            ctx.reply("Sorry i don t know this command.");
            return;
        }
        if(!command.hasPerm(ctx)){
            EmbedBuilder msg= new EmbedBuilder()
                    .setColor(Color.red)
                    .setTitle("ACCESS DENIED!")
                    .setDescription("You don't have high enough permission to use this command.");
            ctx.channel.sendMessage(msg);
            return;
        }
        Message message=ctx.event.getMessage();
        List<MessageAttachment> mas = message.getAttachments();
        boolean tooFew = ctx.args.length<command.minArgs,tooMatch=ctx.args.length>command.maxArgs;
        boolean correctFiles = command.attachment==null || (mas.size() == 1 && mas.get(0).getFileName().endsWith(command.attachment));
        if(tooFew || tooMatch || !correctFiles){
            EmbedBuilder eb= new EmbedBuilder()
                    .setColor(Color.red)
                    .setDescription("Valid format : " + config.prefix + name + " " + command.argStruct );
            if(tooFew) eb.setTitle("TOO FEW ARGUMENTS!" );
            else if(tooMatch) eb.setTitle( "TOO MATCH ARGUMENTS!");
            else eb.setTitle("INCORRECT ATTACHMENT!");
            ctx.channel.sendMessage(eb);
            return;
        }
        command.run(ctx);
    }
}
