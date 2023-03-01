package com.yyb.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenws on 2019/10/9.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DupPublishMessageBO{

    private String clientId;

    private String topicFilter;

    private Integer mqttQoS;

    private Integer messageId;

    private byte[] messageBytes;

}
