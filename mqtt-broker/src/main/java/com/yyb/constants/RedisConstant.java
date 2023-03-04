package com.yyb.constants;

/**
 * Created by yyb on 2022/10/10.
 */
public interface RedisConstant {
    /**
     * 订阅主题带有匹配符
     */
    String WILDCARD_TOPIC = "Aurora:subscribe:wildcard:";

    /**
     * 订阅主题不带有匹配符
     */
    String NOT_WILDCARD_TOPIC = "Aurora:subscribe:notWildcard:";

    /**
     * 客户端的主题集合
     */
    String CLIENT_TOPIC = "Aurora:subscribe:clientTopic:";

    /**
     * broker重发消息存储,publish
     */
    String DUP_PUBLISH_MESSAGE = "Aurora:publish:dup:";

    /**
     * broker重发消息存储,pubrel
     */
    String DUP_PUBREL_MESSAGE = "Aurora:pubrel:dup:";

    /**
     * retain保留信息
     */
    String RETAIN_MESSAGE = "Aurora:retain:";


}
