package com.martin.customsign.domain.dto;

public class ReceiveFormbean {
	// 会话ID，保持唯一
	private String sessionID;
	// 原始JSON数据
	private String json;
	// 需要加签的数据
	private String signStr;

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getSignStr() {
		return signStr;
	}

	public void setSignStr(String signStr) {
		this.signStr = signStr;
	}

}
