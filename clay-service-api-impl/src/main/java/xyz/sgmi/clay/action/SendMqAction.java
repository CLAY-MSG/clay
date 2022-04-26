package xyz.sgmi.clay.action;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import xyz.sgmi.clay.domain.SendTaskModel;
import xyz.sgmi.clay.enums.RespStatusEnum;
import xyz.sgmi.clay.pipeline.BusinessProcess;
import xyz.sgmi.clay.pipeline.ProcessContext;
import xyz.sgmi.clay.utils.KafkaUtils;
import xyz.sgmi.clay.vo.BasicResultVO;

/**
 * @Author: MSG
 * @Date:
 * @Version 1.0
 * 将消息发送到MQ
 */
@Slf4j
public class SendMqAction implements BusinessProcess {

    @Autowired
    private KafkaUtils kafkaUtils;

    @Value("${austin.business.topic.name}")
    private String topicName;

    @Override
    public void process(ProcessContext context) {
        SendTaskModel sendTaskModel = (SendTaskModel) context.getProcessModel();
        String message = JSON.toJSONString(sendTaskModel.getTaskInfo(), new SerializerFeature[]{SerializerFeature.WriteClassName});

        try {
            kafkaUtils.send(topicName, message);
        } catch (Exception e) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR));
            log.error("send kafka fail! e:{},params:{}", Throwables.getStackTraceAsString(e)
                    , JSON.toJSONString(CollUtil.getFirst(sendTaskModel.getTaskInfo().listIterator())));
        }
    }
}
