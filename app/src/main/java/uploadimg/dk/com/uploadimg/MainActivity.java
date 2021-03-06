package uploadimg.dk.com.uploadimg;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = "LoginActivity";
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 6;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE2 = 7;
    private ImageView img;
    private EditText img_content;
    private Button nati;
    private Button pai;
    private Button submit;

    private LinearLayout photo_full;

    private static String srcPath;
    private static final int TIME_OUT = 10*1000;   //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private String loginKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }
    private void initView() {
        img = (ImageView) findViewById(R.id.img);
        nati = (Button) findViewById(R.id.natives);
        pai = (Button) findViewById(R.id.pai);
        submit = (Button) findViewById(R.id.submit);
        img_content=(EditText)findViewById(R.id.img_content);
        photo_full = (LinearLayout)findViewById(R.id.photo_full);
        nati.setOnClickListener(this);
        pai.setOnClickListener(this);
        submit.setOnClickListener(this);

//        View.OnClickListener keyboard_hide = new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                InputMethodManager imm = (InputMethodManager) MainActivity.this
//                        .getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//            }
//
//        };
//        photo_full.setClickable(true);
//        photo_full.setOnClickListener(keyboard_hide);
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.natives:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE2);

                }else {
                    Intent local = new Intent();
                    local.setType("image/*");
                    local.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(local, 2);
                }
                break;
            case R.id.pai:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);

                }else {
                    Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(it, 1);
                }
                break;
            case R.id.submit:
                if (srcPath == null || srcPath=="") {
                    Toast.makeText(MainActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
                }else{
                    submitUploadFile();
                }
                break;
        }
    }
    /**
     * 拍照上传
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case 1:
                    Bundle extras = data.getExtras();
                    Bitmap b = (Bitmap) extras.get("data");
                    img.setImageBitmap(b);
                    String name = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                    String fileNmae = Environment.getExternalStorageDirectory().toString()+ File.separator+"dong/image/"+name+".jpg";
                    srcPath = fileNmae;
                    System.out.println(srcPath+"----------保存路径1");
                    File myCaptureFile =new File(fileNmae);
                    try {
                        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                            if(!myCaptureFile.getParentFile().exists()){
                                myCaptureFile.getParentFile().mkdirs();
                            }
                            BufferedOutputStream bos;
                            bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                            b.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                            bos.flush();
                            bos.close();
                        }else{

                            Toast toast= Toast.makeText(MainActivity.this, "保存失败，SD卡无效", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
//                    Uri uri = data.getData();
//                    img.setImageURI(uri);
//                    ContentResolver cr = this.getContentResolver();
//                    Cursor c = cr.query(uri, null, null, null, null);
//                    c.moveToFirst();
//                    //这是获取的图片保存在sdcard中的位置
//                    srcPath = c.getString(c.getColumnIndex("_data"));
//                    System.out.println(srcPath+"----------保存路径2");
                    if (resultCode == RESULT_OK) {
                        try {
                            /**
                             * 该uri是上一个Activity返回的
                             */
                            Uri uri = data.getData();
                            Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                            img.setImageBitmap(bit);
                            name = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                            fileNmae = Environment.getExternalStorageDirectory().toString()+ File.separator+"dong/image/"+name+".jpg";
                            srcPath = fileNmae;
                            System.out.println(srcPath+"----------保存路径1");
                            myCaptureFile =new File(fileNmae);
                            try {
                                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                    if(!myCaptureFile.getParentFile().exists()){
                                        myCaptureFile.getParentFile().mkdirs();
                                    }
                                    BufferedOutputStream bos;
                                    bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                                    bit.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                                    bos.flush();
                                    bos.close();
                                }else{

                                    Toast toast= Toast.makeText(MainActivity.this, "保存失败，SD卡无效", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("tag",e.getMessage());
                            Toast.makeText(this,"程序崩溃",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Log.i("liang", "失败");
                    }
                    break;
                default:
                    break;
            };
        }
//      n =1;
    }

    private void submitUploadFile(){
        final File file=new File(srcPath);
        final String RequestURL="http://192.168.1.53:8080/appmap/log/log.do";
        if (file == null || (!file.exists())) {
            return;
        }

        Log.i(TAG, "请求的URL=" + RequestURL);
        Log.i(TAG, "请求的fileName=" + file.getName());
        final Map<String, String> params = new HashMap<String, String>();
        params.put("title", "1234");
        params.put("mainbody", "你好");
        params.put("phonenumber", "16868686688");
        params.put("content", img_content.getText().toString());

        new Thread(new Runnable() { //开启线程上传文件
            @Override
            public void run() {
                uploadFile(file, RequestURL,params);
            }
        }).start();
    }

    /**
     * android上传文件到服务器
     * @param file  需要上传的文件
     * @param RequestURL  请求的rul
     * @return  返回响应的内容
     */
    private String uploadFile(File file,String RequestURL,Map<String, String> param){
        String result = null;
        String  BOUNDARY =  UUID.randomUUID().toString();  //边界标识   随机生成
        String PREFIX = "--" , LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";   //内容类型
        // 显示进度框
//      showProgressDialog();
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if(file!=null){
                /**
                 * 当文件不为空，把文件包装并且上传
                 */
                DataOutputStream dos = new DataOutputStream( conn.getOutputStream());
                StringBuffer sb = new StringBuffer();

                String params = "";
                if (param != null && param.size() > 0) {
                    Iterator<String> it = param.keySet().iterator();
                    while (it.hasNext()) {
                        sb = null;
                        sb = new StringBuffer();
                        String key = it.next();
                        String value = param.get(key);
                        sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                        sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINE_END).append(LINE_END);
                        sb.append(value).append(LINE_END);
                        params = sb.toString();
                        Log.i(TAG, key+"="+params+"##");
                        dos.write(params.getBytes());
//                      dos.flush();
                    }
                }
                sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * 这里重点注意：
                 * name里面的值为服务器端需要key   只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的   比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=\"upfile\";filename=\""+file.getName()+"\""+LINE_END);
                sb.append("Content-Type: image/pjpeg; charset="+CHARSET+LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while((len=is.read(bytes))!=-1){
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
                dos.write(end_data);

                dos.flush();
                /**
                 * 获取响应码  200=成功
                 * 当响应成功，获取响应的流
                 */

                int res = conn.getResponseCode();
                System.out.println("res========="+res);
                if(res==200){
                    InputStream input =  conn.getInputStream();
                    StringBuffer sb1= new StringBuffer();
                    int ss ;
                    while((ss=input.read())!=-1){
                        sb1.append((char)ss);
                    }
                    result = sb1.toString();
//                 // 移除进度框
//                  removeProgressDialog();
                    finish();
                }
                else{
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
