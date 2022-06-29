package com.sunmi.v2.printer;

import android.content.BroadcastReceiver;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map;
import java.util.HashMap;

// import woyou.aidlservice.jiuiv5.IWoyouService;
// import woyou.aidlservice.jiuiv5.ICallback;
import android.os.RemoteException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.graphics.Bitmap;
import android.util.Log;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.IntentFilter;
import android.widget.Toast;

import com.sunmi.peripheral.printer.ExceptionConst;
import com.sunmi.peripheral.printer.InnerLcdCallback;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;
import com.sunmi.peripheral.printer.WoyouConsts;
import com.sunmi.printerhelper.R;


public class SunmiV2PrinterModule extends ReactContextBaseJavaModule {
    public static ReactApplicationContext reactApplicationContext = null;
    private IWoyouService woyouService;
    private BitmapUtils bitMapUtils;
    private PrinterReceiver receiver = new PrinterReceiver();

    public static String noPrinter = "NO_PRINTER_INIT";
    public static int NoSunmiPrinter = 0x00000000;
    public static int CheckSunmiPrinter = 0x00000001;
    public static int FoundSunmiPrinter = 0x00000002;
    public static int LostSunmiPrinter = 0x00000003;

    /**
     *  sunmiPrinter means checking the printer connection status
     */
    public int sunmiPrinter = CheckSunmiPrinter;
    /**
     *  SunmiPrinterService for API
     */
    private SunmiPrinterService sunmiPrinterService;

    private static SunmiV2PrinterModule helper = new SunmiV2PrinterModule();

    private SunmiV2PrinterModule() {}

    public static SunmiV2PrinterModule getInstance() {
        return helper;
    }

    private InnerPrinterCallback innerPrinterCallback = new InnerPrinterCallback() {
        @Override
        protected void onConnected(SunmiPrinterService service) {
            sunmiPrinterService = service;
            checkSunmiPrinterService(service);
        }

        @Override
        protected void onDisconnected() {
            sunmiPrinterService = null;
            sunmiPrinter = LostSunmiPrinter;
        }
    };

    // public final static String OUT_OF_PAPER_ACTION = "woyou.aidlservice.jiuv5.OUT_OF_PAPER_ACTION";
    // public final static String ERROR_ACTION = "woyou.aidlservice.jiuv5.ERROR_ACTION";
    // public final static String NORMAL_ACTION = "woyou.aidlservice.jiuv5.NORMAL_ACTION";
    // public final static String COVER_OPEN_ACTION = "woyou.aidlservice.jiuv5.COVER_OPEN_ACTION";
    // public final static String COVER_ERROR_ACTION = "woyou.aidlservice.jiuv5.COVER_ERROR_ACTION";
    // public final static String KNIFE_ERROR_1_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_1";
    // public final static String KNIFE_ERROR_2_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_2";
    // public final static String OVER_HEATING_ACITON = "woyou.aidlservice.jiuv5.OVER_HEATING_ACITON";
    // public final static String FIRMWARE_UPDATING_ACITON = "woyou.aidlservice.jiuv5.FIRMWARE_UPDATING_ACITON";
    
    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Service disconnected: " + name);
            woyouService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Service connected: " + name);
            // woyouService = IWoyouService.Stub.asInterface(service);
        }
    };

    private static final String TAG = "SunmiV2PrinterModule";

    public SunmiV2PrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactApplicationContext = reactContext;
        Intent intent = new Intent();
        // intent.setPackage("woyou.aidlservice.jiuiv5");
        // intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        reactContext.startService(intent);
        reactContext.bindService(intent, connService, Context.BIND_AUTO_CREATE);
        bitMapUtils = new BitmapUtils(reactContext);
        IntentFilter mFilter = new IntentFilter();
        // mFilter.addAction(OUT_OF_PAPER_ACTION);
        // mFilter.addAction(ERROR_ACTION);
        // mFilter.addAction(NORMAL_ACTION);
        // mFilter.addAction(COVER_OPEN_ACTION);
        // mFilter.addAction(COVER_ERROR_ACTION);
        // mFilter.addAction(KNIFE_ERROR_1_ACTION);
        // mFilter.addAction(KNIFE_ERROR_2_ACTION);
        // mFilter.addAction(OVER_HEATING_ACITON);
        // mFilter.addAction(FIRMWARE_UPDATING_ACITON);
        getReactApplicationContext().registerReceiver(receiver, mFilter);
    }

    @Override
    public String getName() {
        return "SunmiV2Printer";
    }


    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        final Map<String, Object> constantsChildren = new HashMap<>();


        constants.put("Constants", constantsChildren);

        constants.put("hasPrinter", hasPrinter());

        try {
            constants.put("printerVersion", getPrinterVersion());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerSerialNo", getPrinterSerialNo());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerModal", getPrinterModal());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }

        return constants;
    }

    /**
     * Initialize the printer, reset the logic program of the printer, but do not clear the buffer data, so
     * Incomplete print jobs will continue after reset
     *
     * @return
     */    
    @ReactMethod
    public void printerInit(final Promise promise) {
        try {
            boolean ret =  InnerPrinterManager.getInstance().bindService(context,innerPrinterCallback);
            if(!ret){
                sunmiPrinter = NoSunmiPrinter;
                promise.reject("" + 0, noPrinter);
                
            }
            promise.resolve(null);

        } catch (InnerPrinterException e) {
            e.printStackTrace();
            promise.reject("" + 0, e.getMessage());

        }
    }

    /**
     * Printer self-test, the printer will print a self-test page
     *
     * @param callback resault callback
     */
    // @ReactMethod
    // public void printerSelfChecking(final Promise p) {
    //     // final IWoyouService printerService = woyouService;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 printerService.printerSelfChecking(new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int par1, String par2) {
    //                         Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             p.resolve(null);
    //                         } else {
    //                             p.reject("0", isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         p.resolve(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         p.reject("" + code, msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 p.reject("" + 0, e.getMessage());
    //             }
    //         }
    //     });
    // }

    /**
     * Get the printer board serial number
     */
    // @ReactMethod
    // public void getPrinterSerialNo(final Promise p) {
    //     try {
    //         p.resolve(getPrinterSerialNo());
    //     } catch (Exception e) {
    //         Log.i(TAG, "ERROR: " + e.getMessage());
    //         p.reject("" + 0, e.getMessage());
    //     }
    // }

    // private String getPrinterSerialNo() throws Exception {
    //     // final IWoyouService printerService = woyouService;
    //     return printerService.getPrinterSerialNo();
    // }

    /**
     * Get the printer firmware version number
     */
    // @ReactMethod
    // public void getPrinterVersion(final Promise p) {
    //     try {
    //         p.resolve(getPrinterVersion());
    //     } catch (Exception e) {
    //         Log.i(TAG, "ERROR: " + e.getMessage());
    //         p.reject("" + 0, e.getMessage());
    //     }
    // }

    // private String getPrinterVersion() throws Exception {
    //     // final IWoyouService printerService = woyouService;
    //     return printerService.getPrinterVersion();
    // }

    /**
     * Get the printer model
     */
    // @ReactMethod
    // public void getPrinterModal(final Promise p) {
    //     try {
    //         p.resolve(getPrinterModal());
    //     } catch (Exception e) {
    //         Log.i(TAG, "ERROR: " + e.getMessage());
    //         p.reject("" + 0, e.getMessage());
    //     }
    // }

    // private String getPrinterModal() throws Exception {
    //     //Caution: This method is not fully test -- Januslo 2018-08-11
    //     // final IWoyouService printerService = woyouService;
    //     return printerService.getPrinterModal();
    // }

    // @ReactMethod
    // public void hasPrinter(final Promise p) {
    //     try {
    //         p.resolve(hasPrinter());
    //     } catch (Exception e) {
    //         Log.i(TAG, "ERROR: " + e.getMessage());
    //         p.reject("" + 0, e.getMessage());
    //     }
    // }

    /**
     * Is there a printer service
     * return {boolean}
     */
    // private boolean hasPrinter() {
    //     // final IWoyouService printerService = woyouService;
    //     final boolean hasPrinterService = printerService != null;
    //     return hasPrinterService;
    // }
    /**
     * Get print head print length
     */
    // @ReactMethod
    // public void getPrintedLength(final Promise p) {
    //     // final IWoyouService printerService = woyouService;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 printerService.getPrintedLength(new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int par1, String par2) {
    //                         Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             p.resolve(null);
    //                         } else {
    //                             p.reject("0", isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         p.resolve(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         p.reject("" + code, msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 p.reject("" + 0, e.getMessage());
    //             }
    //         }
    //     });
    // }

    /**
     * The printer feeds the paper (forced line feed, and feeds n lines after finishing the previous print content)
     *
     * @param n:       Lines of paper
     * @param callback resault callback
     * @return
     */
    // @ReactMethod
    // public void lineWrap(int n, final Promise p) {
    //     // final IWoyouService ss = woyouService;
    //     final int count = n;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.lineWrap(count, new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int par1, String par2) {
    //                         Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             p.resolve(null);
    //                         } else {
    //                             p.reject("0", isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         p.resolve(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         p.reject("" + code, msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 p.reject("" + 0, e.getMessage());
    //             }
    //         }
    //     });
    // }

    /**
     * print with raw instructions
     *
     * @param data     instruction
     * @param callback resault callback
     */
    // @ReactMethod
    public void sendRAWData(String base64EncriptedData, final Promise promise) {

        public void sendRawData(byte[] data) {
            if(sunmiPrinterService == null){
                promise.reject("" + 0, noPrinter);
            }
            try {
                final byte[] data = Base64.decode(base64EncriptedData, Base64.DEFAULT);

                sunmiPrinterService.sendRAWData(data, null);
                promise.resolve(null);

            } catch (RemoteException e) {
                handleRemoteException(e);
                Log.i(TAG, "ERROR: " + e.getMessage());
                p.reject("" + 0, e.getMessage());
            }
        }
    }

    /**
     * Set the alignment mode, which will affect subsequent printing unless initialized
     *
     * @param alignment: Alignment 0--left, 1--center, 2--right
     * @param callback   resault callback
     */
    // @ReactMethod
    public void setAlignment(int alignment, final Promise promise) {
        if(sunmiPrinterService == null){
            promise.reject("" + 0, noPrinter);
            return;
        }
        try {
            sunmiPrinterService.setAlignment(alignment, null);
            promise.resolve(null);
        } catch (RemoteException e) {
            handleRemoteException(e);
            promise.reject("" + 0, e.getMessage());
        }
    }

    /**
     * Set the print font, which will affect subsequent printing unless initialized
     * (Only one font "gh" is currently supported, gh is a monospaced Chinese font, and more font choices will be provided in the future)
     *
     * @param typeface: font name
     */
    // @ReactMethod
    // public void setFontName(String typeface, final Promise p) {
    //     // final IWoyouService ss = woyouService;
    //     final String tf = typeface;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.setFontName(tf, new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int par1, String par2) {
    //                         Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             p.resolve(null);
    //                         } else {
    //                             p.reject("0", isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         p.resolve(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         p.reject("" + code, msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 p.reject("" + 0, e.getMessage());
    //             }
    //         }
    //     });
    // }

    /**
     * Set the font size, which will affect subsequent printing unless initialized
     * Note: Font size is beyond standard international instructions for printing,
     * Adjusting the font size will affect the character width, and the number of characters per line will also change,
     * Therefore, typography in monospaced fonts may be messed up
     *
     * @param fontsize: font size
     */
    // @ReactMethod
    // public void setFontSize(float fontsize, final Promise p) {
    //     // final IWoyouService ss = woyouService;
    //     final float fs = fontsize;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.setFontSize(fs, new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int par1, String par2) {
    //                         Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             p.resolve(null);
    //                         } else {
    //                             p.reject("0", isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         p.resolve(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         p.reject("" + code, msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 p.reject("" + 0, e.getMessage());
    //             }
    //         }
    //     });
    // }


    /**
     * Print the text of the specified font, the font setting is only valid for this time
     *
     * @param text:     to print text
     * @param typeface: Font name (currently only "gh" font is supported)
     * @param fontsize: font size
     */
    // @ReactMethod
    // public void printTextWithFont(String text, String typeface, float fontsize, final Promise p) {
    //     // final IWoyouService ss = woyouService;
    //     final String txt = text;
    //     final String tf = typeface;
    //     final float fs = fontsize;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.printTextWithFont(txt, tf, fs, new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int par1, String par2) {
    //                         Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             p.resolve(null);
    //                         } else {
    //                             p.reject("0", isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         p.resolve(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         p.reject("" + code, msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 p.reject("" + 0, e.getMessage());
    //             }
    //         }
    //     });
    // }

    /**
     * 打印表格的一行，可以指定列宽、对齐方式
     *
     * @param colsTextArr  各列文本字符串数组
     * @param colsWidthArr 各列宽度数组(以英文字符计算, 每个中文字符占两个英文字符, 每个宽度大于0)
     * @param colsAlign    各列对齐方式(0居左, 1居中, 2居右)
     *                     备注: 三个参数的数组长度应该一致, 如果colsText[i]的宽度大于colsWidth[i], 则文本换行
     */
    // @ReactMethod
    // public void printColumnsText(ReadableArray colsTextArr, ReadableArray colsWidthArr, ReadableArray colsAlign, final Promise p) {
    //     // final IWoyouService ss = woyouService;
    //     final String[] clst = new String[colsTextArr.size()];
    //     for (int i = 0; i < colsTextArr.size(); i++) {
    //         clst[i] = colsTextArr.getString(i);
    //     }
    //     final int[] clsw = new int[colsWidthArr.size()];
    //     for (int i = 0; i < colsWidthArr.size(); i++) {
    //         clsw[i] = colsWidthArr.getInt(i);
    //     }
    //     final int[] clsa = new int[colsAlign.size()];
    //     for (int i = 0; i < colsAlign.size(); i++) {
    //         clsa[i] = colsAlign.getInt(i);
    //     }
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.printColumnsText(clst, clsw, clsa, new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int par1, String par2) {
    //                         Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             p.resolve(null);
    //                         } else {
    //                             p.reject("0", isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         p.resolve(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         p.reject("" + code, msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 p.reject("" + 0, e.getMessage());
    //             }
    //         }
    //     });
    // }


    /**
     * 打印图片
     *
     * @param bitmap: 图片bitmap对象(最大宽度384像素，超过无法打印并且回调callback异常函数)
     */
    @ReactMethod
    public void printBitmap(String data, int width, int height, int orientation, final Promise promise) {
        if(sunmiPrinterService == null){
            promise.reject("" + 0, noPrinter);
            return;
        }

        try {
            byte[] decoded = Base64.decode(data, Base64.DEFAULT);
            final Bitmap bitMap = bitMapUtils.decodeBitmap(decoded, width, height);
            // TODO review
            if(orientation == 0){
                sunmiPrinterService.printBitmap(bitmap, null);
                sunmiPrinterService.printText("横向排列\n", null);
                sunmiPrinterService.printBitmap(bitmap, null);
                sunmiPrinterService.printText("横向排列\n", null);
            }else{
                sunmiPrinterService.printBitmap(bitmap, null);
                sunmiPrinterService.printText("\n纵向排列\n", null);
                sunmiPrinterService.printBitmap(bitmap, null);
                sunmiPrinterService.printText("\n纵向排列\n", null);
            }
            promise.resolve(null);
        } catch (RemoteException e) {
            e.printStackTrace();
            promise.reject("" + 0, e.getMessage());
        }
    }

    /**
     * 打印一维条码
     *
     * @param data:         条码数据
     * @param symbology:    条码类型
     *                      0 -- UPC-A，
     *                      1 -- UPC-E，
     *                      2 -- JAN13(EAN13)，
     *                      3 -- JAN8(EAN8)，
     *                      4 -- CODE39，
     *                      5 -- ITF，
     *                      6 -- CODABAR，
     *                      7 -- CODE93，
     *                      8 -- CODE128
     * @param height:       条码高度, 取值1到255, 默认162
     * @param width:        条码宽度, 取值2至6, 默认2
     * @param textposition: 文字位置 0--不打印文字, 1--文字在条码上方, 2--文字在条码下方, 3--条码上下方均打印
     */
    // @ReactMethod
    // public void printBarCode(String data, int symbology, int height, int width, int textposition, final Promise p) {
    //     // final IWoyouService ss = woyouService;
    //     Log.i(TAG, "come: ss:" + ss);
    //     final String d = data;
    //     final int s = symbology;
    //     final int h = height;
    //     final int w = width;
    //     final int tp = textposition;

    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.printBarCode(d, s, h, w, tp, new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int par1, String par2) {
    //                         Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             p.resolve(null);
    //                         } else {
    //                             p.reject("0", isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         p.resolve(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         p.reject("" + code, msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 p.reject("" + 0, e.getMessage());
    //             }
    //         }
    //     });
    // }

    /**
     * 打印二维条码
     *
     * @param data:       二维码数据
     * @param modulesize: 二维码块大小(单位:点, 取值 1 至 16 )
     * @param errorlevel: 二维码纠错等级(0 至 3)，
     *                    0 -- 纠错级别L ( 7%)，
     *                    1 -- 纠错级别M (15%)，
     *                    2 -- 纠错级别Q (25%)，
     *                    3 -- 纠错级别H (30%)
     */
    // @ReactMethod
    // public void printQRCode(String data, int modulesize, int errorlevel, final Promise p) {
    //     // final IWoyouService ss = woyouService;
    //     Log.i(TAG, "come: ss:" + ss);
    //     final String d = data;
    //     final int size = modulesize;
    //     final int level = errorlevel;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.printQRCode(d, size, level, new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int par1, String par2) {
    //                         Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             p.resolve(null);
    //                         } else {
    //                             p.reject("0", isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         p.resolve(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         p.reject("" + code, msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 p.reject("" + 0, e.getMessage());
    //             }
    //         }
    //     });
    // }

    /**
     * 打印文字，文字宽度满一行自动换行排版，不满一整行不打印除非强制换行
     * 文字按矢量文字宽度原样输出，即每个字符不等宽
     *
     * @param text: 要打印的文字字符串
     */
    @ReactMethod
    public void printOriginalText(String text, String typeface,final Promise promise) {
        if(sunmiPrinterService == null){
            promise.reject("" + 0, noPrinter);
            return;
        }
        
        try {
            final Int size = text.length();
            sunmiPrinterService.printTextWithFont(text, typeface, size, null);

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
            
    }

    /**
     * 打印缓冲区内容
     */
    // @ReactMethod
    // public void commitPrinterBuffer() {
    //     // final IWoyouService ss = woyouService;
    //     Log.i(TAG, "come: commit buffter ss:" + ss);
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.commitPrinterBuffer();
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //             }
    //         }
    //     });
    // }

    /**
     * 进入缓冲模式，所有打印调用将缓存，调用commitPrinterBuffe()后打印
     *
     * @param clean: 是否清除缓冲区内容
     */
    // @ReactMethod
    // public void enterPrinterBuffer(boolean clean) {
    //     // final IWoyouService ss = woyouService;
    //     Log.i(TAG, "come: " + clean + " ss:" + ss);
    //     final boolean c = clean;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.enterPrinterBuffer(c);
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //             }
    //         }
    //     });
    // }

    /**
     * 退出缓冲模式
     *
     * @param commit: 是否打印出缓冲区内容
     */
    // @ReactMethod
    // public void exitPrinterBuffer(boolean commit) {
    //     // final IWoyouService ss = woyouService;
    //     Log.i(TAG, "come: " + commit + " ss:" + ss);
    //     final boolean com = commit;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.exitPrinterBuffer(com);
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //             }
    //         }
    //     });
    // }


    // @ReactMethod
    // public void printString(String message, final Promise p) {
    //     // final IWoyouService ss = woyouService;
    //     Log.i(TAG, "come: " + message + " ss:" + ss);
    //     final String msgs = message;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.printText(msgs, new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int par1, String par2) {
    //                         Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             p.resolve(null);
    //                         } else {
    //                             p.reject("0", isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         p.resolve(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         p.reject("" + code, msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 p.reject("" + 0, e.getMessage());
    //             }
    //         }
    //     });
    // }

    @ReactMethod
    public void clearBuffer(final Promise promise    ) {
        try {
            if(sunmiPrinterService != null){
                InnerPrinterManager.getInstance().unBindService(context, innerPrinterCallback);
                sunmiPrinterService = null;
                sunmiPrinter = LostSunmiPrinter;
            }
            promise.resolve(null);
        } catch (InnerPrinterException e) {
            e.printStackTrace();
            promise.reject("" + 0, e.getMessage());
        }
    }

    // @ReactMethod
    // public void exitPrinterBufferWithCallback(final boolean commit, final Callback callback) {
    //     // final IWoyouService ss = woyouService;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 ss.exitPrinterBufferWithCallback(commit, new ICallback.Stub() {
    //                     @Override
    //                     public void onPrintResult(int code, String msg) {
    //                         Log.d(TAG, "ON PRINT RES: " + code + ", " + msg);
    //                         if (code == 0)
    //                             callback.invoke(true);
    //                         else
    //                             callback.invoke(false);
    //                     }

    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         callback.invoke(isSuccess);
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         // callback.invoke(isSuccess);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         callback.invoke(false);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 callback.invoke(false);
    //             }
    //         }
    //     });
    // }

    @ReactMethod
    public void openCashDrawer(final Promise promise) {
        if(sunmiPrinterService == null){
            promise.reject("" + 0, noPrinter);
            return;
        }

        try {
            sunmiPrinterService.openDrawer(null);
            promise.resolve(null);

        } catch (RemoteException e) {
            handleRemoteException(e);
            promise.reject("" + 0, e.getMessage());

        }
    }
    @ReactMethod
    public void cutPaper(final Promise promise) {
        if(sunmiPrinterService == null){
            promise.reject("" + 0, noPrinter);
            return;
        }
        try {
            sunmiPrinterService.cutPaper(null);
            promise.resolve(null);
        } catch (RemoteException e) {
            handleRemoteException(e);
            promise.reject("" + 0, e.getMessage());
        }
    }
    
}
