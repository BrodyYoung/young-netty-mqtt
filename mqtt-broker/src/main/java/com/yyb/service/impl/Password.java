package com.yyb.service.impl;

import com.yyb.service.IdentifyValidService;
import org.springframework.stereotype.Service;

/**
 * Created by yyb on 2022/10/31.
 */
@Service
public class Password implements IdentifyValidService {
    @Override
    public Boolean isValid(String userName, byte[] password, String clientIdentifier) {
        return true;
    }
}
