package cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.modelservice.utils;

import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	 public static String replaceBlank(String str){
		 String dest = "";
		 if(str != null){
			 Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			 Matcher m = p.matcher(str);
			 dest = m.replaceAll("");
		 }
		 return dest;
	 }
	/**
	 * 重命名，UUIU
	 *
	 * @param oleFileName
	 * @return
	 */
	public static String reloadFile(String oleFileName) {
		oleFileName = getFileName(oleFileName);
		if (StringUtils.isEmpty(oleFileName)) {
			return oleFileName;
		}
		//得到后缀
		if (oleFileName.indexOf(".") == -1) {
			//对于没有后缀的文件，直接返回重命名
			return  UUID.randomUUID().toString();
		}
		String[] arr = oleFileName.split("\\.");
		// 根据uuid重命名图片
		String fileName = UUID.randomUUID().toString() + "." + arr[arr.length - 1];

		return fileName;
	}

	private static boolean isEmpty(String str) {
		return str.length() > 0 ? true : false;
	}

	/**
	 * 把带路径的文件地址解析为真实文件名 /25h/upload/hc/1448089199416_06cc07bf-7606-4a81-9844-87d847f8740f.mp4 解析为 1448089199416_06cc07bf-7606-4a81-9844-87d847f8740f.mp4
	 *
	 * @param url
	 */
	public static String getFileName(final String url) {
		if (StringUtils.isEmpty(url)) {
			return url;
		}
		String newUrl = url;
		newUrl = newUrl.split("[?]")[0];
		String[] bb = newUrl.split("/");
		String fileName = bb[bb.length - 1];
		return fileName;
	}
}
