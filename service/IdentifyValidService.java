package com.yyb.service;

/**
 * Created by chenws on 2019/10/31.
 */
public interface IdentifyValidService {

    Boolean isValid(String userName, byte[] password, String clientIdentifier);

}
