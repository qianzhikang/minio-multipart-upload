package com.qzk.upload.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzk.upload.dto.InitTaskDTO;
import com.qzk.upload.vo.TaskInfoVO;
import com.qzk.upload.dto.TaskRecordDTO;
import com.qzk.upload.entity.UploadTask;
import com.qzk.upload.minio.MinioProperties;
import com.qzk.upload.result.RestResult;
import com.qzk.upload.service.UploadTaskService;
import com.qzk.upload.mapper.UploadTaskMapper;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author qianzhikang
 * @description 针对表【sys_upload_task(分片上传-分片任务记录)】的数据库操作Service实现
 * @createDate 2023-02-23 10:44:43
 */
@Service
public class UploadTaskServiceImpl extends ServiceImpl<UploadTaskMapper, UploadTask>
        implements UploadTaskService {

    @Resource
    private UploadTaskMapper uploadTaskMapper;

    @Resource
    private MinioProperties minioProperties;

    @Resource
    private AmazonS3 amazonS3;


    /**
     * 上传url的有效时间
     */
    final Long PRE_SIGN_URL_EXPIRE = 60 * 10 * 1000L;


    /**
     * 根据MD5唯一标识（identifier）获取分片文件上传任务记录
     *
     * @param identifier MD5唯一标识
     * @return UploadTask
     */
    @Override
    public UploadTask getByIdentifier(String identifier) {
        return uploadTaskMapper.selectOne(new LambdaQueryWrapper<UploadTask>().eq(UploadTask::getFileIdentifier, identifier));
    }

    /**
     * 获取上传进度
     *
     * @param identifier 文件md5唯一标识
     * @return RestResult
     */
    @Override
    public RestResult<Object> getTaskInfo(String identifier) {
        // 获取任务上传记录
        UploadTask task = getByIdentifier(identifier);
        if (ObjectUtil.isNull(task)) {
            return new RestResult<>().success();
        }
        // 上传完毕
        TaskInfoVO result = new TaskInfoVO().setFinished(true).setTaskRecord(TaskRecordDTO.convertFromEntity(task)).setPath(getPath(task.getBucketName(), task.getObjectKey()));
        // 是否已经上传完毕，若未上传完毕,返回已上传的分片
        boolean doesObjectExist = amazonS3.doesObjectExist(task.getBucketName(), task.getObjectKey());
        if (!doesObjectExist) {
            // 未上传完，返回已上传的分片
            ListPartsRequest listPartsRequest = new ListPartsRequest(task.getBucketName(), task.getObjectKey(), task.getUploadId());
            PartListing partListing = amazonS3.listParts(listPartsRequest);
            result.setFinished(false).getTaskRecord().setExitPartList(partListing.getParts());
        }
        return new RestResult<>().success(result);
    }

    /**
     * 获取文件地址
     *
     * @param bucket    桶名称
     * @param objectKey 对象key
     * @return String
     */
    @Override
    public String getPath(String bucket, String objectKey) {
        return StrUtil.format("{}/{}/{}", minioProperties.getEndpoint(), bucket, objectKey);
    }

    /**
     * 创建一个分片上传任务，但是不执行上传，上传任务由前端执行（后端生成上传请求的url）。
     *
     * @param initTaskDTO 任务信息
     * @return RestResult
     */
    @Override
    public RestResult<Object> initTask(InitTaskDTO initTaskDTO) {
        // 当前日期
        Date currentDate = new Date();
        // 获取存储桶bucket名称
        String bucketName = minioProperties.getBucket();
        // 获取文件名称
        String fileName = initTaskDTO.getFileName();
        // 获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        // 设置分片文件key
        String key = StrUtil.format("{}/{}.{}", DateUtil.format(currentDate, "YYYY-MM-dd"), IdUtil.randomUUID(), suffix);
        // 推断文件类型
        String contentType = MediaTypeFactory.getMediaType(key).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        // 创建分片文件元对象，设置其文件类型
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        InitiateMultipartUploadResult initiateMultipartUploadResult = amazonS3
                .initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, key).withObjectMetadata(objectMetadata));
        // 获取上传id
        String uploadId = initiateMultipartUploadResult.getUploadId();
        // 创建任务对象
        UploadTask task = new UploadTask();
        // 计算分片数
        int chunkNum = (int) Math.ceil(initTaskDTO.getTotalSize() * 1.0 / initTaskDTO.getChunkSize());
        // 初始化数据库记录
        task.setBucketName(minioProperties.getBucket())
                .setChunkNum(chunkNum)
                .setChunkSize(initTaskDTO.getChunkSize())
                .setTotalSize(initTaskDTO.getTotalSize())
                .setFileIdentifier(initTaskDTO.getIdentifier())
                .setFileName(fileName)
                .setObjectKey(key)
                .setUploadId(uploadId);
        // 入库
        uploadTaskMapper.insert(task);
        // 返回数据
        TaskInfoVO taskInfoVO = new TaskInfoVO().setFinished(false).setTaskRecord(TaskRecordDTO.convertFromEntity(task)).setPath(getPath(bucketName, key));
        return new RestResult<>().success(taskInfoVO);
    }

    /**
     * 生成文件上传的url
     *
     * @param bucketName 桶名称
     * @param objectKey  对象key
     * @param params     分片参数和上传id
     * @return RestResult
     */
    @Override
    public RestResult<Object> genPreSignUploadUrl(String bucketName, String objectKey, Map<String, String> params) {
        // 获取当前时间
        Date currentDate = new Date();
        // 获取过期时间
        Date expireDate = DateUtil.offsetMillisecond(currentDate, PRE_SIGN_URL_EXPIRE.intValue());
        // 设置生成上传url配置：上传的桶名、对象key、过期时间、请求方式
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey)
                .withExpiration(expireDate).withMethod(HttpMethod.PUT);
        // 添加上传请求参数，当前分片数和上传id（上文中创建的上传任务对象中获取的uploadId）
        if (params != null) {
            params.forEach((key, val) -> request.addRequestParameter(key, val));
        }
        // 生成上传的url
        URL preSignedUrl = amazonS3.generatePresignedUrl(request);
        // 返回
        return new RestResult<>().success(preSignedUrl);
    }

    /**
     * 合并文件
     *
     * @param identifier md5
     * @return RestResult
     */
    @Override
    public RestResult<Object> merge(String identifier) {
        // 获取上传任务记录
        UploadTask task = getByIdentifier(identifier);
        if (task == null) {
            throw new RuntimeException("分片任务不存");
        }
        // 获取上传完毕的分片文件list
        ListPartsRequest listPartsRequest = new ListPartsRequest(task.getBucketName(), task.getObjectKey(), task.getUploadId());
        PartListing partListing = amazonS3.listParts(listPartsRequest);
        List<PartSummary> parts = partListing.getParts();
        if (!task.getChunkNum().equals(parts.size())) {
            // 已上传分块数量与记录中的数量不对应，不能合并分块
            throw new RuntimeException("分片缺失，请重新上传");
        }
        // 使用MinioAPI合并分片文件
        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest()
                .withUploadId(task.getUploadId())
                .withKey(task.getObjectKey())
                .withBucketName(task.getBucketName())
                .withPartETags(parts.stream().map(partSummary -> new PartETag(partSummary.getPartNumber(), partSummary.getETag())).collect(Collectors.toList()));
        CompleteMultipartUploadResult result = amazonS3.completeMultipartUpload(completeMultipartUploadRequest);
        return new RestResult<>().success("上传成功！",getPath(result.getBucketName(),result.getKey()));
    }
}




