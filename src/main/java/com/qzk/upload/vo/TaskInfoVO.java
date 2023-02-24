package com.qzk.upload.vo;

import com.qzk.upload.dto.TaskRecordDTO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Description 上传任务信息dto
 * @Date 2023-02-23-11-01
 * @Author qianzhikang
 */
@Data
@Accessors(chain = true) // 允许链式调用
public class TaskInfoVO {
    /**
     * 是否完成上传（是否已经合并分片）
     */
    private Boolean finished;

    /**
     * 文件地址
     */
    private String path;

    /**
     * 上传记录
     */
    private TaskRecordDTO taskRecord;
}
