package com.example.zzx.zbar_demo.PdfRead;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.example.zzx.zbar_demo.R;
import java.io.File;


public class PDFStartActivity extends AppCompatActivity {
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
