package it.unicam.quasylab.sibilla.core.models.dopm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class test {
    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<>();
        map.put("test", 1);
        map.put("testa", 1);
        Set<String> s = map.keySet();
        s.remove("test");
        System.out.println(map.get("test"));
    }
}
