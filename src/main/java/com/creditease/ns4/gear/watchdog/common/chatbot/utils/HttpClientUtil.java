package com.creditease.ns4.gear.watchdog.common.chatbot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil {


	/**
	 * @Description: 发送HTTPClient请求
	 * @author 201504015319
	 * @date 2015年5月21日下午8:25:03
	 * @param paraMap
	 */
	public static HashMap<String,String> doUmpHttp_HttpClient1(Map<String, String> paraMap, String url) {

		HashMap<String,String> resultMap = new HashMap<String, String>();

		// 创建默认的httpClient实例.
		String result = "";
		String retCode = ChatConstants.MSG_SUCCESS;
		resultMap.put("code",retCode);
		resultMap.put("message",result);
		// 创建默认的httpClient实例.    
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try{
	        // 创建httppost
			HttpPost httpPost = new HttpPost(url);
			StringEntity s = new StringEntity(JSONObject.toJSONString(paraMap));
			s.setContentType("application/json");//发送json数据需要设置contentType
			httpPost.setEntity(s);
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(10000)
					.setConnectTimeout(10000).setSocketTimeout(10000).build();
			httpPost.setConfig(requestConfig);
			
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try{
				int resStatu = response.getStatusLine().getStatusCode();
	
				if (resStatu == HttpStatus.SC_OK) {
					// get result data
					HttpEntity entity = response.getEntity();
					String reStr = EntityUtils.toString(entity);
					result = reStr;
				}else {
					retCode = ChatConstants.MSG_FAIL;
					result = "【机器人消息】发送消息失败"+resStatu;
					System.out.println("【机器人消息】发送失败");
				}
			} finally {  
				response.close();
				httpPost.releaseConnection();
            }
		}catch(Exception e){
			retCode = ChatConstants.MSG_FAIL;
			result = "【机器人消息】" + e.getMessage();
			e.printStackTrace();
		}finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultMap;

	}

	public static String doUmpHttp_HttpClient(Map<String, String> paraMap, String httpUrl) {

		String param = JSONObject.toJSONString(paraMap);
		String result = "";
		String retCode = ChatConstants.MSG_SUCCESS;
		// 建立连接
		BufferedReader responseReader= null;
		OutputStream outputStream = null;
		try {
			URL url = new URL(httpUrl);
			HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
			// 设置连接属性
			httpUrlConnection.setDoOutput(true);// 使用 URL 连接进行输出
			httpUrlConnection.setDoInput(true);// 使用 URL 连接进行输入
			httpUrlConnection.setUseCaches(false);// 忽略缓存
			httpUrlConnection.setRequestMethod("POST");// 设置URL请求方法
			httpUrlConnection.setRequestProperty("CHARSET", "UTF-8");
			// 设置连接主机服务器超时时间：15000毫秒
			httpUrlConnection.setConnectTimeout(15000);
			// 设置读取主机服务器返回数据超时时间：60000毫秒
			httpUrlConnection.setReadTimeout(60000);
			// 设置请求属性
			// 获得数据字节数据，请求数据流的编码，必须和下面服务器端处理请求流的编码一致
			byte[] requestStringBytes = param.getBytes("UTF-8");
			httpUrlConnection.setRequestProperty("Content-length", "" + requestStringBytes.length);
			httpUrlConnection.setRequestProperty("Content-Type", "application/json");
			httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
			httpUrlConnection.setRequestProperty("Charset", "UTF-8");

			// 建立输出流，并写入数据
			outputStream = httpUrlConnection.getOutputStream();
			outputStream.write(requestStringBytes);
			outputStream.close();
			// 获得响应状态
			int responseCode = httpUrlConnection.getResponseCode();

			String readLine = null;
			if (HttpURLConnection.HTTP_OK == responseCode) {// 连接成功
				// 当正确响应时处理数据
				StringBuffer sb = new StringBuffer();


				// 处理响应流，必须与服务器响应流输出的编码一致
				responseReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), "UTF-8"));
				while ((readLine = responseReader.readLine()) != null) {
					sb.append(readLine).append("\n");
				}
				responseReader.close();
				String res = sb.toString();
				// 处理返回的参数
				if (!"".equals(res)) {
					result = res;
				}
			} else {
				retCode = ChatConstants.MSG_FAIL;
				result = "【机器人消息】发送失败"+ responseCode;
			}


		} catch (Exception e) {
			retCode = ChatConstants.MSG_FAIL;
			result = e.getMessage();
		}finally {
			try {
				if (responseReader != null) {
					responseReader.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		HashMap<String,String> map = new HashMap<String, String>();
		map.put("code",retCode);
		map.put("message",result);
		return JSONObject.toJSONString(map);
	}

	
	/**
	 * @Description: 参数转换
	 * @author 201504015319
	 * @date 2015年5月21日下午8:24:50
	 * @param paraMap
	 * @return
	 */
	public static List<NameValuePair> queryString2NVPair(Map<String, String> paraMap) {

		List<NameValuePair> pairList = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : paraMap.entrySet()) {
			String value = entry.getValue();
			if (value != null) {
				pairList.add(new BasicNameValuePair(entry.getKey(), value));
			}
		}
		return pairList;
	}
}
