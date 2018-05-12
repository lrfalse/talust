package com.talust.chain.client.handler;

import com.talust.chain.common.model.MessageChannel;
import com.talust.chain.network.MessageHandler;
import com.talust.chain.network.netty.SynRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j//请求当前网络节点的返回
public class NetNodesRespHandler implements MessageHandler {
    @Override
    public boolean handle(MessageChannel message) {
        log.info("远端ip:{} 返回本节点请求所有网络节点消息...", message.getFromIp());
        SynRequest.get().synResp(message);
        return true;
    }

}
