package xyz.sgmi.clay.pending;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import xyz.sgmi.clay.handle.HandlerHolder;
import xyz.sgmi.clay.pojo.TaskInfo;
import xyz.sgmi.clay.service.deduplication.service.DeduplicationRuleService;
import xyz.sgmi.clay.service.discard.DiscardMessageService;

/**
 *  Task 执行器
 * 0.丢弃消息
 * 1.通用去重功能
 * 2.发送消息
 * @Author: MSG
 * @Date:
 * @Version 1.0
 */
@Data
@Accessors(chain = true)
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Task implements Runnable {

    @Autowired
    private HandlerHolder handlerHolder;

    @Autowired
    private DeduplicationRuleService deduplicationRuleService;

    @Autowired
    private DiscardMessageService discardMessageService;

    private TaskInfo taskInfo;

    @Override
    public void run() {

        // 0. 丢弃消息
        if (discardMessageService.isDiscard(taskInfo)) {

            return;
        }

        // 1.平台通用去重
        deduplicationRuleService.duplication(taskInfo);



        // 2. 真正发送消息
        if (CollUtil.isNotEmpty(taskInfo.getReceiver())) {
            handlerHolder.route(taskInfo.getSendChannel())
                    .doHandler(taskInfo);
        }

    }
}