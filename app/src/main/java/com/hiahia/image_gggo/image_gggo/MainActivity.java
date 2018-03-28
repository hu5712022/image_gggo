package com.hiahia.image_gggo.image_gggo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.bt_search)
    Button bt_search;
    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.gv_image)
    GridView gvImage;
    ImageAdapter adapter;

    @BindView(R.id.tv_left)
    TextView tvLeft;
    @BindView(R.id.tv_right)
    TextView tvRight;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;

    BitmapUtils bitmapUtils;
    HttpUtils httpUtils;
    int pn = 0;
    String word = "美女";
    List<String> listUrl = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bitmapUtils = new BitmapUtils(this);
        httpUtils = new HttpUtils();

        adapter = new ImageAdapter();
        gvImage.setAdapter(adapter);

        gvImage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog dialog = new Dialog(MainActivity.this, R.style.Dialog_Fullscreen);


                PhotoView photoView = new PhotoView(MainActivity.this);
                photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                    @Override
                    public void onPhotoTap(View view, float x, float y) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onOutsidePhotoTap() {
                        dialog.dismiss();
                    }
                });

                final String url = parent.getItemAtPosition(position).toString();

//                Glide.with(MainActivity.this).load(url).into(photoView);
                bitmapUtils.display(photoView, url);

                dialog.setTitle(getImgName(url));
                dialog.setContentView(photoView);
                dialog.show();

                WindowManager windowManager = getWindowManager();
                Display display = windowManager.getDefaultDisplay();
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.width = (int) (display.getWidth()); //设置宽度
                lp.height = (int) (display.getHeight()); //设置宽度
                Window window = dialog.getWindow();
                window.setAttributes(lp);


                photoView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String label = url.substring(url.lastIndexOf("."), url.length());
                        String name = word + "_" + System.currentTimeMillis() + label;
                        down(url, name);
                        Toast.makeText(getApplicationContext(), "sdcard/img_gggo/" + name, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        });


        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //重新搜索 需要清空 数据
                listUrl.clear();
                pn = 0;
                search();

            }
        });

        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pn == 0) {
                    Toast.makeText(getApplicationContext(), "已是第一页", Toast.LENGTH_SHORT).show();
                    return;
                }
                pn = pn - 60;
                if (pn < 0) {
                    pn = 0;
                }
                search();
            }
        });
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pn = pn + 60;
                search();
            }
        });

        findViewById(R.id.bt_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < listUrl.size(); i++) {
                    String s = listUrl.get(i);

                    String label = s.substring(s.lastIndexOf("."), s.length());
                    down(listUrl.get(i), word + "_" + pn + i + 1 + label);
                }
            }
        });


    }

    private class ImageAdapter extends BaseAdapter {
        private Context c;

        void setData() {
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return listUrl.size();
        }

        @Override
        public Object getItem(int position) {
            return listUrl.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            if (v == null) {
                v = View.inflate(MainActivity.this, R.layout.item_image, null);
            }
            ImageView iv = (ImageView) v.findViewById(R.id.iv);
            bitmapUtils.display(iv, listUrl.get(position));
//            Glide.with(MainActivity.this).load(listUrl.get(position)).into(iv);

            return v;
        }
    }

    private void down(String url, String name) {
        //sdcard/yjmt/app.apk;
        String dir = "sdcard/img_gggo/";
        httpUtils.download(url, dir + name, new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                File imageFile = responseInfo.result;
                MainActivity.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(imageFile)));
                LogUtil.LogE("下载成功", imageFile.getAbsolutePath());
            }

            @Override
            public void onFailure(HttpException e, String s) {
            }
        });

    }

    private void search() {
        //http://image.baidu.com/search/flip?tn=baiduimage&word=h&pn=61
        word = etSearch.getText().toString().trim();

        String url = "http://image.baidu.com/search/flip?tn=baiduimage&word=" + word + "&pn=" + pn;

        httpUtils.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String res = responseInfo.result;
                List<String> imageUrl = getImageUrl(res);
                List<String> imageSrc = getImageSrc(imageUrl);
                if (imageSrc != null)
                    listUrl = imageSrc;
                LogUtil.LogE("size:" + imageSrc.size() + "  " + imageSrc);
                adapter.setData();
            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });
    }

    /***
     * 获取ImageUrl地址
     *
     * @param HTML
     * @return
     */
    private List<String> getImageUrl(String HTML) {
        String IMGURL_REG = "objURL\":\"(.*?)\"";
        Matcher matcher = Pattern.compile(IMGURL_REG).matcher(HTML);
        List<String> listImgUrl = new ArrayList<String>();
        while (matcher.find()) {
            listImgUrl.add(matcher.group());
        }
        return listImgUrl;
    }

    /***
     * 获取ImageSrc地址
     *
     * @param listImageUrl
     * @return
     */
    private List<String> getImageSrc(List<String> listImageUrl) {
        String IMGSRC_REG = "http:\"?(.*?)(\"|>|\\s+)";
        List<String> listImgSrc = new ArrayList<String>();
        for (String image : listImageUrl) {
            Matcher matcher = Pattern.compile(IMGSRC_REG).matcher(image);
            while (matcher.find()) {
                String url = matcher.group().substring(0, matcher.group().length() - 1);
//                try {
//                    url = URLDecoder.decode(url, "UTF-8");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
                listImgSrc.add(url);
            }
        }
        return listImgSrc;
    }

    private String getImgName(String path) {
        return path.substring(path.lastIndexOf("/") + 1, path.length());
    }
}
