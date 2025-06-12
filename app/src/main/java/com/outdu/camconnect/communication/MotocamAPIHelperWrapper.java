package com.outdu.camconnect.communication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MotocamAPIHelperWrapper {

    private static final int MAX_BYTES = 255;
    private static final String TAG = "MotocamAPIHelperWrapper";
    public static String DEVICE_IP_ADDRESS="192.168.2.1";
    public static List<String> deviceIdAddressList=new ArrayList<>();
    public static final int MOTOCAM_CLIENT_SOCKET_PORT = 9000;
    public static final int MOTOCAM_SERVER_SOCKET_PORT = 9002;

    private static MotocamAPIHelperWrapper motocamAPIHelperWrapper;

    public static void findDevice() {
        Log.i(TAG, "findDevice");
        try {
            List<String> deviceIdAddressList1=new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                Log.i(TAG, line);
                String[] splitted = line.split(" +");
                String ipAddress = splitted[0].trim();
                String mac = splitted[3].trim();
                Log.i(TAG, "ipAddress="+ipAddress);
                if(!"IP".equals(ipAddress) && !"00:00:00:00:00:00".equals(mac))
                    deviceIdAddressList1.add(ipAddress);
            }

            for(String ipAddress:deviceIdAddressList1) {
                Log.i(TAG, "try to connect "+ipAddress);
                if(new MotocamSocketClient().checkDevice(ipAddress)) {
                    Log.i(TAG, "found device ipAddress="+ipAddress);
                    DEVICE_IP_ADDRESS = ipAddress;
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static MotocamAPIHelperWrapper newInstance() {
        if(motocamAPIHelperWrapper==null) {
            motocamAPIHelperWrapper = new MotocamAPIHelperWrapper();
        }
        return motocamAPIHelperWrapper;
    }

    public void setDeviceIpAddress(String ipAddress) {
        DEVICE_IP_ADDRESS = ipAddress;
    }

    public Map<String, Object> getConfig(String type) throws Exception {
        Log.i(TAG, type);
        switch (type){
            case "Factory": return getFactoryConfig();
            case "Default": return getDefaultConfig();
            case "Current": return getCurrentConfig();
        }
        return null;
    }

    public Map<String, Object> getFactoryConfig() throws Exception {
        Log.i(TAG, "getFactoryConfig");
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.getFactoryConfigCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        Map<String, Object> config = MotocamAPIHelper.getFactoryConfigCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return config;
    }

    public Map<String, Object> getDefaultConfig() throws Exception {
        Log.i(TAG, "getDefaultConfig");
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.getDefaultConfigCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        Map<String, Object> config = MotocamAPIHelper.getDefaultConfigCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return config;
    }

    public Map<String, Object> getCurrentConfig() throws Exception {
        Log.i(TAG, "getCurrentConfig");
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.getCurrentConfigCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        Map<String, Object> config = MotocamAPIHelper.getCurrentConfigCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return config;
    }

    public boolean setIrBrightness(Integer irBrightness) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setImgIRBrightnessCmd(irBrightness);
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setImgIRBrightnessCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setImgZoom(MotocamAPIHelper.ZOOM zoom) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setImgZoomCmd(zoom.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setImgZoomCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setImgResolution(MotocamAPIHelper.RESOLUTION val) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setImgResolutionCmd(val.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setImgResolutionCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setImgTilt(MotocamAPIHelper.TILT val) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setImgTiltCmd(val.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setImgTiltCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setImgRotation(MotocamAPIHelper.ROTATION rotation) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setImgRotationCmd(rotation.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setImgRotationCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setIrCutFilter(MotocamAPIHelper.IRCUTFILTER ircutfilter) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setImgIRCutFilterCmd(ircutfilter.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setImgIRCutFilterCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setMirror(MotocamAPIHelper.MIRROR val) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setMirrorCmd(val.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setMirrorCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setFlip(MotocamAPIHelper.FLIP val) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setFlipCmd(val.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setFlipCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setWdr(MotocamAPIHelper.WDR val) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setWdrCmd(val.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setWdrCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setEis(MotocamAPIHelper.EIS val) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setEisCmd(val.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setEisCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setMisc(int val) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setMiscCmd(val);
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setMiscCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setDayMode(MotocamAPIHelper.DAYMODE daymode) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setImgDayModeCmd(daymode.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setImgDayModeCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setGyroReader(MotocamAPIHelper.GYROREADER val) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setImgGyroReaderCmd(val.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setImgGyroReaderCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setMic(MotocamAPIHelper.MIC mic) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setAudioMicCmd(mic.getDisplayVal());
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setAudioMicCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setWifiHotspot(String ssid, String encryptionType, String encryptionKey, String ipAddress, String subnetMask) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setWifiHotspotCmd(ssid, encryptionType, encryptionKey, ipAddress, subnetMask);
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setWifiHotspotCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setWifiClient(String ssid, String encryptionType, String encryptionKey, String ipAddress, String subnetMask) throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setWifiClientCmd(ssid, encryptionType, encryptionKey, ipAddress, subnetMask);
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setWifiClientCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public MotocamAPIHelper.WifiState getWifiState() throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.getWifiStateCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        MotocamAPIHelper.WifiState wifiState = MotocamAPIHelper.getWifiStateCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return wifiState;
    }

    public Map<String, Object> getWifiHotspotConfig() throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.getWifiHotspotCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        Map<String, Object> wifiHotspotConfig = MotocamAPIHelper.getWifiHotspotCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return wifiHotspotConfig;
    }

    public Map<String, Object> getWifiClientConfig() throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.getWifiClientCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        Map<String, Object> wifiHotspotConfig = MotocamAPIHelper.getWifiClientCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return wifiHotspotConfig;
    }


    public boolean setDefaultToFactory() throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setDefaultToFactoryCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setDefaultToFactoryCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setDefaultToCurrent() throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setDefaultToCurrentCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setDefaultToCurrentCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setCurrentToFactory() throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setCurrentToFactoryCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setCurrentToFactoryCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean setCurrentToDefault() throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.setCurrentToDefaultCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.setCurrentToDefaultCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean shutdownCamera() throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.shutdownCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.shutdownCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public void uploadFile(String fileName, InputStream input) throws Exception {
        Log.i(TAG, "fileName="+fileName);
        if(!DEVICE_IP_ADDRESS.isEmpty()) {
            Log.i(TAG, "fileName="+fileName);
            FTPUploader ftpUploader = new FTPUploader(DEVICE_IP_ADDRESS, "root", "ota");
            ftpUploader.uploadFile(input, fileName, "");
            ftpUploader.disconnect();
            Log.i(TAG, "Upload Done");
        }
    }

    public boolean startStream() throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.startStreamCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.startStreamCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

    public boolean stopStream() throws Exception {
        MotocamSocketClient motocamClient=new MotocamSocketClient();
        motocamClient.init();
        int[] reqCmd = MotocamAPIHelper.stopStreamCmd();
        int[] resbytes = new int[MAX_BYTES];
        int resbytesSize = motocamClient.sendCmd(reqCmd,resbytes);
        boolean ret = MotocamAPIHelper.stopStreamCmdResponseParse(resbytes, resbytesSize);
        motocamClient.destroy();
        return ret;
    }

}
