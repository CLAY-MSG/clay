package xyz.sgmi.clay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: MSG
 * @Date:
 * @Version 1.0
 * 短信内容模型
 *
 * 在前端填写的时候分开，但最后处理的时候会将url拼接在content上
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmsContentModel extends ContentModel {

    /**
     * 短信发送内容
     */
    private String content;

    /**
     * 短信发送链接
     */
    private String url;

}

