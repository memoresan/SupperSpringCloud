package spark;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.apache.spark.serializer.KryoRegistrator;

public class MyKryoRegistrator  implements KryoRegistrator {
    @Override
    public void registerClasses(Kryo kryo) {
        B b = new B();
        Output output = new Output(1256);
        kryo.writeObject(output, b);
        output.flush();
        output.close();
       // kryo.register(B.class);
    }
}
