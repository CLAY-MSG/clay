package xyz.sgmi.clay.utils;

import xyz.sgmi.clay.enums.ChannelType;
import xyz.sgmi.clay.enums.MessageType;
import xyz.sgmi.clay.pojo.TaskInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * groupId 标识着每一个消费者组
 * @Author: MSG
 * @Date:
 * @Version 1.0
 */
public class GroupIdMappingUtils {

    /**
     * 获取所有的groupIds
     * (不同的渠道不同的消息类型拥有自己的groupId)
     */
    public static List<String> getAllGroupIds() {
        List<String> groupIds = new ArrayList<>();
        for (ChannelType channelType : ChannelType.values()) {
            for (MessageType messageType : MessageType.values()) {
                groupIds.add(channelType.getCodeEn() + "." + messageType.getCode_en());
            }
        }
        return groupIds;
    }


    /**
     * 根据TaskInfo获取当前消息的groupId
     * @param taskInfo
     * @return
     */
    public static String getGroupIdByTaskInfo(TaskInfo taskInfo) {
        String channelCodeEn = ChannelType.getEnumByCode(taskInfo.getSendChannel()).getCodeEn();
        String msgCodeEn = MessageType.getEnumByCode(taskInfo.getMsgType()).getCode_en();
        return channelCodeEn + "." + msgCodeEn;
    }
}
