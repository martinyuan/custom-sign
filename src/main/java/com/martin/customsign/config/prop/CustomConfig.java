package com.martin.customsign.config.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties(prefix = "app.conf.customs")
public class CustomConfig {

	// 证书密码
	private String signPassword;

	// 待加签数据临时保存目录
	private String receivePath;

	// 加签处理后的保存目录
	private String donePath;

	// 加签后原始数据和证书、签名提交地址
	private String postUrl;

	public String getSignPassword() {
		return signPassword;
	}

	public void setSignPassword(String signPassword) {
		this.signPassword = signPassword;
	}

	public String getReceivePath() {
		return receivePath;
	}

	public void setReceivePath(String receivePath) {
		this.receivePath = receivePath;
	}

	public String getDonePath() {
		return donePath;
	}

	public void setDonePath(String donePath) {
		this.donePath = donePath;
	}

	public String getPostUrl() {
		return postUrl;
	}

	public void setPostUrl(String postUrl) {
		this.postUrl = postUrl;
	}

}
