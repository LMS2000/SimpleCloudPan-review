package com.lms.cloudpan.service.impl;


import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lms.cloudpan.client.OssClient;
import com.lms.cloudpan.config.OssProperties;
import com.lms.cloudpan.constants.FileConstants;
import com.lms.cloudpan.constants.ShareConstants;
import com.lms.cloudpan.entity.dao.File;
import com.lms.cloudpan.entity.dao.Folder;
import com.lms.cloudpan.entity.dao.UploadLog;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.FileDto;
import com.lms.cloudpan.entity.vo.DownloadFileVo;
import com.lms.cloudpan.entity.vo.FileVo;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.mapper.FileMapper;
import com.lms.cloudpan.mapper.FolderMapper;
import com.lms.cloudpan.mapper.UserMapper;
import com.lms.cloudpan.service.IFileService;
import com.lms.cloudpan.utis.FileSafeUploadUtil;
import com.lms.cloudpan.utis.FileUtil;
import com.lms.cloudpan.utis.ShareUtil;
import com.lms.contants.HttpCode;
import com.lms.redis.RedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xmlunit.builder.Input;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {



    private final FileMapper fileMapper;



    private final FolderMapper folderMapper;


    private final OssProperties ossProperties;

    private final RedisCache redisCache;


    private final UserMapper userMapper;



    //上传文件不能大于1G
    private static final Long MAX_FILE_SIZE = 1024L * 1024L * 1024L;

    private static final String STATIC_REQUEST_PREFIX = "static";

    /**
     * 获取当前路径下的当前用户的文件列表
     * @param path
     * @param uid
     * @return
     */
    @Override
    public List<FileDto> getUserFileByPath(String path, Integer uid) {
        //先根据path 和uid查找folder_id
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        //一般folder_name 和uid获取的都是唯一的一条记录
        Folder folder = folderMapper.selectOne(new QueryWrapper<Folder>().eq("user_id", uid)
                .eq("folder_name", path).eq("delete_flag",0));

        if (folder == null) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "想要获取的文件夹不存在");
        }
        //根据folder_id 和uid 得到满足条件的文件列表
        List<File> files = fileMapper.selectList(new QueryWrapper<File>().eq("folder_id", folder.getFolderId())
                .eq("user_id", uid).eq("delete_flag",0));

        //mapstruct映射转换
        List<FileDto> result = new ArrayList<>();
        files.forEach(file -> {
            FileDto fileDto = new FileDto();
            BeanUtils.copyProperties(file, fileDto);
            result.add(fileDto);
        });
        return result;
    }

    /**
     * 上传文件:
     * 1.先判断保存的路径是否存在，然后判断文件是否合法（包括文件大小，是否超过用户的配额）
     * 2.生成异步任务id，启动异步任务然后返回id
     * 异步任务:
     * 1.根据上传问文件名生成唯一的路径，记录文件日志，然后上传文件
     * 2.然后记录文件信息，更新用户配额信息
     * 3.如果过程中发生异常则回滚，并且根据文件日志删除文件删除文件日志
     * @param fileVo 包含multipartfile 和文件上传路径
     * @param uid
     * @return
     */
    @Override
    public String insertFile(FileVo fileVo, Integer uid,String fingerPrint) {
        User user = userMapper.selectById(uid);
        //先校验文件的路径是否存在
        Folder folder = validPath(fileVo.getFolderPath(), uid);

        //
        MultipartFile file = fileVo.getFile();

        validFile(file, user);


        //下面这段改为异步

        //创建taskId

        String taskId = UUID.randomUUID().toString();

        //设置上传状态，
        FileVo.changeUploadState(redisCache,taskId, FileConstants.FILE_UPLOAD_RUNNING);

        //开启异步任务
        CompletableFuture.runAsync(()-> {
                    try {
                        SpringUtil.getBean(FileServiceImpl.class).uoloadFileAsync(file.getInputStream(),file,user,folder,taskId,fingerPrint);
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                SpringUtil.getBean("asyncExecutor", Executor.class));
        return taskId;
    }


    /**
     *
     * @param inputStream 上传文件数据流
     * @param user 用户信息
     * @param folder 文件夹信息
     * @param taskId 任务id
     */
    @Transactional
    public  void uoloadFileAsync(InputStream inputStream , MultipartFile file, User user, Folder folder, String taskId,String fingerPrint){
        Integer uid = user.getUserId();
        String bucketName = "bucket_user_" + uid;
        String fileName = file.getOriginalFilename() == null ? file.getName() : file.getOriginalFilename();

       //校验redis上是否有这个MD5，是否系统已经存储了相同的文件（秒传功能）
        String md5FileUrl = FileSafeUploadUtil.checkMd5String(fingerPrint);

        //如果文件储存在系统上，就直接提交
        if(md5FileUrl!=null){
            log.info("命中MD5文件缓存");
            //获取文件后缀
            String fileSuffix = cn.hutool.core.io.FileUtil.getSuffix(file.getOriginalFilename());
            //保存文件信息
            this.save(File.builder().fileName(fileName).fileUrl(md5FileUrl).size(file.getSize())
                    .fileType(fileSuffix).folderId(folder.getFolderId()).userId(uid).build());
            //设置异步任务执行成功
            FileVo.changeUploadState(redisCache,taskId,FileConstants.FILE_UPLOAD_SUCCESS);
            return;

        }


        //这里的fileName其实是实际的存储路径
        String filePath;
        UploadLog uploadLog=null;
        try {
            String randomPath =
                    FileUtil.generatorFileName(fileName);
            String datePath = new DateTime().toString("yyyy/MM/dd");

            filePath = StringUtils.isEmpty(datePath) ? randomPath : datePath + "/" + randomPath;



//           //记录上传文件日志并上传文件
             uploadLog = FileSafeUploadUtil.doUpload(inputStream, bucketName, filePath);

            String fileUrl = FileUtil.getFileUrl(ossProperties.getEndpoint(), STATIC_REQUEST_PREFIX, bucketName, filePath);

            //开始记录文件
            File saveFile = new File();
            saveFile.setFileName(fileName);
            saveFile.setFileUrl(fileUrl);
            saveFile.setSize(file.getSize());
            saveFile.setFolderId(folder.getFolderId());
            saveFile.setUserId(uid);

            //获取文件类型
            String type =cn.hutool.core.io.FileUtil.getSuffix(file.getOriginalFilename());

            saveFile.setFileType(type);
            //记录文件信息
             this.save(saveFile);

            //修改用户的可用容量
            userMapper.updateById(User.builder()
                    .userId(uid).useQuota(file.getSize() + user.getUseQuota()).build());

            //修改文件夹容量
            folderMapper.updateById(Folder.builder()
                    .folderId(folder.getFolderId()).size(folder.getSize() + file.getSize()).build());
            //设置异步任务执行成功
            FileVo.changeUploadState(redisCache,taskId,FileConstants.FILE_UPLOAD_SUCCESS);

            //设置md5缓存
            FileSafeUploadUtil.setMd5String(fingerPrint,fileUrl);

        } catch (Exception e) {
            log.error("文件上传失败，{}",e);
            //文件回滚
            if(uploadLog!=null){
                FileSafeUploadUtil.deleteFile(uploadLog);
            }
        }
    }

    @Override
    public boolean renameFile(Integer id, String newName, Integer uid) {
        //先检查是否存在
        File file = fileMapper.selectOne(new QueryWrapper<File>().eq("file_id", id).eq("user_id", uid));
        if (file == null) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "文件不存在");
        }
        ShareUtil.isSharedResource(ShareConstants.FILE_TYPE,uid,file.getFileId());

        return fileMapper.updateById(File.builder().fileId(id).fileName(newName + "." + file.getFileType()).build()) > 0;
    }

    @Override
    public List<FileDto> searchFile(String fileName, Integer uid) {

        List<File> files = fileMapper
                .selectList(new QueryWrapper<File>().eq("user_id", uid).like("file_name", fileName));
        List<FileDto> result = new ArrayList<>();
        files.forEach(file -> {
            FileDto fileDto = new FileDto();
            BeanUtils.copyProperties(file, fileDto);
            result.add(fileDto);
        });
        return result;
    }

    /**
     * 删除多个文件(这里前端都是同一路径下的，所有文件都是一个路径下）
     *
     * @param ids
     * @param user
     * @return
     */
    @Override
    public boolean deleteFiles(List<Integer> ids, Integer uid) {
        User user = userMapper.selectById(uid);
        //先查找全部的文件
        List<File> filesByIds = fileMapper.selectList(null);

        Integer folderId = filesByIds.get(0).getFolderId();

        Folder folder = folderMapper
                .selectOne(new QueryWrapper<Folder>().eq("folder_id", folderId));
        AtomicLong filesSize = new AtomicLong();
        filesByIds.forEach(file -> {
            filesSize.addAndGet(file.getSize());
        });

        //标记删除
        fileMapper.update(File.builder().deleteFlag(1).build(),new QueryWrapper<File>()
                .in("file_id",ids));

        //修改文件夹大小
        folderMapper
                .updateById(Folder.builder().folderId(folderId).size(folder.getSize() - filesSize.get()).build());
        Long useQuota = user.getUseQuota();
      //删除后修改用户配额
     userMapper.updateById(User.builder().userId(uid).useQuota(useQuota - filesSize.get()).build());


        return fileMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 将多个文件移到指定文件夹，逻辑移动（本来还需修改文件夹大小，但是前端没实现就不做了）
     *
     * @param ids
     * @param path
     * @param uid
     * @return
     */
    @Override
    public Boolean moveFiles(List<Integer> ids, String path, Integer uid) {

        Folder folder = folderMapper.selectOne(new QueryWrapper<Folder>().eq("folderName", path).eq("delete_flag",0));
        if (folder == null) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "移动到的文件夹不存在");
        }
        //判断是否是分享资源
        ShareUtil.isSharedResource(ShareConstants.FILE_TYPE,uid,ids);
        //  修改文件信息
        return fileMapper.update(File.builder().folderId(folder.getFolderId()).build(), new QueryWrapper<File>()
                .in("file_id", ids)) > 0;

    }

    @Override
    public byte[] downloadFile(String url) {
        String[] split = url.split(STATIC_REQUEST_PREFIX);
        // http://localhost:8080/pan/static/bucket_user_4/root/test.png
        // http://localhost:8080/pan/static/temp/4/asdasaads.zip
        String realPath = ossProperties.getRootPath() + split[1];
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(Paths.get(realPath));
        } catch (IOException e) {
            throw new BusinessException(HttpCode.OPERATION_ERROR, "下载文件失败");
        }
        return bytes;
    }

    @Override
    public Boolean checkUpload(String taskId) {
        return FileVo.getUploadState(redisCache,taskId);
    }


    //校验文件是否合法
    private void validFile(MultipartFile file, User user) {
        long size = file.getSize();
//        String fileSuffix = cn.hutool.core.io.FileUtil.getSuffix(file.getOriginalFilename());
        if (size > MAX_FILE_SIZE) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "文件大小不能超过 1G");
        }
        //判断用户上传这个文件的时候会不会满配额
        Long quota = user.getQuota();
        Long useQuota = user.getUseQuota();
        long afterQuota = useQuota + file.getSize();
        if (afterQuota >= quota) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "已超过用户当前配额");
        }
    }

    private Folder validPath(String path, Integer uid) {
        //如果path是一个多级文件夹，获取最后一级的文件夹
        //如，/root/demo/hello
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        Folder folder = folderMapper.selectOne(new QueryWrapper<Folder>().eq("user_id", uid)
                .eq("folder_name", path).eq("delete_flag",0));
        if (folder == null) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "要保存到的路径不存在");
        }
        ShareUtil.isSharedResource(ShareConstants.FOLDER_TYPE, uid, folder.getFolderId());
        return folder;
    }
}
