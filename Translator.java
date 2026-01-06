import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;

public class Translator{
    private final Map<Character,String> map=new HashMap<>();
    private final Map<String,String> inv_map=new HashMap<>();
    public Translator(){
        loadProperties();
    }
    void loadProperties(){
        Properties props=new Properties();
        try(FileInputStream fis=new FileInputStream(System.getProperty("user.dir")+File.separator+"Mapping.properties")){
            props.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Mapping.properties", e);
        }
        for(String key:props.stringPropertyNames()){
            if(key.equals("NEWLINE")) map.put('\n',props.getProperty(key));
            else if(key.equals("SPACE")) map.put(' ',props.getProperty(key));
            else map.put(key.charAt(0),props.getProperty(key));
        }
        Properties props2=new Properties();
        try(FileInputStream fis=new FileInputStream("Inverse_Mapping.properties")){
            props2.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Mapping.properties", e);
        }
        for(String key:props2.stringPropertyNames()){
            if(props2.getProperty(key).equals("NEWLINE")) inv_map.put(key,"\n");
            else if(props2.getProperty(key).equals("SPACE")) inv_map.put(key," ");
            else inv_map.put(key,props2.getProperty(key));
        }
    }
    String encodeEnToPs(char ch){
        return map.getOrDefault(ch,String.valueOf(ch));
    }
    String translateEnToPs(String en){
        StringBuilder ps=new StringBuilder();
        for(int i=0;i<en.length();i++){
            ps.append(
            map.getOrDefault(en.charAt(i), String.valueOf(en.charAt(i)))
            );        
        }
        return ps.toString();
    }
    String translatePsToEn(String ps){
        StringBuilder en = new StringBuilder();
        for (int i = 0; i < ps.length(); i++) {
            if (i + 1 < ps.length()) {
                String two = ps.substring(i, i + 2);
                if (inv_map.containsKey(two)) {
                    en.append(inv_map.get(two));
                    i++;
                    continue;
                }
            }
            String one = String.valueOf(ps.charAt(i));
            en.append(inv_map.getOrDefault(one, one));
        }
        return en.toString();
    }

}