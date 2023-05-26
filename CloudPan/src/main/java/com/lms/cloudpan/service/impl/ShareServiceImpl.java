package com.lms.cloudpan.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lms.cloudpan.config.OssProperties;
import com.lms.cloudpan.constants.FileConstants;
import com.lms.cloudpan.constants.ShareConstants;
import com.lms.cloudpan.entity.dao.File;
import com.lms.cloudpan.entity.dao.Folder;
import com.lms.cloudpan.entity.dao.Shares;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.FolderDto;
import com.lms.cloudpan.entity.dto.UserDto;
import com.lms.cloudpan.entity.vo.CancelShareVo;
import com.lms.cloudpan.entity.vo.ShareVo;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.mapper.FileMapper;
import com.lms.cloudpan.mapper.FolderMapper;
import com.lms.cloudpan.mapper.ShareMapper;
import com.lms.cloudpan.service.IFileService;
import com.lms.cloudpan.service.IFolderService;
import com.lms.cloudpan.service.IShareService;
import com.lms.cloudpan.service.IUserService;
import com.lms.cloudpan.utis.FileUtil;
import com.lms.cloudpan.utis.MybatisUtils;
import com.lms.cloudpan.utis.ShareUtil;
import com.lms.cloudpan.utis.ZipUtil;
import com.lms.contants.HttpCode;
import com.lms.redis.RedisCache;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.lms.cloudpan.entity.factory.FolderFactory.FOLDER_CONVERTER;
import static com.lms.cloudpan.entity.factory.ShareFactory.SHARE_CONVERTER;

@Service
@Slf4j
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Shares> implements IShareService {


    //一天的时间戳
    private static final Long EXPIRA_TIME = 24 * 60 * 60 * 1000L;



    @Resource
    private RedisCache redisCache;

    @Resource
    private IFolderService folderService;


    @Resource
    private IFileService fileService;

    @Resource
    private OssProperties ossProperties;


    @Resource
    private IUserService userService;

    /**
     * 用户发起分享，校验分享信息后将被分享的信息备份，如何缓存分享信息，生成分享链接
     * @param shareVo 分享信息
     * @param uid 当前用户
     * @return
     */
    @Override
    @Transactional
    public String shareResource(ShareVo shareVo, Integer uid) {


        //校验(根据id和type去判断资源是否存在)
        Integer shareType = shareVo.getShareType();
        boolean existCheck = true;
        if (ShareConstants.FILE_TYPE.equals(shareType)) {
            existCheck = MybatisUtils.existCheck(fileService, Map.of("file_id", shareVo.getSharedId()));
        } else if (ShareConstants.FOLDER_TYPE.equals(shareType)) {
            existCheck = MybatisUtils.existCheck(folderService, Map.of("folder_id", shareVo.getSharedId()));
        } else {
            existCheck = false;
        }
        if (!existCheck) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "分享资源不存在");
        }


        //记录这个分享信息
        Shares shares = SHARE_CONVERTER.toShare(shareVo);
        //随机生成唯一标识
        String shareKey = UUID.randomUUID().toString();


        shares.setShareKey(shareKey);
        //设置分享时间
        shares.setShareTime(new Date());
        //设置过期时间
        long timestamp = System.currentTimeMillis() + shareVo.getExpiration() * EXPIRA_TIME;
        shares.setExpirationDate(new Date(timestamp));
        shares.setShareUser(uid);

        //记录分享信息
        this.save(shares);

        //构建分享链接
        String shareLink = ShareConstants.SHARE_LINK_HEADER + shareKey;
        //修改资源的shareLink
        if (ShareConstants.FILE_TYPE.equals(shareType)) {
            fileService.updateById(File.builder().fileId(shareVo.getSharedId()).shareLink(shareLink).build());
        } else {
            folderService.updateById(Folder.builder().folderId(shareVo.getSharedId()).shareLink(shareLink).build());
        }


        //调用异步设置锁定资源缓存
        CompletableFuture.runAsync(()-> {SpringUtil.getBean(ShareServiceImpl.class).setlockResourceCache(shareVo,shareKey,uid);},
                SpringUtil.getBean("asyncExecutor", Executor.class));
        return shareLink;
    }

    /**
     * 异步设置锁定资源的缓存
     * @param shareVo
     * @param shareKey
     * @param uid
     */
    public void setlockResourceCache(ShareVo shareVo,String shareKey,Integer uid){
        Integer shareType = shareVo.getShareType();
        String mapKey = ShareConstants.SHARE_MAP + shareKey;

        Map<String, Object> cacheMap = redisCache.getCacheMap(mapKey);
        if (cacheMap != null && cacheMap.size() != 0) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "该资源已经处于分享状态");
        }

        //设置缓存锁定分享资源
        if (ShareConstants.FILE_TYPE.equals(shareType)) {
            String key=ShareConstants.SHARED_FILEIDS+uid+"_"+shareKey;
            redisCache.setCacheList(key,List.of(shareVo.getSharedId()));
        } else {
            //获取层级的文件夹，然后递归添加锁定资源的文件夹id列表
            Folder sharedFolder = folderService.getOne(new QueryWrapper<Folder>().eq("folder_id", shareVo.getSharedId()));

            List<Folder> sharedFolderList =
                    folderService.list(new QueryWrapper<Folder>().eq("user_id", uid));
            List<File> fileList = fileService.list(new QueryWrapper<File>().eq("user_id", uid));

            FolderDto tierFolder = ZipUtil.getTierFolder(sharedFolder.getFolderName(), FOLDER_CONVERTER.toListFolderDto(sharedFolderList));

            Set<Integer> files=new HashSet<>();
            Set<Integer> folders=new HashSet<>();
            addResourceList(tierFolder,folders);

            fileList.stream().filter(file -> folders.contains(file.getFolderId())).map(File::getFileId).forEach(files::add);
            String fileKey=ShareConstants.SHARED_FILEIDS+uid+"_"+shareKey;
            String folderKey=ShareConstants.SHARED_FOLDERIDS+uid+"_"+shareKey;
            redisCache.setCacheSet(fileKey,files);
            redisCache.expire(fileKey,shareVo.getExpiration(),TimeUnit.DAYS);
            redisCache.setCacheSet(folderKey,folders);
            redisCache.expire(folderKey,shareVo.getExpiration(),TimeUnit.DAYS);
        }


        Map<String, Object> map = new HashMap<>();
        map.put(ShareConstants.SHARE_NUM_KEY, 0);
        map.put(ShareConstants.SHARE_MAX_NUM_KEY, shareVo.getDownloadCount());
        map.put(ShareConstants.SHARE_SECRET_KEY, shareVo.getShareSecretKey());
        //设置过期时间
        redisCache.setCacheMap(mapKey, map);
        redisCache.expire(mapKey, shareVo.getExpiration(), TimeUnit.DAYS);
    }

    //递归获取因分享而锁定的资源
    private void addResourceList(FolderDto folder,Set<Integer> folders){
        if(folder==null)return;
        folders.add(folder.getFolderId());
        for (FolderDto folderDto : folder.getChildrenList()) {
            addResourceList(folderDto,folders);
        }
    }


    /**
     *
     * @param shareKey    分享key
     * @param shareSecret 分享码
     * @param curPath     当前路径
     * @param uid         当前用户id
     * @return
     */
    @Override
    public Boolean setShareResource(String shareKey, String shareSecret, String curPath, Integer uid) {

        //校验分享码

        //查看缓存是否存在

        //缓存的过期时间就是分享链接的过期时间，

        String mapkey = ShareConstants.SHARE_MAP + shareKey;

        Map<String, Object> cacheMap = redisCache.getCacheMap(mapkey);
        if (cacheMap == null || cacheMap.size() == 0) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "分享资源不存在");
        }

        //比对下载次数和分享码
        String shareSec = (String) cacheMap.get(ShareConstants.SHARE_SECRET_KEY);
        Integer shareNum = (Integer) cacheMap.get(ShareConstants.SHARE_NUM_KEY);
        Integer shareMaxNum = (Integer) cacheMap.get(ShareConstants.SHARE_MAX_NUM_KEY);

        if (!shareSecret.equals(shareSec)) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "分享码不正确");
        }
        //获取当前用户的当前路径
        Folder folder = folderService.getOne(new QueryWrapper<Folder>().eq("folder_name", curPath)
                .eq("user_id",uid));

        if (folder == null) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "文件夹不存在");
        }

        if (shareNum.equals(shareMaxNum)) {
            //到达最大下载次数删除缓存删除分享信息


            Shares deletedShare = this.getOne(new QueryWrapper<Shares>().eq("share_key", shareKey));
            Integer shareType = deletedShare.getShareType();
            //删除缓存
            ShareUtil.deleteSharedResourceCache(deletedShare.getShareType(),uid,deletedShare.getShareKey());
            this.remove(new QueryWrapper<Shares>().eq("share_key", shareKey));
            //修改资源的sharelink为null
            if (ShareConstants.FILE_TYPE.equals(shareType)) {
                fileService.updateById(File.builder().fileId(deletedShare.getSharedId()).shareLink("#").build());
            } else {
                folderService.updateById(Folder.builder().folderId(deletedShare.getSharedId()).shareLink("#").build());
            }
            throw new BusinessException(HttpCode.PARAMS_ERROR, "已到达最大下载次数");
        }
        Shares share = this.getOne(new QueryWrapper<Shares>().eq("share_key", shareKey));
        Integer shareType = share.getShareType();

        UserDto userById = userService.getUserById(uid);


        //文件类型直接拷贝
        if (ShareConstants.FILE_TYPE.equals(shareType)) {
            //获取文件信息
            File targetFile = fileService.getOne(new QueryWrapper<File>().eq("file_id", share.getSharedId()));

            //校验接收分享资源是否超过用户的配额
            validUserQuota(targetFile.getSize() + userById.getUseQuota(), userById.getQuota());

            //开始备份文件
            String fileName = targetFile.getFileName();
            String fileUrl = targetFile.getFileUrl();
            String[] split = fileUrl.split("static");
            Path sourcePath = Paths.get(ossProperties + split[1]);
            String randomPath = FileUtil.generatorFileName(fileName);
            String datePath = new DateTime().toString("yyyy/MM/dd");

            String filePath = StringUtils.isEmpty(datePath) ? randomPath : datePath + "/" + randomPath;
            String bucketName = "bucket_user_" + uid;

            Path toPath = Paths.get(ossProperties.getRootPath() + "/" + bucketName + "/" + filePath);

            try {
                Files.copy(sourcePath, toPath);
                log.info("文件备份成功 {} to {}", sourcePath, toPath);
            } catch (IOException e) {
                throw new BusinessException(HttpCode.PARAMS_ERROR, "备份文件失败");
            }

            String tofileUrl = FileUtil.getFileUrl(ossProperties.getEndpoint(), "static", bucketName, filePath);

            //记录文件信息
            fileService.save(File.builder().fileUrl(tofileUrl).fileName(fileName)
                    .fileType(targetFile.getFileType()).folderId(folder.getFolderId()).size(targetFile.getSize())
                    .userId(uid).build());
            //累加用户使用额度
            userService.updateById(User.builder().userId(uid)
                    .useQuota(userById.getUseQuota() + targetFile.getSize()).build());
            //累加文件夹的大小
            folderService.updateById(Folder.builder().folderId(folder.getFolderId())
                    .size(folder.getSize() + targetFile.getSize()).build());

            return true;
        } else {
            //如果是文件夹的话，


            //根据这个文件夹id获取文件夹目录树

            //然后根据被分享用户文件夹得到文件夹目录树
            //
            Folder sharedFolder = folderService.getOne(new QueryWrapper<Folder>().eq("folder_id", share.getSharedId()));

            List<Folder> sharedFolderList =
                    folderService.list(new QueryWrapper<Folder>().eq("user_id", share.getShareUser()));

            FolderDto tierFolder = ZipUtil.getTierFolder(sharedFolder.getFolderName(), FOLDER_CONVERTER.toListFolderDto(sharedFolderList));

            //校验用户配额
            validUserQuota(userById.getUseQuota() + tierFolder.getSize(), userById.getQuota());

            List<File> files = fileService.list(new QueryWrapper<File>().eq("user_id", share.getShareUser()));


            //todo 递归写入文件夹信息，和文件信息，后期使用异步+redis记录异步任务的完成情况
            insertFolder(curPath+"/", tierFolder, files, uid,folder.getFolderId());

            //修改用户的配额
            userService.updateById(User.builder().userId(uid)
                    .useQuota(userById.getUseQuota() + tierFolder.getSize()).build());

            //设置缓存的下载次数
            redisCache.setCacheMapValue(mapkey,ShareConstants.SHARE_NUM_KEY,shareNum+1);
            return true;
        }
    }

    @Override
    @Transactional
    public Boolean cancelShare(CancelShareVo cancelShareVo, Integer uid) {

        //删除缓存
        //删除分享信息缓存
        //删除分享锁定资源缓存
        ShareUtil.deleteSharedResourceCache(cancelShareVo.getShareType(),uid,cancelShareVo.getShareKey());

        //删除分享信息

        this.remove(new QueryWrapper<Shares>().eq("share_key",cancelShareVo.getShareKey()));

        //修改资源的
        Integer shareType = cancelShareVo.getShareType();

        if(shareType.equals(ShareConstants.FILE_TYPE)){
            fileService.updateById(File.builder().fileId(cancelShareVo.getSharedId())
                    .shareLink("#").build());

        }else{
            folderService.updateById(Folder.builder().folderId(cancelShareVo.getSharedId())
                    .shareLink("#").build());
        }

        return true;
    }


    private void insertFolder(String parentFolderName, FolderDto folder, List<File> files, Integer uid,Integer pid) {

        String folderName = folder.getFolderName();
        int lastIndex = folderName.lastIndexOf("/");
        if ("".equals(parentFolderName)) {
            folderName = folderName.substring(lastIndex + 1);
        } else {
            folderName = parentFolderName + folderName.substring(lastIndex + 1);
        }

//        ZipEntry folderEntry = new ZipEntry(folderName + "/");
//        zos.putNextEntry(folderEntry);
//        zos.closeEntry();
        //插入文件夹信息
        Folder build = Folder.builder().folderName(folderName).parentFolder(pid)
                .userId(uid).size(folder.getSize()).build();
        folderService.save(build);


        // 遍历子文件夹
        for (FolderDto subFolder : folder.getChildrenList()) {
            insertFolder(folderName + "/", subFolder, files, uid,build.getFolderId());
        }
        //获取该目录下的子文件
        List<File> subFiles = new ArrayList<>();
        files.stream().forEach(file -> {
            if (folder.getFolderId().equals(file.getFolderId())) {
                subFiles.add(file);
            }
        });

        //将文件添加到zip中
        for (File file : subFiles) {
            insertFile(file, uid, build.getFolderId());
        }
    }

    //备份文件
    private void insertFile(File file, Integer uid, Integer folderId) {
        //备份文件
        String fileName = file.getFileName();
        String fileUrl = file.getFileUrl();
        //http://localhost:9998/pan/static/bucket_user_10/2023/05/22/8978ade8-7940-4909-bc9d-4dfda80424e5.png
        String[] split = fileUrl.split("static");
        Path sourcePath = Paths.get(ossProperties.getRootPath() + split[1]);
        String randomPath = FileUtil.generatorFileName(fileName);
        String datePath = new DateTime().toString("yyyy/MM/dd");

        String filePath = StringUtils.isEmpty(datePath) ? randomPath : datePath + "/" + randomPath;
        String bucketName = "bucket_user_" + uid;

        Path toPath = Paths.get(ossProperties.getRootPath() + "/" + bucketName + "/" + filePath);

        try {
            if (Files.exists(toPath)) {
                Files.delete(toPath); // 删除已有的toPath
            }
            Files.createDirectories(toPath.getParent());
            Files.copy(sourcePath, toPath);
            log.info("文件备份成功 {} to {}", sourcePath, toPath);
        } catch (IOException e) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "备份文件失败");
        }

        String tofileUrl = FileUtil.getFileUrl(ossProperties.getEndpoint(), "static", bucketName, filePath);

        //记录文件信息
        fileService.save(File.builder().fileUrl(tofileUrl).fileName(fileName)
                .fileType(file.getFileType()).folderId(folderId).size(file.getSize())
                .userId(uid).build());

    }


    private void validUserQuota(Long useQuota, Long quota) {
        if (useQuota > quota) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "超出用户最大配额");
        }
    }


}
