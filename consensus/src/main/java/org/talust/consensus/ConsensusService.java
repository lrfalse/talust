package org.talust.consensus;

import lombok.extern.slf4j.Slf4j;
import org.talust.common.model.SuperNode;
import org.talust.common.tools.CacheManager;
import org.talust.common.tools.Configure;
import org.talust.common.tools.DateUtil;
import org.talust.core.core.SynBlock;
import org.talust.core.network.MainNetworkParams;
import org.talust.core.server.NtpTimeService;
import org.talust.network.netty.ChannelContain;
import org.talust.network.netty.ConnectionManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j //共识服务
public class ConsensusService {
    private static ConsensusService instance = new ConsensusService();

    private ConsensusService() {
    }

    public static ConsensusService get() {
        return instance;
    }
    private Conference conference = Conference.get();
    private PackBlockTool packBlockTool = new PackBlockTool();
    private ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
    private AtomicBoolean genRunning = new AtomicBoolean(false);
    private int checkSecond;//检测区块是否正常的时长

    public void start() {
        boolean superNode = ConnectionManager.get().superNode;
        //如果当前节点是超级节点,则启动共识机制
        if (superNode) {
            log.info("当前节点是超级节点,启动参与共识....");
            checkSecond = Configure.BLOCK_GEN_TIME / 3;
            genBlock();
            SuperNode master = conference.reqNetMaster();
            if (master != null) {
                log.info("获取会议master ip:{}", master.getIp());
                if (master.getIp().equals(ConnectionManager.get().getSelfIp())) {
                    startGenBlock();
                }
            }
        }
    }

    private void genBlock() {
        long delay = 0;
        //当前最新区块生成时间
        long currentBlockTime = MainNetworkParams.get().getBestBlockHeader().getTime();
        if (currentBlockTime > 0) {
            long timeSecond = NtpTimeService.currentTimeSeconds();
            delay = Configure.BLOCK_GEN_TIME - timeSecond + currentBlockTime;
            if (delay < 0) {
                delay = 0;
            }
        }

        service.scheduleAtFixedRate(() -> {
            if (genRunning.get()) {
                log.info("打包ip:{}", ConnectionManager.get().masterIp);
                long packageTime =  NtpTimeService.currentTimeSeconds();
                packBlockTool.pack(packageTime);//打包
            }
        }, delay, Configure.BLOCK_GEN_TIME, TimeUnit.SECONDS);
        log.info("启动定时任务生成区块,延时:{}...", delay);


        new Thread(() -> {
            //TODO 区块同步完成后再进行检测,并且验证是否有连接存在
            while (true) {
                if(ChannelContain.get().getMyChannels().size()>0){
                    if(!SynBlock.get().getSyning().get()){
                        try {
                            TimeUnit.SECONDS.sleep(checkSecond);
                        } catch (InterruptedException e) {
                        }
                        try {
                            //检测master是否正常,通过块判断
                            int nowSecond = DateUtil.getTimeSecond();
                            long ct = MainNetworkParams.get().getBestBlockHeader().getTime();
                            if (ct > 0) {
                                if ((nowSecond - ct) >= (Configure.BLOCK_GEN_TIME + checkSecond)) {//未收到区块响应
                                    conference.changeMaster();
                                }
                            }
                        } catch (Throwable e) {
                            log.error("error:", e);
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 开始生成块
     */
    public void startGenBlock() {
        genRunning.set(true);
    }

    /**
     * 停止生成块
     */
    public void stopGenBlock() {
        genRunning.set(false);
    }




}
