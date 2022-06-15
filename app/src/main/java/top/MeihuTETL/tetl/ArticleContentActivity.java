package top.MeihuTETL.tetl;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.immersionbar.ImmersionBar;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;

public class ArticleContentActivity extends AppCompatActivity {

    //自动刷新布局
    private RefreshLayout refreshLayout_article;
    private Toolbar toolbar;
    //定义文章正文的线性布局
    private LinearLayout ll_content;
    //定义评论输入线性布局
    private LinearLayout ll_comment;
    //定义评论输入框
    private EditText comment_comment;
    //定义提交评论按钮
    private TextView send_comment;
    //定义底部线性布局，显示收藏、评论、点赞等信息
    private LinearLayout ll_bottom;
    //定义开启输入评论按钮
    private LinearLayout comment;
    //定义文章作者显示
    private TextView author;
    //作者头像
    private ImageView head_pic;
    //定义文章发布日期
    private TextView registerTime;
    //定义文章正文显示组件
    private TextView content;
    //定义评论总数显示组件
    private TextView commentSum;
    //定义点赞总数显示组件
    private TextView starSum;
    //收藏按钮
    private LinearLayout ll_shoucang;
    private ImageView iv_shoucang;
    private TextView tv_shoucang;
    //点赞
    private LinearLayout ll_star;
    private ImageView iv_star;
    private TextView tv_starSum;
    
    //评论列表
    private ListView content_comment_list;
    private int articleId;
    private Bundle data;
    private Integer commentNumber;

    //保存数据库返回的评论id
    private ArrayList<Integer> commentIdList=new ArrayList<>();
    //保存数据库返回的评论作者id
    private ArrayList<Integer> authorIdList=new ArrayList<>();
    //保存数据库返回的作者真实姓名
    private ArrayList<String> authorRealNameList=new ArrayList<>();
    //保存作者昵称
    private ArrayList<String> authorUserNameList=new ArrayList<>();
    //保存评论时间
    private ArrayList<String> commentTimeList=new ArrayList<>();
    //保存评论内容
    private ArrayList<String> commentContentList=new ArrayList<>();
    //保存评论点赞数
    private ArrayList<Integer> commentStarsList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_content);
        /*接收数据*/
        Intent intent=getIntent();
        data=intent.getExtras();
        //获取文章id
        articleId=data.getInt("id");
        commentNumber=data.getInt("commentSum");

        //初始话视图
        initView();
        //设置点击返回事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //给评论按钮设置点击事件
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_bottom.setVisibility(View.GONE);
                ll_comment.setVisibility(View.VISIBLE);
            }
        });
        //点击正文区域时，隐藏Edit Text框
        ll_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_bottom.setVisibility(View.VISIBLE);
                ll_comment.setVisibility(View.GONE);
            }
        });

        //提交评论按钮点击事件
        send_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentContent;
                commentContent=comment_comment.getText().toString();
                System.out.println(commentContent);
                commentNumber++;
                commentSum.setText(String.valueOf(commentNumber));
                //获取当前时间
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
                Date date = new Date(System.currentTimeMillis());
                String nowTime;
                nowTime=simpleDateFormat.format(date);
                //获取作者的id
                SharedPreferences sharedPreferences= getSharedPreferences("user",
                        Activity.MODE_PRIVATE);
                Integer authorId =sharedPreferences.getInt("id",1);
                //新建线程，将评论保存到数据库
                final int[] count = {0};
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            //动态加载类
                            Class.forName(DatabaseURL.DRIVER);
                            Connection connection= DriverManager.getConnection(DatabaseURL.HOST,
                                    DatabaseURL.USER,DatabaseURL.PASSWORD);//加入后面的一串是为了解决插入数据库时的中文乱码
                            Statement statement=connection.createStatement();
                            //发布评论
                            boolean resultSet0=statement.execute("UPDATE article SET comment="+ (data.getInt("commentSum")+1)+" WHERE id=" +data.getInt("id"));
                            boolean resultSet=statement.execute("INSERT INTO comment(articleId,commentContent,authorId,time) VALUES('"+articleId+"','"+commentContent+"','"+authorId+"','"+nowTime+"');");
                        }catch (Exception e){
                            String error;
                            error = e.toString();
                            System.out.println(error);
                        }
                        while (count[0] <=10){

                            try{
                                Thread.sleep(100);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();//开启线程
                Toast.makeText(ArticleContentActivity.this,"评论成功",Toast.LENGTH_SHORT).show();
            }
        });
        //点击头像跳转到作者详情页
        head_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //初始化意图
                Intent intent1=new Intent(ArticleContentActivity.this,PersonalInformationDetailsPageActivity.class);
                //传递数据信息
                Bundle bundle=new Bundle();
                bundle.putInt("id",data.getInt("authorid"));

                intent1.putExtras(bundle);
                //激活意图
                startActivity(intent1);
                //改变activity切换动画
                overridePendingTransition(R.anim.slide_in_right,R.anim.anim_no);
            }
        });
        //点击昵称跳转到作者详情页
        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //初始化意图
                Intent intent1=new Intent(ArticleContentActivity.this,PersonalInformationDetailsPageActivity.class);
                //传递数据信息
                Bundle bundle=new Bundle();
                bundle.putInt("id",data.getInt("authorid"));

                intent1.putExtras(bundle);
                //激活意图
                startActivity(intent1);
                //改变activity切换动画
                overridePendingTransition(R.anim.slide_in_right,R.anim.anim_no);
            }
        });

        ImmersionBar.with(this)
                .statusBarColor(R.color.white)     //状态栏颜色，不写默认透明色
                .fitsSystemWindows(true)

                .statusBarDarkFont(true, 0.2f) //原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
                .init();

    }

    class Task extends AsyncTask<Void,Void,Void> {
        String error="";
        @Override
        protected Void doInBackground(Void... voids) {
            //清空原始数据，主要是刷新时
            commentIdList.clear();
            authorIdList.clear();
            commentTimeList.clear();
            commentContentList.clear();
            commentStarsList.clear();
            authorRealNameList.clear();
            authorUserNameList.clear();
            try {
                //动态加载类
                Class.forName(DatabaseURL.DRIVER);
                Connection connection= DriverManager.getConnection(DatabaseURL.HOST,
                        DatabaseURL.USER,DatabaseURL.PASSWORD);
                Statement statement=connection.createStatement();
                ResultSet resultSet=statement.executeQuery("SELECT * FROM comment WHERE articleId="+ articleId + " ORDER BY id desc");
                //将查询到的数据保存的LISt中
                while (resultSet.next()){
                    commentIdList.add(resultSet.getInt("id"));
                    authorIdList.add(resultSet.getInt("authorId"));
                    commentTimeList.add(resultSet.getString("time"));
                    commentContentList.add(resultSet.getString("commentContent"));
                    commentStarsList.add(resultSet.getInt("star"));
                }
                //查询评论的作者信息
                for(int i=0;i<=authorIdList.size()+1;i++){
                    ResultSet findRealAuthor=statement.executeQuery("SELECT * FROM user WHERE id="+authorIdList.get(i));
                    while (findRealAuthor.next()){
                        authorRealNameList.add(findRealAuthor.getString("realName"));
                        authorUserNameList.add(findRealAuthor.getString("userName"));
                    }
                }
            }catch (Exception e){
                error = e.toString();
                System.out.println(error);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            MyAdapter commentAdapter=new MyAdapter(commentIdList,authorRealNameList,authorUserNameList,commentTimeList, commentContentList,commentStarsList,authorIdList);
            content_comment_list.setAdapter(commentAdapter);
            content_comment_list.setVisibility(View.VISIBLE);
            commentAdapter.notifyDataSetInvalidated();
            super.onPostExecute(aVoid);
        }
    }

    //初始化各个组件
    private void initView() {

        //新建异步线程，链接查询数据库
        new Task().execute();
        //初始化toolbar
        toolbar=findViewById(R.id.content_toolbar);
        //初始化
        ll_comment=findViewById(R.id.ll_comment);
        comment_comment=findViewById(R.id.comment_content);
        send_comment=findViewById(R.id.send_comment);
        ll_bottom=findViewById(R.id.ll_bottom);
        comment=findViewById(R.id.comment);
        ll_content=findViewById(R.id.ll_content);
        head_pic=findViewById(R.id.article_head_pic);

        author=findViewById(R.id.contet_userName);
        registerTime=findViewById(R.id.content_release_time);
        content=findViewById(R.id.content_article_content);
        commentSum=findViewById(R.id.content_comment_sum);
        starSum=findViewById(R.id.star_sum);
        refreshLayout_article=findViewById(R.id.refreshLayout_article);
        ll_shoucang=findViewById(R.id.ll_shoucang);
        iv_shoucang=findViewById(R.id.iv_shoucang);
        //定义点击收藏后的事件
        ll_shoucang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_shoucang.setSelected(true);
                Toast.makeText(ArticleContentActivity.this,"收藏成功!",Toast.LENGTH_SHORT).show();
            }
        });
        //点赞
        ll_star=findViewById(R.id.ll_star);
        iv_star=findViewById(R.id.iv_star);
        tv_starSum=findViewById(R.id.tv_starSum);
        ll_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_star.setSelected(true);
                starSum.setText(String.valueOf(data.getInt("starSum")+1));
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            //动态加载类
                            Class.forName(DatabaseURL.DRIVER);
                            Connection connection= DriverManager.getConnection(DatabaseURL.HOST,
                                    DatabaseURL.USER,DatabaseURL.PASSWORD);
                            Statement statement=connection.createStatement();
                            //更新点赞数
                            boolean resultSet=statement.execute("UPDATE article SET star="+ (data.getInt("starSum")+1)+" WHERE id=" +data.getInt("id"));
                        }catch (Exception e){
                            String error;
                            error = e.toString();
                            System.out.println(error);
                        }
                    }
                }.start();//开启线程
                Toast.makeText(ArticleContentActivity.this,"点赞成功!",Toast.LENGTH_SHORT).show();
            }
        });

        //评论列表
        content_comment_list=findViewById(R.id.content_comment_list);

        //下拉刷新
        refreshLayout_article.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout_article.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                commentSum.setText(String.valueOf(commentNumber));
                //新建异步线程，链接查询数据库
                new Task().execute();
                refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            }
        });

        /*将接受到的数据显示在对应的组件上*/
        if(data.getString("author") != null){
            author.setText(data.getString("author"));
        }else {
            author.setText(data.getString("username"));
        }
        registerTime.setText(data.getString("time"));
        content.setText(data.getString("content"));
        if(data.getInt("commentSum") > 0){
            commentSum.setText(String.valueOf(commentNumber));
        }
        if (data.getInt("starSum")>0){
            starSum.setText(String.valueOf(data.getInt("starSum")));
        }
        System.out.println(data);
    }


    /*ListView适配器*/
    class MyAdapter extends BaseAdapter{

        private ArrayList<Integer> commentIdList=new ArrayList<>();
        //保存数据库返回的作者真实姓名
        private ArrayList<String> authorRealNameList=new ArrayList<>();
        //保存作者昵称
        private ArrayList<String> authorUserNameList=new ArrayList<>();
        //保存评论时间
        private ArrayList<String> commentTimeList=new ArrayList<>();
        //保存评论内容
        private ArrayList<String> commentContentList=new ArrayList<>();
        //保存评论点赞数
        private ArrayList<Integer> commentStarsList=new ArrayList<>();
        //保存评论作者id
        private ArrayList<Integer> authorIdList=new ArrayList<>();

        public MyAdapter(ArrayList<Integer> id,ArrayList<String> realname,ArrayList<String> username,
                         ArrayList<String> time,ArrayList<String> content,
                         ArrayList<Integer> star,ArrayList<Integer> authorId){
            this.authorRealNameList=realname;
            this.authorUserNameList=username;
            this.commentTimeList=time;
            this.commentContentList=content;
            this.commentStarsList=star;
            this.authorIdList=authorId;
            this.commentIdList=id;
        }

        @Override
        public int getCount() {
            return authorRealNameList.size();
        }

        @Override
        public Object getItem(int position) {
            return authorRealNameList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view=null;
            view=View.inflate(ArticleContentActivity.this,R.layout.list_items_comment,null);

            LinearLayout ll_comment_star=view.findViewById(R.id.ll_comment_star);
            ImageView iv_comment_star=view.findViewById(R.id.iv_comment_star);
            TextView comment_authorName=view.findViewById(R.id.comment_authorName);
            TextView commentSum=view.findViewById(R.id.commentStarSum);
            TextView commentContent=view.findViewById(R.id.commentContent);
            TextView commentTime=view.findViewById(R.id.commentTime);
            ImageView comment_head_pic=view.findViewById(R.id.comment_head_pic);
            ll_comment_star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv_comment_star.setSelected(true);
                    commentSum.setText(String.valueOf(commentStarsList.get(position)+1));
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                //动态加载类
                                Class.forName(DatabaseURL.DRIVER);
                                Connection connection= DriverManager.getConnection(DatabaseURL.HOST,
                                        DatabaseURL.USER,DatabaseURL.PASSWORD);
                                Statement statement=connection.createStatement();
                                //更新点赞数
                                boolean resultSet=statement.execute("UPDATE comment SET star="+ (commentStarsList.get(position)+1)+" WHERE id=" +(int)commentIdList.get(position));
                            }catch (Exception e){
                                String error;
                                error = e.toString();
                                System.out.println(error);
                            }
                        }
                    }.start();//开启线程
                }
            });

            System.out.println(authorRealNameList);
            if(authorRealNameList.get(position)!=null){
                comment_authorName.setText(authorRealNameList.get(position).toString());
            }else {
                comment_authorName.setText(authorUserNameList.get(position).toString());
            }
            if(commentStarsList.get(position)>0){
                commentSum.setText(String.valueOf(commentStarsList.get(position)));
            }
            commentContent.setText(commentContentList.get(position).toString());
            commentTime.setText(commentTimeList.get(position).toString());

            //点击头像跳转到作者详情页
            comment_head_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //初始化意图
                    Intent intent1=new Intent(ArticleContentActivity.this,PersonalInformationDetailsPageActivity.class);
                    //传递数据信息
                    Bundle bundle=new Bundle();
                    bundle.putInt("id",authorIdList.get(position));

                    intent1.putExtras(bundle);
                    //激活意图
                    startActivity(intent1);
                    //改变activity切换动画
                    overridePendingTransition(R.anim.slide_in_right,R.anim.anim_no);
                }
            });
            //点击昵称跳转到作者详情页
            comment_authorName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //初始化意图
                    Intent intent1=new Intent(ArticleContentActivity.this,PersonalInformationDetailsPageActivity.class);
                    //传递数据信息
                    Bundle bundle=new Bundle();
                    bundle.putInt("id",authorIdList.get(position));

                    intent1.putExtras(bundle);
                    //激活意图
                    startActivity(intent1);
                    //改变activity切换动画
                    overridePendingTransition(R.anim.slide_in_right,R.anim.anim_no);
                }
            });

            return view;
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