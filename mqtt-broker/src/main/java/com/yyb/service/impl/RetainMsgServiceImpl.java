package com.yyb.service.impl;

import cn.hutool.core.util.StrUtil;
import com.yyb.bean.RetainMessageBO;
import com.yyb.service.RetainMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.yyb.constants.RedisConstant.RETAIN_MESSAGE;


/**
 * Created by chenws on 2019/10/11.
 */
@Service
public class RetainMsgServiceImpl implements RetainMsgService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void put(String topic, RetainMessageBO retainMessageStore) {
        redisTemplate.opsForValue().set(RETAIN_MESSAGE + topic, retainMessageStore);
    }

    @Override
    public RetainMessageBO get(String topic) {
        return (RetainMessageBO) redisTemplate.opsForValue().get(RETAIN_MESSAGE + topic);
    }

    @Override
    public void remove(String topic) {
        redisTemplate.delete(RETAIN_MESSAGE + topic);
    }

    @Override
    public boolean containsKey(String topic) {
        return redisTemplate.hasKey(RETAIN_MESSAGE + topic);
    }

    @Override
    public List<RetainMessageBO> search(String topicFilter) {
        List<RetainMessageBO> retainMessageStores = new ArrayList<>();
        if (!StrUtil.contains(topicFilter, '#') && !StrUtil.contains(topicFilter, '+')) {
            if (containsKey(topicFilter)) {
                retainMessageStores.add(get(topicFilter));
            }
        } else {
            Map<String, RetainMessageBO> map = new HashMap<>();
            Set<String> set = redisTemplate.keys(RETAIN_MESSAGE + "*");
            if (set != null) {
                set.forEach(
                        entry -> {
                            map.put(entry.substring(RETAIN_MESSAGE.length()), (RetainMessageBO) redisTemplate.opsForValue().get(entry));
                        }
                );
            }

            map.forEach((topic, retainMessageBO) -> {
                if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
                    List<String> splitTopics = StrUtil.split(topic, '/');
                    List<String> spliteTopicFilters = StrUtil.split(topicFilter, '/');
                    StringBuilder newTopicFilter = new StringBuilder();
                    for (int i = 0; i < spliteTopicFilters.size(); i++) {
                        String value = spliteTopicFilters.get(i);
                        if (value.equals("+")) {
                            newTopicFilter.append("+/");
                        } else if (value.equals("#")) {
                            newTopicFilter.append("#/");
                            break;
                        } else {
                            newTopicFilter.append(splitTopics.get(i)).append("/");
                        }
                    }
                    newTopicFilter = new StringBuilder(StrUtil.removeSuffix(newTopicFilter.toString(), "/"));
                    if (topicFilter.equals(newTopicFilter.toString())) {
                        retainMessageStores.add(retainMessageBO);
                    }
                }
            });
        }
        return retainMessageStores;
    }
}
