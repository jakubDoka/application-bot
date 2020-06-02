package Bot;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static java.lang.System.out;

public class Tools {
    public static String milsToTime(long mils){
        long sec=mils/1000;
        long min=sec/60;
        long hour=min/60;
        long days=hour/24;
        return String.format("%d:%02d:%02d:%02d",
                days%365,hour%24,min%60,sec%60);
    }

    public static void loadJson(String filename, load load, Runnable save){
        try (FileReader fileReader = new FileReader(filename)) {
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(fileReader);
            JSONObject saveData = (JSONObject) obj;
            load.run(saveData);
            fileReader.close();
            out.println("Data from "+filename+" loaded.");
        } catch (FileNotFoundException ex) {
            out.println("No "+filename+" found.");
            if(save==null) return;
            out.println("Default one wos created.");
            save.run();
        } catch (ParseException ex) {
            out.println("Json file "+filename+" is invalid.");
        } catch (IOException ex) {
            out.println("Error when loading data from " + filename + ".");
        }
    }

    public static void saveJson(String filename,String success, String toSave){
        try (FileWriter file = new FileWriter(filename)) {
            file.write(toSave);
            file.close();
            out.println(success==null ? "Data saved to "+filename+"." : success);
        } catch (IOException ex) {
            out.println("Error when creating/updating "+filename+".");
        }
    }

    public static boolean canParseInt(String s){
        if(s.equals("")) return false;
        for(int i = 0; i<s.length();i++){
            if(!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    public interface load {
        void run(JSONObject data);
    }

}

