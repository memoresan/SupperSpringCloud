package util.presto.udf;


import com.google.common.collect.ImmutableSet;
import io.prestosql.spi.Plugin;

import java.util.Set;

public class MyFunctionsPlugin implements Plugin {

    @Override
    public Set<Class<?>> getFunctions() {
        return ImmutableSet.<Class<?>>builder()
                .add(MyUDF.class)
                .add(MyUdaf.class)
                .build();
    }
}


