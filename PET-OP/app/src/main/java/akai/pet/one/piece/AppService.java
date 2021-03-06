package akai.pet.one.piece;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import akai.floatView.op.zoro.R;
import akai.pet.one.piece.person.Chopper;
import akai.pet.one.piece.person.DownloadPerson;
import akai.pet.one.piece.person.Law;
import akai.pet.one.piece.person.Luffy;
import akai.pet.one.piece.person.Nanjing;
import akai.pet.one.piece.person.Panda;
import akai.pet.one.piece.person.Person;
import akai.pet.one.piece.person.Zoro;
import akai.pet.one.piece.settings.MainSettings;
import akai.pet.one.piece.store.KLog2File;

public class AppService extends Service implements OnSharedPreferenceChangeListener{
    private WindowManager windowManager;
    /**悬浮窗界面的显示*/
    public static LayoutParams layoutParams;
    /**属于桌面的应用包名称*/
    private List<String> homePackageNames;
    /**线程的使用*/
    private Handler handler = new Handler();
    private DrawRunnable drawRunnable = new DrawRunnable();
    private HomeRunnable homeRunnable = new HomeRunnable();
    private ChangRunnable changRunnable = new ChangRunnable();
    /**人物类*/
    private Person person;
    /**存储数据*/
    private SharedPreferences sp;
    /**是否在桌面的标识*/
    private boolean isHome = true;
    /**每帧运行时间,默认150*/
    private int frameTime;
    /**随机动画改变时间,默认5秒*/
    private int randomTime;
    /**注册监听器，监听屏幕的Off和on状态*/
    private Receivers receivers= new Receivers();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        init();
        registerReceivers();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(drawRunnable);
        handler.removeCallbacks(changRunnable);
        handler.removeCallbacks(homeRunnable);
        removePerson();
        unregisterReceiver(receivers);
    }
    /**
     * 初始化数据
     */
    private void init() {
        //sp监听
        sp = getSharedPreferences(getString(R.string.sp_name), MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(this);
        //只获取一次
        homePackageNames = getHomes();
        layoutParams = new LayoutParams();
        windowManager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //参数设置
        layoutParams.type = LayoutParams.TYPE_PHONE;//格式设置
        layoutParams.format = PixelFormat.RGBA_8888;//图片格式，背景透明
        layoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.gravity = Gravity.LEFT|Gravity.TOP;
        //
        createPerson();
        //显示
        addPerson();
        handler.post(drawRunnable);
        handler.post(changRunnable);
        if(!sp.getBoolean("all_app_visible", false))
            handler.post(homeRunnable);
        frameTime = Integer.parseInt(sp.getString("frame_time", "200"));
        randomTime = Integer.parseInt(sp.getString("random_time", "5000"));
        //屏幕为on
        sp.edit().putBoolean("screen_on", true).commit();
    }
    /**
     * 人物的创建，根据用户的设置而显示
     */
    public void createPerson() {
        try{

            String personName = sp.getString("person_show_name", "panda");
            if("luffy".equals(personName)){
                person = new Luffy(this);
            }
            else if("zoro".equals(personName)){
                person = new Zoro(this);
            }
            else if("law".equals(personName)){
                person = new Law(this);
            }
            else if("chopper".equals(personName)){
                person = new Chopper(this);
            }
            else if("panda".equals(personName)){
                person = new Panda(this);
            }
            else if("nanjing".equals(personName)){
                person = new Nanjing(this);
            }
            else{
                person = new DownloadPerson(this, personName);
            }
            //参数设置
            layoutParams.x = (int) person.getX();
            layoutParams.y = (int) person.getY();
            layoutParams.width = person.getBmpW();//设置大小
            layoutParams.height = person.getBmpH();

        }catch(Exception e){
            e.printStackTrace();
            showErrorAndClose(e);
        }
    }
    /**
     * 屏幕旋转
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        person.measureScreen();
    }
    /**
     * 图像绘制，悬浮窗位置变化
     */
    class DrawRunnable implements Runnable{
        @Override
        public void run() {
//			System.out.println("PET_LOG: DrawRunnable running");
            long start = System.currentTimeMillis();
            try{
                person.invalidate();
                layoutParams.x = (int) person.getX();
                layoutParams.y = (int) person.getY();
                windowManager.updateViewLayout(person, layoutParams);
                //长按启动
                if(person.getOnPerson() == 1 && System.currentTimeMillis()-person.getTouchDownTime() > 1000){
                    ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
                    if(!rti.get(0).topActivity.getPackageName().equals("akai.floatView.op.zoro")){
                        Intent intent = new Intent(AppService.this, MainSettings.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    person.setOnPerson(0);
                }
                long end = System.currentTimeMillis();
                if (end - start < frameTime) {
                    handler.postDelayed(this, frameTime - (end-start));
                }else{
                    handler.post(this);
                }
            }catch (Exception e) {
                e.printStackTrace();
                showErrorAndClose(e);
            }
        }
    }
    /**
     * 动画之间变换
     */
    class ChangRunnable implements Runnable{
        @Override
        public void run() {
            try{
                person.randomChange();
                handler.postDelayed(this, randomTime);
            }catch(Exception e){
                e.printStackTrace();
                showErrorAndClose(e);
            }
        }
    }
    /**
     * 检测是否在桌面上,每秒钟检测一次
     */
    class HomeRunnable implements Runnable{
        @Override
        public void run() {
//			System.out.println("PET_LOG: isHome:" + isHome);
            if(isHome() && !isHome){
                isHome = true;
                addPerson();
                handler.post(drawRunnable);
                handler.postDelayed(changRunnable, randomTime);
            }
            else if(!isHome() && isHome){
                isHome = false;
                handler.removeCallbacks(drawRunnable);
                handler.removeCallbacks(changRunnable);
                removePerson();
            }
            handler.postDelayed(this, 1000);
        }
    }
    /**
     * 数据改变监听
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if(person != null)
            person.onSharedPreferenceChanged(sp, key);
        if("all_app_visible".equals(key)){
            if(!sp.getBoolean("all_app_visible", false))
                handler.post(homeRunnable);
            else
                handler.removeCallbacks(homeRunnable);
        }
        else if("person_show_name".equals(key)){
            removePerson();
            createPerson();
            addPerson();
        }
        else if("frame_time".equals(key)){
            frameTime = Integer.parseInt(sp.getString("frame_time", "200"));
        }
        else if("random_time".equals(key)){
            randomTime = Integer.parseInt(sp.getString("random_time", "5000"));
        }
        else if("person_size".equals(key)){
            try {
                layoutParams.width = person.getBmpW();//设置大小
                layoutParams.height = person.getBmpH();
            } catch (Exception e) {
            }
        }
        else if("screen_on".equals(key)){//屏幕休息
            if(sp.getBoolean(key, false)){//on
//				System.out.println("PET_LOG: screen on");
                if(!sp.getBoolean("all_app_visible", false)){
                    isHome = false;
                    handler.post(homeRunnable);
                }
                else if(sp.getBoolean("person_visible", false)){
                    addPerson();
                    handler.post(drawRunnable);
                    handler.post(changRunnable);
                }
            }else{//off
//				System.out.println("PET_LOG: screen off");
                handler.removeCallbacks(drawRunnable);
                handler.removeCallbacks(changRunnable);
                handler.removeCallbacks(homeRunnable);
                removePerson();
            }
        }
    }
    /**
     * 尝试移除显示窗口
     */
    private void removePerson(){
        try{
            windowManager.removeView(person);
        }catch (Exception e) {}
    }
    /**
     * 尝试添加显示窗口
     */
    private void addPerson(){
        try{
            windowManager.addView(person, layoutParams);
        }catch(Exception e){}
    }
    /**
     * 获得属于桌面的应用的应用包名称
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        //属性
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for(ResolveInfo ri : resolveInfo){
            names.add(ri.activityInfo.name);
//			System.out.println("name: " + ri.activityInfo.name);
        }
        names.add("akai.pet.one.piece.settings.MainSettings");
        names.add("akai.pet.one.piece.store.StoreActivity");
        return names;
    }
    /**
     * 判断当前界面是否是桌面
     */
    private boolean isHome(){
        ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        if(rti.size() == 0){
            return false;
        }
        return homePackageNames.contains(rti.get(0).topActivity.getClassName());
    }
    /**
     * 广播监听器的注册
     */
    private void registerReceivers(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receivers, filter);
    }
    /**
     * 监听当前屏幕是否亮着
     */
    public static class Receivers extends BroadcastReceiver{
        public Receivers(){}
        @Override
        public void onReceive(final Context context, Intent intent) {
            SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.sp_name), Activity.MODE_PRIVATE);

            if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
                Toast.makeText(context, "开机启动", Toast.LENGTH_LONG).show();
                if(sp.getBoolean("boot_startup", false) && sp.getBoolean("person_visible", false)){
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            context.startService(new Intent(context, AppService.class));
                        }
                    }, 2*60*1000);
                }
            }
            else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                sp.edit().putBoolean("screen_on", true).commit();
            }
            else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                sp.edit().putBoolean("screen_on", false).commit();
            }
        }
    }

    /**
     * 资源错误导致报错，关闭提示并关闭服务
     */
    private void showErrorAndClose(Exception e){
        KLog2File.saveLog2File(e);
        Toast.makeText(AppService.this, sp.getString("person_show_name", "")
                + getString(R.string.str_app_res_error), Toast.LENGTH_LONG).show();
        sp.edit().putBoolean("person_visible", false).commit();
        stopSelf();
    }
}
