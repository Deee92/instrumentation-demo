package org.example.instrumentation.myplugin;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.thoughtworks.xstream.XStream;
import org.glowroot.agent.plugin.api.*;
import org.glowroot.agent.plugin.api.weaving.*;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;

public class PureAspect1 {
    @Pointcut(className = "com.turn.ttorrent.bcodec.BEValue", methodName = "getValue",
            methodParameterTypes = {}, timerName = "BE value")
    public static class PureMethodAdvice {

        private static final TimerName timer = Agent.getTimerName(PureMethodAdvice.class);
        private static final String transactionType = "Pure";
        private static Logger logger = Logger.getLogger(PureMethodAdvice.class);
        private static XStream xStream = new XStream();

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context,
                                          @BindReceiver Object receivingObject,
                                          @BindMethodName String methodName) {
            try {
                xStream.toXML(receivingObject, new FileWriter("/home/user/object-data/xstream-receiving-1.xml", true));
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            try {
                xStream.toXML(returnedObject, new FileWriter("/home/user/object-data/xstream-1.xml", true));
            } catch (Exception e) {
                e.printStackTrace();
            }
            traceEntry.end();
        }

        @OnThrow
        public static void onThrow(@BindThrowable Throwable throwable,
                                   @BindTraveler TraceEntry traceEntry) {
            traceEntry.endWithError(throwable);
        }
    }
}
