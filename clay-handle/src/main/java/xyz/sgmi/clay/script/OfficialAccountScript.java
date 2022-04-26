package xyz.sgmi.clay.script;

import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;

import java.util.List;

/**
 * @Author: MSG
 * @Date:
 * @Version 1.0
 */

public interface OfficialAccountScript {

    /**
     * 发送模板消息
     *
     * @param wxMpTemplateMessages 模板消息列表
     * @return
     * @throws Exception
     */
    List<String> send(List<WxMpTemplateMessage> wxMpTemplateMessages) throws Exception;

}