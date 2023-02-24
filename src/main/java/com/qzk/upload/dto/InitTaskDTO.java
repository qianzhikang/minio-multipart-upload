package com.qzk.upload.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Description 分片任务的DTO
 * @Date 2023-02-23-12-37
 * @Author qianzhikang
 */
@Data
public class InitTaskDTO {
    /**
     * 文件唯一标识(MD5)
     */
    @NotBlank(message = "文件标识不能为空")
    private String identifier;

    /**
     * 文件大小（byte）
     */
    @NotNull(message = "文件大小不能为空")
    private Long totalSize;

    /**
     * 分片大小（byte）
     */
    @NotNull(message = "分片大小不能为空")
    private Long chunkSize;

    /**
     * 文件名称
     */
    @NotBlank(message = "文件名称不能为空")
    private String fileName;
}
