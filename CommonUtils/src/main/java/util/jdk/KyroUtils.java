package util.jdk;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class KyroUtils implements Serializable {
    //由于线程不安全在多线程情况下使用threadlocal保证线程安全
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL =
            new ThreadLocal<Kryo>(){
                @Override
                protected Kryo initialValue()
                {
                    return new Kryo();
                }
            };

    public static <T> byte[] writeObject(T obj, int maxBufferSize)
    {
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        Output output = new Output(maxBufferSize);
        kryo.writeObject(output, obj);
        output.flush();
        output.close();
        return output.toBytes();
    }

    public static <T> T readObject(byte[] bytes, Class<T> cls)
    {
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        Input input = new Input(bytes);
        return kryo.readObject(input, cls);
    }

    public <T> byte[] writeObjectBySpark(T obj, int maxBufferSize)
    {
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        Output output = new Output(maxBufferSize);
        kryo.writeObject(output, obj);
        output.flush();
        output.close();
        return output.toBytes();
    }

    public <T> T readObjectBySpark(byte[] bytes, Class<T> cls)
    {
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        Input input = new Input(bytes);
        return kryo.readObject(input, cls);
    }

}
