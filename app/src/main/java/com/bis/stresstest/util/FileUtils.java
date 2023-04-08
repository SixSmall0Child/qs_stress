package com.bis.stresstest.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.bis.stresstest.app.MyApplication;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


import static com.bis.stresstest.util.Config.FileAssets.OUTSIDE_FILE;


/**
 * Created by shenhua on 1/17/2017.
 * Email shenhuanet@126.com
 */
public class FileUtils {

    private static FileUtils instance;
    private static final int SUCCESS = 1;
    private static final int FAILED = 0;
    private Context context;
    private FileOperateCallback callback;
    private volatile boolean isSuccess;
    private String errorStr;

    public static FileUtils getInstance(Context context) {
        if (instance == null) {
            instance = new FileUtils(context);
        }
        return instance;
    }

    private FileUtils(Context context) {
        this.context = context;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (callback != null) {
                if (msg.what == SUCCESS) {
                    callback.onSuccess();
                }
                if (msg.what == FAILED) {
                    callback.onFailed(msg.obj.toString());
                }
            }
        }
    };

    public FileUtils copyAssetsToSD(final String srcPath, final String sdPath) {
        new Thread(() -> {
            copyAssetsToDst(context, srcPath, sdPath);
            if (isSuccess) {
                handler.obtainMessage(SUCCESS).sendToTarget();
            } else {
                handler.obtainMessage(FAILED, errorStr).sendToTarget();
            }
        }).start();
        return this;
    }


    public void setFileOperateCallback(FileOperateCallback callback) {
        this.callback = callback;
    }

    private void copyAssetsToDst(Context context, String srcPath, String dstPath) {
        try {
            String fileNames[] = context.getAssets().list(srcPath);
            if (fileNames.length > 0) {
                File file = new File(dstPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                for (String fileName : fileNames) {
                    if (!srcPath.equals("")) { // assets 文件夹下的目录
                        copyAssetsToDst(context, srcPath + File.separator + fileName, dstPath + File.separator + fileName);
                    } else { // assets 文件夹
                        copyAssetsToDst(context, fileName, dstPath + File.separator + fileName);
                    }
                }
            } else {
                File outFile = new File(dstPath);
                InputStream is = context.getAssets().open(srcPath);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            errorStr = e.getMessage();
            isSuccess = false;
            Log.i("TAG", srcPath + "========" + e.toString());
        }
    }

    public interface FileOperateCallback {
        void onSuccess();

        void onFailed(String error);
    }


    /**
     * @return
     */
    public static String getIPAddress() {
        /**
         * 获取ip地址
         * @return
         */
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();

                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)&&ni.getName().equals("eth0")) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i(Config.TAG, "SocketException:"+e);
            e.printStackTrace();
        }
        return hostIp;
    }


    public void splitIP() {

    }


    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param delFile 要删除的文件夹或文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(Context context, String delFile) {
        Logger.i("删除文件，文件名：" + delFile);
        File file = new File(delFile);
        if (!file.exists()) {
            Log.i(TAG, "没有该文件：" + delFile);
            return false;
        } else {
            if (file.isFile()) {
                return deleteSingleFile(context, delFile);
            } else {
                return deleteDirectory(context, delFile);
            }
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteSingleFile(Context context, String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.i(Config.TAG, "删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                Log.i(Config.TAG, "删除单个文件" + filePath$Name + "失败！");
                return false;
            }
        } else {
            Log.i(Config.TAG, "删除单个文件失败：" + filePath$Name + "不存在！");
            return false;
        }
    }

    /**
     * @param fileName 文件名字
     * @return 文件的地址，默认在recorder文件夹下
     */
    public static String filePath(String fileName) {


        String state = Environment.getExternalStorageState();
        String filePath;
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/" + fileName;
        } else {
            filePath = MyApplication.getContext().getCacheDir().getAbsolutePath() + "/record/" + fileName;
        }
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdir();
        }
        return filePath;
    }



    /**
     * @return 文件的地址，默认在recorder文件夹下
     */
    public static String filePath() {
        String filePath = null;
        Config.filePathName = filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Screen/" + DateUtils.getData() + ".mp4";
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdir();
        }
        return filePath;
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(Context context, String filePath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            Log.i(Config.TAG, "删除目录失败：" + filePath + "不存在！");
            return false;
        }
        boolean flag = true;
        // // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // 删除子文件
            if (file.isFile()) {
                flag = deleteSingleFile(context, file.getAbsolutePath());
                if (!flag) {

                }
                break;
            }
            // 删除子目录
            else if (file.isDirectory()) {
                flag = deleteDirectory(context, file.getAbsolutePath());
                if (!flag) {

                }
                break;
            }
        }
        if (!flag) {
            Log.i(Config.TAG, "删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            Log.e("TAG", "Copy_Delete.deleteDirectory: 删除目录" + filePath + "成功！");
            return true;
        } else {
            Log.i(Config.TAG, "删除目录：" + filePath + "失败！");
            return false;
        }
    }

    /**
     * 判断文件是否存在
     *
     * @return
     */
    public static boolean fileIsExists(String fileName) {
        try {
            File f = new File(MyApplication.getContext().getFilesDir().getAbsolutePath() + "/chaquopy/AssetFinder/app/" + fileName);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * 将输入流写入文件
     *
     * @param inputString
     */
    public static void writeFile(InputStream inputString , String fileName) {
        File file = new File(fileName);
        Logger.d(file.toString());
        if (file.exists()) {
            file.mkdir();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);

            byte[] b = new byte[1024];

            int len;
            while ((len = inputString.read(b)) != -1) {
                fos.write(b, 0, len);
            }
            inputString.close();
            fos.close();

        } catch (FileNotFoundException e) {

        } catch (IOException e) {
        }

    }


    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    public static final String TAG = "SNMP";

    /**
     * 获取sdcard目录
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        }
        return sdDir.toString();
    }

    public static String mkdirs(String path) {
        String sdcard = getSDPath();
        if (path.indexOf(getSDPath()) == -1) {
            path = sdcard + (path.indexOf("/") == 0 ? "" : "/") + path;
        }
        File destDir = new File(path);
        if (!destDir.exists()) {
            path = makedir(path);
            if (path == null) {
                return null;
            }
        }
        return path;
    }

    private static String makedir(String path) {
        String sdPath = getSDPath();
        String[] dirs = path.replace(sdPath, "").split("/");
        StringBuffer filePath = new StringBuffer(sdPath);
        for (String dir : dirs) {
            if (!"".equals(dir) && !dir.equals(sdPath)) {
                filePath.append("/").append(dir);
                File destDir = new File(filePath.toString());
                if (!destDir.exists()) {
                    boolean b = destDir.mkdirs();
                    if (!b) {
                        return null;
                    }
                }
            }
        }
        return filePath.toString();
    }


    /**
     * 写入数据到SD中
     *
     * @param text
     */
    public static void writeTxt(String text, String fileName) {
        //新建文件夹
        File sdCardDir = new File(OUTSIDE_FILE);
        if (!sdCardDir.exists()) {
            if (!sdCardDir.mkdirs()) {
                try {
                    sdCardDir.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            //新建文件
            File saveFile = new File(sdCardDir, fileName);
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            // FileOutputStream outStream =null;
            //outStream = new FileOutputStream(saveFile);
            final FileOutputStream outStream = new FileOutputStream(saveFile);
            outStream.write(text.getBytes());

            /* outStream.write(text.getBytes("utf-8"));*/
            outStream.close();
            //outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 写入数据到SD中
     *
     * @param text
     */
    public static void writeTextUtf8(String text, String fileName) {
        //新建文件夹
        File sdCardDir = new File(OUTSIDE_FILE);
        if (!sdCardDir.exists()) {
            if (!sdCardDir.mkdirs()) {
                try {
                    sdCardDir.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            //新建文件
            File saveFile = new File(sdCardDir, fileName);
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            // FileOutputStream outStream =null;
            //outStream = new FileOutputStream(saveFile);
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(new FileOutputStream(saveFile), "utf-8");
            oStreamWriter.write(text);
            oStreamWriter.close();
            //outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入文件到sdcard文件夹中
     *
     * @param filename
     * @param content
     */
    public static void writeStopRun(String filename, String content) {
        File file = new File("/sdcard/" + filename + ".txt");
        if (!file.exists()) {
            file.mkdir();
        }
        //获取文件在内存卡中files目录下的路径
//        filename = Environment.getExternalStorageDirectory().getAbsolutePath()+ filename;
        filename = "/sdcard/" + filename + ".txt";
        //打开文件输出流
        FileOutputStream outputStream = null;
        Log.i(Config.TAG, filename);
        try {
            outputStream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("TAG", e.toString());
        }
        //写数据到文件中
        try {
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", e.toString());
        }
    }

    /**
     * 写Internal Card文件
     *
     * @param filename
     * @param content
     * @throws IOException
     */
    public static void writeInternal(String filename, String content) {
        File appDir = new File(Config.FileAssets.OUTSIDE_FILE);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        //获取文件在内存卡中files目录下的路径
//        filename = Environment.getExternalStorageDirectory().getAbsolutePath()+ filename;
        filename = Config.FileAssets.OUTSIDE_FILE + filename + ".txt";
        //打开文件输出流
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filename, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("TAG", e.toString());
        }
        //写数据到文件中
        try {
            if (outputStream != null && content.length() != 0) {
                outputStream.write(content.getBytes());
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", e.toString());
        }
    }

    public static byte[] intToBuffer(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }


    /**
     * @param filename
     * @return
     */
    public static String readExternalIp(String filename) {
        if (!new File(filename).exists()) {
            return "100.168.2.62";
        } else {
            return readExternal(filename);
        }
    }


    /**
     * 读取文件内容
     *
     * @param filename
     * @return
     */
    public static String readExternal(String filename) {
        StringBuilder sb = new StringBuilder("");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //打开文件输入流
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(filename);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Logger.d(e.toString());
            }

            byte[] buffer = new byte[1024];
            int len = 0;
            try {
                if (inputStream != null) {
                    len = inputStream.read(buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //读取文件内容
            while (len > 0) {
                sb.append(new String(buffer, 0, len));

                //继续将数据放到buffer中
                try {
                    len = inputStream.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.d(e.toString());
                }
            }
            if (inputStream != null){
                //关闭输入流
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("TAG", e.toString());
                }
            }

        }

        if (sb.toString().equals("")){
            return "80";
        }else {
            return sb.toString();
        }

    }


    public static String getLocalMacAddressFromWifiInfo(Context context) {
        @SuppressLint("WifiManagerPotentialLeak") WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo winfo = wifi.getConnectionInfo();
        @SuppressLint("HardwareIds") String mac = winfo.getMacAddress();
        return mac;
    }

    public class SimpleModel {
        public String Level;
        public String ID;
        public String Type;
        public String record_url;
    }


    /**
     * 查询文件夹下的文件
     *
     * @param path 目录的文件夹路径
     * @return 文件夹下面的文件
     */

    public static List<String> getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "空目录");
            return null;
        }
        List<String> s = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            s.add(files[i].getAbsolutePath());
        }
        return s;
    }

    /**
     * 获取指定文件大小
     */
    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {

            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }


    public static byte[] File2Bytes(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        int byte_size = 1024;
        byte[] b = new byte[byte_size];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
                    byte_size);
            for (int length; (length = fileInputStream.read(b)) != -1; ) {
                outputStream.write(b, 0, length);
            }
            fileInputStream.close();
            outputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static File saveFile(Bitmap bm, String path, String fileName) {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(path, fileName);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.JPEG, 50, bos);
            bos.flush();
            bos.close();
            ShellUtils.CommandResult commandResult2 = ShellUtils.execCommand("mv sdcard/Pictures/camera_image.jpg /data/misc/cameraserver/image/", true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return myCaptureFile;
    }
}
