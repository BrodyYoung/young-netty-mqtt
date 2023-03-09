package com.yyb.service;


import com.yyb.bean.DupPublishMessageBO;

import java.util.List;

/**
 * Created by yyb on 2022/10/10.
 */
public interface DupPublishMsgService {

    /**
     * 把重复推送消息存储
     * @param clientId
     * @param dupPublishMessageBO
     */
    void put(String clientId, DupPublishMessageBO dupPublishMessageBO);

    /**
     * 获取重发消息
     * @param clientId
     * @return
     */
    List<DupPublishMessageBO> get(String clientId);

    /**
     * 删除
     * @param clientId
     * @param messageId
     */
    void remove(String clientId, int messageId);

    /**
     * 删除
     * @param clientId
     */
    void removeByClient(String clientId);
}
