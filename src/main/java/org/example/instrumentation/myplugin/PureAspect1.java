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

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context, @BindMethodName String methodName) {
            logger.info("methodName: " + methodName);
            MessageSupplier messageSupplier = MessageSupplier.create(
                    "method: {}",
                    methodName
            );
            return context.startTransaction(transactionType, methodName, messageSupplier, timer, OptionalThreadContext.AlreadyInTransactionBehavior.CAPTURE_NEW_TRANSACTION);
        }

        @OnReturn
        public static void onReturn(@BindReturn Object returnedObject, @BindTraveler TraceEntry traceEntry) {
            Dog dog = new Dog("Kaaju", 4, "yellow");
            try {
//                Kryo kryo = new Kryo();
//                kryo.setRegistrationRequired(false);
//                logger.info(String.valueOf(returnedObject.hashCode()));
//                kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
//
//                Output output = new Output(new FileOutputStream("/home/user/file.txt", true));
//                kryo.writeClassAndObject(output, returnedObject);
//                output.close();
//
//                Input input = new Input(new FileInputStream("/home/user/file.txt"));
//                Object deserializedReturnedObject = kryo.readClassAndObject(input);
//                if (returnedObject.getClass().isInstance(deserializedReturnedObject)) {
//                    logger.info("Object deserialized");
//                    logger.info(String.valueOf(deserializedReturnedObject.hashCode()));
//                }
//                input.close();
                XStream xStream = new XStream();
                xStream.toXML(returnedObject, new FileWriter("/home/user/xstream-1.xml", true));
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
