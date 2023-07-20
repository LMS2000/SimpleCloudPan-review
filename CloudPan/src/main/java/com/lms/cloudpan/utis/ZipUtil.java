package com.lms.cloudpan.utis;

import com.lms.cloudpan.config.OssProperties;
import com.lms.cloudpan.entity.dao.File;
import com.lms.cloudpan.entity.vo.FolderVo;
import org.springframework.util.StreamUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    /**
     * 在本地创建临时压缩文件夹，然后返回下载地址
     *
     * @param folderDto
     * @param files
     * @param ossProperties
     * @param uid
     * @return
     * @throws IOException
     */
    public static String compressFilesInFolder(FolderVo folderVo, List<File> files, OssProperties ossProperties, Integer uid) throws IOException {

        // 声明压缩文件名，以及临时文件存储路径
        String folderName = folderVo.getFolderName();

        // /root/sad
        int lastIndex = folderName.lastIndexOf("/");

        String zipName = folderName.substring(lastIndex + 1) + ".zip";
        String tmpPath = ossProperties.getRootPath() + "/temp/" + uid + "/" + zipName;

        java.io.File tempDir = new java.io.File(tmpPath);
        if (!tempDir.getParentFile().exists()) {
            tempDir.getParentFile().mkdirs();
        }
        // 压缩文件
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmpPath))) {
            // 将文件夹内的所有子文件夹和文件打包压缩到 zip 文件中
            addFolderToZip("", folderVo, zos, files, ossProperties);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        // 将临时文件读取到字节数组中
//        byte[] bytes =  Files.readAllBytes(Paths.get(tmpPath));
//
//        // 对byte数组进行Base64编码
//        byte[] encodedBytes = Base64.getEncoder().encode(bytes);
        String zipUrl = ossProperties.getEndpoint() + "/static/temp/" + uid + "/" + zipName;
        return zipUrl;
    }

    // 将文件夹和其内部的所有子文件夹和文件打包压缩到 zip 中
    private static void addFolderToZip(String parentFolderName, FolderVo folder, ZipOutputStream zos, List<File> files, OssProperties ossProperties) throws IOException {

        // 将文件夹本身添加到 zip 中

        String folderName = folder.getFolderName();
        int lastIndex = folderName.lastIndexOf("/");
        if ("".equals(parentFolderName)) {
            folderName = folderName.substring(lastIndex + 1);
        } else {
            folderName = parentFolderName + folderName.substring(lastIndex + 1);
        }

        ZipEntry folderEntry = new ZipEntry(folderName + "/");
        zos.putNextEntry(folderEntry);
        zos.closeEntry();

        // 将文件夹内的子文件夹和文件添加到 zip 中
        for (FolderVo subFolder : folder.getChildrenList()) {
            addFolderToZip(folderName + "/", subFolder, zos, files, ossProperties);
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
            addFileToZip(folderName + "/", file, zos, ossProperties);
        }
    }

    // 将文件添加到 zip 中
    private static void addFileToZip(String parentFolderName, File file, ZipOutputStream zos, OssProperties ossProperties) throws IOException {
        String fileName = parentFolderName + file.getFileName();
        ZipEntry fileEntry = new ZipEntry(fileName);
        zos.putNextEntry(fileEntry);
        // http://localhost:8080/pan/static/bucket_user_4/root/test.png
        String fileUrl = file.getFileUrl();
        String[] split = fileUrl.split("static");
        String realPath = ossProperties.getRootPath() + split[1];
        try (InputStream is = new FileInputStream(realPath)) {
            StreamUtils.copy(is, zos);
        }
        zos.closeEntry();
    }


    public static List<FolderVo> getFolderNode(List<FolderVo> list){

        //获取全部的顶级文件夹
        //获取一级的分类
        List<FolderVo> resultList= list.stream()
                .filter(folderDto -> folderDto.getParentFolder()==0)
                .map(folderDto -> {
                    folderDto.setChildrenList(getChildrenList(folderDto,list));
                    return folderDto;
                })
                .collect(Collectors.toList());
        return resultList;
    }


    public static List<FolderVo> getChildrenList(FolderVo cur, List<FolderVo> allList) {
        List<FolderVo> res = allList.stream()
                .filter(categoryEntity -> categoryEntity.getParentFolder().equals(cur.getFolderId()))
                .map(folderDto -> {
                    folderDto.setChildrenList(getChildrenList(folderDto, allList));
                    return folderDto;
                }).collect(Collectors.toList());
        return res;
    }



    public static FolderVo getTierFolder(String pathName, List<FolderVo> list){
        List<FolderVo> folderNode = getFolderNode(list);
      return   getPath(pathName,folderNode.get(0));
    }

    //多叉树遍历问题，每个用户只有一个根路径，每个路径都有若干个子路径
    public static FolderVo getPath(String path, FolderVo folderNode) {
        if (folderNode == null)
            return null;
        if (folderNode.getFolderName().equals(path)) {
            return folderNode;
        }
        if (folderNode.getChildrenList() == null) return null;
        //根据path获取对应的节点
        for (FolderVo node : folderNode.getChildrenList()) {
            FolderVo path1 = getPath(path, node);
            if (path1 != null) return path1;
        }
        return null;
    }
}
