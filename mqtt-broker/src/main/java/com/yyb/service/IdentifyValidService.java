package com.yyb.service;

/**
 * Created by yyb on 2022/10/31.
 */
public interface IdentifyValidService {

    Boolean isValid(String userName, byte[] password, String clientIdentifier);

}
