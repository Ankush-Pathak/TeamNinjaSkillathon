package ankushpathak1511962.com.teamninjaskillathon;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ankushpathak1511962.com.teamninjaskillathon.R;

import static ankushpathak1511962.com.teamninjaskillathon.R.id.fab;
import static ankushpathak1511962.com.teamninjaskillathon.R.id.view_offset_helper;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    ListView listView;
    DatabaseReference databaseReference;
    FirebaseListAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setup();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Form.class));
                finish();
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
        listAdapter=new FirebaseListAdapter<Entry>(this,Entry.class,android.R.layout.simple_list_item_2,databaseReference) {
            @Override
            protected void populateView(View v, Entry model, int position) {
                ((TextView)v.findViewById(android.R.id.text1)).setText(model.getName());
                ((TextView)v.findViewById(android.R.id.text2)).setText(model.getSerialNo());
            }
        };
        listView.setAdapter(listAdapter);
    }

    void setup(){
        //view initialization
        fab = (FloatingActionButton) findViewById(R.id.fab);
        listView = (ListView)findViewById(R.id.listView);
        //firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("TeamNinja");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
