package org.example.instrumentation.myplugin;

import com.thoughtworks.xstream.XStream;
import org.example.instrumentation.converters.FileDescriptorConverter;
import org.example.instrumentation.converters.RandomAccessFileConverter;
import org.example.instrumentation.converters.ThreadConverter;
import org.example.instrumentation.converters.ThreadGroupConverter;
import org.glowroot.agent.plugin.api.*;
import org.glowroot.agent.plugin.api.weaving.*;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class PureAspect50 {
    @Pointcut(className = "com.turn.ttorrent.common.creation.Source", methodName = "getSourceHolder",
            methodParameterTypes = {}, timerName = "Source - getSourceHolder")
    public static class PureMethodAdvice {

        private static final TimerName timer = Agent.getTimerName(PureMethodAdvice.class);
        private static final String transactionType = "Pure";
        private static Logger logger = Logger.getLogger(PureMethodAdvice.class);
        private static XStream xStream = new XStream();
        private static final String receivingObjectFilePath = "/home/user/object-data/50-receiving.xml";
        private static final String returnedObjectFilePath = "/home/user/object-data/50-returned.xml";

        public static synchronized void writeObjectXMLToFile(Object objectToWrite, String objectFilePath) {
            try {
                FileWriter objectFileWriter = new FileWriter(objectFilePath, true);
                xStream.toXML(objectToWrite, objectFileWriter);
                BufferedWriter bw = new BufferedWriter(objectFileWriter);
                bw.newLine();
                bw.flush();
                bw.close();
            } catch (Exception e) {
                logger.info("PureAspect50");
                e.printStackTrace();
            }
        }

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context,
                                          @BindReceiver Object receivingObject,
                                          @BindMethodName String methodName) {
            xStream.registerConverter(new FileDescriptorConverter());
            xStream.registerConverter(new ThreadConverter());
            xStream.registerConverter(new ThreadGroupConverter());
            xStream.registerConverter(new RandomAccessFileConverter());
            writeObjectXMLToFile(receivingObject, receivingObjectFilePath);
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
