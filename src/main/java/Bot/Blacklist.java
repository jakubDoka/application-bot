package Bot;

import org.javacord.api.entity.user.User;
import org.json.simple.JSONObject;

import java.util.Date;
import java.util.HashMap;

import static Bot.BotMain.config;
import static Bot.BotMain.dir;

public class Blacklist {
    private static final String saveFile = dir+"blacklist.json";


    private final HashMap<Long,Long> data = new HashMap<>();

    public Blacklist(){
        load();
    }

    public void add(User user){
        data.put(user.getId(),new Date().getTime()+config.penalty);
        save();
    }

    public Long contains(User user){
        if(!data.containsKey(user.getId())) return null;
        long left = data.get(user.getId())-new Date().getTime();
        //remove it if it expired
        if(left<0){
            data.remove(user.getId());
            return null;
        }
        return left;
    }

    public void save() {
        JSONObject saved = new JSONObject();
        for(Long l:data.keySet()){
            saved.put(l,data.get(l));
        }
        Tools.saveJson(saveFile,"Blacklist saved.",saved.toJSONString());
    }

    public void load(){
        Tools.loadJson(saveFile,(loaded)->{
            data.clear();
            for (Object o:loaded.keySet()){
                data.put(Long.parseLong((String)o),(Long) loaded.get(o));
            }
        },this::save);
    }
}
