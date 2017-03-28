package com.notrace;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.notrace.email.Email;

public class MainActivity extends AppCompatActivity {
    private EditText et_email,et_title,et_content,et_attach;
    private Button btn_send;

    private String userName="cy_nforget@126.com";
    private String password="struggle365";
    private String fromEmail="cy_nforget@126.com";
    private String toEmail="";
    private String server="smtp.126.com";
    private String title;
    private String body;
    private String attatch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void initView(){
        et_email= (EditText) findViewById(R.id.et_email);
        et_title= (EditText) findViewById(R.id.et_title);
        et_content= (EditText) findViewById(R.id.et_content);
        et_attach= (EditText) findViewById(R.id.et_attach);
        btn_send= (Button) findViewById(R.id.btn_send);


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toEmail=et_email.getText().toString().trim();
                title=et_title.getText().toString().trim();
                body=et_content.getText().toString().trim();
                attatch=et_attach.getText().toString().trim();


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                            new Email.Builder()
                                    .userName(userName)
                                    .password(password)
                                    .fromMail(fromEmail)
                                    .toMail(toEmail)
                                    .server(server)
                                    .title(title)
                                    .body(body)
                                    .attachment(attatch)
                                    .build().send();
                        }
                            catch (Exception e)
                            {

                                Log.d("====",e.toString());
                            }
                        }
                    }).start();



            }
        });
    }
}
