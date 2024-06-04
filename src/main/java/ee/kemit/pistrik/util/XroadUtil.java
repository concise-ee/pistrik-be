package ee.kemit.pistrik.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class XroadUtil {

  private static final String delimiter = ":";

  public static String removeSOAPStuff(Object body) {
    Gson gson = new Gson();
    String bodyAsJson = gson.toJson(body);
    Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
    Map<String, Object> map = gson.fromJson(bodyAsJson, type);
    Set<String> prefixes = new HashSet<>();
    collectPotentialPrefixes(map, prefixes);
    return bodyAsJson.replaceAll(String.join("|", prefixes), "");
  }

  private static void collectPotentialPrefixes(
      Map<String, Object> map, Set<String> potentialPrefixes) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (key.contains(delimiter)) {
        potentialPrefixes.add(key.substring(0, key.indexOf(delimiter) + 1));
      }
      if (value instanceof Map) {
        collectPotentialPrefixes((Map<String, Object>) value, potentialPrefixes);
      }
    }
  }
}
