package com.jacob.circle.lock;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    private TextView mTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textView);

        LockPatternView lockPatternView = (LockPatternView) findViewById(R.id.lock_pattern_view);
        lockPatternView.setOnLockPatternListener(new LockPatternView.OnLockPatternListener() {
            @Override
            public void onLockPatternSuccess(String pwd) {
                mTextView.setText(pwd);
            }

            @Override
            public void onLockPatterError() {
                mTextView.setText("绘制长度不够");
            }

            @Override
            public void onLockPatterStart() {
                mTextView.setText("请开始绘制");
            }
        });
    }

}
