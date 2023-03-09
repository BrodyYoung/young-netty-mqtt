package com.yyb.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yyb on 2022/10/9.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DupPubRelMessageBO{

    private String clientId;

    private int messageId;

}
