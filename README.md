#执行海关数据签名，部署到台式机上(windows系统)，需要插U盾或读卡器及安装驱动

###编译项目
mvn clean package -Dmaven.test.skip=true

###第一步：
安装JDK，建议安装JDK1.8

###第二步：
创建如下目录，分别记录接受到的待加签数据，和保存已经加签的数据，还有日志：
d:/custom/receive d:/custom/done d:/logs/

###第三步:
将custom-sign-0.0.1.jar和start.bat文件复制到任意位置,但是要在同一个目录下

###第四步：
修改start.bat文件中的C:/develop/jdk1.8.0_181，替换成自己实际的JDK安装路径
双击start.bat文件完成java程序启动

###第四步骤
打开浏览器，访问如下地址，检查待签名数据并加签处理。
http://localhost:8888/Custom/sign