# SimpleCloudPan-review
重构后的简易网盘,优化文件删除，上传文件的异步操作，分享功能，文件秒传功能等
# myCloudPan

简易网盘项目，可以上传下载文件修改删除文件,可以增删改查文件夹，也可以通过分享链接给别人下载文件或者文件夹。



![image-20230518194444931](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230518194444931.png)





![image-20230526163302839](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230526163302839.png)




![image-20230526163419756](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230526163419756.png)




部署：项目中使用了自己做的一个工具库starter，已经放在项目的mystarter文件夹下，项目涉及到的sql在sql文件夹下
如果你要启动项目的话需要使用

```
mvn install:install-file -Dfile=lms-utils-1.0-SNAPSHOT.jar -Dmaven.repo.local=D:\apache-maven-3.6.1\maven_repository -DgroupId=com.lms -DartifactId=lms-utils -Dversion=1.0-SNAPSHOT -Dpackaging=jar

```

在jar包所在路径下使用这条命令
其中-Dmaven.repo.local指定你的maven本地仓库路径，

另外前端修改需要在.env.development修改你的后端地址
关于文件夹表和文件表：
文件夹是虚拟路径，没有实际创建
文件存储的实际路径是根据用户bucket桶跟日期存放路径。主要目的是解决上传重复文件的问题
每个用户都有一个默认的路径根目录(/root)


- 用户注册：前端传递账号密码给后端，后端首先判断用户名是否存在，然后加密密码，记录用户信息，并且创建用户的根目录(/root) 然后分配配额(都是10G)

- 用户登录: 用户发送账号密码，后端使用spring security的登录拦截器`TokenLoginAuthFilter`  接收账号密码，经过校验后，生成token，这里的token的荷载中存储的是redis缓存中用户登录信息的key。而缓存中存储了用户的登陆信息，之后就不需要去数据库拿取，并且在用户携带token访问的时候后端使用鉴权拦截器`DaoTokenAuthenticationFilter` 进行鉴权，如果通过后会将用户的token刷新，也就是token续期操作，比如说用户的登录信息在缓存上保存了30分钟，而用户携带token，后端发现用户的token过期时间小于20分钟，就会动态的刷新。这样用户只要使用系统进行操作就会不断的刷新，这样对于用户来说是比较友好的。

- 上传文件：
  前端上传multifile文件和当前的路径发送给后端
  后端获取文件和路径，根据SecurityUtils工具类中的方法，其实就是使用`SecurityContextHolder` 在上下文中获取当前用户的登录信息，然后得到uid后，进行文件的校验流程包括了文件的大小等，然后会开启一个异步操作，返回异步任务的id，而用户还需要用这个异步任务的id请求一次后端查看上传情况。

- 下载文件：
  前端发送文件名称， 后端去查找文件File表，找到文件，根据file_path获取文件，然后将文件流转换为二进制数据传递前端

- 创建文件夹：
  前端发送要创建的文件夹名和当前的路径给后端，后端查看同一路径下有没有重名文件夹，根据当前路径和文件夹名作为文件夹名插入folder表

- 修改文件名：
  前端发送文件id和新文件名给后端，后端根据id和新文件名修改文件

- 修改文件夹名：
  跟修改文件类似。

- 删除文件：
  前端传递文件id给后端，后端标记删除文件和记录的同时，还要修改用户配额，修改所属文件夹的大小

- 删除文件夹：前端传递文件夹id,后端首先根据文件夹id查出文件夹树，然后递归标记删除每一级文件夹所包含的文件和文件夹记录，最后修改用户配额

- 下载文件夹： 
  前端传递文件夹名（路径） ，后端接收然后得到文件夹的得到文件夹树，然后创建zip临时压缩文件，递归文件夹将文件夹添加进去，同时添加所包含的文件

- 分享功能：根据用户的分享设置（天数，下载次数，分享码）得到一个分享链接，其他用户可以使用这个分享链接和分享码导入分享的资源到当前路径下。具体思路如下：

   1）用户分享：后端接收到用户的分享信息，开始校验分享信息，然后锁定分享信息，就是缓存锁定资源的id，因为这个操作可能影响响应所以采用异步方式。之后响应用户分享链接

  2）其他用户导入分享资源：其他用户在后端校验完分享链接和分享码后，后端根据分享的资源：

  ​     如果是文件就返回byte数组，文件夹就返回下载链接

- 文件删除的定时任务： 定时扫描有删除标记的资源进行删除








```sql
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '瀵嗙爜',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `enable` tinyint(1) DEFAULT '0' COMMENT '是否可用',
  `use_quota` bigint(11) DEFAULT NULL COMMENT '使用情况',
  `quota` bigint(11) DEFAULT NULL COMMENT '总容量',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '#' COMMENT '婢舵潙鍎?',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COMMENT='用户表';



-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `rid` int(11) NOT NULL AUTO_INCREMENT COMMENT 'rid',
  `enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '鍚敤',
  `role_name` varchar(50) NOT NULL COMMENT '角色名',
  `description` varchar(50) NOT NULL COMMENT '角色描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`rid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='角色表';




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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='用户角色表';



-- ----------------------------
-- Table structure for authority
-- ----------------------------
DROP TABLE IF EXISTS `authority`;
CREATE TABLE `authority` (
  `aid` int(11) NOT NULL AUTO_INCREMENT COMMENT 'aid',
  `enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0启用,1禁用',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '0未删除，1删除',
  `name` varchar(50) NOT NULL COMMENT '资源名',
  `description` varchar(50) NOT NULL COMMENT '资源描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`aid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8 COMMENT='权限表';



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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='角色权限表';



-- ----------------------------
-- Table structure for operation_log
-- ----------------------------

DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `operation_name` varchar(255) NOT NULL COMMENT '操作者姓名',
  `operation_content` varchar(255) NOT NULL COMMENT '操作内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志记录表';



DROP TABLE IF EXISTS `upload_log`;
CREATE TABLE `upload_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `bucket_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '桶名',
  `file_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件名',
  `file_md5` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件md5值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='上传日志表';



DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
  `file_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `file_name` varchar(50) DEFAULT NULL COMMENT '文件名',
  `file_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '鏂囦欢璺緞',
  `size` bigint(11) DEFAULT NULL COMMENT '文件大小',
  `user_id` int(11) DEFAULT NULL COMMENT '所属用户id',
  `folder_id` int(11) DEFAULT NULL COMMENT '所属文件夹id',
  `file_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `delete_flag` tinyint(1) DEFAULT '0',
  `share_link` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '#' COMMENT '鍒嗕韩閾炬帴',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`file_id`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8 COMMENT='文件表';



DROP TABLE IF EXISTS `folder`;
CREATE TABLE `folder` (
  `folder_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `folder_name` varchar(50) NOT NULL COMMENT '文件夹名称',
  `parent_folder` int(11) DEFAULT '0',
  `size` bigint(20) DEFAULT '0',
  `user_id` int(11) NOT NULL COMMENT '所属用户',
  `share_link` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '#' COMMENT '鍒嗕韩閾炬帴',
  `delete_flag` tinyint(1) DEFAULT '0',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`folder_id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8 COMMENT='文件夹表';




DROP TABLE IF EXISTS `shares`;
CREATE TABLE `shares` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `shared_id` int(11) NOT NULL COMMENT '被分享文件或者文件夹的id',
  `share_type` tinyint(1) NOT NULL COMMENT '被分享文件或者文件夹的类型 0为文件 1为文件夹',
  `share_key` varchar(50) NOT NULL COMMENT '分享唯一标识',
  `share_user` int(11) NOT NULL COMMENT '分享的用户',
  `share_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分享时间',
  `expiration_date` datetime NOT NULL COMMENT '过期时间',
  `download_count` int(11) DEFAULT '0' COMMENT '资源被下载次数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `share_key` (`share_key`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;


```













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

















