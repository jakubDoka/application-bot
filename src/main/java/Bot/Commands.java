package Bot;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.Optional;
import java.util.Scanner;

import static java.lang.System.out;
import static Bot.BotMain.*;

public class Commands {
    private boolean active = true;
    private final ConsoleCommandHandler consoleHandler = new ConsoleCommandHandler();
    private final DiscordCommandHandler discordHandler = new DiscordCommandHandler();

    public Commands(){
        registerDiscordCommands(discordHandler);
        registerConsoleCommands(consoleHandler);
        Scanner input = new Scanner(System.in);
        while(active){
            consoleHandler.onCommand(input.nextLine());
        }
    }

    private void registerConsoleCommands(ConsoleCommandHandler handler) {
        handler.registerCommand(new Command("help") {
            {
                description = "Shows all commands and descriptions.";
            }
            @Override
            public void run(CommandContext ctx) {
                StringBuilder sb = new StringBuilder();
                for(Command c:handler.commands.values()){
                    sb.append(c.getInfo()).append("\n");
                }
                out.println(sb.toString().replace("*","").replace("-for everyone",""));
            }
        });

        handler.registerCommand(new Command("connect") {
            {
                description = "Connects the bot with discord.";
            }
            @Override
            public void run(CommandContext ctx) {
                if(api!=null){
                    out.println("Disconnecting...");
                    api.disconnect();
                    api=null;
                }
                out.println("Connecting...");
                config = new BotConfig();
                if(api!=null){
                    api.addMessageCreateListener(discordHandler);
                    out.println("Connected.");
                }

            }
        });

        handler.registerCommand(new Command("disconnect") {
            {
                description = "Disconnects the bot.";
            }
            @Override
            public void run(CommandContext ctx) {
                if(api==null) {
                    out.println("Nothing to disconnect.");
                    return;
                }
                api.disconnect();
                api=null;
                out.println("Disconnected.");
            }
        });

        handler.registerCommand(new Command("exit") {
            {
                description="Exits the application.";
            }
            @Override
            public void run(CommandContext ctx) {
                handler.runCommand("disconnect",new CommandContext(new String[0]));
                out.println("Shutting down...");
                active=false;
            }
        });

        handler.registerCommand(new Command("load","<test/blacklist>") {
            {
                description = "Reloads data of target.";
            }
            @Override
            public void run(CommandContext ctx) {
                switch (ctx.args[0]){
                    case "test":
                        tester.load();
                    case "blacklist":
                        blacklist.load();
                }
            }
        });
    }

    private void registerDiscordCommands(DiscordCommandHandler handler) {
        handler.registerCommand(new Command("help") {
            {
                description = "Shows all commands and their description.";
            }
            @Override
            public void run(CommandContext ctx) {
                EmbedBuilder eb =new EmbedBuilder()
                        .setTitle("COMMANDS")
                        .setColor(Color.orange);
                StringBuilder sb=new StringBuilder();
                sb.append("*!commandName - restriction - <necessary> [optional] |.fileExtension| - description*\n");
                for(String s:handler.commands.keySet()){
                    sb.append(handler.commands.get(s).getInfo()).append("\n");
                }
                ctx.channel.sendMessage(eb.setDescription(sb.toString()));
            }
        });

        handler.registerCommand(new Command("apply") {
            {
                description = "Starts application test.";
            }
            @Override
            public void run(CommandContext ctx) {
                if(!tester.haveQuestions()){
                    ctx.reply("Testing is not possible right now.");
                }
                Optional<User> optionalUser = ctx.author.asUser();
                if(!optionalUser.isPresent()){
                    ctx.reply("Sorry i cannot test you.");
                    return;
                }
                User applicant = optionalUser.get();
                Long penalty=blacklist.contains(applicant);
                if(penalty!=null){
                    ctx.reply("You still have penalty from lats time so i mustn't let you apply yet.("+Tools.milsToTime(penalty)+")");
                    return;
                }
                if(tester.isTesting(applicant.getId())){
                    ctx.reply("You are currently being tested, I cannot open two tests for you.");
                }
                tester.startTest(applicant);
            }
        });

        handler.registerCommand(new Command("answer","<option-index>") {
            {
                description = "Is valid only if you use it in private conversation with bot. Answers on current question.";
            }
            @Override
            public void run(CommandContext ctx) {
                if(!Tools.canParseInt(ctx.args[0])){
                    ctx.reply("Option index has to be integer");
                    return;
                }
                Optional<User> optionalUser = ctx.author.asUser();
                if(!optionalUser.isPresent()){
                    ctx.reply("Sorry i cannot test you.");
                    return;
                }
                long applicantId = optionalUser.get().getId();
                if(!tester.isTesting(applicantId)){
                    ctx.reply("You are not being tested, use "+config.prefix+"apply to start the test.");
                    return;
                }
                tester.answer(ctx,applicantId);
            }
        });
    }
}
