import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Person {
    String a;
    Map<String,Integer> map = new HashMap<>();
    List<String> list = new ArrayList<>();

    public Person() {
    }

    public Person(String a, Map<String, Integer> map, List<String> list) {
        this.a = a;
        this.map = map;
        this.list = list;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public Map<String, Integer> getMap() {
        return map;
    }

    public void setMap(Map<String, Integer> map) {
        this.map = map;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
