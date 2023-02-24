package com.qzk.upload.service;

import com.qzk.upload.dto.InitTaskDTO;
import com.qzk.upload.entity.UploadTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzk.upload.result.RestResult;

import java.util.Map;

/**
* @author qianzhikang
* @description 针对表【sys_upload_task(分片上传-分片任务记录)】的数据库操作Service
* @createDate 2023-02-23 10:44:43
*/
public interface UploadTaskService extends IService<UploadTask> {


    /**
     * 根据MD5唯一标识（identifier）获取分片文件上传任务记录
     * @param identifier MD5唯一标识
     * @return UploadTask
     */
    UploadTask getByIdentifier (String identifier);


    /**
     * 获取上传进度
     * @param identifier 文件md5唯一标识
     * @return RestResult
     */
    RestResult<Object> getTaskInfo(String identifier);

    /**
     * 获取文件地址
     * @param bucket        桶名称
     * @param objectKey     对象key
     * @return              String
     */
    String getPath (String bucket, String objectKey);

    /**
     * 初始化一个分片上传任务（不执行上传操作）
     * @param initTaskDTO    任务信息
     * @return               RestResult
     */
    RestResult<Object> initTask(InitTaskDTO initTaskDTO);

    /**
     * 生成文件上传的url
     * @param bucketName   桶名称
     * @param objectKey    对象key
     * @param params       分片参数和上传id
     * @return             RestResult
     */
    RestResult<Object> genPreSignUploadUrl(String bucketName, String objectKey, Map<String, String> params);

    /**
     * 合并文件
     * @param identifier   md5
     * @return RestResult
     */
    RestResult<Object> merge(String identifier);
}
