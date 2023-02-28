package com.yyb.protocol;

import com.yyb.executor.ExecutorManager;
import com.yyb.session.MqttSessionCache;
import lombok.Getter;

/**
 * Created by chenws on 2019/10/8.
 */
@Getter
public class MqttProcess {

    private final MqttSessionCache mqttSessionCache;

    private final Connect connect;

    private final DisConnect disConnect;

    private final Publish publish;

    private final Subscribe subscribe;

    private final PubAck pubAck;

    private final PubComp pubComp;

    private final PubRec pubRec;

    private final PubRel pubRel;

    private final UnSubscribe unSubscribe;

    private final PingReq pingReq;

    private final ExecutorManager executorManager;

    public MqttProcess(Connect connect, MqttSessionCache mqttSessionCache, ExecutorManager executorManager, DisConnect disConnect, Publish publish, Subscribe subscribe, PubAck pubAck, PubComp pubComp, PubRec pubRec, PubRel pubRel, UnSubscribe unSubscribe, PingReq pingReq) {
        this.connect = connect;
        this.mqttSessionCache = mqttSessionCache;
        this.executorManager = executorManager;
        this.disConnect = disConnect;
        this.publish = publish;
        this.subscribe = subscribe;
        this.pubAck = pubAck;
        this.pubComp = pubComp;
        this.pubRec = pubRec;
        this.pubRel = pubRel;
        this.unSubscribe = unSubscribe;
        this.pingReq = pingReq;
    }
}
