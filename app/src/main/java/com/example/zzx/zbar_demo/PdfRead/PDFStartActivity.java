package com.example.zzx.zbar_demo.PdfRead;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.example.zzx.zbar_demo.R;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnDrawListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;



public class PDFStartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_read);
        PDFView pdfView = (PDFView)findViewById(R.id.pdfView);
        //测试例子 assets目录下sample.pdf
        pdfView.fromAsset("sample2.pdf")
                .defaultPage(1)          //设置默认显示第1页
                .showMinimap(false)      //pdf放大的时候，是否在屏幕的右上角生成小地图
                .swipeVertical( false )  //pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                .enableSwipe(true)       //是否允许翻页，默认是允许翻
                .onDraw(new OnDrawListener() {  //绘图监听
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {

                    }
                })
                .onPageChange(new OnPageChangeListener() {  //用户翻页时回调
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        Toast.makeText(getApplicationContext(),page + "/" + pageCount,Toast.LENGTH_SHORT).show();
                    }
                })
                .load();
    }




/*

   private MuPDFCore core;
    public static String documentUri = "http://www.adobe.com/content/dam/Adobe/en/devnet/acrobat/pdfs/pdf_open_parameters.pdf";

    private TextView tv_read;
    private Button btn_read;
    private String pdfName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(R.layout.activity_pdf_read);
        btn_read = findViewById(R.id.btn_read_PDF);

        Uri uri = Uri.parse(documentUri);
        startMuPDFActivity(uri);
    }

    //URI查看
    public void startMuPDFActivity(Uri uri) {
        Intent intent = new Intent(this, DocumentActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(intent);
    }

    //本地文件查看
    public void startMuPDFActivityWithExampleFile() {
        File dir = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS);
        File file = new File(dir, "example.pdf");
        Uri uri = Uri.fromFile(file);
        startMuPDFActivity(uri);
    }
*/

}
