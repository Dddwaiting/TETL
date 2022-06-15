package top.MeihuTETL.tetl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.immersionbar.ImmersionBar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Register extends AppCompatActivity {

    private EditText register_phone;
    private EditText register_password;
    private EditText register_pwd;
    private EditText register_code;
    private EditText register_username;
    private Button btn_verify;
    private ImageView image;
    private Button register_button;
    private TextView tv_protocol;
    private CheckBox cb_protocol;

    private CodeUtils codeUtils;

    private String phoneNumber;
    private String username;
    private String password;
    private String passwordAgain;
    private String verificationcode;
    private ArrayList<String> phoneList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        ImmersionBar.with(this)
                .statusBarColor(R.color.gray)     //状态栏颜色，不写默认透明色
                .fitsSystemWindows(true)
                .statusBarDarkFont(true, 0.2f) //原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
                .init();
    }

    private void initView() {
        register_phone=findViewById(R.id.register_phone);
        register_username=findViewById(R.id.register_username);
        register_password=findViewById(R.id.register_password);
        register_pwd=findViewById(R.id.register_pwd);
        register_code=findViewById(R.id.register_code);
        btn_verify=findViewById(R.id.btn_verify);
        image=findViewById(R.id.image);
        tv_protocol=findViewById(R.id.tv_protocol);
        cb_protocol=findViewById(R.id.cb_protocol);
        register_button=findViewById(R.id.register_button);

        //生成验证码
        codeUtils = CodeUtils.getInstance();
        Bitmap bitmap = codeUtils.createBitmap();
        image.setImageBitmap(bitmap);
        String code=codeUtils.getCode();
        System.out.println(code);


        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeUtils = CodeUtils.getInstance();
                Bitmap bitmap = codeUtils.createBitmap();
                image.setImageBitmap(bitmap);
                String code=codeUtils.getCode();
                System.out.println(code);
            }
        });

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reges="[1][0-9]{10}";
                String regex1="[\\d\\p{Alpha}\\p{Punct}]+";//数字,字母,和符号组成的表达式
                phoneNumber=register_phone.getText().toString();
                username=register_username.getText().toString();
                password=register_password.getText().toString();
                passwordAgain=register_pwd.getText().toString();
                verificationcode=register_code.getText().toString();
                //调用异步线程，查询手机号是否注册
                new Task1().execute();
                if(!phoneNumber.matches(reges)){
                    Toast.makeText(Register.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                }
                else if(username.length()>=10){
                    Toast.makeText(Register.this, "用户名不能超过10位", Toast.LENGTH_SHORT).show();
                }
                else if(!password.matches(regex1)){
                    Toast.makeText(Register.this, "密码由数字，字母，符合组成", Toast.LENGTH_SHORT).show();
                }
                else if(password.length()<=6){
                    Toast.makeText(Register.this, "密码的长度要大于6位", Toast.LENGTH_SHORT).show();
                }
                else if(phoneList.contains(phoneNumber)){
                    Toast.makeText(Register.this,"该手机号已注册,请前往登录",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(Register.this,"请输入手机号",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(username)){
                    Toast.makeText(Register.this,"请输入昵称",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this,"请输入密码",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(passwordAgain)){
                    Toast.makeText(Register.this,"请输入确认密码",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(verificationcode)){
                    Toast.makeText(Register.this,"请输入验证码",Toast.LENGTH_SHORT).show();
                }else if(!verificationcode.equalsIgnoreCase(codeUtils.getCode())){
                    Toast.makeText(Register.this,"验证码错误,请重新输入",Toast.LENGTH_SHORT).show();
                }else if(!password.equals(passwordAgain)){
                    Toast.makeText(Register.this,"两次输入的密码不一致，请重新输入",Toast.LENGTH_SHORT).show();
                } else if(cb_protocol.isChecked()==false){
                    Toast.makeText(Register.this,"请阅读并同意服务条款",Toast.LENGTH_SHORT).show();
                }else {
                    new Task().execute();
                }
            }
        });

    }
    /*实现查询手机号是否已经注册的异步线程*/
    class Task1 extends AsyncTask<Void,Void,Void> {

        String error = "";

        @Override
        protected Void doInBackground(Void... voids) {
            phoneList.clear();
            try {
                //动态加载类
                Class.forName(DatabaseURL.DRIVER);
                Connection connection= DriverManager.getConnection(DatabaseURL.HOST,
                        DatabaseURL.USER,DatabaseURL.PASSWORD);
                Statement statement = connection.createStatement();
                //mysql简单查询语句
                ResultSet resultSet = statement.executeQuery("SELECT phoneNumber FROM user ORDER BY id desc");

                //将查询到的数据保存的LISt中
                while (resultSet.next()) {
                    phoneList.add(resultSet.getString("phoneNumber"));
                }
            } catch (Exception e) {
                error = e.toString();
                System.out.println(error);
            }
            return null;
        }
    }

    /*实现注册的异步线程*/
    class Task extends AsyncTask<Void,Void,Void> {

        String error="";
        @Override
        protected Void doInBackground(Void... voids) {
            //获取当前时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            String nowTime;
            nowTime=simpleDateFormat.format(date);
            try {
                //动态加载类
                Class.forName(DatabaseURL.DRIVER);
                Connection connection= DriverManager.getConnection(DatabaseURL.HOST,
                        DatabaseURL.USER,DatabaseURL.PASSWORD);
                Statement statement=connection.createStatement();
                //将数据插入mysql
                boolean resultSet=statement.execute("INSERT INTO user(phoneNumber,userName,passWord,registerTime) VALUES('"+phoneNumber+"','"+username+"','"+password+"','"+nowTime+"');");
            }catch (Exception e){
                error = e.toString();
                System.out.println(error);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(Register.this,"注册成功,请前往登录",Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(Register.this,Login.class);
            startActivity(intent);
            //改变activity切换动画
            overridePendingTransition(R.anim.slide_in_right,R.anim.anim_no);
            super.onPostExecute(aVoid);
        }
    }



    /**
     重写dispatchTouchEvent
     * 点击软键盘外面的区域关闭软键盘
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 获得当前得到焦点的View，
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                //根据判断关闭软键盘
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    /**
     * 判断用户点击的区域是否是输入框
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        return false;
    }



    @Override
    /*重写finish方法，改变返回时的动画*/
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_right);
    }
}