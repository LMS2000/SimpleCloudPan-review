
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
   `finger_print` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件MD5',
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



INSERT INTO `role` VALUES (1, 0, 'admin', '超级管理员', '2023-5-19 22:24:48', '2023-5-19 22:24:50');
INSERT INTO `role` VALUES (2, 0, 'user', '一般用户', '2023-5-19 22:25:12', '2023-5-19 22:25:14');



INSERT INTO `role_authority` VALUES (1, 1, 1);
INSERT INTO `role_authority` VALUES (2, 2, 6);




INSERT INTO `user` VALUES (1, 'root', '$2a$10$hiUy0sXHsvoO1eAB3HfZyej.bWufIliqfQGcckz.V5NvSIFzGYIgG', NULL, 0, 0, 0, NULL, '2023-5-19 20:39:45', '2023-5-19 20:39:52');
INSERT INTO `user` VALUES (10, 'zhangsan', '$2a$10$gNzZEH.JU0iWx8tdCbxq5uGUpXbAVfRP2hB6pewYRtzz4QSe5KuXK', NULL, 0, 734372, 10485760, NULL, '2023-5-19 22:34:11', '2023-5-22 18:48:39');
INSERT INTO `user` VALUES (11, 'kunkun', '$2a$10$E2Y.qspbIVB4MyUicxFWKOvDphDXsWIT7m.W4mxkTU1GUR.AduGYC', NULL, 0, 132602, 10485760, 'http://localhost:9998/pan/static/bucket_user_11/avatar/3f8d257e-ca3a-4ed5-835d-a4b70ee8955a.png', '2023-5-22 19:23:01', '2023-5-26 14:26:51');





INSERT INTO `authority` VALUES (1, 0, 0, '*:*:*', '全部权限', '2023-4-21 14:58:39', '2023-4-21 14:58:42');
INSERT INTO `authority` VALUES (2, 0, 0, 'pan:authority:save', '新增权限', '2023-4-22 10:06:51', '2023-4-22 10:06:55');
INSERT INTO `authority` VALUES (3, 0, 0, 'pan:authority:get', '根据id查询权限', '2023-4-22 10:07:21', '2023-4-22 10:07:24');
INSERT INTO `authority` VALUES (4, 0, 0, 'pan:authority:list', '分页批量查询权限', '2023-4-22 10:07:53', '2023-4-22 10:07:56');
INSERT INTO `authority` VALUES (5, 0, 0, 'pan:authority:delete', '删除权限', '2023-4-22 10:08:26', '2023-4-22 10:08:30');
INSERT INTO `authority` VALUES (6, 0, 0, 'pan:operationLog:get', '根据id查询操作日志', '2023-4-22 10:09:09', '2023-4-22 10:09:12');
INSERT INTO `authority` VALUES (7, 0, 0, 'pan:operationLog:list', '分页批量查询操作日志', '2023-4-22 10:09:43', '2023-4-22 10:09:45');
INSERT INTO `authority` VALUES (8, 0, 0, 'pan:operationLog:delete', '删除操作日志', '2023-4-22 10:11:26', '2023-4-22 10:11:29');
INSERT INTO `authority` VALUES (9, 0, 0, 'pan:roleAuthority:get', '获取角色权限', '2023-4-22 10:12:22', '2023-4-22 10:12:25');
INSERT INTO `authority` VALUES (10, 0, 0, 'pan:roleAuthority:releaseAuthorityToRole', '赋予角色权限', '2023-4-22 10:13:25', '2023-4-22 10:13:28');
INSERT INTO `authority` VALUES (11, 0, 0, 'pan:roleAuthority:revokeAuthorityFromRole', '收回角色权限', '2023-4-22 10:14:03', '2023-4-22 10:14:05');
INSERT INTO `authority` VALUES (12, 0, 0, 'pan:role:save', '新增角色', '2023-4-22 10:16:41', '2023-4-22 10:16:44');
INSERT INTO `authority` VALUES (13, 0, 0, 'pan:role:get', '根据id查询角色', '2023-4-22 10:17:11', '2023-4-22 10:17:14');
INSERT INTO `authority` VALUES (14, 0, 0, 'pan:role:enable', '启动角色', '2023-4-22 10:17:32', '2023-4-22 10:17:36');
INSERT INTO `authority` VALUES (15, 0, 0, 'pan:role:disable', '禁用角色', '2023-4-22 10:18:00', '2023-4-22 10:18:03');
INSERT INTO `authority` VALUES (16, 0, 0, 'pan:role:list', '分页批量查询角色', '2023-4-22 10:18:25', '2023-4-22 10:18:27');
INSERT INTO `authority` VALUES (17, 0, 0, 'pan:role:delete', '删除角色', '2023-4-22 10:18:51', '2023-4-22 10:18:53');
INSERT INTO `authority` VALUES (18, 0, 0, 'pan:user:save', '新增用户', '2023-4-22 10:19:18', '2023-4-22 10:19:22');
INSERT INTO `authority` VALUES (19, 0, 0, 'pan:user:get', '根据id查询用户', '2023-4-22 10:21:13', '2023-4-22 10:21:16');
INSERT INTO `authority` VALUES (20, 0, 0, 'pan:user:enable', '启用用户', '2023-4-22 10:22:08', '2023-4-22 10:22:10');
INSERT INTO `authority` VALUES (21, 0, 0, 'pan:user:disable', '禁用用户', '2023-4-22 10:22:29', '2023-4-22 10:22:31');
INSERT INTO `authority` VALUES (22, 0, 0, 'pan:user:list', '分页批量查询用户', '2023-4-22 10:22:53', '2023-4-22 10:22:55');
INSERT INTO `authority` VALUES (23, 0, 0, 'pan:user:delete', '删除用户', '2023-4-22 10:23:14', '2023-4-22 10:23:17');
INSERT INTO `authority` VALUES (24, 0, 0, 'pan:userRole:get', '获取用户角色', '2023-4-22 10:23:41', '2023-4-22 10:23:43');
INSERT INTO `authority` VALUES (25, 0, 0, 'pan:userRole:releaseRoleToUser', '赋予用户角色', '2023-4-22 10:24:07', '2023-4-22 10:24:09');
INSERT INTO `authority` VALUES (26, 0, 0, 'pan:userRole:revokeRoleFromUser', '回收用户角色', '2023-4-22 10:24:26', '2023-4-22 10:24:28');
