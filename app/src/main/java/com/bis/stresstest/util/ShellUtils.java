package com.bis.stresstest.util;

import android.app.Activity;
import com.bis.stresstest.app.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ShellUtils {


    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";


    /**
     * 配置运行环境
     *
     * @param activity
     */
    public static void copyRunEnvironment(final Activity activity) {
        CommandResult rmAllResult = ShellUtils.execCommand("rm -r data/local/tmp/*", true);
        Logger.d("rm -r data/local/tmp/* \n + 结果：" + rmAllResult.toString());

        FileUtils.getInstance(activity).copyAssetsToSD("atx-agent", MyApplication.filePath + "atx-agent");
        FileUtils.getInstance(activity).copyAssetsToSD("minicap", MyApplication.filePath + "minicap");
        FileUtils.getInstance(activity).copyAssetsToSD("minicap.so", MyApplication.filePath + "minicap.so");
        FileUtils.getInstance(activity).copyAssetsToSD("minitouch", MyApplication.filePath + "minitouch");

        CommandResult commandResult1 = ShellUtils.execCommand("ls -al /system/xbin/su", true);
        Logger.d("ls -al /system/xbin/su \n + 结果：" + commandResult1.toString());
        CommandResult commandResult2 = ShellUtils.execCommand("chmod 777 /data/local/tmp", true);
        Logger.d("chmod 777 /data/local/tmp \n + 结果：" + commandResult2.toString());
        CommandResult commandResult3 = ShellUtils.execCommand("chmod 777 /data/local/tmp/atx-agent", true);
        Logger.d("chmod 777 /data/local/tmp/atx-agent \n + 结果：" + commandResult3.toString());
        CommandResult commandResult4 = ShellUtils.execCommand("chmod 777 /data/local/tmp/minicap", true);
        Logger.d("chmod 777 /data/local/tmp/minicap \n + 结果：" + commandResult4.toString());
        CommandResult commandResult5 = ShellUtils.execCommand("chmod 777 /data/local/tmp/minitouch", true);
        Logger.d("chmod 777 /data/local/tmp/minitouch \n + 结果：" + commandResult5.toString());
        if (AppTool.isApplicationAvilible(activity, "com.bis.monitor")) {
            CommandResult commandResult6 = ShellUtils.execCommand("pm uninstall com.bis.monitor", true);
            Logger.d("pm uninstall com.bis.monito \n + 结果：" + commandResult6.toString());
        }
        CommandResult commandResult = ShellUtils.execCommand("pm install -r /data/local/tmp/push.apk", true);
        CommandResult commandResult11 = ShellUtils.execCommand("/data/local/tmp/atx-agent  server  --stop", true);
        Logger.d("/data/local/tmp/atx-agent  server  --stop \n + 结果：" + commandResult11.toString());
//        if (commandResult11.errorMsg.contains("not found")){
//            Intent intent = activity.getBaseContext().getPackageManager().getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            //与正常页面跳转一样可传递序列化数据,在Launch页面内获得
//            intent.putExtra("REBOOT", "reboot");
//            activity.startActivity(intent);
//        }
        CommandResult commandResult12 = ShellUtils.execCommand("/data/local/tmp/atx-agent server --nouia -d", true);
        Logger.d("/data/local/tmp/atx-agent server --nouia -d \n + 结果：" + commandResult12.toString());
        CommandResult commandResult13 = ShellUtils.execCommand("LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P 1080x1920@1080x1920/0 -t", true);
        Logger.d("LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P 1080x1920@1080x1920/0 -t \n + 结果：" + commandResult13.toString());


    }


    private ShellUtils() {
        throw new AssertionError();
    }


    /**
     * 查看是否有了root权限
     *
     * @return
     */
    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }


    /**
     * 执行shell命令，默认返回结果
     *
     * @param command command
     * @return
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, true, true);
    }


    /**
     * 执行shell命令，默认返回结果
     *
     * @param commands command list
     * @return
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand2(String commands, boolean isRoot,boolean is2GB) {
        return execPrint(new String[]{commands}, true, is2GB,true);
    }


    /**
     * 执行shell命令，默认返回结果
     *
     * @param commands command array
     * @return
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }


    /**
     * execute shell command
     *
     * @param command         command
     * @param isNeedResultMsg whether need result msg
     * @return
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot,
                                            boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }


    /**
     * execute shell commands
     *
     * @param commands command list
     * @return
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands,
                                            boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(
                commands == null ? null : commands.toArray(new String[]{}),
                isRoot, isNeedResultMsg);
    }


    /**
     * execute shell commands
     *
     * @param commands command array
     * @return <ul>
     * <li>if isNeedResultMsg is false, {@link CommandResult#successMsg}
     * is null and {@link CommandResult#errorMsg} is null.</li>
     * <li>if {@link CommandResult#result} is -1, there maybe some
     * excepiton.</li>
     * </ul>
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }


        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;


        DataOutputStream os = null;
        try {
            String execString;

//            if (isRoot) execString = "/system/xbin/qssu root";
            if (isRoot) execString = "su root";
            else execString = "sh";

            process = Runtime.getRuntime().exec(execString);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset
                // error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s + "\n");
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null
                : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }


    /**
     * execute shell commands
     *
     * @param commands command array
     * @return <ul>
     * <li>if isNeedResultMsg is false, {@link CommandResult#successMsg}
     * is null and {@link CommandResult#errorMsg} is null.</li>
     * <li>if {@link CommandResult#result} is -1, there maybe some
     * excepiton.</li>
     * </ul>
     */
    public static CommandResult execPrint(String[] commands, boolean isRoot,boolean is2GB,
                                          boolean isNeedResultMsg) {

        writeNetworkFile(0,0,9999,9999,1,0);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;
        try {
            String execString;
//            if (isRoot) execString = "/system/xbin/qssu root";
            execString = "su";
            process = Runtime.getRuntime().exec(execString);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));
                String s;
                int low800Mbps = 0;
                int low500Mbps = 0;
                int disconnectTime = 0;
                int disconnectCount = 0;
                int loopCount = 0;
                boolean isDisconnect = false;
                double networkSpeed = 0;
                String data = DateUtils.getCatalogData();
                while ((s = successResult.readLine()) != null) {
                    FileUtils.writeInternal("logFile", s + "\n");
                    FileUtils.writeInternal(data, s + "\n");
                    if (s.substring(s.length() - 13).contains("s/sec")) {
                        String[] strings = s.split(" ");
                        loopCount++;
                        Logger.i("本次输出:"+s);
//                        if (!strings[strings.length - 5].equals(company)) {
//                            Logger.i("测速结束，平均值:" + strings[strings.length - 2]);
//                            writeNetworkFile(low800Mbps,low500Mbps,disconnectTime,disconnectCount,1,Integer.parseInt(strings[strings.length - 2]));
//                            execCommand("am broadcast -n com.bis.stresstest/.receiver.MyReceiver -a stresstest.intent.action.stopNetWorkTest", true);
//                        } else {
                            if (strings[strings.length - 3].equals("bits")) {
                                if (!isDisconnect){
                                    isDisconnect = true;
                                    disconnectCount++;
                                }
                                Logger.i("断流时间：:" + disconnectTime + "秒");
                                disconnectTime++;
                            } else {
                                isDisconnect = false;
                                double nowSpeed = Double.parseDouble(strings[strings.length - 2]);
                                if (is2GB){
                                    if (!strings[strings.length-1].contains("Mbits/sec")){
                                        nowSpeed = nowSpeed * 1024;
                                    }
                                }
                                Logger.i("速度：:" + nowSpeed );
                                if (nowSpeed < 500) {
                                    low500Mbps++;
                                } else if (nowSpeed < 800) {
                                    low800Mbps++;
                                }
                                networkSpeed = networkSpeed+nowSpeed;
                            }
                            writeNetworkFile(low800Mbps,low500Mbps,disconnectTime,disconnectCount,loopCount,networkSpeed);
//                        }
                    }
                }
                while ((s = errorResult.readLine()) != null) {
                    Logger.e(s);
                    errorMsg.append(s);
                    if (Config.NETWORK_TEST_STATUS == 1) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        Logger.i("结束！！！！");
        return new CommandResult(1, successMsg == null ? null
                : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }


    public static void writeNetworkFile(int low800Mbps, int low500Mbps, int disconnectTime, int disconnectCount, int loopCount, double networkSpeed)  {
        JSONObject jo = new JSONObject();
//        Logger.i("平均速度："+networkSpeed/loopCount+",次数："+loopCount+",总速度"+networkSpeed);
        try {
            jo.put("averageSpeed",networkSpeed/loopCount);
            jo.put("low800",low800Mbps);
            jo.put("low500",low500Mbps);
            jo.put("disconnectTime",disconnectTime);
            jo.put("disconnectCount",disconnectCount);
            jo.put("sn",execCommand(" getprop ro.serialno",true).getSuccessMsg());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FileUtils.writeTxt(jo.toString(), "result.txt");
    }


    /**
     * 运行结果
     * <ul>
     * <li>{@link CommandResult#result} means result of command, 0 means normal,
     * else means error, same to excute in linux shell</li>
     * <li>{@link CommandResult#successMsg} means success message of command
     * result</li>
     * <li>{@link CommandResult#errorMsg} means error message of command result</li>
     * </ul>
     *
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a>
     * 2013-5-16
     */
    public static class CommandResult {


        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public String getSuccessMsg() {
            return successMsg;
        }

        public void setSuccessMsg(String successMsg) {
            this.successMsg = successMsg;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        /**
         * 运行结果
         **/
        public int result;
        /**
         * 运行成功结果
         **/
        public String successMsg;
        /**
         * 运行失败结果
         **/
        public String errorMsg;


        public CommandResult(int result) {
            this.result = result;
        }


        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "CommandResult{" +
                    "result=" + (result == 0 ? "执行成功：" + successMsg : "执行失败：" + errorMsg) +
                    '}';
        }
    }


}