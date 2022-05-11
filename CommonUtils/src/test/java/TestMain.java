
import com.google.common.collect.Maps;
import util.jdk.KyroUtils;

import java.util.Collections;

public class TestMain {
    public static void main(String[] args){

        Person person = new Person("1",   Maps.newLinkedHashMap(), Collections.emptyList());
        byte[] bytes = KyroUtils.writeObject(person, 100);
        Person result = KyroUtils.readObject(bytes, Person.class);
        System.out.println(result.getA()+"-"+result.map.size()+"-"+result.list.size());


    }
}
