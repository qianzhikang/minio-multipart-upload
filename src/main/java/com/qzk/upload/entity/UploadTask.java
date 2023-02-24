package com.qzk.upload.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 分片上传-分片任务记录
 * @TableName sys_upload_task
 */
@TableName(value ="sys_upload_task")
@Data
@Accessors(chain = true) // 允许链式调用
public class UploadTask implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 分片上传的uploadId
     */
    private String uploadId;

    /**
     * 文件唯一标识（md5）
     */
    private String fileIdentifier;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 所属桶名
     */
    private String bucketName;

    /**
     * 文件的key
     */
    private String objectKey;

    /**
     * 文件大小（byte）
     */
    private Long totalSize;

    /**
     * 每个分片大小（byte）
     */
    private Long chunkSize;

    /**
     * 分片数量
     */
    private Integer chunkNum;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}