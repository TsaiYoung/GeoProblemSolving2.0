package cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Dao;

import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class fileUtils {
    public static String downloadFileFromURL(String url, String dirLocation, String fileName) {
        int byteread = 0;
        int bytesum = 0;
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)httpUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.connect();

            int type = conn.getResponseCode();
            if(type == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = conn.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(dirLocation + fileName);

                byte[] bytes = new byte[1024];
                while ((byteread = inputStream.read(bytes)) != -1) {
                    bytesum += byteread;
                    fileOutputStream.write(bytes, 0, byteread);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to download " + fileName + "!");
        }
        return "success";
    }

    public static boolean upZipFile(String zipPath, String folderPath) {

        boolean flag = false;

        File zipFile = new File(zipPath);
        File pathFile = new File(folderPath);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }

        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile, Charset.forName("gbk"));//防止中文目录，乱码

            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {

                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();

                InputStream in = zip.getInputStream(entry);

                //指定解压后的文件夹+当前zip文件的名称
                String outPath = (folderPath + zipEntryName).replace("/", File.separator);

                //判断路径是否存在,不存在则创建文件路径
                File file = new File(outPath.substring(0, outPath.lastIndexOf(File.separator)));
                if (!file.exists()) {
                    file.mkdirs();
                }

                //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                if (new File(outPath).isDirectory()) {
                    continue;
                }

                //保存文件路径信息
                System.out.println("unzip：" + outPath);
                OutputStream out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[2048];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
                in.close();
                out.close();
            }
            flag = true;
            zip.close();

        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        return flag;
    }

    public static void zipFile(ArrayList<String> filePaths, String zipFilePath) throws IOException {
        byte[] buf = new byte[1024];
        File zipFile = new File(zipFilePath);
        //zip文件不存在，则创建文件，用于压缩
        if (!zipFile.exists())
            zipFile.createNewFile();
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            for (int i = 0; i < filePaths.size(); i++) {
                String relativePath = filePaths.get(i);
                if (StringUtils.isEmpty(relativePath)) {
                    continue;
                }
                File sourceFile = new File(relativePath);//绝对路径找到file
                if (sourceFile == null || !sourceFile.exists()) {
                    continue;
                }

                FileInputStream fis = new FileInputStream(sourceFile);
                //保持目录结构
                zos.putNextEntry(new ZipEntry(sourceFile.getName()));

                //System.out.println("压缩当前文件："+sourceFile.getName());
                int len;
                while ((len = fis.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
            //System.out.println("压缩完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delFolder(String folderPath) {
        try {
            delAllFiles(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean delAllFiles(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFiles(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }
}
