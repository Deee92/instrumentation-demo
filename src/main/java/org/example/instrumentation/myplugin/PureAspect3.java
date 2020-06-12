package org.example.instrumentation.myplugin;

import com.thoughtworks.xstream.XStream;
import org.glowroot.agent.plugin.api.*;
import org.glowroot.agent.plugin.api.weaving.*;

import java.io.FileWriter;

public class PureAspect3 {
    @Pointcut(className = "com.turn.ttorrent.client.Piece", methodName = "equals",
            methodParameterTypes = {"java.lang.Object"}, timerName = "piece equality")
    public static class PureMethodAdvice {

        private static final TimerName timer = Agent.getTimerName(PureMethodAdvice.class);
        private static final String transactionType = "Pure";
        private static Logger logger = Logger.getLogger(PureMethodAdvice.class);

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context, @BindMethodName String methodName, @BindParameter Object parameterObject) {
            logger.info("methodName: " + methodName);
            logger.info("parameter: " + parameterObject.getClass());
            MessageSupplier messageSupplier = MessageSupplier.create(
                    "method: {}",
                    methodName
            );
            return context.startTransaction(transactionType, methodName, messageSupplier, timer, OptionalThreadContext.AlreadyInTransactionBehavior.CAPTURE_NEW_TRANSACTION);
        }

        @OnReturn
        public static void onReturn(@BindReturn Object returnedObject, @BindTraveler TraceEntry traceEntry) {
            try {
                XStream xStream = new XStream();
                xStream.toXML(returnedObject, new FileWriter("/home/user/xstream-3.xml", true));
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("returnedObject: " + returnedObject.getClass().getSimpleName());
            traceEntry.end();
        }

        @OnThrow
        public static void onThrow(@BindThrowable Throwable throwable,
                                   @BindTraveler TraceEntry traceEntry) {
            traceEntry.endWithError(throwable);
        }
    }
}
