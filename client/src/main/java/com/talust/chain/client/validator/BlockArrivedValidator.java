package com.talust.chain.client.validator;

import com.talust.chain.block.SynBlock;
import com.talust.chain.block.model.Block;
import com.talust.chain.block.model.BlockHead;
import com.talust.chain.block.model.TranType;
import com.talust.chain.block.model.Transaction;
import com.talust.chain.common.model.Message;
import com.talust.chain.common.model.MessageChannel;
import com.talust.chain.common.tools.SerializationUtil;
import com.talust.chain.network.MessageValidator;
import com.talust.chain.network.netty.queue.MessageQueueHolder;
import com.talust.chain.storage.BlockStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 块数据接收到
 */
@Slf4j
public class BlockArrivedValidator implements MessageValidator {
    private BlockStorage storageService = BlockStorage.get();
    private TransactionValidator transactionValidator = new TransactionValidator();

    @Override
    public boolean check(MessageChannel messageChannel) {
        boolean result = false;
        Block block = SerializationUtil.deserializer(messageChannel.getMessage().getContent(), Block.class);
        BlockHead head = block.getHead();
        int height = head.getHeight();
        boolean checkRepeat = MessageQueueHolder.get().checkRepeat(("block_height:" + height).getBytes());
        if (checkRepeat) {//说明本节点接收到过同样的消息,则直接将该消息扔掉
            return false;
        }
        if (height > 1) {
            byte[] prevBlock = head.getPrevBlock();//前一区块hash
            byte[] preBlockBytes = storageService.get(prevBlock);
            if (preBlockBytes != null) {
                Block preBlock = SerializationUtil.deserializer(preBlockBytes, Block.class);
                int preHeight = preBlock.getHead().getHeight();
                int nowHeight = block.getHead().getHeight();
                if ((nowHeight - preHeight) == 1) {
                    result = true;
                }
            } else {
                log.info("本区块同步有问题,未存储所接收到的区块的前一区块的数据,应该启用同步区块程序,当前区块高度:{}", height);
                SynBlock.get().startSynBlock();
            }
        } else {
            result = true;
        }
        if (result) {//继续校验区块里面的每一条数据
            List<byte[]> data = block.getBody().getData();
            for (byte[] datum : data) {
                MessageChannel nm = new MessageChannel();
                Message msg = SerializationUtil.deserializer(datum, Message.class);
                nm.setMessage(msg);
                boolean check = transactionValidator.check(nm);
                if (!check) {
                    log.info("区块中的交易信息校验失败...");
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
}
