/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package com.yyb.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenws on 2019/10/11.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetainMessageBO {

	private String topic;

	private byte[] messageBytes;

	private int mqttQoS;

}
