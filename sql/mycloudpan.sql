
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
                        `username` varchar(50) DEFAULT NULL COMMENT '用户名',
                        `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '瀵嗙爜',
                        `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
                        `enable` tinyint(1) DEFAULT '0' COMMENT '是否可用',
                        `use_quota` bigint(11) unsigned DEFAULT '0' COMMENT '浣跨敤鎯呭喌',
                        `quota` bigint(11) DEFAULT NULL COMMENT '总容量',
                        `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '#' COMMENT '婢舵潙鍎?',
                        `remark` text,
                        `nick_name` varchar(50) DEFAULT NULL,
                        `delete_flag` tinyint(1) DEFAULT '0',
                        `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                        `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                        PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COMMENT='用户表';

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
                        `rid` int(11) NOT NULL AUTO_INCREMENT COMMENT 'rid',
                        `enable` tinyint(4) NOT NULL DEFAULT '0' COMMENT '閸氼垳鏁?',
                        `role_name` varchar(50) NOT NULL COMMENT '角色名',
                        `description` varchar(50) NOT NULL COMMENT '角色描述',
                        `create_time` datetime NOT NULL COMMENT '创建时间',
                        `update_time` datetime NOT NULL COMMENT '更新时间',
                        PRIMARY KEY (`rid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='角色表';

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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COMMENT='用户角色表';

-- ----------------------------
-- Table structure for authority
-- ----------------------------
DROP TABLE IF EXISTS `authority`;
CREATE TABLE `authority` (
                             `aid` int(11) NOT NULL AUTO_INCREMENT COMMENT 'aid',
                             `parent_id` int(11) DEFAULT '0',
                             `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '閻犙冨缁噣骞撹箛姘墯',
                             `perms` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '閻犙冨缁噣宕?',
                             `enable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0鍚敤,1绂佺敤',
                             `delete_flag` tinyint(1) DEFAULT '0' COMMENT '0鏈垹闄わ紝1鍒犻櫎',
                             `path` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '#',
                             `visible` tinyint(1) DEFAULT '0',
                             `icon` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '#',
                             `component` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
                             `create_time` datetime NOT NULL COMMENT '创建时间',
                             `update_time` datetime NOT NULL COMMENT '更新时间',
                             `auth_type` varchar(50) DEFAULT NULL,
                             PRIMARY KEY (`aid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8 COMMENT='权限表';


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
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8 COMMENT='角色权限表';

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
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='上传日志表';


DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
                        `file_id` varchar(10) NOT NULL COMMENT 'id',
                        `file_name` varchar(50) DEFAULT NULL COMMENT '文件名',
                        `file_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件路径',
                        `size` bigint(11) DEFAULT '0' COMMENT '文件大小',
                        `user_id` int(11) DEFAULT NULL COMMENT '所属用户id',
                        `pid` varchar(10) DEFAULT '0' COMMENT '所属文件夹id',
                        `folder_type` tinyint(2) DEFAULT '0' COMMENT '0为文件，1为文件夹',
                        `delete_flag` tinyint(1) DEFAULT '0' COMMENT '0为正常，1为删除，2为回收站',
                        `file_md5` char(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件md5值',
                        `file_type` tinyint(2) DEFAULT NULL COMMENT '文件类型',
                        `file_category` tinyint(2) DEFAULT NULL COMMENT '文件大类型',
                        `file_status` tinyint(2) unsigned zerofill NOT NULL DEFAULT '02' COMMENT '文件合并状态',
                        `create_time` datetime NOT NULL COMMENT '创建时间',
                        `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                        PRIMARY KEY (`file_id`,`create_time`,`file_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='文件表';





DROP TABLE IF EXISTS `shares`;
CREATE TABLE `shares` (
                          `share_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '被分享文件或者文件夹的id',
                          `file_id` varchar(10) DEFAULT NULL,
                          `share_type` tinyint(1) NOT NULL COMMENT '被分享文件或者文件夹的类型 0为文件 1为文件夹',
                          `valid_type` tinyint(1) DEFAULT '0',
                          `share_key` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '分享码',
                          `share_user` int(11) NOT NULL COMMENT '分享的用户',
                          `share_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分享时间',
                          `expiration_date` datetime NOT NULL COMMENT '过期时间',
                          `download_count` int(11) DEFAULT '0' COMMENT '资源被下载次数',
                          PRIMARY KEY (`share_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `user` VALUES (1, 'root', '$2a$10$hiUy0sXHsvoO1eAB3HfZyej.bWufIliqfQGcckz.V5NvSIFzGYIgG', NULL, 0, 0, 0, 'http://localhost:9998/pan/static/bucket_user_1/avatar/2b05c85b-20ea-4922-bb2d-f54ff949490e.jpg', '唯一的超级管理员', NULL, 0, '2023-05-19 20:39:45', '2023-07-01 14:27:08');
INSERT INTO `user` VALUES (10, 'zhangsan', '$2a$10$gNzZEH.JU0iWx8tdCbxq5uGUpXbAVfRP2hB6pewYRtzz4QSe5KuXK', NULL, 0, 734372, 10737418240, '#', '测试账号1', NULL, 0, '2023-05-19 22:34:11', '2023-05-22 18:48:39');
INSERT INTO `user` VALUES (11, 'kunkun', '$2a$10$WrYkburpSZCllrP2QGIczeLsPp6gVT1lqYPCm4kBRrq6j8oLUi.YG', NULL, 0, 696540105, 10737418240, 'http://localhost:9998/pan/static/bucket_user_11/avatar/3f8d257e-ca3a-4ed5-835d-a4b70ee8955a.png', '测试账号2', NULL, 0, '2023-05-22 19:23:01', '2023-08-03 11:23:46');
INSERT INTO `user` VALUES (15, 'aikunkun', '$10$hiUy0sXHsvoO1eAB3HfZyej.bWufIliqfQGcckz.V5NvSIFzGYIgG', '12312@qq.com', 0, 0, 10737418240, '#', '粗纤维', NULL, 1, '2023-06-23 22:46:38', '2023-06-24 14:05:50');
INSERT INTO `user` VALUES (16, 'aikkk', '$2a$10$kQhUfPbrf4XjYgAx509m5O/NBr5dYKN332Wzn63ImT5j7GKlumVX.', '12312@qq.com', 0, 0, 0, '#', 'asddsa', NULL, 1, '2023-06-24 14:55:34', '2023-06-24 14:59:45');
INSERT INTO `user` VALUES (17, 'lisi', '$2a$10$5NjCai0/Pz3ULkSKLLpXUeIc8CsgNennLtMnECPILHj3kkW6FN4Gu', '123123@qq.com', 0, 0, 4785820700, '#', 'ceshi2', NULL, 0, '2023-07-01 11:13:06', '2023-07-01 11:13:06');
INSERT INTO `user` VALUES (19, 'lisiwowo', '$2a$10$IrVgubn8xI/aaCMjDMipE.35pftjcjEWl8JeCecIMz8HmODfalPSe', NULL, 0, 0, 10737418240, '#', NULL, NULL, 0, '2023-07-20 16:27:07', '2023-07-20 16:27:07');
INSERT INTO `user` VALUES (20, 'linglili', '$2a$10$y.epQ.K0N.MGTCSJHjvFrOc7IOXstHAv5lOrCqpvwBrJJ/v7br5Fe', NULL, 0, 0, 10737418240, '#', NULL, NULL, 0, '2023-07-30 11:20:28', '2023-07-30 11:20:28');


INSERT INTO `role` VALUES (1, 0, 'admin', '超级管理员', '2023-05-19 22:24:48', '2023-05-19 22:24:50');
INSERT INTO `role` VALUES (2, 0, 'user', '一般用户', '2023-05-19 22:25:12', '2023-06-29 22:16:47');
INSERT INTO `role` VALUES (7, 0, 'manager', '测试管理员', '2023-07-20 09:31:54', '2023-07-20 09:31:54');
INSERT INTO `role` VALUES (9, 0, 'role_teset', '萨达', '2023-07-20 09:34:26', '2023-07-20 09:34:26');


INSERT INTO `role_authority` VALUES (1, 1, 1);
INSERT INTO `role_authority` VALUES (2, 2, 6);








INSERT INTO `authority` VALUES (1, 0, '权限管理', 'authory', 0, 0, 'authority', 0, 'form', '/authority/index', '2023-06-29 18:50:56', '2023-06-29 18:50:58', 'C');
INSERT INTO `authority` VALUES (2, 0, '角色管理', 'role', 0, 0, 'role', 0, 'form', '/role/index', '2023-06-29 18:59:23', '2023-06-29 18:59:26', 'C');
INSERT INTO `authority` VALUES (3, 0, '用户管理', 'user', 0, 0, 'user', 0, 'form', '/user/index', '2023-06-29 19:00:02', '2023-06-29 19:00:06', 'C');
INSERT INTO `authority` VALUES (102, 1, '新增权限', 'pan:authority:save', 0, 0, NULL, 0, '#', NULL, '2023-04-22 10:06:51', '2023-04-22 10:06:55', 'F');
INSERT INTO `authority` VALUES (104, 1, '查询权限', 'pan:authority:list', 0, 0, NULL, 0, '#', NULL, '2023-04-22 10:07:53', '2023-04-22 10:07:56', 'F');
INSERT INTO `authority` VALUES (105, 1, '删除权限', 'pan:authority:delete', 0, 0, NULL, 0, '#', NULL, '2023-04-22 10:08:26', '2023-04-22 10:08:30', 'F');
INSERT INTO `authority` VALUES (112, 2, '新增角色', 'pan:role:save', 0, 0, NULL, 0, '#', NULL, '2023-04-22 10:16:41', '2023-04-22 10:16:44', 'F');
INSERT INTO `authority` VALUES (116, 2, '查询角色', 'pan:role:list', 0, 0, NULL, 0, '#', NULL, '2023-04-22 10:18:25', '2023-04-22 10:18:27', 'F');
INSERT INTO `authority` VALUES (117, 2, '删除角色', 'pan:role:delete', 0, 0, NULL, 0, '#', NULL, '2023-04-22 10:18:51', '2023-04-22 10:18:53', 'F');
INSERT INTO `authority` VALUES (118, 3, '新增用户', 'pan:user:save', 0, 0, NULL, 0, '#', NULL, '2023-04-22 10:19:18', '2023-04-22 10:19:22', 'F');
INSERT INTO `authority` VALUES (122, 3, '查询用户', 'pan:user:list', 0, 0, NULL, 0, '#', NULL, '2023-04-22 10:22:53', '2023-04-22 10:22:55', 'F');
INSERT INTO `authority` VALUES (123, 3, '删除用户', 'pan:user:delete', 0, 0, NULL, 0, '#', NULL, '2023-04-22 10:23:14', '2023-04-22 10:23:17', 'F');
INSERT INTO `authority` VALUES (126, 1, '修改权限', 'pan:authority:update', 0, 0, '#', 0, '#', NULL, '2023-07-20 09:26:43', '2023-07-20 09:26:43', 'F');
