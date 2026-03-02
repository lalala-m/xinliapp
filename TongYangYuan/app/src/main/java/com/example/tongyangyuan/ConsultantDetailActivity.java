package com.example.tongyangyuan;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tongyangyuan.consult.Consultant;
import com.example.tongyangyuan.webview.WebViewActivity;

public class ConsultantDetailActivity extends AppCompatActivity {

    public static final String KEY_CONSULTANT = "consultant";
    private Consultant consultant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        consultant = (Consultant) getIntent().getSerializableExtra(KEY_CONSULTANT);
        if (consultant == null) {
            finish();
            return;
        }
        
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "consultant_detail.html");
        intent.putExtra("name", consultant.getName());
        intent.putExtra(AppointmentActivity.KEY_CONSULTANT, consultant);
        startActivity(intent);
        finish();
    }

    public Consultant getConsultant() {
        return consultant;
    }
}
