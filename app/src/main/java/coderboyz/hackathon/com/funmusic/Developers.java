package coderboyz.hackathon.com.funmusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

public class Developers extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.developers);
        LinearLayout LL= (LinearLayout) findViewById(R.id.my_layout2);
        Intent intent=getIntent();
        if(intent.getIntExtra("fromIntent1",0)==0){
            LL.setBackgroundResource(R.drawable.skin_101);
        }
        else{
            LL.setBackgroundResource(R.drawable.skin_102);
        }
    }

}
