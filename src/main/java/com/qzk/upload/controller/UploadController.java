package com.qzk.upload.controller;

import com.qzk.upload.dto.InitTaskDTO;
import com.qzk.upload.entity.UploadTask;
import com.qzk.upload.result.RestResult;
import com.qzk.upload.service.UploadTaskService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 文件上传
 * @Date 2023-02-23-10-46
 * @Author qianzhikang
 */
@RestController
@RequestMapping("/v1/minio/tasks")
public class UploadController {

    @Resource
    private UploadTaskService uploadTaskService;

    /**
     * 获取上传进度
     * @param identifier 文件md5
     * @return
     */
    @GetMapping("/{identifier}")
    public RestResult<Object> taskInfo (@PathVariable("identifier") String identifier) {
        return uploadTaskService.getTaskInfo(identifier);
    }

    /**
     * 创建一个上传任务(并不执行上传)
     * @return
     */
    @PostMapping
    public RestResult<Object> initTask (@Valid @RequestBody InitTaskDTO initTaskDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new RestResult<>().error(bindingResult.getFieldError().getDefaultMessage());
        }
        return uploadTaskService.initTask(initTaskDTO);
    }


    /**
     * 获取每个分片的预签名上传地址
     * @param identifier 文件唯一md5
     * @param partNumber 当前分片数
     * @return  上传的URL
     */
    @GetMapping("/{identifier}/{partNumber}")
    public RestResult<Object> preSignUploadUrl (@PathVariable("identifier") String identifier, @PathVariable("partNumber") Integer partNumber) {
        // 获取任务记录
        UploadTask task = uploadTaskService.getByIdentifier(identifier);
        if (task == null) {
            return new RestResult<>().error("分片任务不存在");
        }
        Map<String, String> params = new HashMap<>();
        params.put("partNumber", partNumber.toString());
        params.put("uploadId", task.getUploadId());
        return uploadTaskService.genPreSignUploadUrl(task.getBucketName(), task.getObjectKey(), params);
    }


    /**
     * 合并分片
     * @param identifier
     * @return
     */
    @PostMapping("/merge/{identifier}")
    public RestResult<Object> merge (@PathVariable("identifier") String identifier) {
        return uploadTaskService.merge(identifier);
    }
}
