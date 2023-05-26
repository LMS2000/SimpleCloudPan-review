# SimpleCloudPan-review
重构后的简易网盘
# myCloudPan

简易网盘项目，可以上传下载文件修改删除文件,可以增删改查文件夹，也可以通过分享链接给别人下载文件或者文件夹。



![image-20230518194444931](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230518194444931.png)





![image-20230518194457449](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230518194457449.png)









部署：项目中使用了自己做的一个工具库starter，已经放在项目的mystarter文件夹下，项目涉及到的sql在sql文件夹下
如果你要启动项目的话需要使用

```
mvn install:install-file -Dfile=lms-utils-1.0-SNAPSHOT.jar -Dmaven.repo.local=D:\apache-maven-3.6.1\maven_repository -DgroupId=com.lms -DartifactId=lms-utils -Dversion=1.0-SNAPSHOT -Dpackaging=jar

```

在jar包所在路径下使用这条命令
其中-Dmaven.repo.local指定你的maven本地仓库路径，

另外前端修改需要在.env.development修改你的后端地址

需求是每个用户都有自己独立的存储空间可以存储文件下载文件，创建文件夹，修改文件或文件夹名字，删除文件或者文件夹

关于文件夹表和文件表：
文件夹是虚拟路径，没有实际创建
文件存储的实际路径是根据用户bucket桶跟日期存放路径。主要目的是解决上传重复文件的问题
每个用户都有一个默认的路径根目录(/root)


- 用户注册：前端传递账号密码给后端，后端首先判断用户名是否存在，然后加密密码，记录用户信息，并且创建用户的根目录(/root) 然后分配配额(都是10G)
- 用户登录: 前端传递账号密码给后端，后端校验成功后将用户信息记录到session中，这样响应体会携带sessionid给前端，前端自动将sessionid设置到cookie中，要访问其他资源的时候都需要携带sessionid(前端配置请求设置携带cookie)
- 上传文件：
  前端上传multifile文件和当前的路径发送给后端
  后端获取文件和路径，再根据session获取用户id，判断文件夹是否存在,然后上传文件插入File表，同时修改文件夹大小，修改用户配额。
- 下载文件：
  前端发送文件名称， 后端去查找文件File表，找到文件，根据file_path获取文件，然后将文件流转换为二进制数据传递前端
- 创建文件夹：
  前端发送要创建的文件夹名和当前的路径给后端，后端查看同一路径下有没有重名文件夹，根据当前路径和文件夹名作为文件夹名插入folder表
- 修改文件名：
  前端发送文件id和新文件名给后端，后端根据id和新文件名修改文件
- 修改文件夹名：
  跟修改文件类似。
- 删除文件：
  前端传递文件id给后端，后端删除文件和记录的同时，还要修改用户配额，修改所属文件夹的大小
- 删除文件夹：前端传递文件夹id,后端首先根据文件夹id查出文件夹树，然后递归删除每一级文件夹所包含的文件和文件夹记录，最后修改用户配额
- 下载文件夹： 
  前端传递文件夹名（路径） ，后端接收然后得到文件夹的得到文件夹树，然后创建zip临时压缩文件，递归文件夹将文件夹添加进去，同时添加所包含的文件









```sql
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(50) DEFAULT NULL COMMENT '密码',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `enable` tinyint(1) DEFAULT '0' COMMENT '是否可用',
  `use_quota` bigint(11) DEFAULT NULL COMMENT '使用情况',
  `quota` bigint(11) DEFAULT NULL COMMENT '总容量',
  `avatar` varchar(50) DEFAULT NULL COMMENT '头像',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='用户表';

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `rid` int(11) NOT NULL AUTO_INCREMENT COMMENT 'rid',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '启用',
  `role_name` varchar(50) NOT NULL COMMENT '角色名',
  `description` varchar(50) NOT NULL COMMENT '角色描述',
   `create_time` datetime NOT NULL  COMMENT '创建时间',
  `update_time` datetime NOT NULL  COMMENT '更新时间',
  PRIMARY KEY (`rid`) USING BTREE
) ENGINE=InnoDB COMMENT='角色表';


-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `uid` int(11) NOT NULL COMMENT '用户id',
  `rid` int(11) NOT NULL COMMENT '角色id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `uid` (`uid`) USING BTREE
) ENGINE=InnoDB COMMENT='用户角色表';

-- ----------------------------
-- Table structure for authority
-- ----------------------------
DROP TABLE IF EXISTS `authority`;
CREATE TABLE `authority` (
  `aid` int(11) NOT NULL AUTO_INCREMENT COMMENT 'aid',
  `enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0启用,1禁用',
   `is_deleted` tinyint(1)  DEFAULT '0' COMMENT '0未删除，1删除',
  `name` varchar(50)  NOT NULL COMMENT '资源名',
  `description` varchar(50)  NOT NULL COMMENT '资源描述',
  `create_time` datetime NOT NULL  COMMENT '创建时间',
  `update_time` datetime NOT NULL  COMMENT '更新时间',
  PRIMARY KEY (`aid`) USING BTREE
) ENGINE=InnoDB  COMMENT='权限表';

-- ----------------------------
-- Table structure for role_authority
-- ----------------------------
DROP TABLE IF EXISTS `role_authority`;
CREATE TABLE `role_authority` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `rid` int(11) NOT NULL COMMENT '角色id',
  `aid` int(11) NOT NULL COMMENT '权限id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `rid` (`rid`) USING BTREE
) ENGINE=InnoDB  COMMENT='角色权限表';



-- ----------------------------
-- Table structure for operation_log
-- ----------------------------

DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `operation_name` varchar(255)  NOT NULL COMMENT '操作者姓名',
  `operation_content` varchar(255)  NOT NULL COMMENT '操作内容',
  `create_time` datetime(0) NOT NULL  COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL  COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  COMMENT = '操作日志记录表' ;

DROP TABLE IF EXISTS `upload_log`;
CREATE TABLE `upload_log`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `bucket_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '桶名',
  `file_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件名',
  `file_md5` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件md5值',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '上传日志表' ROW_FORMAT = Compact;


DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
  `file_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `file_name` varchar(50) DEFAULT NULL COMMENT '文件名',
  `file_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '鏂囦欢璺緞',
  `size` bigint(11) DEFAULT NULL COMMENT '文件大小',
  `user_id` int(11) DEFAULT NULL COMMENT '所属用户id',
  `folder_id` int(11) DEFAULT NULL COMMENT '所属文件夹id',
  `file_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `share_link` VARCHAR(255) DEFAULT NULL COMMENT '分享链接',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`file_id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8 COMMENT='文件表';

DROP TABLE IF EXISTS `folder`;
CREATE TABLE `folder` (
  `folder_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `folder_name` varchar(50) NOT NULL COMMENT '文件夹名称',
  `parent_folder` int(11) DEFAULT NULL,
  `size` bigint(20) DEFAULT '0',
  `user_id` int(11) NOT NULL COMMENT '所属用户',
  `share_link` VARCHAR(255) DEFAULT NULL COMMENT '分享链接',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`folder_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8 COMMENT='文件夹表';



DROP TABLE IF EXISTS `shares`;
CREATE TABLE `shares` (
    `id` INT(11) NOT NULL AUTO_INCREMENT  COMMENT 'id',
    `shared_id` INT(11) NOT NULL COMMENT '被分享文件或者文件夹的id',
    `share_type` tinyint(1) NOT NULL  COMMENT '被分享文件或者文件夹的类型 0为文件 1为文件夹',
    `share_key` VARCHAR(50) NOT NULL COMMENT '分享唯一标识' ,
    `share_user` int(11) NOT NULL COMMENT '分享的用户' ,
    `share_time` TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '分享时间',
    `expiration_date` DATETIME NOT NULL COMMENT '过期时间',
    `download_count`INT  DEFAULT 0 COMMENT '资源被下载次数',
    PRIMARY KEY (`id`),
    UNIQUE (`share_key`)
);

```







分享功能思路：

 用户选择指定的文件或者文件夹发起分享，可以设置分享链接的过期时间，访问次数，然后后端记录这个分享信息，并且生成一个分享的唯一key。

用户发起分享传递了这个分享文件或者文件夹的id,和类型，  过期时间，访问次数，分享码（share_pwd）,后端接收这个信息后

校验分享资源，

备份分享资源

备份号就是分享唯一标识

备份的资源名称都需要使用实际的文件名或者文件夹

备份路径为 bucket_user_uid/temp/备份号/备份文件

备份文件：

​       直接copy到目录下并且按照层级结构



备份文件夹：

 递归copy到目录下











记录分享信息，构建一个链接，如下

/pan/share/+唯一标识   

唯一标识是用来区别分享信息的。使用uuid生成

并且还需要使用这个唯一标识作为key缓存到redis上，value就是分享码和访问次数，过期时间就是分享用户设置的过期时间

这里应该使用redis的set类型



最后返回用户这个分享链接







访问分享链接：

用户发送分享链接给后端，后端校验分析链接，然后将分享的文件或者文件夹备份到当前用户的路径下，并且记录相关的文件和文件夹信息



用户发送这个分享链接和分享码给后端，后端根据这个唯一标识在redis上查找这个信息，比对分享码是否正确以及是否超过访问次数，校验通过后，准备备份数据.

先判断文件的大小是否超过了用户的配额



分享的是文件，备份目标文件，修改用户的usequota



分享的是文件夹，先获取分享资源用户的全部文件夹，然后构建层级的文件夹目录树，递归目录树，

每一个层级都根据用户的当前文件夹路径和层级文件夹记录新的文件夹信息

然后记录 每个层级的文件，备份文件，记录文件

由于文件夹本来就已经记录了其下所有的文件的大小，所有每次记录层级文件夹的时候都要记录分享文件夹层级的大小









设置redis上这个key访问数量+1，然后如果大于等于最大的访问数，就删除这条缓存，并且删除分享表这个记录,然后设置这个文件或者文件夹记录的share_link为null







取消分享：

  通过shareLink 字段判断是否处于分享状态。发送分享key请求给后端

后端：删除缓存，删除分享信息





明日：

1. 移动操作

2. 取消分享
3. 锁定用户操作





关于用户修改分享资源的问题，直接锁定这个资源，不能修改，除非取消分享



锁定资源：

  锁定资源的文件信息可以放在redis中



可以将锁定资源的 设计成一个list  获取锁定资源的文件id，和文件夹id

key设置成 shared_fileIds: 唯一标识            

key设置成 shared_folderIds: 唯一标识   





分享资源锁定缓存：

key使用  缓存头：uid_唯一标识





对分享的锁定资源设置缓存操作添加异步实现来加快分享速度





对上传文件也采用异步上传，采用异步+2pc方式，用户需要发送两次请求，第一次上传文件，后端开启异步任务，并返回异步任务的id，第二次发送请求查看文件是否上传（异步任务是否执行完毕）



```java
   /**
     * 上传文件
     * 先判断保存的路径是否存在，然后判断文件是否合法（包括文件大小，是否超过用户的配额）
     * 生成异步任务id，启动异步任务然后返回id
     * 根据上传问文件名生成唯一的路径，记录文件日志，然后上传文件
     * 然后记录文件信息，更新用户配额信息
     *如果过程中发生异常则回滚，并且根据文件日志删除文件删除文件日志
     * @param fileVo
     * @param user
     * @return
     */
    @Override
    public String insertFile(FileVo fileVo, User user) {
        Integer uid = user.getUserId();
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
                        SpringUtil.getBean(FileServiceImpl.class).uoloadFileAsync(file.getInputStream(),file,user,folder,taskId);
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                SpringUtil.getBean("asyncExecutor", Executor.class));
        return taskId;
    }
```

















