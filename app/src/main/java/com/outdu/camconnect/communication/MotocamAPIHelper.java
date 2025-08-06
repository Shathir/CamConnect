package com.outdu.camconnect.communication;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr on 21/8/19.
 */
public class MotocamAPIHelper {

    public enum Header {
        SET(1), GET(2), ACK(3), RESPONSE(4);
        private final int val;
        Header(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    };

    public enum Commands {
        STREAMING(1), NETWORK(2), CONFIG(3), IMAGE(4), AUDIO(5), SYSTEM(6);
        private final int val;
        Commands(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    };

    public enum StreamingSubCommands {
        START(1), STOP(2);
        private final int val;
        StreamingSubCommands(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    };

    public enum StreamingState {
        START(1), STOP(0);
        private final int val;
        StreamingState(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    };

    public enum WifiState {
        WifiHotspot(1), WifiClient(2);
        private final int val;
        WifiState(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    }

    public enum NetworkSubCommands {
        WifiHotspot(1), WifiClient(2), WifiState(3);
        private final int val;
        NetworkSubCommands(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    };

    public enum ConfigSetSubCommands {
        DefaultToFactory(9), DefaultToCurrent(11), CurrentToFactory(13), CurrentToDefault(14);
        private final int val;
        ConfigSetSubCommands(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    };

    public enum ConfigGetSubCommands {
        Factory(4), Default(8), Current(12);
        private final int val;
        ConfigGetSubCommands(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    };

    public enum ImageSubCommands {
        ZOOM(1), ROTATION(2), IRCUTFILTER(3), IRBRIGHTNESS(4), DAYMODE(5),
        RESOLUTION(6), MIRROR(7), FLIP(8), TILT(9), WDR(10), EIS(11), GYROREADER(12), MISC(13);
        private final int val;
        ImageSubCommands(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    };

    public enum AudioSubCommands {
        MIC(1);
        private final int val;
        AudioSubCommands(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    };

//    public enum SystemSubCommands {
//        SHUTDOWN(1);
//        private final int val;
//        SystemSubCommands(int i) {
//            this.val = i;
//        }
//        public int getVal() {
//            return this.val;
//        }
//    };

    public enum SystemSubCommands {
        // GET Commands
        CAMERA_NAME(1),
        FIRMWARE(2),
        MAC_ADDRESS(3),
        LOGIN_PIN(4),
        OTA_STATUS(5),
        HEALTH_CHECK(6),

        // SET Commands
        SET_CAMERA_NAME(1),
        SET_LOGIN_PIN(2),
        FACTORY_RESET(3),
        SHUTDOWN(4),
        OTA_UPDATE(5);

        private final int val;

        SystemSubCommands(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }


    //Config seq hard coded in getConfigCmd
    public enum ConfigProperties {
        ZOOM(1), ROTATION(2), IRCUTFILTER(3), IRBRIGHTNESS(4);
        private final int val;
        ConfigProperties(int i) {
            this.val = i;
        }
        public int getVal() {
            return this.val;
        }
    };

    public enum ZOOM {
        X1(1, "x1"), X2(2, "x2"), X3(3, "x3"), X4(4, "x4");
        private final int val;
        private final String displayVal;
        ZOOM(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static ZOOM get(int v) {
            switch (v){
                case 1:return X1;
                case 2:return X2;
                case 3:return X3;
                case 4:return X4;
                default:return null;
            }
        }
        public static ZOOM get(String v) {
            switch (v){
                case "x1":return X1;
                case "x2":return X2;
                case "x3":return X3;
                case "x4":return X4;
                default:return null;
            }
        }
        public static String getKey() {
            return "ZOOM";
        }
    };

    public enum ROTATION {
        R0(1, "0"), R90(2, "90"), R180(3, "180"), R270(4, "270");
        private final int val;
        private final String displayVal;
        ROTATION(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static ROTATION get(int v) {
            switch (v){
                case 1:return R0;
                case 2:return R90;
                case 3:return R180;
                case 4:return R270;
                default:return null;
            }
        }
        public static ROTATION get(String v) {
            switch (v){
                case "0":return R0;
                case "90":return R90;
                case "180":return R180;
                case "270":return R270;
                default:return null;
            }
        }
        public static String getKey() {
            return "ROTATION";
        }
    };

    public enum RESOLUTION {
        MODE0(0, "mode0"), MODE1(1, "mode1");
        private final int val;
        private final String displayVal;
        RESOLUTION(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static RESOLUTION get(int v) {
            switch (v){
                case 0:return MODE0;
                case 1:return MODE1;
                default:return null;
            }
        }
        public static RESOLUTION get(String v) {
            switch (v){
                case "mode0":return MODE0;
                case "mode1":return MODE1;
                default:return null;
            }
        }
        public static String getKey() {
            return "RESOLUTION";
        }
    };

    public enum TILT {
        T0(0, "0"), T1(1, "1"), T2(2, "2"), T3(3, "3"), T4(4, "4"), T5(5, "5");
        private final int val;
        private final String displayVal;
        TILT(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static TILT get(int v) {
            switch (v){
                case 0:return T0;
                case 1:return T1;
                case 2:return T2;
                case 3:return T3;
                case 4:return T4;
                case 5:return T5;
                default:return null;
            }
        }
        public static TILT get(String v) {
            switch (v){
                case "0":return T0;
                case "1":return T1;
                case "2":return T2;
                case "3":return T3;
                case "4":return T4;
                case "5":return T5;
                default:return null;
            }
        }
        public static String getKey() {
            return "TILT";
        }
    };

    public enum IRCUTFILTER {
        OFF(0, "Off"), ON(1, "On");
        private final int val;
        private final String displayVal;
        IRCUTFILTER(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static IRCUTFILTER get(int v) {
            switch (v){
                case 0:return OFF;
                case 1:return ON;
                default:return null;
            }
        }
        public static IRCUTFILTER get(String v) {
            switch (v){
                case "Off":return OFF;
                case "On":return ON;
                default:return null;
            }
        }
        public static String getKey() {
            return "IRCUTFILTER";
        }
    };

    public enum MIRROR {
        OFF(0, "Off"), ON(1, "On");
        private final int val;
        private final String displayVal;
        MIRROR(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static MIRROR get(int v) {
            switch (v){
                case 0:return OFF;
                case 1:return ON;
                default:return null;
            }
        }
        public static MIRROR get(String v) {
            switch (v){
                case "Off":return OFF;
                case "On":return ON;
                default:return null;
            }
        }
        public static String getKey() {
            return "MIRROR";
        }
    };

    public enum FLIP {
        OFF(0, "Off"), ON(1, "On");
        private final int val;
        private final String displayVal;
        FLIP(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static FLIP get(int v) {
            switch (v){
                case 0:return OFF;
                case 1:return ON;
                default:return null;
            }
        }
        public static FLIP get(String v) {
            switch (v){
                case "Off":return OFF;
                case "On":return ON;
                default:return null;
            }
        }
        public static String getKey() {
            return "FLIP";
        }
    };

    public enum WDR {
        OFF(0, "Off"), ON(1, "On");
        private final int val;
        private final String displayVal;
        WDR(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static WDR get(int v) {
            switch (v){
                case 0:return OFF;
                case 1:return ON;
                default:return null;
            }
        }
        public static WDR get(String v) {
            switch (v){
                case "Off":return OFF;
                case "On":return ON;
                default:return null;
            }
        }
        public static String getKey() {
            return "WDR";
        }
    };

    public enum EIS {
        OFF(0, "Off"), ON(1, "On");
        private final int val;
        private final String displayVal;
        EIS(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static EIS get(int v) {
            switch (v){
                case 0:return OFF;
                case 1:return ON;
                default:return null;
            }
        }
        public static EIS get(String v) {
            switch (v){
                case "Off":return OFF;
                case "On":return ON;
                default:return null;
            }
        }
        public static String getKey() {
            return "EIS";
        }
    };

//    public enum IRBRIGHTNESS {
//        OFF(0, "Off"), ON(1, "On"), MEDIUM(2, "Med"), LOW(3, "Low");
//        private final int val;
//        private final String displayVal;
//        IRBRIGHTNESS(int i, String s) {
//            this.val = i;
//            this.displayVal = s;
//        }
//        IRBRIGHTNESS(int i) {
//            this.val = i;
//        }
//        public int getVal() {
//            return this.val;
//        }
//        public String getDisplayVal() {
//            return this.displayVal;
//        }
//        public static IRBRIGHTNESS get(int v) {
//            switch (v){
//                case 0:return OFF;
//                case 1:return ON;
//                case 2:return MEDIUM;
//                case 3:return LOW;
//                default:return null;
//            }
//        }
//        public static IRBRIGHTNESS get(String v) {
//            switch (v){
//                case "Off":return OFF;
//                case "On":return ON;
//                case "Med":return MEDIUM;
//                case "Low":return LOW;
//                default:return null;
//            }
//        }
//        public static String getKey() {
//            return "IRBRIGHTNESS";
//        }
//    };

    public enum DAYMODE {
        OFF(0, "Off"), ON(1, "On");
        private final int val;
        private final String displayVal;
        DAYMODE(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static DAYMODE get(int v) {
            switch (v){
                case 0:return OFF;
                case 1:return ON;
                default:return null;
            }
        }
        public static DAYMODE get(String v) {
            switch (v){
                case "Off":return OFF;
                case "On":return ON;
                default:return null;
            }
        }
        public static String getKey() {
            return "DAYMODE";
        }
    };

    public enum GYROREADER {
        OFF(0, "Off"), ON(1, "On");
        private final int val;
        private final String displayVal;
        GYROREADER(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static GYROREADER get(int v) {
            switch (v){
                case 0:return OFF;
                case 1:return ON;
                default:return null;
            }
        }
        public static GYROREADER get(String v) {
            switch (v){
                case "Off":return OFF;
                case "On":return ON;
                default:return null;
            }
        }
        public static String getKey() {
            return "GYROREADER";
        }
    };

    public enum MIC {
        OFF(0, "Off"), ON(1, "On");
        private final int val;
        private final String displayVal;
        MIC(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static MIC get(int v) {
            switch (v){
                case 0:return OFF;
                case 1:return ON;
                default:return null;
            }
        }
        public static MIC get(String v) {
            switch (v){
                case "Off":return OFF;
                case "On":return ON;
                default:return null;
            }
        }
        public static String getKey() {
            return "MIC";
        }
    };

    public enum EncryptionType {
//        Open_Network(1, "Open Network"), WEP(2, "WEP"), WPA_TKIP(3, "WPA+TKIP"), WPA_AES(4, "WPA+AES"),
//        WPA2_TKIP(5, "WPA2+TKIP"), WPA2_AES(6, "WPA2+AES"), WPA2_TKIP_AES(7, "WPA2+TKIP/AES");
        WEP(1, "WEP"),WPA(2, "WPA"),WPA2(3, "WPA2");
        private final int val;
        private final String displayVal;
        EncryptionType(int i, String s) {
            this.val = i;
            this.displayVal = s;
        }
        public int getVal() {
            return this.val;
        }
        public String getDisplayVal() {
            return this.displayVal;
        }
        public static EncryptionType get(int v) {
            switch (v){
//                case 1:return Open_Network;
//                case 2:return WEP;
//                case 3:return WPA_TKIP;
//                case 4:return WPA_AES;
//                case 5:return WPA2_TKIP;
//                case 6:return WPA2_AES;
//                case 7:return WPA2_TKIP_AES;
                case 1:return WEP;
                case 2:return WPA;
                case 3:return WPA2;
                default:return null;
            }
        }
        public static EncryptionType get(String v) {
            switch (v){
//                case "Open Network":return Open_Network;
//                case "WEP":return WEP;
//                case "WPA+TKIP":return WPA_TKIP;
//                case "WPA+AES":return WPA_AES;
//                case "WPA2+TKIP":return WPA2_TKIP;
//                case "WPA2+AES":return WPA2_AES;
//                case "WPA2+TKIP/AES":return WPA2_TKIP_AES;
                case "WEP":return WEP;
                case "WPA":return WPA;
                case "WPA2":return WPA2;
                default:return null;
            }
        }
        public static String getKey() {
            return "encryptiontype";
        }
    };


    private static boolean setCmdResponseParse(int response[], int length, int command_req, int subCommand_req) throws Exception {
        if(length < 7) throw new Exception("Invalid response length");
        int header = response[0];
        int command = response[1];
        int subCommand = response[2];
        int dataLength = response[3];

        if(header != Header.ACK.getVal()) {
            throw new Exception("Invalid header in response");
        }
        if(command != command_req) {
            throw new Exception("Invalid command in response");
        }
        if(subCommand != subCommand_req) {
            throw new Exception("Invalid sub command in response");
        }
        if(dataLength != 2) {
            throw new Exception("Invalid data/data length in response");
        }

        int s_or_e = response[4];
        if(s_or_e == 0) {//0 success
            int s_val = response[5];
            if(s_val == 0) {
                return true;
            } else {
                throw new Exception("Invalid success val in response");
            }
        } else if(s_or_e == 1){//1 failed
            int e_val = response[5];
            throw new Exception("error response val="+ e_val);
        } else {
            throw new Exception("Invalid data in response");
        }
    }

    private static Map<String, Object> getConfigCmdResponseParse(int response[], int length, ConfigGetSubCommands configGetSubCommand)
            throws Exception {
        int minDataLength = 7; //header + command + subcommand + datalength + success/error flag + success/error val + crc
        if(length < minDataLength) throw new Exception("Invalid response length");
        int header = response[0];
        int command = response[1];
        int subCommand = response[2];
        int dataLength = response[3];
        int s_or_e = response[4];

        if(header != Header.RESPONSE.getVal()) {
            throw new Exception("Invalid header in response");
        }
        if(command != Commands.CONFIG.getVal()) {
            throw new Exception("Invalid command in response");
        }
        if(subCommand != configGetSubCommand.getVal()) {
            throw new Exception("Invalid sub command in response");
        }
        if((s_or_e == 0 && dataLength != 15) || (s_or_e == 1 && dataLength != 2)) {
            throw new Exception("Invalid data/data length in response");
        }

        if(s_or_e == 0) {//0 success
            Map<String, Object> config = new HashMap<>(13);
            System.out.println(response[5]);
            System.out.println(response[6]);
            System.out.println(response[7]);
            System.out.println(response[8]);
            System.out.println(response[9]);
            System.out.println(response[10]);
            System.out.println(response[11]);
            System.out.println(response[12]);
            System.out.println(response[13]);
            System.out.println(response[14]);
            System.out.println(response[15]);
            System.out.println(response[16]);
            System.out.println(response[17]);
            System.out.println(response[18]);
            int zoom_val = response[5];
            int rotaion_val = response[6];
            int ircutfilter_val = response[7];
            int irbrightness_val = response[8];
            int daymode_val = response[9];
            int resolution_val = response[10];
            int mirror_val = response[11];
            int flip_val = response[12];
            int tilt_val = response[13];
            int wdr_val = response[14];
            int eis_val = response[15];
            int gyroreader_val = response[16];
            int misc_val = response[17];
            int mic_val = response[18];
            ZOOM zoom = ZOOM.get(zoom_val);
            if(zoom != null) {
                config.put(ZOOM.getKey(), zoom);
            } else {
                throw new Exception("Invalid zoom val in response");
            }
            ROTATION rotation = ROTATION.get(rotaion_val);
            if(rotation != null) {
                config.put(ROTATION.getKey(), rotation);
            } else {
                throw new Exception("Invalid rotation val in response");
            }
            IRCUTFILTER ircutfilter = IRCUTFILTER.get(ircutfilter_val);
            if(ircutfilter != null) {
                config.put(IRCUTFILTER.getKey(), ircutfilter);
            } else {
                throw new Exception("Invalid ircutfilter val in response");
            }
            config.put("IRBRIGHTNESS", irbrightness_val);

//            IRBRIGHTNESS irbrightness = IRBRIGHTNESS.get(irbrightness_val);
//            if(irbrightness != null) {
//                config.put(IRBRIGHTNESS.getKey(), irbrightness);
//            } else {
//                throw new Exception("Invalid irbrightness val in response");
//            }
            DAYMODE daymode = DAYMODE.get(daymode_val);
            if(daymode != null) {
                config.put(DAYMODE.getKey(), daymode);
            } else {
                throw new Exception("Invalid daymode val in response");
            }
            GYROREADER gyroreader = GYROREADER.get(gyroreader_val);
            if(gyroreader != null) {
                config.put(GYROREADER.getKey(), gyroreader);
            } else {
                throw new Exception("Invalid gyroreader val in response");
            }
            RESOLUTION resolution = RESOLUTION.get(resolution_val);
            if(resolution != null) {
                config.put(RESOLUTION.getKey(), resolution);
            } else {
                throw new Exception("Invalid resolution val in response");
            }
            MIRROR mirror = MIRROR.get(mirror_val);
            if(mirror != null) {
                config.put(MIRROR.getKey(), mirror);
            } else {
                throw new Exception("Invalid mirror val in response");
            }
            FLIP flip = FLIP.get(flip_val);
            if(flip != null) {
                config.put(FLIP.getKey(), flip);
            } else {
                throw new Exception("Invalid flip val in response");
            }
            TILT tilt = TILT.get(tilt_val);
            if(tilt != null) {
                config.put(TILT.getKey(), tilt);
            } else {
                throw new Exception("Invalid tilt val in response");
            }
            WDR wdr = WDR.get(wdr_val);
            if(wdr != null) {
                config.put(WDR.getKey(), wdr);
            } else {
                throw new Exception("Invalid wdr val in response");
            }
            EIS eis = EIS.get(eis_val);
            if(eis != null) {
                config.put(EIS.getKey(), eis);
            } else {
                throw new Exception("Invalid eis val in response");
            }
            config.put("MISC", misc_val);
            MIC mic = MIC.get(mic_val);
            if(mic != null) {
                config.put(MIC.getKey(), mic);
            } else {
                throw new Exception("Invalid mic val in response");
            }
            return config;
        } else if(s_or_e == 1){//1 failed
            int e_val = response[5];
            throw new Exception("error response val="+ e_val);
        } else {
            throw new Exception("Invalid data in response");
        }
    }

    private static int[] getCmd(int command_req, int subCommand_req) {
        int cmd[] = new int[5];
        cmd[0] = Header.GET.getVal();
        cmd[1] = command_req;
        cmd[2] = subCommand_req;
        cmd[3] = 0;//no data
        cmd[4] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    private static int[] setWifiCmd(String ssid, String encryptionTypeStr, String encryptionKey, String ipAddress, String subnetMask,
                                    NetworkSubCommands networkSubCommand) throws Exception {
        System.out.println("ssid="+ssid+",et="+encryptionTypeStr+",ek="+encryptionKey+
        ",ip="+ipAddress+",sm="+subnetMask);
        EncryptionType encryptionType = EncryptionType.get(encryptionTypeStr);
        if(encryptionType == null) {
            throw new Exception("Invalid encryptionType");
        }
        byte[] ssidByteArray = ssid.getBytes();
        byte[] encryptionKeyByteArray = encryptionKey.getBytes();
        byte[] ipaddressByteArray = ipAddress.getBytes();
        byte[] subnetmaskByteArray = subnetMask.getBytes();

        int dataLength = 1 + ssidByteArray.length + 1 + 1 + encryptionKeyByteArray.length + 1 + ipaddressByteArray.length +
                1 + subnetmaskByteArray.length;
        int packetLength = 5+dataLength;
        int cmd[] = new int[packetLength];
        int wifiConfigIdx=0;
        cmd[wifiConfigIdx] = Header.SET.getVal();
        cmd[++wifiConfigIdx] = Commands.NETWORK.getVal();
        cmd[++wifiConfigIdx] = networkSubCommand.getVal();
        cmd[++wifiConfigIdx] = dataLength;

        cmd[++wifiConfigIdx] = ssidByteArray.length;
        for (byte aSsidByteArray : ssidByteArray) {
            cmd[++wifiConfigIdx] = aSsidByteArray;
        }
        cmd[++wifiConfigIdx] = encryptionType.getVal();
        cmd[++wifiConfigIdx] = encryptionKeyByteArray.length;
        for (byte anEncryptionKeyByteArray : encryptionKeyByteArray) {
            cmd[++wifiConfigIdx] = anEncryptionKeyByteArray;
        }
        cmd[++wifiConfigIdx]=ipaddressByteArray.length;
        for (byte anIpaddressByteArray : ipaddressByteArray) {
            cmd[++wifiConfigIdx] = anIpaddressByteArray;
        }
        cmd[++wifiConfigIdx]=subnetmaskByteArray.length;
        for (byte aSubnetmaskByteArray : subnetmaskByteArray) {
            cmd[++wifiConfigIdx] = aSubnetmaskByteArray;
        }
        cmd[packetLength-1] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    private static Map<String, Object> getWifiCmdResponseParse(int response[], int length, NetworkSubCommands networkSubCommand)
            throws Exception {
        int minDataLength = 7; //header + command + subcommand + datalength + success/error flag + success/error val + crc
        if(length < minDataLength) throw new Exception("Invalid response length");
        int header = response[0];
        int command = response[1];
        int subCommand = response[2];
        int dataLength = response[3];
        int s_or_e = response[4];

        if(header != Header.RESPONSE.getVal()) {
            throw new Exception("Invalid header in response");
        }
        if(command != Commands.NETWORK.getVal()) {
            throw new Exception("Invalid command in response");
        }
        if(subCommand != networkSubCommand.getVal()) {
            throw new Exception("Invalid sub command in response");
        }
        if((s_or_e == 0 && dataLength < 5) || (s_or_e == 1 && dataLength != 2)) {
            throw new Exception("Invalid data/data length in response");
        }

        if(s_or_e == 0) {//0 success
            Map<String, Object> config = new HashMap<>(4);
            int ssidLen = response[5];
            int ssidStartIdx = 6;
            byte[] ssidByteArray = new byte[ssidLen];
            for(int i=0;i<ssidLen;i++) {
                ssidByteArray[i] = (byte) response[ssidStartIdx+i];
            }
            String ssid = new String(ssidByteArray);
            int encryptionTypeInt = response[ssidStartIdx+ssidLen];
            EncryptionType encryptionType = EncryptionType.get(encryptionTypeInt);
            if(encryptionType == null) {
                throw new Exception("Invalid encryption type");
            }
            int encryptionKeyLenIdx = ssidStartIdx + ssidLen + 1;
            int encryptionKeyLen = response[encryptionKeyLenIdx];
            int encryptionKeyStartIdx = encryptionKeyLenIdx + 1;
            byte[] encryptionKeyByteArray = new byte[encryptionKeyLen];
            for(int i=0;i<encryptionKeyLen;i++) {
                encryptionKeyByteArray[i] = (byte) response[encryptionKeyStartIdx+i];
            }
            String encryptionKey = new String(encryptionKeyByteArray);

            int ipAddressLenIdx = encryptionKeyStartIdx + encryptionKeyLen;
            int ipAddressLen = response[ipAddressLenIdx];
            int ipAddressIdx = ipAddressLenIdx + 1;
            byte[] ipaddressByteArray = new byte[ipAddressLen];
            for(int i=0;i<ipAddressLen;i++){
                ipaddressByteArray[i]= (byte) response[ipAddressIdx+i];
            }
            String ipaddress = new String(ipaddressByteArray);

            int subnetmaskLenIdx = ipAddressIdx + ipAddressLen;
            int subnetmaskLen = response[subnetmaskLenIdx];
            int subnetmaskIdx = subnetmaskLenIdx + 1;
            byte[] subnetmaskByteArray = new byte[subnetmaskLen];
            for(int i=0;i<subnetmaskLen;i++){
                subnetmaskByteArray[i]= (byte) response[subnetmaskIdx+i];
            }
            String subnetmask = new String(subnetmaskByteArray);

            config.put("ssid", ssid);
            config.put(EncryptionType.getKey(), encryptionType);
            config.put("encryptionkey", encryptionKey);
            config.put("ipaddress", ipaddress);
            config.put("subnetmask", subnetmask);

            return config;
        } else if(s_or_e == 1){//1 failed
            int e_val = response[5];
            throw new Exception("error response val="+ e_val);
        } else {
            throw new Exception("Invalid data in response");
        }
    }

    public static int[] startStreamCmd() {
        int cmd[] = new int[5];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.STREAMING.getVal();
        cmd[2] = StreamingSubCommands.START.getVal();
        cmd[3] = 0;//no data
        cmd[4] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean startStreamCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.STREAMING.getVal(), StreamingSubCommands.START.getVal());
    }

    public static int[] stopStreamCmd() {
        int cmd[] = new int[5];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.STREAMING.getVal();
        cmd[2] = StreamingSubCommands.STOP.getVal();
        cmd[3] = 0;//no data
        cmd[4] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean stopStreamCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.STREAMING.getVal(), StreamingSubCommands.STOP.getVal());
    }

    public static int[] getStreamStateCmd() {
        return getCmd(Commands.STREAMING.getVal(), 0);//no sub command
    }

    public static StreamingState getStreamStateCmdResponseParse(int response[], int length) throws Exception {
        if(length < 7) throw new Exception("Invalid response length");
        int header = response[0];
        int command = response[1];
        int subCommand = response[2];
        int dataLength = response[3];

        if(header != Header.RESPONSE.getVal()) {
            throw new Exception("Invalid header in response");
        }
        if(command != Commands.STREAMING.getVal()) {
            throw new Exception("Invalid command in response");
        }
        if(subCommand != 0) {
            throw new Exception("Invalid sub command in response");
        }
        if(dataLength != 2) {
            throw new Exception("Invalid data/data length in response");
        }

        int s_or_e = response[4];
        if(s_or_e == 0) {//0 success
            int s_val = response[5];
            if(s_val == StreamingState.START.getVal()) {
                return StreamingState.START;
            } else if(s_val == StreamingState.STOP.getVal()) {
                return StreamingState.STOP;
            } else {
                throw new Exception("Invalid success val in response");
            }
        } else if(s_or_e == 1){//1 failed
            int e_val = response[5];
            throw new Exception("error response val="+ e_val);
        } else {
            throw new Exception("Invalid data in response");
        }
    }

    public static int[] getFactoryConfigCmd() {
        return getCmd(Commands.CONFIG.getVal(), ConfigGetSubCommands.Factory.getVal());
    }

    public static int[] getDefaultConfigCmd() {
        return getCmd(Commands.CONFIG.getVal(), ConfigGetSubCommands.Default.getVal());
    }

    public static int[] getCurrentConfigCmd() {
        return getCmd(Commands.CONFIG.getVal(), ConfigGetSubCommands.Current.getVal());
    }

    public static Map<String, Object> getFactoryConfigCmdResponseParse(int response[], int length) throws Exception {
        return getConfigCmdResponseParse(response, length, ConfigGetSubCommands.Factory);
    }

    public static Map<String, Object> getDefaultConfigCmdResponseParse(int response[], int length) throws Exception {
        return getConfigCmdResponseParse(response, length, ConfigGetSubCommands.Default);
    }

    public static Map<String, Object> getCurrentConfigCmdResponseParse(int response[], int length) throws Exception {
        return getConfigCmdResponseParse(response, length, ConfigGetSubCommands.Current);
    }

    public static int[] setImgZoomCmd(String zoom_val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.ZOOM.getVal();
        cmd[3] = 1;//DataLength
        ZOOM zoom = ZOOM.get(zoom_val);
        if(zoom == null)
            throw new Exception("Invalid zoom val");
        cmd[4] = zoom.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setImgZoomCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.ZOOM.getVal());
    }

    public static int[] setImgRotationCmd(String rotation_val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.ROTATION.getVal();
        cmd[3] = 1;//DataLength
        ROTATION rotation = ROTATION.get(rotation_val);
        if(rotation == null)
            throw new Exception("Invalid rotation val");
        cmd[4] = rotation.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setImgRotationCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.ROTATION.getVal());
    }

    public static int[] setImgResolutionCmd(String val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.RESOLUTION.getVal();
        cmd[3] = 1;//DataLength
        RESOLUTION rotation = RESOLUTION.get(val);
        if(rotation == null)
            throw new Exception("Invalid RESOLUTION val");
        cmd[4] = rotation.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setImgResolutionCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.RESOLUTION.getVal());
    }

    public static int[] setImgTiltCmd(String val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.TILT.getVal();
        cmd[3] = 1;//DataLength
        TILT rotation = TILT.get(val);
        if(rotation == null)
            throw new Exception("Invalid TILT val");
        cmd[4] = rotation.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setImgTiltCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.TILT.getVal());
    }

    public static int[] setImgIRCutFilterCmd(String ircutfilter_val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.IRCUTFILTER.getVal();
        cmd[3] = 1;//DataLength
        IRCUTFILTER ircutfilter = IRCUTFILTER.get(ircutfilter_val);
        if(ircutfilter == null)
            throw new Exception("Invalid ircutfilter val");
        cmd[4] = ircutfilter.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setImgIRCutFilterCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.IRCUTFILTER.getVal());
    }

    public static int[] setMirrorCmd(String val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.MIRROR.getVal();
        cmd[3] = 1;//DataLength
        MIRROR valObj = MIRROR.get(val);
        if(valObj == null)
            throw new Exception("Invalid MIRROR val");
        cmd[4] = valObj.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setMirrorCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.MIRROR.getVal());
    }

    public static int[] setFlipCmd(String val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.FLIP.getVal();
        cmd[3] = 1;//DataLength
        FLIP valObj = FLIP.get(val);
        if(valObj == null)
            throw new Exception("Invalid FLIP val");
        cmd[4] = valObj.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setFlipCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.FLIP.getVal());
    }

    public static int[] setWdrCmd(String val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.WDR.getVal();
        cmd[3] = 1;//DataLength
        WDR valObj = WDR.get(val);
        if(valObj == null)
            throw new Exception("Invalid WDR val");
        cmd[4] = valObj.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setWdrCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.WDR.getVal());
    }

    public static int[] setEisCmd(String val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.EIS.getVal();
        cmd[3] = 1;//DataLength
        EIS valObj = EIS.get(val);
        if(valObj == null)
            throw new Exception("Invalid EIS val");
        cmd[4] = valObj.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setEisCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.EIS.getVal());
    }

    public static int[] setMiscCmd(int val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.MISC.getVal();
        cmd[3] = 1;//DataLength
        cmd[4] = val;
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setMiscCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.MISC.getVal());
    }

    public static int[] setImgIRBrightnessCmd(int irbrightness_val) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.IRBRIGHTNESS.getVal();
        cmd[3] = 1;//DataLength
//        IRBRIGHTNESS irbrightness = IRBRIGHTNESS.get(irbrightness_val);
//        if(irbrightness == null)
//            throw new Exception("Invalid irbrightness val");
        cmd[4] = irbrightness_val;
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setImgIRBrightnessCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.IRBRIGHTNESS.getVal());
    }

    public static int[] setImgDayModeCmd(String daymode) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.DAYMODE.getVal();
        cmd[3] = 1;//DataLength
        DAYMODE daymode1 = DAYMODE.get(daymode);
        if(daymode1 == null)
            throw new Exception("Invalid daymode val");
        cmd[4] = daymode1.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static int[] setImgGyroReaderCmd(String gyroreader) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.IMAGE.getVal();
        cmd[2] = ImageSubCommands.GYROREADER.getVal();
        cmd[3] = 1;//DataLength
        GYROREADER gyroreader1 = GYROREADER.get(gyroreader);
        if(gyroreader1 == null)
            throw new Exception("Invalid gyroreader val");
        cmd[4] = gyroreader1.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setImgDayModeCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.DAYMODE.getVal());
    }

    public static boolean setImgGyroReaderCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.IMAGE.getVal(), ImageSubCommands.GYROREADER.getVal());
    }

    public static int[] setDefaultToFactoryCmd() throws Exception {
        int cmd[] = new int[5];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.CONFIG.getVal();
        cmd[2] = ConfigSetSubCommands.DefaultToFactory.getVal();
        cmd[3] = 0;//no data
        cmd[4] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setDefaultToFactoryCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.CONFIG.getVal(), ConfigSetSubCommands.DefaultToFactory.getVal());
    }

    public static int[] setDefaultToCurrentCmd() throws Exception {
        int cmd[] = new int[5];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.CONFIG.getVal();
        cmd[2] = ConfigSetSubCommands.DefaultToCurrent.getVal();
        cmd[3] = 0;//no data
        cmd[4] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setDefaultToCurrentCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.CONFIG.getVal(), ConfigSetSubCommands.DefaultToCurrent.getVal());
    }

    public static int[] setCurrentToFactoryCmd() throws Exception {
        int cmd[] = new int[5];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.CONFIG.getVal();
        cmd[2] = ConfigSetSubCommands.CurrentToFactory.getVal();
        cmd[3] = 0;//no data
        cmd[4] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setCurrentToFactoryCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.CONFIG.getVal(), ConfigSetSubCommands.CurrentToFactory.getVal());
    }

    public static int[] setCurrentToDefaultCmd() throws Exception {
        int cmd[] = new int[5];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.CONFIG.getVal();
        cmd[2] = ConfigSetSubCommands.CurrentToDefault.getVal();
        cmd[3] = 0;//no data
        cmd[4] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setCurrentToDefaultCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.CONFIG.getVal(), ConfigSetSubCommands.CurrentToDefault.getVal());
    }

/*    public static int[] setIPAddressAndSubnetMaskCmd(String ipAddress, String subnetMask) throws Exception{
        String ipAddressArray[] = ipAddress.split(".");
        if(ipAddressArray.length != 4) {
            throw new Exception("Invalid IpAddress");
        }
        String subnetMaskArray[] = subnetMask.split(".");
        if(subnetMaskArray.length != 4) {
            throw new Exception("Invalid subnet mask");
        }

        int cmd[] = new int[13];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.NETWORK.getVal();
        cmd[2] = NetworkSubCommands.IPAddressSubnetMask.getVal();
        cmd[3] = 8;
        cmd[4] = Integer.parseInt(ipAddressArray[0]);
        cmd[5] = Integer.parseInt(ipAddressArray[1]);
        cmd[6] = Integer.parseInt(ipAddressArray[2]);
        cmd[7] = Integer.parseInt(ipAddressArray[3]);
        cmd[8] = Integer.parseInt(subnetMaskArray[0]);
        cmd[9] = Integer.parseInt(subnetMaskArray[1]);
        cmd[10] = Integer.parseInt(subnetMaskArray[2]);
        cmd[11] = Integer.parseInt(subnetMaskArray[3]);
        cmd[12] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setIPAddressAndSubnetMaskCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.NETWORK.getVal(), NetworkSubCommands.IPAddressSubnetMask.getVal());
    }

    public static int[] getIPAddressAndSubnetMaskCmd() throws Exception {
        return getCmd(Commands.NETWORK.getVal(), NetworkSubCommands.IPAddressSubnetMask.getVal());
    }

    public static Map<String, Object> getIPAddressAndSubnetMaskCmdResponseParse(int response[], int length)
            throws Exception {
        int minDataLength = 7; //header + command + subcommand + datalength + success/error flag + success/error val + crc
        if(length < minDataLength) throw new Exception("Invalid response length");
        int header = response[0];
        int command = response[1];
        int subCommand = response[2];
        int dataLength = response[3];
        int s_or_e = response[4];

        if(header != Header.RESPONSE.getVal()) {
            throw new Exception("Invalid header in response");
        }
        if(command != Commands.NETWORK.getVal()) {
            throw new Exception("Invalid command in response");
        }
        if(subCommand != NetworkSubCommands.IPAddressSubnetMask.getVal()) {
            throw new Exception("Invalid sub command in response");
        }
        if((s_or_e == 0 && dataLength != 8) || (s_or_e == 1 && dataLength != 2)) {
            throw new Exception("Invalid data/data length in response");
        }

        if(s_or_e == 0) {//0 success
            Map<String, Object> config = new HashMap<>(2);
            String ipAddressBuilder = String.valueOf(response[5]) +
                    "." +
                    response[6] +
                    "." +
                    response[7] +
                    "." +
                    response[8];
            String subnetMaskBuilder = String.valueOf(response[9]) +
                    "." +
                    response[10] +
                    "." +
                    response[11] +
                    "." +
                    response[12];

            config.put("ipaddress", ipAddressBuilder);
            config.put("subnetmask", subnetMaskBuilder);
            return config;
        } else if(s_or_e == 1){//1 failed
            int e_val = response[5];
            throw new Exception("error response val="+ e_val);
        } else {
            throw new Exception("Invalid data in response");
        }
    }*/

    public static int[] setWifiHotspotCmd(String ssid, String encryptionTypeStr, String encryptionKey, String ipAddress, String subnetMask) throws Exception{
        return setWifiCmd(ssid, encryptionTypeStr, encryptionKey, ipAddress, subnetMask, NetworkSubCommands.WifiHotspot);
    }

    public static boolean setWifiHotspotCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.NETWORK.getVal(), NetworkSubCommands.WifiHotspot.getVal());
    }

    public static int[] setWifiClientCmd(String ssid, String encryptionTypeStr, String encryptionKey, String ipAddress, String subnetMask) throws Exception{
        return setWifiCmd(ssid, encryptionTypeStr, encryptionKey, ipAddress, subnetMask, NetworkSubCommands.WifiClient);
    }

    public static boolean setWifiClientCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.NETWORK.getVal(), NetworkSubCommands.WifiClient.getVal());
    }

    public static int[] getWifiHotspotCmd() throws Exception {
        return getCmd(Commands.NETWORK.getVal(), NetworkSubCommands.WifiHotspot.getVal());
    }

    public static int[] getWifiClientCmd() throws Exception {
        return getCmd(Commands.NETWORK.getVal(), NetworkSubCommands.WifiClient.getVal());
    }

    public static Map<String, Object> getWifiHotspotCmdResponseParse(int response[], int length) throws Exception {
        return getWifiCmdResponseParse(response, length, NetworkSubCommands.WifiHotspot);
    }

    public static Map<String, Object> getWifiClientCmdResponseParse(int response[], int length) throws Exception {
        return getWifiCmdResponseParse(response, length, NetworkSubCommands.WifiClient);
    }

    public static int[] getWifiStateCmd() throws Exception {
        return getCmd(Commands.NETWORK.getVal(), NetworkSubCommands.WifiState.getVal());
    }

    public static WifiState getWifiStateCmdResponseParse(int response[], int length) throws Exception {
        if(length < 7) throw new Exception("Invalid response length");
        int header = response[0];
        int command = response[1];
        int subCommand = response[2];
        int dataLength = response[3];

        if(header != Header.RESPONSE.getVal()) {
            throw new Exception("Invalid header in response");
        }
        if(command != Commands.NETWORK.getVal()) {
            throw new Exception("Invalid command in response");
        }
        if(subCommand != NetworkSubCommands.WifiState.getVal()) {
            throw new Exception("Invalid sub command in response");
        }
        if(dataLength != 2) {
            throw new Exception("Invalid data/data length in response");
        }

        int s_or_e = response[4];
        if(s_or_e == 0) {//0 success
            int s_val = response[5];
            if(s_val == WifiState.WifiHotspot.getVal()) {
                return WifiState.WifiHotspot;
            } else if(s_val == WifiState.WifiClient.getVal()) {
                return WifiState.WifiClient;
            } else {
                throw new Exception("Invalid success val in response");
            }
        } else if(s_or_e == 1){//1 failed
            int e_val = response[5];
            throw new Exception("error response val="+ e_val);
        } else {
            throw new Exception("Invalid data in response");
        }
    }


    public static int[] setAudioMicCmd(String mic) throws Exception {
        int cmd[] = new int[6];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.AUDIO.getVal();
        cmd[2] = AudioSubCommands.MIC.getVal();
        cmd[3] = 1;//DataLength
        MIC mic1 = MIC.get(mic);
        if(mic1 == null)
            throw new Exception("Invalid mic val");
        cmd[4] = mic1.getVal();
        cmd[5] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean setAudioMicCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.AUDIO.getVal(), AudioSubCommands.MIC.getVal());
    }

    public static int[] shutdownCmd() {
        int cmd[] = new int[5];
        cmd[0] = Header.SET.getVal();
        cmd[1] = Commands.SYSTEM.getVal();
        cmd[2] = SystemSubCommands.SHUTDOWN.getVal();
        cmd[3] = 0;//no data
        cmd[4] = 0;//crc will be calculated before sending cmd
        return cmd;
    }

    public static boolean shutdownCmdResponseParse(int response[], int length) throws Exception {
        return setCmdResponseParse(response, length, Commands.SYSTEM.getVal(), SystemSubCommands.SHUTDOWN.getVal());
    }

    /*public static void uploadPatchFile(String ipAddress, String username, String pwd, String filePath, String storeFilePath) {
        System.out.println("filePath="+filePath);
        FTPClient con = null;
        try
        {
            con = new FTPClient();
            con.connect(ipAddress);
            if (con.login(username, pwd))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);
                FileInputStream in = new FileInputStream(new File(filePath));
                boolean result = con.storeFile(storeFilePath, in);
                in.close();
                if (result) Log.v("upload result", "succeeded");
                con.logout();
                con.disconnect();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/


    public static int[] getHealthCheckCmd() {
        int[] cmd = new int[5];
        cmd[0] = Header.GET.getVal();                  // 0x02
        cmd[1] = Commands.SYSTEM.getVal();             // 0x06
        cmd[2] = SystemSubCommands.HEALTH_CHECK.getVal(); // 0x06
        cmd[3] = 0; // Data length = 0
//        cmd[4] = 0; // CRC will be computed before send
        return cmd;
    }

    public static HealthStatus parseHealthCheckResponse(int[] response, int length) throws Exception {
        // Validate packet length
        if (length < 14)
            throw new Exception("Incomplete health check response. Expected 13, got " + length);

        // Validate header
        if (response[0] != Header.RESPONSE.getVal())
            throw new Exception("Invalid header: " + response[0]);

        // Validate command & sub-command
        if (response[1] != Commands.SYSTEM.getVal() || response[2] != SystemSubCommands.HEALTH_CHECK.getVal())
            throw new Exception("Command mismatch");

        // Check success flag
        if (response[4] != 0)
            throw new Exception("Health check failed");

        // Parse data
        return new HealthStatus(
                response[5] == 1,    // rtsps
                response[6] == 1,    // portableRtc
                response[7] == 1,         // CPU %
                response[8],         // Memory %
                response[9],         // ISP Temp
                response[10],        // IR Temp
                response[11],
                response[12]
        );
    }






}
