package com.yyb.service;

import com.yyb.bean.DupPubRelMessageBO;

import java.util.List;

/**
 * Created by chenws on 2019/10/10.
 */
public interface DupPubRelMsgService {

    /**
     * pubrel重发存储
     * @param clientId
     * @param dupPubRelMessageStore
     */
    void put(String clientId, DupPubRelMessageBO dupPubRelMessageStore);

    /**
     * 获取
     * @param clientId
     * @return
     */
    List<DupPubRelMessageBO> get(String clientId);

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
