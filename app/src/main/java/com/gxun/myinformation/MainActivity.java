package com.gxun.myinformation;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.TimePickerView;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileWithBitmapCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import db.AreaBean;
import db.CityBean;
import db.DBManager;
import db.ProvinceBean;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private int mYear;
    private int mMonth;
    private int mDay;
    private AlertDialog alertDialogNation;
    private Button  buttonSubmit,buttonReset;
    private Button datePicker;
    private Button buttonSelectNation;
    private Button buttonSelectAddress;
    // 声明一个独一无二的标识，来作为要显示DatePicker的Dialog的ID：
    static final int DATE_DIALOG_ID = 0;
    //调取系统摄像头的请求码
    private static final int MY_ADD_CASE_CALL_PHONE = 6;
    //打开相册的请求码
    private static final int MY_ADD_CASE_CALL_PHONE2 = 7;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private ImageView imageView, imageView1;
    private TimePickerView pvTime;
    private View layout;
    private TextView takePhotoTV;
    private TextView choosePhotoTV;
    private TextView cancelTV;
    private TextView editTextDate;
    private TextView editTextID;
    private EditText editTextNation;
    private EditText editTextAddress;
    private EditText editTextName;

    private OptionsPickerView pvOptions;//地址选择器
    private ArrayList<ProvinceBean> options1Items = new ArrayList<>();//省
    private ArrayList<ArrayList<CityBean>> options2Items = new ArrayList<>();//市
    private ArrayList<ArrayList<ArrayList<AreaBean>>> options3Items = new ArrayList<>();//区
    private ArrayList<String> Provincestr = new ArrayList<>();//省
    private ArrayList<ArrayList<String>> Citystr = new ArrayList<>();//市
    private ArrayList<ArrayList<ArrayList<String>>> Areastr = new ArrayList<>();//区

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonReset=findViewById(R.id.buttonReset);
        buttonSubmit=findViewById(R.id.buttonSubmit);
        buttonSelectAddress = findViewById(R.id.buttonSelectAddress);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextName=findViewById(R.id.editTextName);
        editTextNation = findViewById(R.id.editTextNation);
        datePicker = (Button) findViewById(R.id.buttonSelect);
        buttonSelectNation = findViewById(R.id.buttonSelectNation);
        imageView = findViewById(R.id.image);
        imageView1 = findViewById(R.id.imageView);
        editTextDate = findViewById(R.id.editTextDate);
        editTextID = findViewById(R.id.editTextID);
        // 获得当前的日期：
        final Calendar currentDate = Calendar.getInstance();
        datePicker.setOnClickListener(new btnDow_OnClickListener());
        mYear = currentDate.get(Calendar.YEAR);
        mMonth = currentDate.get(Calendar.MONTH);
        mDay = currentDate.get(Calendar.DAY_OF_MONTH);

        //设置只读
        editTextNation.setKeyListener(null);
        editTextAddress.setKeyListener(null);
        editTextDate.setKeyListener(null);
         //设置日期
        initData();
        initEvent();

         // 设置文本的内容：
        editTextDate.setText(new StringBuilder().append(mYear).append("年")
                .append(mMonth + 1).append("月")// 得到的月份+1，因为从0开始
                .append(mDay).append("日"));
        //判断身份证是否正确
        editTextID.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                } else {
                    IdCardUtil idCardUtil = new IdCardUtil(editTextID.getText().toString().trim());
                    idCardUtil.isCorrect();
                    String errorMsg = idCardUtil.getErrMsg();
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.d("TAG", errorMsg);
                }

                // 此处为失去焦点时的处理内容
            }

        });

        //判断空值
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IdCardUtil idCardUtil = new IdCardUtil(editTextID.getText().toString().trim());
                idCardUtil.isCorrect();
                String errorMsg = idCardUtil.getErrMsg();
                if (TextUtils.isEmpty(editTextName.getText())){
                    Toast.makeText(MainActivity.this,"姓名不能为空",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(editTextNation.getText())){
                    Toast.makeText(MainActivity.this,"民族不能为空",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(editTextID.getText())){
                    Toast.makeText(MainActivity.this,"身份证不能为空",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(editTextAddress.getText())){
                    Toast.makeText(MainActivity.this,"地址不能为空",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(editTextDate.getText())){
                    Toast.makeText(MainActivity.this,"生日不能为空",Toast.LENGTH_SHORT).show();
                }else if(imageView.getVisibility()==0){
                    Toast.makeText(MainActivity.this,"头像不能为空",Toast.LENGTH_SHORT).show();
                }
                else if ( idCardUtil.isCorrect()==0){
                    Toast.makeText(MainActivity.this,"提交成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }

            }
        });
        //reset
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextName.setText("");
                editTextNation.setText("");
                editTextAddress.setText("");
                editTextDate.setText("");
                editTextID.setText("");

            }
        });

    }

    //民族选择
    public void showList(View view) {
        final String[] items = {"阿昌族", "保安族", "布朗族", "布依族", "白族", "朝鲜族", "德昂族", "独龙族", "达斡尔族", "东乡族",
                "侗族", "傣族", "鄂伦春族", "俄罗斯族", "鄂温克族","高山族", "汉族", "哈尼族", "哈萨克族", "回族", "赫哲族", "基诺族",
                "景颇族", "京族", "柯尔克孜族", "珞巴族", "傈僳族", "黎族", "拉祜族","门巴族", "蒙古族", "毛南族", "满族", "苗族",
                "仫佬族", "纳西族", "怒族", "普米族", "羌族", "撒拉族", "水族", "畲族", "塔吉克族","土家族", "塔塔尔族", "土族", "维吾尔族",
                "壮族", "乌孜别克族", "锡伯族", "裕固族", "彝族", "瑶族", "仡佬族", "佤族", "藏族"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("请选择民族");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //  Toast.makeText(MainActivity.this, items[i], Toast.LENGTH_SHORT).show();
                editTextNation.setText(items[i].toString());
                alertDialogNation.dismiss();
            }
        });
        alertDialogNation = alertBuilder.create();
        alertDialogNation.show();
    }

    private void initData() {
        //选项选择器
        pvOptions = new OptionsPickerView(this);
        // 获取数据库
        SQLiteDatabase db = DBManager.getdb(getApplication());
        //省
        Cursor cursor = db.query("province", null, null, null, null, null,
                null);
        while (cursor.moveToNext()) {
            int pro_id = cursor.getInt(0);
            String pro_code = cursor.getString(1);
            String pro_name = cursor.getString(2);
            String pro_name2 = cursor.getString(3);
            ProvinceBean provinceBean = new ProvinceBean(pro_id, pro_code, pro_name, pro_name2);
            options1Items.add(provinceBean);//添加一级目录
            Provincestr.add(cursor.getString(2));
            //查询二级目录，市区
            Cursor cursor1 = db.query("city", null, "province_id=?", new String[]{pro_id + ""}, null, null,
                    null);
            ArrayList<CityBean> cityBeanList = new ArrayList<>();
            ArrayList<String> cityStr = new ArrayList<>();
            //地区集合的集合（注意这里要的是当前省份下面，当前所有城市的地区集合我去）
            ArrayList<ArrayList<AreaBean>> options3Items_03 = new ArrayList<>();
            ArrayList<ArrayList<String>> options3Items_str = new ArrayList<>();
            while (cursor1.moveToNext()) {
                int cityid = cursor1.getInt(0);
                int province_id = cursor1.getInt(1);
                String code = cursor1.getString(2);
                String name = cursor1.getString(3);
                String provincecode = cursor1.getString(4);
                CityBean cityBean = new CityBean(cityid, province_id, code, name, provincecode);
                //添加二级目录
                cityBeanList.add(cityBean);
                cityStr.add(cursor1.getString(3));
                //查询三级目录
                Cursor cursor2 = db.query("area", null, "city_id=?", new String[]{cityid + ""}, null, null,
                        null);
                ArrayList<AreaBean> areaBeanList = new ArrayList<>();
                ArrayList<String> areaBeanstr = new ArrayList<>();
                while (cursor2.moveToNext()) {
                    int areaid = cursor2.getInt(0);
                    int city_id = cursor2.getInt(1);
//                    String code0=cursor2.getString(2);
                    String areaname = cursor2.getString(3);
                    String citycode = cursor2.getString(4);
                    AreaBean areaBean = new AreaBean(areaid, city_id, areaname, citycode);
                    areaBeanList.add(areaBean);
                    areaBeanstr.add(cursor2.getString(3));
                }
                options3Items_str.add(areaBeanstr);//本次查询的存储内容
                options3Items_03.add(areaBeanList);
            }
            options2Items.add(cityBeanList);//增加二级目录数据
            Citystr.add(cityStr);
            options3Items.add(options3Items_03);//添加三级目录
            Areastr.add(options3Items_str);
        }
        //设置三级联动效果
        pvOptions.setPicker(Provincestr, Citystr, Areastr, true);
        //设置选择的三级单位
//        pvOptions.setLabels("省", "市", "区");
        pvOptions.setTitle("选择城市");
        //设置是否循环滚动
        pvOptions.setCyclic(false, false, false);
        //设置默认选中的三级项目
        //监听确定选择按钮
        pvOptions.setSelectOptions(0, 0, 0);

        pvOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
                String tx = options1Items.get(options1).getPro_name()
                        + options2Items.get(options1).get(option2).getName()
                        + options3Items.get(options1).get(option2).get(options3).getName();
                editTextAddress.setText(tx);
            }
        });
    }

    private void initEvent() {
        buttonSelectAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvOptions.show();
            }
        });
    }

    private String getTimes(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    /*
    初始化控件方法
     */
    public void viewInit() {

        builder = new AlertDialog.Builder(this);//创建对话框
        inflater = getLayoutInflater();
        layout = inflater.inflate(R.layout.dialog_select_photo, null);//获取自定义布局
        builder.setView(layout);//设置对话框的布局
        dialog = builder.create();//生成最终的对话框
        dialog.show();//显示对话框

        takePhotoTV = layout.findViewById(R.id.photograph);
        choosePhotoTV = layout.findViewById(R.id.photo);
        cancelTV = layout.findViewById(R.id.cancel);
        //设置监听
        takePhotoTV.setOnClickListener(this);
        choosePhotoTV.setOnClickListener(this);
        cancelTV.setOnClickListener(this);


    }

    public class btnDow_OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
// 调用Activity类的方法来显示Dialog:调用这个方法会允许Activity管理该Dialog的生命周期，
// 并回调用 onCreateDialog(int)回调函数来请求一个Dialog
            showDialog(DATE_DIALOG_ID);

        }
    }

    // 需要定义弹出的DatePicker对话框的事件监听器：
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;

            String str1 = new String(new StringBuilder().append(mYear)
                    .append("年").append(mMonth + 1).append("月")// 得到的月份+1，因为从0开始
                    .append(mDay).append("日"));


// 设置文本的内容：
            editTextDate.setText(str1);
        }
    };


    /**
     * 当Activity调用showDialog函数时会触发该函数的调用：
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                        mDay);
        }
        return null;
    }

    /**
     * 修改头像按钮执行方法
     *
     * @param view
     */
    public void UpdatePhoto(View view) {
        viewInit();
    }

    private void takePhoto() throws IOException {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        // 获取文件
        File file = createFileIfNeed("UserIcon.png");
        //拍照后原图回存入此路径下
        Uri uri;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            uri = Uri.fromFile(file);
        } else {
            /**
             * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
             * 并且这样可以解决MIUI系统上拍照返回size为0的情况
             */
            uri = FileProvider.getUriForFile(this, "com.gxun.myinformation.fileprovider", file);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, 1);
    }

    // 在sd卡中创建一保存图片（原图和缩略图共用的）文件夹
    private File createFileIfNeed(String fileName) throws IOException {
        String fileA = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/nbinpic";
        File fileJA = new File(fileA);
        if (!fileJA.exists()) {
            fileJA.mkdirs();
        }
        File file = new File(fileA, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /**
     * 打开相册
     */
    private void choosePhoto() {
        //这是打开系统默认的相册(就是你系统怎么分类,就怎么显示,首先展示分类列表)
        Intent picture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(picture, 2);
    }

    /**
     * 申请权限回调方法
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == MY_ADD_CASE_CALL_PHONE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    takePhoto();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "拒绝了你的请求", Toast.LENGTH_SHORT).show();
                //"权限拒绝");
                // TODO: 2018/12/4 这里可以给用户一个提示,请求权限被拒绝了
            }
        }


        if (requestCode == MY_ADD_CASE_CALL_PHONE2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhoto();
            } else {
                //"权限拒绝");
                // TODO: 2018/12/4 这里可以给用户一个提示,请求权限被拒绝了
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * startActivityForResult执行后的回调方法，接收返回的图片
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode != Activity.RESULT_CANCELED) {

            String state = Environment.getExternalStorageState();
            if (!state.equals(Environment.MEDIA_MOUNTED)) return;
            // 把原图显示到界面上
            Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
            Tiny.getInstance().source(readpic()).asFile().withOptions(options).compress(new FileWithBitmapCallback() {
                @Override
                public void callback(boolean isSuccess, Bitmap bitmap, String outfile, Throwable t) {
                    saveImageToServer(bitmap, outfile);//显示图片到imgView上
                }
            });
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK
                && null != data) {
            try {
                Uri selectedImage = data.getData();//获取路径
                Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
                Tiny.getInstance().source(selectedImage).asFile().withOptions(options).compress(new FileWithBitmapCallback() {
                    @Override
                    public void callback(boolean isSuccess, Bitmap bitmap, String outfile, Throwable t) {
                        saveImageToServer(bitmap, outfile);
                    }
                });
            } catch (Exception e) {
                //"上传失败");
            }
        }
    }

    /**
     * 从保存原图的地址读取图片
     */
    private String readpic() {
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/nbinpic/" + "UserIcon.png";
        return filePath;
    }

    private void saveImageToServer(final Bitmap bitmap, String outfile) {
        File file = new File(outfile);
        // TODO: 2018/12/4  这里就可以将图片文件 file 上传到服务器,上传成功后可以将bitmap设置给你对应的图片展示
        imageView.setImageBitmap(bitmap);
        imageView1.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photograph:
                //"点击了照相";
                //  6.0之后动态申请权限 摄像头调取权限,SD卡写入权限
                //判断是否拥有权限，true则动态申请
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_ADD_CASE_CALL_PHONE);
                } else {
                    try {
                        //有权限,去打开摄像头
                        takePhoto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                dialog.dismiss();
                break;
            case R.id.photo:
                //"点击了相册";
                //  6.0之后动态申请权限 SD卡写入权限
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_ADD_CASE_CALL_PHONE2);

                } else {
                    //打开相册
                    choosePhoto();
                }
                dialog.dismiss();
                break;
            case R.id.cancel:
                dialog.dismiss();//关闭对话框
                break;
            default:
                break;
        }
    }


}