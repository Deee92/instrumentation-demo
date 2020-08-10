package org.example.instrumentation.myplugin;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.glowroot.agent.plugin.api.*;
import org.glowroot.agent.plugin.api.weaving.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class PDFBoxAspect58 {
    @Pointcut(className = "org.apache.fontbox.cmap.CIDRange", methodName = "map",
            methodParameterTypes = {"char"}, timerName = "CIDRange - map")
    public static class PureMethodAdvice implements AdviceTemplate {
        private static final TimerName timer = Agent.getTimerName(PureMethodAdvice.class);
        private static final String transactionType = "Pure";
        private static final int COUNT = 58;
        private static String receivingObjectFilePath;
        private static String paramObjectsFilePath;
        private static String returnedObjectFilePath;
        private static Logger logger = Logger.getLogger(PureMethodAdvice.class);

        private static void setup() {
            AdviceTemplate.setUpXStream();
            String[] fileNames = AdviceTemplate.setUpFiles("org.apache.fontbox.cmap.CIDRange.map");
            receivingObjectFilePath = fileNames[0];
            paramObjectsFilePath = fileNames[1];
            returnedObjectFilePath = fileNames[2];
        }

        public static synchronized void writeObjectXMLToFile(Object objectToWrite, String objectFilePath) {
            try {
                FileWriter objectFileWriter = new FileWriter(objectFilePath, true);
                String xml = xStream.toXML(objectToWrite);
                xml = xml.replaceAll("(&#x)(\\w+;)", "&amp;#x$2");
                BufferedReader reader = new BufferedReader(new StringReader(xml));
                BufferedWriter writer = new BufferedWriter(objectFileWriter);
                while ((xml = reader.readLine()) != null) {
                    writer.write(xml);
                    writer.newLine();
                }
                writer.flush();
                writer.close();
            } catch (Exception e) {
                logger.info("PDFBoxAspect" + COUNT);
                e.printStackTrace();
            }
        }

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context,
                                          @BindReceiver Object receivingObject,
                                          @BindParameterArray Object parameterObjects,
                                          @BindMethodName String methodName) {
            setup();
            writeObjectXMLToFile(receivingObject, receivingObjectFilePath);
            writeObjectXMLToFile(parameterObjects, paramObjectsFilePath);
            MessageSupplier messageSupplier = MessageSupplier.create(
                    "className: {}, methodName: {}",
                    PureMethodAdvice.class.getAnnotation(Pointcut.class).className(),
                    methodName
            );
            return context.startTransaction(transactionType, methodName, messageSupplier, timer, OptionalThreadContext.AlreadyInTransactionBehavior.CAPTURE_NEW_TRANSACTION);
        }

        @OnReturn
        public static void onReturn(@BindReturn Object returnedObject,
                                    @BindTraveler TraceEntry traceEntry) {
            writeObjectXMLToFile(returnedObject, returnedObjectFilePath);
            traceEntry.end();
        }

        @OnThrow
        public static void onThrow(@BindThrowable Throwable throwable,
                                   @BindTraveler TraceEntry traceEntry) {
            traceEntry.endWithError(throwable);
        }
    }
}
