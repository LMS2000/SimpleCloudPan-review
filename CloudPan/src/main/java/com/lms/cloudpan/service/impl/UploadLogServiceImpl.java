package com.lms.cloudpan.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lms.cloudpan.client.OssClient;
import com.lms.cloudpan.entity.dao.UploadLog;
import com.lms.cloudpan.entity.dto.UploadLogDto;
import com.lms.cloudpan.entity.vo.UploadLogVo;
import com.lms.cloudpan.mapper.UploadLogMapper;
import com.lms.cloudpan.service.IUploadLogService;
import com.lms.page.CustomPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.lms.cloudpan.entity.factory.UploadLogFactory.UPLOAD_LOG_CONVERTER;


@Service
@RequiredArgsConstructor
public class UploadLogServiceImpl extends ServiceImpl<UploadLogMapper, UploadLog> implements IUploadLogService {


    @Resource
    private OssClient ossClient;

    @Override
    public Integer saveCourserUploadLog(UploadLogVo uploadLogVo) {
        UploadLog courseruploadlog = UPLOAD_LOG_CONVERTER.toUploadLog(uploadLogVo);
        save(courseruploadlog);
        return courseruploadlog.getId();
    }

    @Override
    public UploadLogDto getCourserUploadLogById(Integer id) {
        UploadLog courseruploadlog = getById(id);
        return UPLOAD_LOG_CONVERTER.toUploadLogDto(courseruploadlog);
    }

//    @Override
//    public List<UploadLogDto> listUploadLog(CustomPage customPage) {
//        List<UploadLog> courseruploadlogList = CustomPage.getPageResult(customPage, new UploadLog(), this, null);
//        return UPLOAD_LOG_CONVERTER.toListUploadLogDto(courseruploadlogList);
//    }

    @Override
    public void delCourserUploadLogById(Integer id) {
        removeById(id);
    }

    @Override
    public void deleteUploadedFile(List<Integer> uploadFileLogRecordList) {
        if(uploadFileLogRecordList.isEmpty()){
            return;
        }
        List<UploadLog> uploadLogs = listByIds(uploadFileLogRecordList);
        try{
            for (UploadLog uploadLog : uploadLogs) {
                  ossClient.deleteObject(uploadLog.getBucketName(),uploadLog.getFileName());
//                UploadClient.deleteFile(courserUploadLog.getBucketName(),courserUploadLog.getFileName());getFileName
            }
        } catch (Exception e){
            log.error("文件上传失败,删除已经上传成功的课程失败时出现异常: ",e);
        }
        removeBatchByIds(uploadFileLogRecordList);
    }
}
