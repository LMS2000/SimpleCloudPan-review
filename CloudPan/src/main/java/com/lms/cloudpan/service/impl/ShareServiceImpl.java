package com.lms.cloudpan.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lms.cloudpan.config.OssProperties;
import com.lms.cloudpan.constants.CommonConstants;
import com.lms.cloudpan.constants.ShareConstants;
import com.lms.cloudpan.entity.dao.*;
import com.lms.cloudpan.entity.dto.*;
import com.lms.cloudpan.entity.enums.FileFolderTypeEnums;
import com.lms.cloudpan.entity.enums.ShareValidTypeEnums;
import com.lms.cloudpan.entity.vo.*;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.mapper.ShareMapper;
import com.lms.cloudpan.service.IFileService;
import com.lms.cloudpan.service.IShareService;
import com.lms.cloudpan.service.IUserService;
import com.lms.cloudpan.utils.*;
import com.lms.contants.HttpCode;
import com.lms.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lms.cloudpan.entity.factory.FileFactory.FILE_CONVERTER;
import static com.lms.cloudpan.entity.factory.ShareFactory.SHARE_CONVERTER;

@Service
@Slf4j
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Shares> implements IShareService {


    //一天的时间戳
    private static final Long EXPIRA_TIME = 24 * 60 * 60 * 1000L;


    @Resource
    private RedisCache redisCache;


    @Resource
    private IFileService fileService;

    @Resource
    private OssProperties ossProperties;


    @Resource
    private IUserService userService;

    /**
     * 用户发起分享，校验分享信息后将被分享的信息备份，如何缓存分享信息，生成分享链接
     *
     * @param shareDto 分享信息
     * @param uid      当前用户
     * @return
     */
    @Override
    @Transactional
    public SharesVo shareResource(ShareDto shareDto, Integer uid) {

        Integer validType = shareDto.getValidType();
        String code = shareDto.getShareKey();

        ShareValidTypeEnums shareValidTypeEnums = ShareValidTypeEnums.getByType(validType);
        Shares shares = new Shares();

        if (shareValidTypeEnums != ShareValidTypeEnums.FOREVER) {
            shares.setExpirationDate(DateUtil.getAfterDate(shareValidTypeEnums.getDays()));
        }
        Date curDate = new Date();
        shares.setShareTime(curDate);
        if (StringUtils.isEmpty(code)) {
            shares.setShareKey(StringTools.getRandomString(CommonConstants.LENGTH_5));
        }else{
            shares.setShareKey(shareDto.getShareKey());
        }
        shares.setShareUser(uid);
        shares.setShareType(shareDto.getShareType());
        shares.setFileId(shareDto.getFileId());
        this.save(shares);
        //设置缓存
        SharesVo sharesVo = SHARE_CONVERTER.toSharesVo(shares);
        redisCache.setCacheObject(ShareConstants.SHARED_FILEIDS + shares.getShareId(), sharesVo, shareValidTypeEnums.getDays(), TimeUnit.DAYS);
        return sharesVo;
    }

//    /**
//     * 异步设置锁定资源的缓存
//     * @param shareDto
//     * @param shareKey
//     * @param uid
//     */
//    public void setlockResourceCache(ShareDto shareDto,String shareKey,Integer uid){
//        Integer shareType = shareDto.getShareType();
//        String mapKey = ShareConstants.SHARE_MAP + shareKey;
//
//        Map<String, Object> cacheMap = redisCache.getCacheMap(mapKey);
//        if (cacheMap != null && cacheMap.size() != 0) {
//            throw new BusinessException(HttpCode.PARAMS_ERROR, "该资源已经处于分享状态");
//        }
//
//        //设置缓存锁定分享资源
//        if (ShareConstants.FILE_TYPE.equals(shareType)) {
//            String key=ShareConstants.SHARED_FILEIDS+uid+"_"+shareKey;
//            redisCache.setCacheList(key,List.of(shareDto.getSharedId()));
//        } else {
//            //获取层级的文件夹，然后递归添加锁定资源的文件夹id列表
//            Folder sharedFolder = folderService.getOne(new QueryWrapper<Folder>().eq("folder_id", shareDto.getSharedId()));
//
//            List<Folder> sharedFolderList =
//                    folderService.list(new QueryWrapper<Folder>().eq("user_id", uid));
//            List<File> fileList = fileService.list(new QueryWrapper<File>().eq("user_id", uid));
//
//            FolderVo tierFolder = ZipUtil.getTierFolder(sharedFolder.getFolderName(), FOLDER_CONVERTER.toListFolderVo(sharedFolderList));
//
//            Set<Integer> files=new HashSet<>();
//            Set<Integer> folders=new HashSet<>();
//            addResourceList(tierFolder,folders);
//
//            fileList.stream().filter(file -> folders.contains(file.getFolderId())).map(File::getFileId).forEach(files::add);
//            String fileKey=ShareConstants.SHARED_FILEIDS+uid+"_"+shareKey;
//            String folderKey=ShareConstants.SHARED_FOLDERIDS+uid+"_"+shareKey;
//            redisCache.setCacheSet(fileKey,files);
//            redisCache.expire(fileKey,shareDto.getExpiration(),TimeUnit.DAYS);
//            redisCache.setCacheSet(folderKey,folders);
//            redisCache.expire(folderKey,shareDto.getExpiration(),TimeUnit.DAYS);
//        }
//
//
//        Map<String, Object> map = new HashMap<>();
//        map.put(ShareConstants.SHARE_NUM_KEY, 0);
//        map.put(ShareConstants.SHARE_MAX_NUM_KEY, shareDto.getDownloadCount());
//        map.put(ShareConstants.SHARE_SECRET_KEY, shareDto.getShareSecretKey());
//        //设置过期时间
//        redisCache.setCacheMap(mapKey, map);
//        redisCache.expire(mapKey, shareDto.getExpiration(), TimeUnit.DAYS);
//    }
//
//    //递归获取因分享而锁定的资源
//    private void addResourceList(FolderVo folder,Set<Integer> folders){
//        if(folder==null)return;
//        folders.add(folder.getFolderId());
//        for (FolderVo folderVo : folder.getChildrenList()) {
//            addResourceList(folderVo,folders);
//        }
//    }


//    /**
//     * @param shareKey    分享key
//     * @param shareSecret 分享码
//     * @param curPath     当前路径
//     * @param uid         当前用户id
//     * @return
//     */
//    @Override
//    public Boolean setShareResource(String shareKey, String shareSecret, String curPath, Integer uid) {
//
//        //校验分享码
//
//        //查看缓存是否存在
//
//        //缓存的过期时间就是分享链接的过期时间，
//
//        String mapkey = ShareConstants.SHARE_MAP + shareKey;
//
//        Map<String, Object> cacheMap = redisCache.getCacheMap(mapkey);
//        if (cacheMap == null || cacheMap.size() == 0) {
//            throw new BusinessException(HttpCode.PARAMS_ERROR, "分享资源不存在");
//        }
//
//        //比对下载次数和分享码
//        String shareSec = (String) cacheMap.get(ShareConstants.SHARE_SECRET_KEY);
//        Integer shareNum = (Integer) cacheMap.get(ShareConstants.SHARE_NUM_KEY);
//        Integer shareMaxNum = (Integer) cacheMap.get(ShareConstants.SHARE_MAX_NUM_KEY);
//
//        if (!shareSecret.equals(shareSec)) {
//            throw new BusinessException(HttpCode.PARAMS_ERROR, "分享码不正确");
//        }
//        //获取当前用户的当前路径
//        Folder folder = folderService.getOne(new QueryWrapper<Folder>().eq("folder_name", curPath)
//                .eq("user_id", uid));
//
//        if (folder == null) {
//            throw new BusinessException(HttpCode.PARAMS_ERROR, "文件夹不存在");
//        }
//
//        if (shareNum.equals(shareMaxNum)) {
//            //到达最大下载次数删除缓存删除分享信息
//
//
//            Shares deletedShare = this.getOne(new QueryWrapper<Shares>().eq("share_key", shareKey));
//            Integer shareType = deletedShare.getShareType();
//            //删除缓存
//            ShareUtil.deleteSharedResourceCache(deletedShare.getShareType(), uid, deletedShare.getShareKey());
//            this.remove(new QueryWrapper<Shares>().eq("share_key", shareKey));
//            //修改资源的sharelink为null
//            if (ShareConstants.FILE_TYPE.equals(shareType)) {
//                fileService.updateById(File.builder().fileId(deletedShare.getSharedId()).shareLink("#").build());
//            } else {
//                folderService.updateById(Folder.builder().folderId(deletedShare.getSharedId()).shareLink("#").build());
//            }
//            throw new BusinessException(HttpCode.PARAMS_ERROR, "已到达最大下载次数");
//        }
//        Shares share = this.getOne(new QueryWrapper<Shares>().eq("share_key", shareKey));
//        Integer shareType = share.getShareType();
//
//        UserVo userById = userService.getUserById(uid);
//
//
//        //文件类型直接拷贝
//        if (ShareConstants.FILE_TYPE.equals(shareType)) {
//            //获取文件信息
//            File targetFile = fileService.getOne(new QueryWrapper<File>().eq("file_id", share.getSharedId()));
//
//            //校验接收分享资源是否超过用户的配额
//            validUserQuota(targetFile.getSize() + userById.getUseQuota(), userById.getQuota());
//
//            //开始备份文件
//            String fileName = targetFile.getFileName();
//            String fileUrl = targetFile.getFileUrl();
//            String[] split = fileUrl.split("static");
//            Path sourcePath = Paths.get(ossProperties + split[1]);
//            String randomPath = FileUtil.generatorFileName(fileName);
//            String datePath = new DateTime().toString("yyyy/MM/dd");
//
//            String filePath = StringUtils.isEmpty(datePath) ? randomPath : datePath + "/" + randomPath;
//            String bucketName = "bucket_user_" + uid;
//
//            Path toPath = Paths.get(ossProperties.getRootPath() + "/" + bucketName + "/" + filePath);
//
//            try {
//                Files.copy(sourcePath, toPath);
//                log.info("文件备份成功 {} to {}", sourcePath, toPath);
//            } catch (IOException e) {
//                throw new BusinessException(HttpCode.PARAMS_ERROR, "备份文件失败");
//            }
//
//            String tofileUrl = FileUtil.getFileUrl(ossProperties.getEndpoint(), "static", bucketName, filePath);
//
//            //记录文件信息
//            fileService.save(File.builder().fileUrl(tofileUrl).fileName(fileName)
//                    .fileType(targetFile.getFileType()).folderId(folder.getFolderId()).size(targetFile.getSize())
//                    .userId(uid).build());
//            //累加用户使用额度
//            userService.updateById(User.builder().userId(uid)
//                    .useQuota(userById.getUseQuota() + targetFile.getSize()).build());
//            //累加文件夹的大小
//            folderService.updateById(Folder.builder().folderId(folder.getFolderId())
//                    .size(folder.getSize() + targetFile.getSize()).build());
//
//            return true;
//        } else {
//            //如果是文件夹的话，
//
//
//            //根据这个文件夹id获取文件夹目录树
//
//            //然后根据被分享用户文件夹得到文件夹目录树
//            //
//            Folder sharedFolder = folderService.getOne(new QueryWrapper<Folder>().eq("folder_id", share.getSharedId()));
//
//            List<Folder> sharedFolderList =
//                    folderService.list(new QueryWrapper<Folder>().eq("user_id", share.getShareUser()));
//
//            FolderVo tierFolder = ZipUtil.getTierFolder(sharedFolder.getFolderName(), FOLDER_CONVERTER.toListFolderVo(sharedFolderList));
//
//            //校验用户配额
//            validUserQuota(userById.getUseQuota() + tierFolder.getSize(), userById.getQuota());
//
//            List<File> files = fileService.list(new QueryWrapper<File>().eq("user_id", share.getShareUser()));
//
//
//            //todo 递归写入文件夹信息，和文件信息，后期使用异步+redis记录异步任务的完成情况
//            insertFolder(curPath + "/", tierFolder, files, uid, folder.getFolderId());
//
//            //修改用户的配额
//            userService.updateById(User.builder().userId(uid)
//                    .useQuota(userById.getUseQuota() + tierFolder.getSize()).build());
//
//            //设置缓存的下载次数
//            redisCache.setCacheMapValue(mapkey, ShareConstants.SHARE_NUM_KEY, shareNum + 1);
//            return true;
//        }
//    }

    @Override
    @Transactional
    public Boolean cancelShare(List<String> ids, Integer uid) {
        ids.forEach(id -> {
            redisCache.deleteObject(ShareConstants.SHARED_FILEIDS + id);
        });
        return this.remove(new QueryWrapper<Shares>()
                .in(CollectionUtil.isNotEmpty(ids), "share_id", ids).eq("share_user", uid));
    }

    /**
     * 分页查询分享文件列表
     *
     * @param uid
     * @param pageSharesDto
     * @return
     */
    @Override
    public Page<SharesVo> pageShares(Integer uid, PageSharesDto pageSharesDto) {
        Integer shareType = pageSharesDto.getShareType();
        String fileName = pageSharesDto.getFileName();
        Integer pageNum = pageSharesDto.getPageNum();
        Integer pageSize = pageSharesDto.getPageSize();


        //获取文件
        List<File> files = fileService.list(new QueryWrapper<File>().like(!StringUtils.isEmpty(fileName), "file_name", fileName));

        Map<String, FileVo> fileMap = FILE_CONVERTER.toListFileVo(files).stream().collect(Collectors.toMap(FileVo::getFileId, Function.identity()));

        Page<Shares> sharesPage = this.page(new Page<>(pageNum, pageSize), new QueryWrapper<Shares>()
                .eq(validShareType(shareType), "share_type", shareType));
        List<Shares> records = sharesPage.getRecords();

        List<SharesVo> sharesVos = SHARE_CONVERTER.toListSharesVo(records);
        Page<SharesVo> sharesVoPage = new Page<>(pageNum, pageSize, sharesPage.getTotal());
        sharesVos.forEach(sharesVo -> {
            sharesVo.setFileVo(fileMap.getOrDefault(sharesVo.getFileId(), null));
        });
        sharesVoPage.setRecords(sharesVos);
        return sharesVoPage;
    }

    @Override
    public GetWebShareLoginVo getShareInfo(String shareId, Integer uid) {

        SharesVo sharesVo = redisCache.getCacheObject(ShareConstants.SHARED_FILEIDS + shareId);

        BusinessException.throwIf(sharesVo == null);


        String fileId = sharesVo.getFileId();
        File fileInfo = fileService.getById(fileId);

        BusinessException.throwIf(fileInfo == null);
        User user = userService.getById(sharesVo.getShareUser());
        GetWebShareLoginVo getWebShareLoginVo = GetWebShareLoginVo.builder().shareTime(sharesVo.getShareTime()).expireTime(sharesVo.getExpirationDate())
                .fileName(fileInfo.getFileName()).fileId(fileId).userId(sharesVo.getShareUser())
                .avatar(user.getAvatar()).nickName(user.getNickName()).build();
        if (uid != null) {
            getWebShareLoginVo.setCurrentUser(uid.equals(user.getUserId()));
        }

        return getWebShareLoginVo;
    }

    @Override
    public Boolean checkCode(String shareId, String code, HttpSession session) {

        SharesVo sharesVo = redisCache.getCacheObject(ShareConstants.SHARED_FILEIDS + shareId);

        BusinessException.throwIf(sharesVo == null, HttpCode.OPERATION_ERROR, "分享资源已过期");

        BusinessException.throwIfNot(sharesVo.getShareKey().equals(code), HttpCode.PARAMS_ERROR, "分享码不正确");

        session.setAttribute(ShareConstants.SESSION_SHARE_KEY + shareId, sharesVo);

        this.update(new UpdateWrapper<Shares>().setSql("download_count=download_count+1")
                .eq("share_id", shareId));
        return true;
    }

    /**
     * 根据分享id获取文件分页信息，如果有pid说明时子目录需要判断子路径是否正确，否则直接查看redis中分享id的文件id来获取文件
     *
     * @param pageShareFileDto
     * @return
     */
    @Override
    public Page<FileVo> pageFileList(PageShareFileDto pageShareFileDto) {

        String pid = pageShareFileDto.getPid();
        String shareId = pageShareFileDto.getShareId();
        Integer pageNum = pageShareFileDto.getPageNum();
        Integer pageSize = pageShareFileDto.getPageSize();

        SharesVo sharesVo = redisCache.getCacheObject(ShareConstants.SHARED_FILEIDS + shareId);


        QueryWrapper<File> wrapper = new QueryWrapper<>();
        String fileId = sharesVo.getFileId();
        Integer userId = sharesVo.getShareUser();
        if (!StringUtils.isEmpty(pid) && !CommonConstants.ZERO_STR.equals(pid)) {
            if (!pid.equals(sharesVo.getFileId())) {
                checkRootFilePid(pid, fileId, userId);
            }
            wrapper.eq("pid", sharesVo.getFileId());
        } else {
            wrapper.eq("file_id", sharesVo.getFileId());
        }
        Page<File> filePage = fileService.page(new Page<>(pageNum, pageSize), wrapper);
        List<File> records = filePage.getRecords();
        List<FileVo> fileVos = FILE_CONVERTER.toListFileVo(records);
        Page<FileVo> fileVoPage = new Page<>(pageNum, pageSize, filePage.getTotal());
        fileVoPage.setRecords(fileVos);
        return fileVoPage;
//        FileInfoQuery query = new FileInfoQuery();
//        if (!StringTools.isEmpty(filePid) && !Constants.ZERO_STR.equals(filePid)) {
//            fileInfoService.checkRootFilePid(shareSessionDto.getFileId(), shareSessionDto.getShareUserId(), filePid);
//            query.setFilePid(filePid);
//        } else {
//            query.setFileId(shareSessionDto.getFileId());
//        }
    }

    @Override
    public String createDownloadUrl(String shareId, String fileId) {

        SharesVo sharesVo = redisCache.getCacheObject(ShareConstants.SHARED_FILEIDS + shareId);
        BusinessException.throwIf(sharesVo == null);
        return fileService.createDownloadUrl(fileId, sharesVo.getShareUser());
    }

    @Override
    public Boolean saveShareFiles(SaveWebShareDto shareDto, Integer uid) {
        String shareId = shareDto.getShareId();
        String folderId = shareDto.getMyFolderId();
        List<String> shareFileIds = shareDto.getShareFileIds();
        SharesVo sharesVo = redisCache.getCacheObject(ShareConstants.SHARED_FILEIDS + shareId);

        BusinessException.throwIf(uid.equals(sharesVo.getShareUser()),
                HttpCode.OPERATION_ERROR, "不可分享到自己的网盘");
        //获取当前用户的目标路径下的文件map  key为文件名  value为文件对象
        Map<String, File> currentUserFileMap = fileService.list(new QueryWrapper<File>().eq("user_id", uid).eq("pid", folderId))
                .stream().collect(Collectors.toMap(File::getFileName, Function.identity(), (file1, file2) -> file1));

        //获取分享的文件列表
        List<File> sharedList = fileService.list(new QueryWrapper<File>()
                .in("file_id", shareFileIds).eq("user_id", sharesVo.getShareUser()));

        List<File> copyFileList = new ArrayList<>();

        //将路径下重名的文件改名
        for (File file : sharedList) {
            String fileName = file.getFileName();
            File haveFile = currentUserFileMap.get(fileName);
            if (haveFile != null) {
                fileName = StringTools.rename(fileName);
                file.setFileName(fileName);
            }
            findAllSubFile(copyFileList, file, sharesVo.getShareUser(), uid, folderId);
        }
        return fileService.saveBatch(copyFileList);
    }


    /**
     * 查找分享文件中所有子文件，并添加到copyFileList
     *
     * @param copyFileList
     * @param fileInfo
     * @param sourceUserId
     * @param currentUserId
     * @param newFilePid
     */
    private void findAllSubFile(List<File> copyFileList, File fileInfo, Integer sourceUserId, Integer currentUserId, String newFilePid) {

        String sourceFileId = fileInfo.getFileId();
        String newFileId = StringTools.getRandomString(CommonConstants.LENGTH_10);
        fileInfo.setPid(newFilePid);
        fileInfo.setFileId(newFileId);
        fileInfo.setUserId(currentUserId);
        fileInfo.setCreateTime(null);
        fileInfo.setUpdateTime(null);
        copyFileList.add(fileInfo);
        if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
            List<File> list = fileService.list(new QueryWrapper<File>()
                    .eq("pid", sourceFileId).eq("user_id", sourceUserId));
            for (File item : list) {
                findAllSubFile(copyFileList, item, sourceUserId, currentUserId, newFileId);
            }
        }
    }

    private void checkRootFilePid(String pid, String fileId, Integer uid) {
        File fileInfo =
                fileService.getOne(new QueryWrapper<File>().eq("user_id", uid).eq("file_id", fileId));

        BusinessException.throwIf(fileInfo == null || CommonConstants.ZERO_STR.equals(fileInfo.getPid()));
        if (fileInfo.getPid().equals(pid)) {
            return;
        }
        checkRootFilePid(pid, fileInfo.getFileId(), uid);
    }

//

    private boolean validShareType(Integer shareType) {
        List<Integer> integers = List.of(0, 1);
        return !ObjectUtils.isEmpty(shareType) && integers.contains(shareType);
    }


//    private void insertFolder(String parentFolderName, FolderVo folder, List<File> files, Integer uid, Integer pid) {
//
//        String folderName = folder.getFolderName();
//        int lastIndex = folderName.lastIndexOf("/");
//        if ("".equals(parentFolderName)) {
//            folderName = folderName.substring(lastIndex + 1);
//        } else {
//            folderName = parentFolderName + folderName.substring(lastIndex + 1);
//        }
//
////        ZipEntry folderEntry = new ZipEntry(folderName + "/");
////        zos.putNextEntry(folderEntry);
////        zos.closeEntry();
//        //插入文件夹信息
//        Folder build = Folder.builder().folderName(folderName).parentFolder(pid)
//                .userId(uid).size(folder.getSize()).build();
//        folderService.save(build);
//
//
//        // 遍历子文件夹
//        for (FolderVo subFolder : folder.getChildrenList()) {
//            insertFolder(folderName + "/", subFolder, files, uid, build.getFolderId());
//        }
//        //获取该目录下的子文件
//        List<File> subFiles = new ArrayList<>();
//        files.stream().forEach(file -> {
//            if (folder.getFolderId().equals(file.getFolderId())) {
//                subFiles.add(file);
//            }
//        });
//
//        //将文件添加到zip中
//        for (File file : subFiles) {
//            insertFile(file, uid, build.getFolderId());
//        }
//    }

    //备份文件
//    private void insertFile(File file, Integer uid, Integer folderId) {
//        //备份文件
//        String fileName = file.getFileName();
//        String fileUrl = file.getFileUrl();
//        //http://localhost:9998/pan/static/bucket_user_10/2023/05/22/8978ade8-7940-4909-bc9d-4dfda80424e5.png
//        String[] split = fileUrl.split("static");
//        Path sourcePath = Paths.get(ossProperties.getRootPath() + split[1]);
//        String randomPath = FileUtil.generatorFileName(fileName);
//        String datePath = new DateTime().toString("yyyy/MM/dd");
//
//        String filePath = StringUtils.isEmpty(datePath) ? randomPath : datePath + "/" + randomPath;
//        String bucketName = "bucket_user_" + uid;
//
//        Path toPath = Paths.get(ossProperties.getRootPath() + "/" + bucketName + "/" + filePath);
//
//        try {
//            if (Files.exists(toPath)) {
//                Files.delete(toPath); // 删除已有的toPath
//            }
//            Files.createDirectories(toPath.getParent());
//            Files.copy(sourcePath, toPath);
//            log.info("文件备份成功 {} to {}", sourcePath, toPath);
//        } catch (IOException e) {
//            throw new BusinessException(HttpCode.PARAMS_ERROR, "备份文件失败");
//        }
//
//        String tofileUrl = FileUtil.getFileUrl(ossProperties.getEndpoint(), "static", bucketName, filePath);
//
//        //记录文件信息
//        fileService.save(File.builder().fileUrl(tofileUrl).fileName(fileName)
//                .fileType(file.getFileType()).folderId(folderId).size(file.getSize())
//                .userId(uid).build());
//
//    }


    private void validUserQuota(Long useQuota, Long quota) {
        if (useQuota > quota) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "超出用户最大配额");
        }
    }


}
