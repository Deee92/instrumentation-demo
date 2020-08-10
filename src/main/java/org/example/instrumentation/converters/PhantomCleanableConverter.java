package org.example.instrumentation.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PhantomCleanableConverter implements Converter {
    @Override
    public void marshal(Object o, HierarchicalStreamWriter hierarchicalStreamWriter, MarshallingContext marshallingContext) {
        hierarchicalStreamWriter.startNode("phantom");
        hierarchicalStreamWriter.setValue("nothing");
        hierarchicalStreamWriter.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
        return null;
    }

    @Override
    public boolean canConvert(Class aClass) {
        return (aClass.getCanonicalName().equals("jdk.internal.ref.CleanerImpl") ||
                aClass.getCanonicalName().equals("java.lang.ref.Cleaner.Cleanable") ||
                aClass.getCanonicalName().equals("java.lang.ref.PhantomReference") ||
                aClass.getCanonicalName().equals("java.util.zip.Inflater") ||
                aClass.getCanonicalName().equals("jdk.internal.ref.PhantomCleanable") ||
                aClass.getCanonicalName().equals("jdk.internal.ref.CleanerImpl$PhantomCleanableRef") ||
                aClass.getCanonicalName().equals("java.lang.ref.Cleaner") ||
                aClass.getCanonicalName().equals("java.io.InputStream"));

    }
}
