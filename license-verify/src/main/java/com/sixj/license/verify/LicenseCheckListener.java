package com.sixj.license.verify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * @author sixiaojie
 * @date 2021-05-25-15:42
 */
@Slf4j
public class LicenseCheckListener implements CommandLineRunner {
    /**
     * 证书subject
     */
    private String subject = "pushi-kn-graph";

    /**
     * 公钥别称
     */
    private String publicAlias = "HUOMIAO";

    /**
     * 访问公钥库的密码
     */
    private String storePass ;

    /**
     * 证书生成路径
     */
    private String licensePath;

    /**
     * 密钥库存储路径
     */
    private String publicKeysStorePath;


    public void install(String passWorld) throws Exception {
        this.storePass =passWorld;
        run();
    }
    @Override
    public void run(String... args) throws Exception {
        String dirPath = ResourceUtils.getURL("license").getPath()+"/";
        File dir=new File(dirPath);
        if (!dir.exists()) {//判断文件目录的存在
            boolean mkdir = dir.mkdir();
            if (!mkdir){
                log.info("{}证书目录不存在已自动创建失败！",dirPath);
            }
            log.info("{}证书目录不存在已自动创建！",dirPath);
        }
        this.licensePath = dirPath+"license.lic";
        this.publicKeysStorePath = dirPath+"publicCerts.keystore";


        if(!StringUtils.isEmpty(licensePath)){
            log.info("++++++++ 开始安装证书 ++++++++");

            LicenseVerifyParam param = new LicenseVerifyParam();
            param.setSubject(subject);
            param.setPublicAlias(publicAlias);
            param.setStorePass(storePass);
            param.setLicensePath(licensePath);
            param.setPublicKeysStorePath(publicKeysStorePath);

            LicenseVerify licenseVerify = new LicenseVerify();
            //安装证书
            licenseVerify.install(param);

            log.info("++++++++ 证书安装结束 ++++++++");
        }else {
            throw new RuntimeException("++++++++ 未配置License证书 ++++++++");
        }
    }
}
