# SimpleCloudPan-review

重构后的简易网盘,优化文件删除，上传文件使用分片上传，分享功能，文件秒传功能等

# myCloudPan

简易网盘项目，可以上传下载文件修改删除文件,可以增删改查文件夹，也可以通过分享链接给别人下载文件。

![image-20230806172957354](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230806172957354.png)





![image-20230806173030970](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230806173030970.png)



![image-20230806173129686](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230806173129686.png)






![image-20230806173058046](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230806173058046.png)





![image-20230806173157889](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_areaimage-20230806173157889.png)




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


- 用户注册：前端传递账号密码给后端，后端首先判断用户名是否存在，然后加密密码，记录用户信息， 然后分配配额(都是10G)
- 用户登录: 用户发送账号密码和图片校验码，后端使用spring security的登录拦截器`TokenLoginAuthFilter`  接收账号密码，经过校验后，生成token，这里的token的荷载中存储的是redis缓存中用户登录信息的key。而缓存中存储了用户的登陆信息，之后就不需要去数据库拿取，并且在用户携带token访问的时候后端使用鉴权拦截器`DaoTokenAuthenticationFilter` 进行鉴权，如果通过后会将用户的token刷新，也就是token续期操作，比如说用户的登录信息在缓存上保存了30分钟，而用户携带token，后端发现用户的token过期时间小于20分钟，就会动态的刷新。这样用户只要使用系统进行操作就会不断的刷新，这样对于用户来说是比较友好的。
- 上传文件：
  前端根据上传文件的大小分片成好几片，然后顺序发送给后端，如果是第一次分片上传文件，判断文件的md5值是否在数据库中已经存在了，如果是的话就直接将已存在的文件路径插入文件信息，直接返回上传成功。否则就将文件的分片临时存放在系统约定好的位置

![时序图(1)](https://service-edu-2000.oss-cn-hangzhou.aliyuncs.com/pic_go_area%E6%97%B6%E5%BA%8F%E5%9B%BE(1).png)

​    


- 下载文件：
  前端发送需要下载的文件id,后端根据id生成随机字符串code，然后将下载信息缓存到redis，然后前端跳转下载地址

  

- 分享功能：根据用户的分享设置（天数类型，分享码）得到一个分享链接，其他用户可以使用这个分享链接访问分享地址，然后输入分享码可以提取文件

- 文件删除的定时任务： 定时扫描有删除标记的资源进行删除



