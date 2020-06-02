package Bot;


import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.json.simple.JSONArray;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static Bot.BotMain.*;
public class Tester {
    //more people can be tested at the same time
    private static final HashMap<Long,Test> tests = new HashMap<>();
    private static final String testFile = dir+"Test.json";
    private static final ArrayList<Question> questions = new ArrayList<>();

    public Tester(){
        load();
    }

    public void load(){
        questions.clear();
        tests.clear();
        Tools.loadJson(testFile,(test)->{
            for(Object o:test.keySet()){
                String question = (String)o;
                ArrayList<String> options = new ArrayList<>();
                for(Object op:(JSONArray)test.get(o)){
                    options.add((String)op);
                }
                questions.add(new Question(question,options));
            }
        },this::defaultTest);
    }

    private void defaultTest() {
        Tools.saveJson(testFile,"Default test created, edit it to build your own questions.",
                "{\n" +
                        "\t\"this question has one correct answer\":[\n" +
                        "\t\t\"#this option is correct\",\n" +
                        "\t\t\"this is incorrect\",\n" +
                        "\t\t\"this one is incorrect too\"\n" +
                        "\t],\n" +
                        "\t\"What color is see?\":[\n" +
                        "\t\t\"#blue\",\n" +
                        "\t\t\"green\",\n" +
                        "\t\t\"purple\"\n" +
                        "\t],\n" +
                        "\t\"This question has two correct answers and also more options\":[\n" +
                        "\t\t\"#correct\",\n" +
                        "\t\t\"#orrect\",\n" +
                        "\t\t\"incorrect\",\n" +
                        "\t\t\"incorrect\"\n" +
                        "\t]\n" +
                        "}"

                );
    }

    public boolean isTesting(long id){
        return tests.containsKey(id);
    }

    public boolean haveQuestions(){
        return questions.size()!=0;
    }

    public void answer(CommandContext ctx,long id){
        int answer = Integer.parseInt(ctx.args[0]);
        Test test = tests.get(id);
        if(answer<1 || answer>=test.current.options.size()){
            ctx.reply("You have to pick one of the options.");
        }
        if(test.current.isCorrect(answer-1)) test.correct++;
        ctx.reply("Answer processed.");
        test.ask();
    }

    public void startTest(User applicant) {
        tests.put(applicant.getId(),new Test(applicant));
    }

    private static class Question {
        String question;
        ArrayList<String> options;

        Question(String question,ArrayList<String> options){
            this.question=question;
            this.options=options;
        }

        boolean isCorrect(int option){
            return options.get(option).contains("#");
        }
    }

    private static class Test {
       User tested;
       Question current = null;
       int qIdx = 0;
       int correct = 0;

       Test(User tested){
           this.tested=tested;
           ask();
       }

       private void ask(){
           if(qIdx>=questions.size()){
               evaluate();
               return;
           }
           current = questions.get(qIdx);
           StringBuilder sb = new StringBuilder().append("**").append(current.question).append("**\n");
           for(int i=0;i<current.options.size();i++){
               sb.append("**").append(i+1).append(").**");
               sb.append(current.options.get(i).replace("#","")).append("\n");
           }
           EmbedBuilder eb = new EmbedBuilder().setTitle("QUESTION " + (qIdx+1))
                   .setDescription(sb.toString()+"*Write "+config.prefix+"answer <1-"+current.options.size()+">*")
                   .setColor(Color.blue);
           tested.sendMessage(eb);
           qIdx++;
       }

        private void evaluate() {
            EmbedBuilder eb = new EmbedBuilder();
           if(questions.size()==correct){
                Role role = config.getRole("candidate");
                eb.setColor(Color.green).setTitle("SUCCESS");
                eb.setDescription("Congratulations you passed the test. I em assigning **"+
                       role.getName()+"** role to you.");
                tested.sendMessage(eb);
                tested.addRole(role);
           } else {
               eb.setColor(Color.red).setTitle("FAIL");
               eb.setDescription("Sorry but some of your answers were incorrect. You can retake test after your penalty " +
                       "expires.("+Tools.milsToTime(config.penalty)+")");
               blacklist.add(tested);
               tested.sendMessage(eb);
           }
           tests.remove(tested.getId());
        }
    }
}
