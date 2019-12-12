package tool.demo.javapack.javaweb;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName: ChangeLogUtil
 * @Description:从changeLog生成增量包工具
 * @author: lin-jianjun
 * @date: 2018年4月27日 上午12:11:24
 * @version:V1.0
 */
public class ChangeLogUtil {

    // 指向工程所在的根目录,以/结尾
    static String WEBAPPS = "E:/work/PROJECT_GPMANAGE/02branches/4.2.0.0/sxcgweb/out/artifacts/";
    //	// 工程名
//	static String PROJECT = "gpms";
    // 代码编译后目录
//	static String DEPLOY_DIR = "G:/work/BJworkspace/01trunk/agentlib/out/artifacts";// class类目录
    // 指向changeLog文件
    static String CHANGE_LOG_FILE_NAME = "E:\\project\\desktop\\changelog.txt";
    static String CUTSVN = "/02code/02branches/4.2.0.0/";//不同SVN路径下不需要的前缀
    // 增量包目录，后面将会自动创建以最新SVN版本号作为名称的文件夹
    // 注意变更标签版本号（这里用日期）
    static String OTARGET = "C:/Users/12148/Desktop/bug修改";// 补丁包存放的地址

    static String TARGET = OTARGET;// 补丁包存放的地址

    static String PAGENAME = "补丁包";// 补丁包命名

    static {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        // TARGET = TARGET+sdf.format(new Date()) + "_" + PAGENAME +
        // "/"+PROJECT+"/";//补丁包路径及命名
        // 读取svn中的版本号
        File file = new File(CHANGE_LOG_FILE_NAME);
        StringBuilder sb = new StringBuilder();
        try {
            // 解決changeLog中文乱码问题
            InputStreamReader read = new InputStreamReader(new FileInputStream(file), "GBK");
            BufferedReader br = new BufferedReader(read);
            String line = null;
            String svnVersion = "";
            String tag = "";
            while ((line = br.readLine()) != null) {
                if (line.contains(" | ")) {
                    String[] lineArray = line.split(" | ");
                    if ("".equals(svnVersion)) {
                        svnVersion += "_" + lineArray[0];// 只取最大的版本号
                    }
                    sb.append(line);
                    sb.append("\r\n");
                } else if ("1".equals(tag)) {
                    sb.append(line);
                    sb.append("\r\n----------------\r\n");
                }
                if ("".equals(line)) {
                    tag = "1";
                } else {
                    tag = "0";
                }
            }
            OTARGET = TARGET + sdf.format(new Date()) + "_" + PAGENAME + svnVersion + "/";
            TARGET = OTARGET + "/";// 补丁包路径及命名
            br.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        File f = new File(TARGET);
        if (f.exists()) {
            try {
                del(TARGET);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        f.mkdirs();

        // 从svn读取更新说明，并写入update.txt文件
//		File logFile = new File(OTARGET + "update.txt");
//		try {
//			BufferedWriter bw = new BufferedWriter(new FileWriter(logFile));
//			bw.write(sb.toString());
//			bw.flush();
//			bw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

    }

    public static void main(String[] args) throws Exception {

        int successfile=0;
        int failfile=0;
        int hanghao=0;
        File file = new File(CHANGE_LOG_FILE_NAME);// changelog的路径（在工程目录下！）
        String line = null;
        // 解決changeLog中文乱码问题
        InputStreamReader read = new InputStreamReader(new FileInputStream(file), "GBK");
        BufferedReader br = new BufferedReader(read);
        while ((line = br.readLine()) != null) {
            hanghao++;

//			G:\work\BJworkspace\01trunk\agentlib\out\artifacts\gpms_war_exploded\WEB-INF\classes\com\mk\business\dailyplan\action
//			if (line.contains(PROJECT)) {// 单行全文搜索
            // branches/gpms/是指SVN路径参与增量包的跟目录之前的路径
            // 例如：M /北京市政府采购中心项目/trunk/gpms/src/main/webapp/web/bgpc/js/project/*****.js
            // gpms/目录下的代码才参与打包工作，就设置为trunk/gpms/

            String filePath = line.replaceAll(".*?" + "branches/gpms/" + "(.*?)", "$1").replaceAll("\\.java",
                    ".class").replaceAll(CUTSVN,"");
            String[] str=filePath.split("/");
            filePath=filePath.replaceAll(str[0],str[0]+"_war_exploded").replaceAll("-module","");
//				.replaceAll("gpms","gpms_war_exploded").replaceAll("-module","");

            String source = "", target = "";
            if (filePath.endsWith(".class")) {
                // java类复制
                filePath = filePath.replaceFirst("src", "WEB-INF/classes");
                source = WEBAPPS + filePath;
                target = TARGET  + filePath.replaceFirst("_war_exploded", "");
                //前端
            } else if (filePath.endsWith(".js")||filePath.endsWith(".jsp")||filePath.endsWith(".css")) {
                // web文件，如html、js
                filePath = filePath.replaceFirst("WebContent/", "");
                source = WEBAPPS + filePath;
                target = TARGET + filePath.replaceFirst("_war_exploded", "");
                //图片
            } else if (filePath.endsWith(".jpg")||filePath.endsWith(".png")||filePath.endsWith(".gif")) {
                // web文件，如html、js
                filePath = filePath.replaceFirst("WebContent/", "");
                source = WEBAPPS + filePath;
                target = TARGET + filePath.replaceFirst("_war_exploded", "");
                //后端
            }else if (filePath.endsWith(".xml")||filePath.endsWith(".ftl")) {
                // 配置文件，如xml、properties
                filePath = filePath.replaceFirst("src", "WEB-INF/classes");
                source = WEBAPPS + filePath;
                target = TARGET + filePath.replaceAll("_war_exploded", "");
            }
//				else {
//					// 其他，例如更新文档，sql脚本等非工程目录文件
//					source = WEBAPPS + filePath;
//					target = OTARGET + filePath;
//				}

            if (target.contains(".")) {// 复制文件，文件夹不复制
                File sourceFile = new File(source);
                if (sourceFile.exists()) {
                    try {
                        copyFile(new File(source), new File(target));
                        successfile++;
                        System.out.println(target + "----复制成功！"+successfile+"行号："+hanghao);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    failfile++;
                    System.out.println(line + target + "----复制失败！文件不存在，路径有误！"+failfile+"行号："+hanghao);
                }
            } else {
                failfile++;
                System.out.println(line + target + "----复制失败！非文件类型！"+failfile+"行号："+hanghao);
            }
//			}
        }
        br.close();

    }

    // 复制文件
    public static void copyFile(File sourceFile, File targetFile) throws Exception {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

            if (!targetFile.exists()) {
                try {
                    targetFile.getParentFile().mkdirs();
                    targetFile.createNewFile();
                } catch (Exception e) {
                    System.out.println(targetFile.getAbsolutePath());
                    throw e;
                }
            }
            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            // BufferedOutputStream bos = new BufferedOutputStream
            // (new FileOutputStream(new File(targetFile.toString() + “\” +
            // targetFile[i].getName())));

            // 缓冲数组
            byte[] b = new byte[256];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }

    /**
     *
     * @param filepath
     * @throws IOException
     */
    public static void del(String filepath) throws IOException {
        File f = new File(filepath);// 定义文件路径
        if (f.exists() && f.isDirectory()) {// 判断是文件还是目录
            if (f.listFiles().length == 0) {// 若目录下没有文件则直接删除
                f.delete();
            } else {// 若有则把文件放进数组，并判断是否有下级目录
                File delFile[] = f.listFiles();
                int i = f.listFiles().length;
                for (int j = 0; j < i; j++) {
                    if (delFile[j].isDirectory()) {
                        del(delFile[j].getAbsolutePath());// 递归调用del方法并取得子目录路径
                    }
                    delFile[j].delete();// 删除文件
                }
            }
        }
    }
}
