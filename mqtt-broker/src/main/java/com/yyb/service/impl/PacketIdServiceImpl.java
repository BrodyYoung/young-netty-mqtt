package com.yyb.service.impl;

import com.yyb.service.PacketIdService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by chenws on 2019/10/21.
 */
@Service
public class PacketIdServiceImpl implements PacketIdService {

    private static final ConcurrentLinkedQueue<Integer> CONCURRENT_LINKED_QUEUE = new ConcurrentLinkedQueue<>(Stream.iterate(1, item -> item + 1).limit(65535).collect(Collectors.toList()));

    @Override
    public Integer getPacketId() {
        return CONCURRENT_LINKED_QUEUE.poll();
    }

    @Override
    public void addPacketId(Integer packetId) {
        CONCURRENT_LINKED_QUEUE.add(packetId);
    }
}
