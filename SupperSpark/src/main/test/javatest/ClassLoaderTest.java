package javatest;

import sun.reflect.Reflection;

import java.lang.reflect.InvocationTargetException;

public class ClassLoaderTest {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<?> c = Class.forName("javatest.dog");
        Class<?> ca = ClassLoader.getSystemClassLoader().loadClass("javatest.dog");
        System.out.println(ClassLoader.getSystemClassLoader());
        System.out.println(ca.getConstructor().newInstance());
        //Thread.currentThread().getContextClassLoader().loadClass(name);


    }


}
class dog{

    public dog() {
    }
}