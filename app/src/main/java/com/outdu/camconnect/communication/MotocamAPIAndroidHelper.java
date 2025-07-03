//package com.outdu.camconnect.communication;
//
//import android.os.AsyncTask;
//import android.util.Log;
//
//import java.net.InetAddress;
//import java.util.Map;
//
//public class MotocamAPIAndroidHelper {
//
//    private static final String TAG = "MotocamAPIAndroidHelper";
//
//    public static class GetConfigTask extends AsyncTask<String, Void, Map<String, Object>> {
//
//        public interface Callback {
//            void onComplete(Map<String, Object> result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public GetConfigTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Map<String, Object> doInBackground(String... config) {
//            Log.i(TAG, "GetConfigTask");
//            Log.i(TAG, config[0]);
//            Map<String, Object> configMap = null;
//            try {
//                configMap = MotocamAPIHelperWrapper.getConfig(config[0]);
//            } catch (Exception e) {
//                Log.e(TAG, e.getMessage());
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            Log.i(TAG, "GetConfigTask Done");
//            return configMap;
//        }
//
//        @Override
//        protected void onPostExecute(Map<String, Object> result) {
//            Log.i(TAG, "GetConfigTask onPostExecute");
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetIrBrightnessTask extends AsyncTask<Integer , Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetIrBrightnessTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(Integer... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setIrBrightness(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetZoomTask extends AsyncTask<MotocamAPIHelper.ZOOM, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetZoomTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.ZOOM... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setImgZoom(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetResolutionTask extends AsyncTask<MotocamAPIHelper.RESOLUTION, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetResolutionTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.RESOLUTION... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setImgResolution(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetTiltTask extends AsyncTask<MotocamAPIHelper.TILT, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetTiltTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.TILT... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setImgTilt(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetRotationTask extends AsyncTask<MotocamAPIHelper.ROTATION, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetRotationTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.ROTATION... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setImgRotation(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetIrcutfilterTask extends AsyncTask<MotocamAPIHelper.IRCUTFILTER, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetIrcutfilterTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.IRCUTFILTER... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setIrCutFilter(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetMirrorTask extends AsyncTask<MotocamAPIHelper.MIRROR, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetMirrorTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.MIRROR... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setMirror(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetFlipTask extends AsyncTask<MotocamAPIHelper.FLIP, Void, Boolean> {
//
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetFlipTask(Callback callback1) {
//            Log.i("Flip FUnctionality Check", "construnctor1--------");
//
//            this.callback = callback1;
//            Log.i("Flip FUnctionality Check", "construnctor2-----------");
//
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.FLIP... config) {
//            boolean result = false;
//            try {
//                Log.i("Flip FUnctionality Check", "do in bg");
//
//                result = MotocamAPIHelperWrapper.newInstance().setFlip(config[0]);
//                Log.i("Flip FUnctionality Check", "do in bg end");
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetWdrTask extends AsyncTask<MotocamAPIHelper.WDR, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetWdrTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.WDR... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setWdr(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetEisTask extends AsyncTask<MotocamAPIHelper.EIS, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetEisTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.EIS... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setEis(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetMiscTask extends AsyncTask<Integer, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetMiscTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(Integer... config) {
//            boolean result = false;
//            try {
//                Log.i("@@@@", String.format("misc mode=%d", config[0]));
//                result = MotocamAPIHelperWrapper.newInstance().setMisc(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetDayModeTask extends AsyncTask<MotocamAPIHelper.DAYMODE, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetDayModeTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.DAYMODE... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setDayMode(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetGyroReaderTask extends AsyncTask<MotocamAPIHelper.GYROREADER, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetGyroReaderTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.GYROREADER... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setGyroReader(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetMicTask extends AsyncTask<MotocamAPIHelper.MIC, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetMicTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(MotocamAPIHelper.MIC... config) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setMic(config[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetDefaultToCurrentTask extends AsyncTask<Void, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetDefaultToCurrentTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(Void... val) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setDefaultToCurrent();
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetDefaultToFactoryTask extends AsyncTask<Void, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetDefaultToFactoryTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(Void... val) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setDefaultToFactory();
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetCurrentToDefaultTask extends AsyncTask<Void, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetCurrentToDefaultTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(Void... val) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setCurrentToDefault();
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetCurrentToFactoryTask extends AsyncTask<Void, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetCurrentToFactoryTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(Void... val) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setCurrentToFactory();
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class GetWifiStateTask extends AsyncTask<Void, Void, MotocamAPIHelper.WifiState> {
//
//        public interface Callback {
//            void onComplete(MotocamAPIHelper.WifiState result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public GetWifiStateTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected MotocamAPIHelper.WifiState doInBackground(Void... val) {
//            MotocamAPIHelper.WifiState result=null;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().getWifiState();
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(MotocamAPIHelper.WifiState result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetWifiHotspotTask extends AsyncTask<String, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetWifiHotspotTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(String... wifiHotspot) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setWifiHotspot(wifiHotspot[0], wifiHotspot[1],
//                        wifiHotspot[2],wifiHotspot[3],wifiHotspot[4]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class SetWifiClientTask extends AsyncTask<String, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public SetWifiClientTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(String... wifiHotspot) {
//            boolean result = false;
//            try {
//                result = MotocamAPIHelperWrapper.newInstance().setWifiClient(wifiHotspot[0], wifiHotspot[1],
//                        wifiHotspot[2],wifiHotspot[3],wifiHotspot[4]);
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class GetWifiHotspotConfigTask extends AsyncTask<Void, Void, Map<String, Object>> {
//
//        public interface Callback {
//            void onComplete(Map<String, Object> result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public GetWifiHotspotConfigTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Map<String, Object> doInBackground(Void... val) {
//            Map<String, Object> configMap = null;
//            try {
//                configMap = MotocamAPIHelperWrapper.newInstance().getWifiHotspotConfig();
//            } catch (Exception e) {
//                Log.e(TAG, e.getMessage());
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return configMap;
//        }
//
//        @Override
//        protected void onPostExecute(Map<String, Object> result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class GetWifiClientConfigTask extends AsyncTask<Void, Void, Map<String, Object>> {
//
//        public interface Callback {
//            void onComplete(Map<String, Object> result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public GetWifiClientConfigTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Map<String, Object> doInBackground(Void... val) {
//            Map<String, Object> configMap = null;
//            try {
//                configMap = MotocamAPIHelperWrapper.newInstance().getWifiClientConfig();
//            } catch (Exception e) {
//                Log.e(TAG, e.getMessage());
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return configMap;
//        }
//
//        @Override
//        protected void onPostExecute(Map<String, Object> result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class GetDeviceNameTask extends AsyncTask<String, Void, String> {
//
//        public interface Callback {
//            void onComplete(String result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public GetDeviceNameTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected String doInBackground(String... val) {
//            String result = null;
//            try {
//                InetAddress device=InetAddress.getByName(val[0]);
//                result = device.getHostName();
//            } catch (Exception e) {
//                Log.e(TAG, e.getMessage());
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class StartStreamTask extends AsyncTask<Void, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public StartStreamTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(Void... val) {
//            boolean result = false;
//            try {
//                Log.i(TAG,"StartStreamTask");
//
//                result = MotocamAPIHelperWrapper.newInstance().startStream();
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//    public static class RebootTask extends AsyncTask<Void, Void, Boolean> {
//
//        public interface Callback {
//            void onComplete(Boolean result, String exception);
//        }
//
//        private final Callback callback;
//        private String exception=null;
//
//        public RebootTask(Callback callback1) {
//            this.callback = callback1;
//        }
//        @Override
//        protected Boolean doInBackground(Void... val) {
//            boolean result = false;
//            try {
//                Log.i(TAG,"RebootTask");
//                result = MotocamAPIHelperWrapper.newInstance().shutdownCamera();
//            } catch (Exception e) {
//                e.printStackTrace();
//                exception=e.getMessage();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            this.callback.onComplete(result, exception);
//        }
//    }
//
//}
