package cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.modelservice.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class CommonUtils {

	
	/**
	 * 判断给定字符串是否是GUID
	 * @param stateValue
	 * @return
	 */
	public static boolean IsGUID(String stateValue) {
		if(stateValue.length() == 36){
			String[] tempStr = stateValue.split("-");
			for(String temp : tempStr){
				if(temp.length() == 0){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * 获取给定文件的MD5值
	 * @param filename
	 * @return
	 */
	public static String getFileMd5(String filename) {
		File file = new File(filename);
		try(FileInputStream fileInputStream = new FileInputStream(file)){
			return DigestUtils.md5Hex(fileInputStream);
		}catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static String encryption(String buffer) throws UnsupportedEncodingException {
		byte[] temp1 = buffer.getBytes("UTF-8");
		Base64 base64 = new Base64();
		//hex 加密
        String encodedText = Hex.encodeHexString(temp1);
        //base64 加密
        byte[] temp2 = encodedText.getBytes("UTF-8");
        encodedText = base64.encodeAsString(temp2);
        //拼接字符串
        encodedText = RandomStringUtils.randomAlphabetic(5) + encodedText + RandomStringUtils.randomAlphabetic(5);
        byte[] temp3 = encodedText.getBytes("UTF-8");
        encodedText = base64.encodeAsString(temp3);

        return encodedText;
	}
	
	
	 public static String decryption(String buffer) throws UnsupportedEncodingException, DecoderException {
	     Base64 base64 = new Base64();
	     //base64 解密
	     String decodedText = new String(base64.decode(buffer), "UTF-8");
	     //裁剪字符串，去除该字符串前面和后面五个字符
	     decodedText = decodedText.substring(5,decodedText.length()-5);
	     decodedText = new String(base64.decode(decodedText), "UTF-8");
	     String result = new String(Hex.decodeHex(decodedText), "UTF-8");
	     return result;
	}
	
}
