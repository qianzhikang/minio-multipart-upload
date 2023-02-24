package com.qzk.upload.dto;

import cn.hutool.core.bean.BeanUtil;
import com.amazonaws.services.s3.model.PartSummary;
import com.qzk.upload.entity.UploadTask;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * @Description 上传任务记录dto
 * @Date 2023-02-23-11-01
 * @Author qianzhikang
 */
@Data
@Accessors(chain = true) // 允许链式调用
public class TaskRecordDTO extends UploadTask{

    /**
     * 已上传完的分片
     */
    private List<PartSummary> exitPartList;

    public static TaskRecordDTO convertFromEntity (UploadTask task) {
        TaskRecordDTO dto = new TaskRecordDTO();
        BeanUtil.copyProperties(task, dto);
        return dto;
    }
}
