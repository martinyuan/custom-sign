package com.martin.customsign.domain.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.martin.customsign.config.prop.CustomConfig;
import com.martin.customsign.domain.dto.PostFormbean;
import com.martin.customsign.domain.dto.ReceiveFormbean;

@Controller
@RequestMapping("/Custom")
public class CustomsSignController {

	private static final Logger LOG = LoggerFactory.getLogger(CustomsSignController.class);

	private static final String EXT_FILE_NAME = "json";
	private static final String SIGN_EXT_FILE_NAME = "sign";

	private static final String UTF_8 = "UTF-8";

	@Autowired
	private CustomConfig customConfig;

	/**
	 * 提交需要加签的数据
	 * 
	 * @param data
	 * @param model
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("deprecation")
	@PostMapping("/receive")
	@ResponseBody
	public String receive(@RequestBody ReceiveFormbean formbean) throws FileNotFoundException, IOException {
		String receivePath = customConfig.getReceivePath();
		File path = new File(receivePath);
		if (!path.exists()) {
			path.mkdirs();
		}
		String json = formbean.getJson();
		String signStr = formbean.getSignStr();
		String sessionID = formbean.getSessionID();
		LOG.info("call receive method. json:[], sessionID:[{}]", json, sessionID);
		// 写SIGN
		String file = FilenameUtils.concat(receivePath, sessionID + "." + SIGN_EXT_FILE_NAME);
		FileOutputStream output = new FileOutputStream(new File(file));
		IOUtils.write(signStr, output, "UTF-8");
		IOUtils.closeQuietly(output);
		// 写JSON
		file = FilenameUtils.concat(receivePath, sessionID + "." + EXT_FILE_NAME);
		output = new FileOutputStream(new File(file));
		IOUtils.write(json, output, "UTF-8");
		IOUtils.closeQuietly(output);
		return "OK";
	}

	/**
	 * 被轮询调用，读取加签数据。页面完成加签。
	 * 
	 * @param model
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@GetMapping("/sign")
	public String sign(Model model) throws FileNotFoundException, IOException {
		LOG.info("sign is running....");
		String receivePath = customConfig.getReceivePath();
		File path = new File(receivePath);
		if (!path.exists()) {
			return "sign/sign";
		}
		Collection<File> files = FileUtils.listFiles(new File(receivePath), new String[] { SIGN_EXT_FILE_NAME }, false);
		if (CollectionUtils.isEmpty(files)) {

			return "sign/sign";
		}
		File signFile = files.iterator().next();
		files = FileUtils.listFiles(new File(receivePath), new String[] { EXT_FILE_NAME }, false);
		if (CollectionUtils.isEmpty(files)) {
			return "sign/sign";
		}
		File jsonFile = files.iterator().next();
		// 读取JSON数据
		String signStr = FileUtils.readFileToString(signFile, "UTF-8");
		String json = FileUtils.readFileToString(jsonFile, "UTF-8");
		// 移动文件
		String donePath = customConfig.getDonePath();
		FileUtils.moveToDirectory(signFile, new File(donePath), false);
		FileUtils.moveToDirectory(jsonFile, new File(donePath), false);
		// 将数据返回前台
		String password = customConfig.getSignPassword();

		LOG.info("call sign method. signStr:[{}],json:[{}] password:[{}]", signStr, json, password);

		model.addAttribute("password", password);
		model.addAttribute("signStr", signStr);
		model.addAttribute("json", json);
		return "sign/sign";
	}

	/**
	 * 数据加签完毕后，将原始JSON数据，证书，签名返回给业务系统接口。
	 * 
	 * @param formbean
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@PostMapping("/post")
	@ResponseBody
	public String post(@RequestBody PostFormbean formbean) throws FileNotFoundException, IOException {
		String json = formbean.getJson();
		String sign = formbean.getSign();
		String cerno = formbean.getCerno();

		String postUrl = customConfig.getPostUrl();
		JSONObject obj = new JSONObject();
		obj.put("json", json);
		obj.put("sign", sign);
		obj.put("cerno", cerno);

		String response = apiPostJson(postUrl, obj.toJSONString());
		Assert.isTrue(StringUtils.equals(response, "OK"), "post data to auto custom server error.");
		return "OK";
	}

	@SuppressWarnings("deprecation")
	protected String apiPostJson(String url, String jsonString) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String json = "";
		try {
			HttpPost httpPost = new HttpPost(url);
			if (StringUtils.isNotBlank(jsonString)) {
				StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
				httpPost.setEntity(entity);
			}
			LOG.info("REQ url[{}] JSON:[{}]", url, jsonString);
			response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				json = EntityUtils.toString(response.getEntity(), UTF_8);
				LOG.info("RESP JSON:" + json);
			} else {
				LOG.error("status is error. url:[{}], status:[{}]", url, response.getStatusLine().getStatusCode());
				String msg = EntityUtils.toString(response.getEntity(), UTF_8);
				LOG.error(msg);
			}
		} catch (Exception e) {
			LOG.error("", e);
		} finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(httpClient);
		}
		return json;
	}

}
