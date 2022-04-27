package xyz.sgmi.clay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import xyz.sgmi.clay.constant.ClayConstant;
import xyz.sgmi.clay.dao.MessageTemplateDao;
import xyz.sgmi.clay.domain.MessageTemplate;
import xyz.sgmi.clay.entity.XxlJobInfo;
import xyz.sgmi.clay.enums.AuditStatus;
import xyz.sgmi.clay.enums.MessageStatus;
import xyz.sgmi.clay.enums.RespStatusEnum;
import xyz.sgmi.clay.enums.TemplateType;
import xyz.sgmi.clay.service.CronTaskService;
import xyz.sgmi.clay.service.MessageTemplateService;
import xyz.sgmi.clay.utils.XxlJobUtils;
import xyz.sgmi.clay.vo.BasicResultVO;
import xyz.sgmi.clay.vo.MessageTemplateParam;

import java.util.List;

/**
 * 消息模板管理 Service
 * @Author: MSG
 * @Date:
 * @Version 1.0
 */
@Service
public class MessageTemplateServiceImpl implements MessageTemplateService {
    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Autowired
    private CronTaskService cronTaskService;


    @Autowired
    private XxlJobUtils xxlJobUtils;

    @Override
    public List<MessageTemplate> queryList(MessageTemplateParam param) {
        PageRequest pageRequest = PageRequest.of(param.getPage() - 1, param.getPerPage());
        return messageTemplateDao.findAllByIsDeletedEquals(ClayConstant.FALSE, pageRequest);
    }

    @Override
    public Long count() {
        return messageTemplateDao.countByIsDeletedEquals(ClayConstant.FALSE);
    }

    @Override
    public MessageTemplate saveOrUpdate(MessageTemplate messageTemplate) {
        if (messageTemplate.getId() == null) {
            initStatus(messageTemplate);
        } else {
            resetStatus(messageTemplate);
        }

        messageTemplate.setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
        return messageTemplateDao.save(messageTemplate);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        Iterable<MessageTemplate> messageTemplates = messageTemplateDao.findAllById(ids);
        messageTemplates.forEach(messageTemplate -> messageTemplate.setIsDeleted(ClayConstant.TRUE));
        for (MessageTemplate messageTemplate : messageTemplates) {
            if (messageTemplate.getCronTaskId()!=null && messageTemplate.getCronTaskId() > 0) {
                cronTaskService.deleteCronTask(messageTemplate.getCronTaskId());
            }
        }

        messageTemplateDao.saveAll(messageTemplates);
    }

    @Override
    public MessageTemplate queryById(Long id) {
        return messageTemplateDao.findById(id).get();
    }

    @Override
    public void copy(Long id) {
        MessageTemplate messageTemplate = messageTemplateDao.findById(id).get();
        MessageTemplate clone = ObjectUtil.clone(messageTemplate).setId(null).setCronTaskId(null);
        messageTemplateDao.save(clone);
    }
    @Override
    public BasicResultVO startCronTask(Long id) {
        // 1.修改模板状态
        MessageTemplate messageTemplate = messageTemplateDao.findById(id).get();

        // 2.动态创建或更新定时任务
        XxlJobInfo xxlJobInfo = xxlJobUtils.buildXxlJobInfo(messageTemplate);

        // 3.获取taskId(如果本身存在则复用原有任务，如果不存在则得到新建后任务ID)
        Integer taskId = messageTemplate.getCronTaskId();
        BasicResultVO basicResultVO = cronTaskService.saveCronTask(xxlJobInfo);
        if (taskId == null && RespStatusEnum.SUCCESS.getCode().equals(basicResultVO.getStatus()) && basicResultVO.getData() != null) {
            taskId = Integer.valueOf(String.valueOf(basicResultVO.getData()));
        }

        // 4. 启动定时任务
        if (taskId != null) {
            cronTaskService.startCronTask(taskId);
            MessageTemplate clone = ObjectUtil.clone(messageTemplate).setMsgStatus(MessageStatus.RUN.getCode()).setCronTaskId(taskId).setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
            messageTemplateDao.save(clone);
            return BasicResultVO.success();
        }
        return BasicResultVO.fail();
    }

    @Override
    public BasicResultVO stopCronTask(Long id) {
        // 1.修改模板状态
        MessageTemplate messageTemplate = messageTemplateDao.findById(id).get();
        MessageTemplate clone = ObjectUtil.clone(messageTemplate).setMsgStatus(MessageStatus.STOP.getCode()).setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
        messageTemplateDao.save(clone);

        // 2.暂停定时任务
        return cronTaskService.stopCronTask(clone.getCronTaskId());
    }

    /**
     * 初始化状态信息
     * TODO 创建者 修改者 团队
     *
     * @param messageTemplate
     */
    private void initStatus(MessageTemplate messageTemplate) {
        messageTemplate.setFlowId(StrUtil.EMPTY)
                .setMsgStatus(MessageStatus.INIT.getCode()).setAuditStatus(AuditStatus.WAIT_AUDIT.getCode())
                .setCreator("sgmi").setUpdator("sgmi").setTeam("公众号sgmi").setAuditor("3y")
                .setCreated(Math.toIntExact(DateUtil.currentSeconds()))
                .setIsDeleted(ClayConstant.FALSE);
    }

    /**
     * 1. 重置模板的状态
     * 2. 修改定时任务信息
     *
     * @param messageTemplate
     */
    private void resetStatus(MessageTemplate messageTemplate) {
        messageTemplate.setUpdator(messageTemplate.getUpdator())
                .setMsgStatus(MessageStatus.INIT.getCode()).setAuditStatus(AuditStatus.WAIT_AUDIT.getCode());

        if (messageTemplate.getCronTaskId() != null && TemplateType.CLOCKING.getCode().equals(messageTemplate.getTemplateType())) {
            XxlJobInfo xxlJobInfo = xxlJobUtils.buildXxlJobInfo(messageTemplate);
            cronTaskService.saveCronTask(xxlJobInfo);
            cronTaskService.stopCronTask(messageTemplate.getCronTaskId());
        }
    }
}
