package com.lms.cloudpan.service.impl;


import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lms.cloudpan.config.OssProperties;
import com.lms.cloudpan.constants.CommonConstants;
import com.lms.cloudpan.constants.FileConstants;
import com.lms.cloudpan.constants.ShareConstants;
import com.lms.cloudpan.entity.dao.File;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.*;
import com.lms.cloudpan.entity.enums.*;
import com.lms.cloudpan.entity.vo.DownloadUrlVo;
import com.lms.cloudpan.entity.vo.FileVo;
import com.lms.cloudpan.entity.vo.UploadStatusVo;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.mapper.FileMapper;
import com.lms.cloudpan.mapper.UserMapper;
import com.lms.cloudpan.service.IFileService;
import com.lms.cloudpan.utils.*;
import com.lms.contants.HttpCode;
import com.lms.redis.RedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.lms.cloudpan.entity.factory.FileFactory.FILE_CONVERTER;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {


    private final OssProperties ossProperties;

    private final RedisCache redisCache;


    private final UserMapper userMapper;


    //上传文件不能大于1G
    private static final Long MAX_FILE_SIZE = 1024L * 1024L * 1024L;

    private static final String STATIC_REQUEST_PREFIX = "static";

    /**
     * 获取当前路径下的当前用户的文件列表
     *
     * @param pageFileDto 分页信息
     * @param uid         用户id
     * @return
     */
    @Override
    public Page<FileVo> getUserFileByPath(PageFileDto pageFileDto, Integer uid) {
        //  查看pid记录是否存在
        String pid = pageFileDto.getPid();
        Integer pageSize = pageFileDto.getPageSize();
        Integer pageNum = pageFileDto.getPageNum();
        if (pid != null) {
            BusinessException.throwIfNot(MybatisUtils.existCheck(this, Map.of("file_id", pid)) || pid.equals(CommonConstants.ZERO_STR));

        }

        Page<File> filePage = this.page(new Page<>(pageNum, pageSize),
                new QueryWrapper<File>().eq(!ObjectUtils.isEmpty(pid), "pid", pid)
                        .eq("user_id", uid).eq("delete_flag", FileConstants.USING)
                        .orderByAsc("update_time"));
        List<File> records = filePage.getRecords();
        List<FileVo> fileVos = FILE_CONVERTER.toListFileVo(records);
        Page<FileVo> fileVoPage = new Page<>(pageNum, pageSize, filePage.getTotal());
        fileVoPage.setRecords(fileVos);
        return fileVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadStatusVo insertFile(MultipartFile file, UploadFileDto uploadFileDto, Integer uid) {

        java.io.File tempFileFolder = null;
        Boolean uploadSuccess = true;
        Integer chunkIndex = uploadFileDto.getChunkIndex();
        Integer chunks = uploadFileDto.getChunks();
        String filePid = uploadFileDto.getFilePid();
        String fileMd5 = uploadFileDto.getFileMd5();
        String fileId = uploadFileDto.getFileId();
        String fileName = uploadFileDto.getFileName();
        BusinessException.throwIfNot(MybatisUtils.existCheck(this, Map.of("file_id", filePid)) || filePid.equals(CommonConstants.ZERO_STR));
        try {

            if (StringUtils.isEmpty(fileId)) {
                fileId = StringTools.getRandomNumber(CommonConstants.LENGTH_10);
            }
            User currentUser = userMapper.selectById(uid);


            //如果是第一次分片上传时先判断md5值，看看网盘中是否已经存在相同文件
            if (chunkIndex == 0) {

                List<File> md5List = this.list(new QueryWrapper<File>()
                        .eq("file_md5", fileMd5).eq("delete_flag", FileConstants.USING));
                if (!md5List.isEmpty()) {
                    File md5File = md5List.get(0);

                    //判断文件状态
                    BusinessException.throwIf(md5File.getSize() + currentUser.getUseQuota() > currentUser.getQuota(),
                            HttpCode.OPERATION_ERROR, "网盘空间不足，请扩容");
                    //插入文件信息
                    this.save(File.builder().fileMd5(fileMd5).fileId(fileId).filePath(md5File.getFilePath())
                            .deleteFlag(FileConstants.USING).userId(uid).pid(filePid).size(md5File.getSize())
                            .fileName(fileName).build());

                    //修改用户容量
                    updateUserSpace(currentUser, md5File.getSize());
                    return UploadStatusVo.builder().fileId(fileId).status(UploadStatusEnums.UPLOAD_SECONDS.getCode()).build();
                }
            }

            //暂存在临时目录
            String tempFolderName = ossProperties.getRootPath() + FileConstants.FILE_FOLDER_TEMP;
            String currentUserFolderName = currentUser.getUserId() + "_" + fileId;
            //创建临时目录
            tempFileFolder = new java.io.File(tempFolderName + currentUserFolderName);
            if (!tempFileFolder.exists()) {
                tempFileFolder.mkdirs();
            }
            Long currentTempSize = redisCache
                    .getCacheObject(FileConstants.REDIS_KEY_USER_FILE_TEMP_SIZE + uid + "_" + fileId);

            if(currentTempSize==null){
                currentTempSize=0L;
            }

            BusinessException.throwIf(file.getSize() + currentTempSize + currentUser.getUseQuota() > currentUser.getQuota(),
                    HttpCode.OPERATION_ERROR, "网盘空间不足，请扩容");

            java.io.File newFile = new java.io.File(tempFileFolder.getPath() + "/" + chunkIndex);
            file.transferTo(newFile);
            //保存临时大小
            redisCache.setCacheObject(FileConstants.REDIS_KEY_USER_FILE_TEMP_SIZE + uid + "_" + fileId,
                    file.getSize() + currentTempSize);
            //不是最后一个分片，直接返回
            if (chunkIndex < chunks - 1) {
                return UploadStatusVo.builder().fileId(fileId)
                        .status(UploadStatusEnums.UPLOADING.getCode()).build();
            }
            Date curDate = new Date();
            //最后一个分片上传完成，记录数据库，异步合并分片
            String month = DateUtil.format(curDate, DateTimePatternEnum.YYYYMM.getPattern());
            String fileSuffix = StringTools.getFileSuffix(fileName);
            //真实文件名
            String realFileName = currentUserFolderName + fileSuffix;
            FileTypeEnums fileTypeEnum = FileTypeEnums.getFileTypeBySuffix(fileSuffix);

            //自动重命名
            fileName = autoRename(filePid, currentUser.getUserId(), fileName);
            //保存文件信息
            this.save(File.builder().fileId(fileId).userId(uid).fileMd5(fileMd5).fileName(fileName)
                    .filePath(month + "/" + realFileName).pid(filePid).fileType(fileTypeEnum.getType())
                    .fileCategory(fileTypeEnum.getCategory().getCategory())
                    .deleteFlag(FileConstants.USING).fileStatus(FileStatusEnum.TRANSFER.getStatus()).build());
            //更新用户的使用容量
            Long totalSize = redisCache.getCacheObject(FileConstants.REDIS_KEY_USER_FILE_TEMP_SIZE + uid + "_" + fileId);
            updateUserSpace(currentUser, totalSize);

            //开始整合分片的文件
            //事务提交后调用异步方法
            String finalFileId = fileId;
            //开启异步任务
            CompletableFuture.runAsync(() -> {
                        SpringUtil.getBean(FileServiceImpl.class).unionFileAsync(finalFileId, currentUser,ossProperties.getRootPath(),totalSize);
                    },
                    SpringUtil.getBean("asyncExecutor", Executor.class));


            return UploadStatusVo.builder().fileId(fileId).status(UploadStatusEnums.UPLOAD_FINISH.getCode()).build();
        } catch (Exception e) {
            uploadSuccess = false;
            log.error("文件上传失败", e);
            throw new BusinessException(HttpCode.OPERATION_ERROR, "上传文件失败");
        } finally {
            //如果上传失败，清除临时目录
            if (tempFileFolder != null && !uploadSuccess) {
                try {
                    FileUtils.deleteDirectory(tempFileFolder);
                } catch (IOException e) {
                    log.error("删除临时目录失败");
                }
            }
        }
    }

    //异步合并文件
    private void unionFileAsync(String fileId, User user,String rootPath,Long size) {
        Boolean transferSuccess = true;
        String targetFilePath = null;
        String cover = null;
        FileTypeEnums fileTypeEnum = null;
        File fileInfo = this.getOne(new QueryWrapper<File>()
                .eq("file_id", fileId).eq("user_id", user.getUserId()));
        try {

            if (fileInfo == null || !FileStatusEnum.TRANSFER.getStatus().equals(fileInfo.getFileStatus())) {
                return;
            }
            //临时目录
            String tempFolderName = rootPath + FileConstants.FILE_FOLDER_TEMP;
            String currentUserFolderName = user.getUserId() + "_" + fileId;

            java.io.File fileFolder = new java.io.File(tempFolderName + currentUserFolderName);
            if (!fileFolder.exists()) {
                fileFolder.mkdirs();
            }
            //目标目录
            String targetFolderName = rootPath + FileConstants.FILE_FOLDER_PATH;
            String month = fileInfo.getFilePath().split("/")[0];
            String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());
            java.io.File targetFolder = new java.io.File(targetFolderName + "/" + month);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }

            //真实文件名
            String realFileName = currentUserFolderName + fileSuffix;
            //真实文件路径
            targetFilePath = targetFolder.getPath() + "/" + realFileName;
            //合并文件
            union(fileFolder.getPath(), targetFilePath, fileInfo.getFileName(), true);

            //   202307/3178033358WiedvfP0et.png

        } catch (Exception e) {
            log.error("文件转码失败，文件Id:{},userId:{}", fileId, user.getUserId(), e);
            transferSuccess = false;
        } finally {
            Integer status = transferSuccess ? FileStatusEnum.USING.getStatus() : FileStatusEnum.TRANSFER_FAIL.getStatus();
            this.updateById(File.builder().fileId(fileId)
                    .fileStatus(status).size(new java.io.File(targetFilePath).length()).build());
        }

    }

    //合并文件
    private void union(String dirPath, String toFilePath, String fileName, boolean delSource) {
        java.io.File dir = new java.io.File(dirPath);
        if (!dir.exists()) {
            throw new BusinessException(HttpCode.OPERATION_ERROR, "目录不存在");
        }
        java.io.File fileList[] = dir.listFiles();
        java.io.File targetFile = new java.io.File(toFilePath);
        RandomAccessFile writeFile = null;
        try {
            writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < fileList.length; i++) {
                int len = -1;
                //创建读块文件的对象
                java.io.File chunkFile = new java.io.File(dirPath + java.io.File.separator + i);
                RandomAccessFile readFile = null;
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并分片失败", e);
                    throw new BusinessException(HttpCode.OPERATION_ERROR, "合并文件失败");
                } finally {
                    readFile.close();
                }
            }
        } catch (Exception e) {
            log.error("合并文件:{}失败", fileName, e);
            throw new BusinessException(HttpCode.OPERATION_ERROR, "合并文件" + fileName + "出错了");
        } finally {
            try {
                if (null != writeFile) {
                    writeFile.close();
                }
            } catch (IOException e) {
                log.error("关闭流失败", e);
            }
            if (delSource) {
                if (dir.exists()) {
                    try {
                        FileUtils.deleteDirectory(dir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private String autoRename(String filePid, Integer userId, String fileName) {
        long count = this.count(new QueryWrapper<File>().eq("file_name", fileName)
                .eq("user_id", userId).eq("delete_flag", FileConstants.USING)
                .eq("pid", filePid));
        if (count > 0) {
            return StringTools.rename(fileName);
        }
        return fileName;
    }

    private void updateUserSpace(User currentUser, Long size) {
        Long curQuota = currentUser.getUseQuota() + size;
        userMapper.updateById(User.builder().userId(currentUser.getUserId()).useQuota(curQuota).build());

    }


    /**
     * 修改文件文件名
     *
     * @param id
     * @param newName
     * @param uid
     * @return
     */
    @Override
    public boolean renameFile(String id, String newName, Integer uid) {
        //先检查是否存在
        File file = this.getOne(new QueryWrapper<File>().eq("file_id", id).eq("user_id", uid)
                .eq("delete_flag", FileConstants.USING));
        BusinessException.throwIf(file == null);

        String[] split = newName.split(".");
        if (FileFolderTypeEnums.FILE.getType().equals(file.getFolderType())) {
            newName = newName + StringTools.getFileSuffix(file.getFileName());
        }
        BusinessException.throwIf(this.count(new QueryWrapper<File>()
                        .eq("pid", file.getPid()).eq("file_name", newName)) > 1,
                HttpCode.PARAMS_ERROR, "同路径下重复文件名");
        return this.updateById(File.builder().fileId(id).fileName(newName).build());
    }

//    @Override
//    public List<FileVo> searchFile(String fileName, Integer uid) {
//
//        List<File> files = fileMapper
//                .selectList(new QueryWrapper<File>().eq("user_id", uid).like("file_name", fileName));
//        List<FileVo> result = new ArrayList<>();
//        files.forEach(file -> {
//            FileVo fileVo = new FileVo();
//            BeanUtils.copyProperties(file, fileVo);
//            result.add(fileVo);
//        });
//        return result;
//    }

    /**
     * 删除多个文件(这里前端都是同一路径下的，所有文件都是一个路径下）
     * 标记文件为回收状态
     *
     * @param ids
     * @param ids 删除的id列表
     * @return
     */
    @Override
    public boolean deleteFiles(List<String> ids, Integer uid) {
        //删除同路径下的指定文件，如果有子目录也一并删除


        //获取子文件
        List<File> deleteFiles = new ArrayList<>();
        ids.forEach(id -> {
            List<File> subFile = FileUtil.getSubFile(id, FileConstants.USING);
            deleteFiles.addAll(subFile);
        });

        List<File> idsList = this.list(new QueryWrapper<File>().in("file_id", ids).eq("delete_flag", FileConstants.USING));
         deleteFiles.addAll(idsList);
//        AtomicLong filesSize = new AtomicLong();
//        deleteFiles.forEach(file -> {
//            filesSize.addAndGet(file.getSize());
//        });
        //将这些文件放置到回收站
        List<String> fileIds = deleteFiles.stream().map(File::getFileId).collect(Collectors.toList());
        return this.update(File.builder().deleteFlag(FileConstants.RECYCLE).build(), new QueryWrapper<File>()
                .in("file_id", fileIds));

    }

    //标记
    @Override
    public boolean deleteRecycleFiles(List<String> ids, Integer uid) {
        //删除同路径下的指定文件，如果有子目录也一并删除


        User user = userMapper.selectById(uid);


        //获取子文件
        List<File> deleteFiles = new ArrayList<>();
        ids.forEach(id -> {
            List<File> subFile = FileUtil.getSubFile(id, FileConstants.RECYCLE);
            deleteFiles.addAll(subFile);
        });
        AtomicLong filesSize = new AtomicLong();
        deleteFiles.forEach(file -> {
            filesSize.addAndGet(file.getSize());
        });
        List<File> idsFiles = this.list(new QueryWrapper<File>().in("file_id", ids));
        idsFiles.forEach(file->{
            filesSize.addAndGet(file.getSize());
        });
        //将这些文件标记删除
        List<String> fileIds = deleteFiles.stream().map(File::getFileId).collect(Collectors.toList());
        fileIds.addAll(ids);
        this.update(File.builder().deleteFlag(FileConstants.DELETED).build(), new QueryWrapper<File>()
                .in("file_id", fileIds));
        Long useQuota = user.getUseQuota();
        //修改用户的可用容量
        return userMapper.updateById(User.builder().userId(uid).useQuota(useQuota - filesSize.get()).build()) > 0;

    }

    /**
     * 将选中文件以及子文件变回正常使用状态
     *
     * @param ids
     * @param uid
     * @return
     */
    @Override
    public boolean recoverFileBatch(List<String> ids, Integer uid) {
        //删除同路径下的指定文件，如果有子目录也一并删除


        //获取子文件
        List<File> deleteFiles = new ArrayList<>();
        ids.forEach(id -> {
            List<File> subFile = FileUtil.getSubFile(id, FileConstants.RECYCLE);
            deleteFiles.addAll(subFile);
        });
//        AtomicLong filesSize = new AtomicLong();
//        deleteFiles.forEach(file -> {
//            filesSize.addAndGet(file.getSize());
//        });
        //将这些文件恢复正常
        List<String> fileIds = deleteFiles.stream().map(File::getFileId).collect(Collectors.toList());
        fileIds.addAll(ids);
        return this.update(File.builder().deleteFlag(FileConstants.USING).build(), new QueryWrapper<File>()
                .in("file_id", fileIds));

    }

    /**
     * 将多个文件移到指定文件夹，逻辑移动（本来还需修改文件夹大小，但是前端没实现就不做了）
     * <p>
     * ..
     * .* @param ids
     *
     * @param pid
     * @param uid
     * @return
     */
    @Override
    public Boolean moveFiles(List<String> ids, String pid, Integer uid) {

//        Folder folder = folderMapper.selectOne(new QueryWrapper<Folder>().eq("folderName", path).eq("delete_flag",0));
//        if (folder == null) {
//            throw new BusinessException(HttpCode.PARAMS_ERROR, "移动到的文件夹不存在");
//        }
        //查看pid是否合法

        BusinessException.throwIfNot(MybatisUtils
                .existCheck(this, Map.of("file_id", pid,
                        "delete_flag", FileConstants.USING, "folder_type", FileConstants.FOLDER_TYPE)));

        //  获取选中的文件列表

        if (ids.size() < 1) return false;
        List<File> checkFiles = this.list(new QueryWrapper<File>()
                .in("file_id", ids).eq("delete_flag", FileConstants.USING)
                .select("file_id", "file_name"));
        BusinessException.throwIf(ids.size() != checkFiles.size());

        //获取目标路径下的文件列表
        HashSet<File> subFiles = new HashSet<>(this.list(new QueryWrapper<File>().eq("pid", pid)
                .ne("delete_flag", FileConstants.DELETED)
                .select("file_id", "file_name")));

        //判断选中的文件在目标路径下有重名，有的话就修改选中文件的名字
        checkFiles.forEach(file -> {
            String fileName = file.getFileName();
            if (subFiles.contains(fileName)) {
                fileName = StringTools.rename(fileName);
                file.setFileName(fileName);
            }
            file.setPid(pid);
        });
        return this.updateBatchById(checkFiles);
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
        return FileDto.getUploadState(redisCache, taskId);
    }

    /**
     * 创建文件夹
     *
     * @param uid
     * @param pid
     * @param folderName
     * @return
     */
    @Override
    public Boolean createFolder(Integer uid, String pid, String folderName) {
        //判断路径是否存在
        BusinessException.throwIfNot(MybatisUtils.existCheck(this,
                Map.of("file_id", pid, "folder_type", FileConstants.FOLDER_TYPE,
                        "delete_flag", FileConstants.USING)) || pid.equals(CommonConstants.ZERO_STR));

        //判断路径下是否有同名文件夹
        BusinessException.throwIf(MybatisUtils.existCheck(this, Map.of("pid", pid, "file_name", folderName,
                "delete_flag", FileConstants.USING, "folder_type", FileConstants.FOLDER_TYPE)));
        String newFileId = StringTools.getRandomNumber(CommonConstants.LENGTH_10);
        return this.save(File.builder().fileId(newFileId).pid(pid).fileName(folderName)
                .userId(uid).folderType(FileConstants.FOLDER_TYPE).build());

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

//    private Folder validPath(String path, Integer uid) {
//        //如果path是一个多级文件夹，获取最后一级的文件夹
//        //如，/root/demo/hello
//        if (!path.startsWith("/")) {
//            path = "/" + path;
//        }
//
//        Folder folder = folderMapper.selectOne(new QueryWrapper<Folder>().eq("user_id", uid)
//                .eq("folder_name", path).eq("delete_flag", 0));
//        if (folder == null) {
//            throw new BusinessException(HttpCode.PARAMS_ERROR, "要保存到的路径不存在");
//        }
//        ShareUtil.isSharedResource(ShareConstants.FOLDER_TYPE, uid, folder.getFolderId());
//        return folder;
//    }


    public boolean download(HttpServletRequest request, HttpServletResponse response, String code) {
       String downloadUrl= FileConstants.DOWNLOAD_URL_HEADER+code;
        DownloadUrlVo downloadFileDto = redisCache.getCacheObject(downloadUrl);

        if (null == downloadFileDto) {
            return false;
        }
        String filePath = ossProperties.getRootPath() + FileConstants.FILE_FOLDER_PATH + downloadFileDto.getFilePath();
        String fileName = downloadFileDto.getFileName();
        response.setContentType("application/x-msdownload; charset=UTF-8");
        try {
            if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {//IE浏览器
                fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            } else {
                fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
            }
        } catch (Exception e) {
            throw new BusinessException(HttpCode.OPERATION_ERROR, "下载文件失败");
        }

        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        FileUtil.readFile(response, filePath);
        redisCache.deleteObject(downloadUrl);
        return true;
    }

    @Override
    public String createDownloadUrl(String fileId, Integer uid) {
        File file = this.getOne(new QueryWrapper<File>().eq("file_id", fileId).eq("user_id", uid)
                .eq("delete_flag", FileConstants.USING));

        BusinessException.throwIf(file == null);
        //TODO 暂时先不搞下载文件夹
        String code = StringTools.getRandomString(CommonConstants.LENGTH_50);
        DownloadUrlVo downloadUrlVo = DownloadUrlVo.builder().downloadCode(code).fileId(fileId)
                .filePath(file.getFilePath()).fileName(file.getFileName()).build();
        redisCache.setCacheObject(FileConstants.DOWNLOAD_URL_HEADER + code, downloadUrlVo);

        return code;
    }

    @Override
    public String getPreFiles(String fileId, Integer uid) {
        File fileInfo = this.getById(fileId);
        BusinessException.throwIf(fileInfo == null);
        return fileInfo.getPid();
    }

    /**
     *
     * @param pid  获取移动文件夹的路径父id
     * @param fileId  获取当前路径的父id
     * @param uid
     * @return
     */
    @Override
    public List<FileVo> getFolderInfo(String pid,String fileId,Integer uid) {
       //得到目录的信息
        List<File> list = this.list(new QueryWrapper<File>().eq("user_id", uid).eq("delete_flag", FileConstants.USING)
                .ne("file_id", fileId).eq("pid",pid).eq("folder_type",FileConstants.FOLDER_TYPE));
        return FILE_CONVERTER.toListFileVo(list);
    }

    @Override
    public Page<FileVo> pageRecycleFiles(RecyclePageDto recyclePageDto, Integer uid) {

        Integer pageSize = recyclePageDto.getPageSize();
        Integer pageNum = recyclePageDto.getPageNum();
        String fileName = recyclePageDto.getFileName();
        Integer folderType = recyclePageDto.getFolderType();

        Page<File> filePage = this.page(new Page<>(pageNum, pageSize), new QueryWrapper<File>().eq("delete_flag", FileConstants.RECYCLE)
                .like(!StringUtils.isEmpty(fileName), "file_name", fileName)
                .eq("user_id",uid)
                .eq(validFoldeType(folderType), "folder_type", folderType));
        List<File> records = filePage.getRecords();
        List<FileVo> fileVos = FILE_CONVERTER.toListFileVo(records);
        Page<FileVo> fileVoPage=new Page<>(pageNum,pageSize,filePage.getTotal());
        fileVoPage.setRecords(fileVos);
        return fileVoPage;
    }

    @Override
    public Page<FileVo> searchFile(SearchFileDto searchFileDto, Integer uid) {
        Integer fileCategory = searchFileDto.getFileCategory();
        String fileName = searchFileDto.getFileName();
        Integer pageNum = searchFileDto.getPageNum();
        Integer pageSize = searchFileDto.getPageSize();

        FileCategoryEnums byCategoy = FileCategoryEnums.getByCategoy(fileCategory);
        Page<File> filePage = this.page(new Page<>(pageNum, pageSize), new QueryWrapper<File>()
                .eq(!ObjectUtils.isEmpty(byCategoy), "file_category", fileCategory)
                .like(!StringUtils.isEmpty(fileName), "file_name", fileName)
                .eq("delete_flag", FileConstants.USING).orderByAsc("update_time"));

        List<File> records = filePage.getRecords();
        Page<FileVo> fileVoPage=new Page<>(pageNum,pageSize,filePage.getTotal());

        List<FileVo> fileVos = FILE_CONVERTER.toListFileVo(records);
        fileVoPage.setRecords(fileVos);
        return fileVoPage;

    }

    @Override
    public List<FileVo> getSaveFolder(String pid, Integer uid) {


        //得到目录的信息
        List<File> list = this.list(new QueryWrapper<File>().eq("user_id", uid).eq("delete_flag", FileConstants.USING)
               .eq("pid",pid).eq("folder_type",FileConstants.FOLDER_TYPE));
        return FILE_CONVERTER.toListFileVo(list);
    }


    private boolean validFoldeType(Integer type){
        return !ObjectUtils.isEmpty(type)&&(type.equals(0)||type.equals(1));
    }


}
