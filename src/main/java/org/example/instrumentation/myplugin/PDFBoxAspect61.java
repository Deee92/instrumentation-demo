package org.example.instrumentation.myplugin;

import org.glowroot.agent.plugin.api.*;
import org.glowroot.agent.plugin.api.weaving.*;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class PDFBoxAspect61 {
    @Pointcut(className = "org.apache.xmpbox.type.BooleanType", methodName = "getStringValue",
            methodParameterTypes = {}, timerName = "BooleanType - getStringValue")
    public static class PureMethodAdvice implements AdviceTemplate {
        private static final TimerName timer = Agent.getTimerName(PureMethodAdvice.class);
        private static final String transactionType = "Pure";
        private static final int COUNT = 61;
        private static String receivingObjectFilePath;
        private static String paramObjectsFilePath;
        private static String returnedObjectFilePath;
        private static Logger logger = Logger.getLogger(PureMethodAdvice.class);

        private static void setup() {
            AdviceTemplate.setUpXStream();
            String[] fileNames = AdviceTemplate.setUpFiles("org.apache.xmpbox.type.BooleanType.getStringValue");
            receivingObjectFilePath = fileNames[0];
            paramObjectsFilePath = fileNames[1];
            returnedObjectFilePath = fileNames[2];
        }

        public static synchronized void writeObjectXMLToFile(Object objectToWrite, String objectFilePath) {
            try {
                FileWriter objectFileWriter = new FileWriter(objectFilePath, true);
                xStream.toXML(objectToWrite, objectFileWriter);
                BufferedWriter bw = new BufferedWriter(objectFileWriter);
                bw.newLine();
                bw.flush();
                bw.close();
            } catch (Exception e) {
                logger.info("PDFBoxAspect" + COUNT);
            }
        }

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context,
                                          @BindReceiver Object receivingObject,
                                          @BindMethodName String methodName) {
            setup();
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
