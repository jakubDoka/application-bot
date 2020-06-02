package Bot;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class CommandContext {
    public TextChannel channel;
    public MessageAuthor author;
    public MessageCreateEvent event;
    public String[] args;
    public String message;

    public CommandContext(String[] args){
        this.args=args;
    }

    public CommandContext(MessageCreateEvent event, String[] args, String message) {
        this.event=event;
        this.args=args;
        this.message=message;
        this.author=event.getMessageAuthor();
        this.channel=event.getChannel();
    }

    public void reply(String message) {
        MessageBuilder mb = new MessageBuilder();
        mb.append(message);
        mb.send(channel);
    }
}
