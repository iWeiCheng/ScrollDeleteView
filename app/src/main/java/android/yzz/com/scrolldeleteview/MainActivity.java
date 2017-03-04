package android.yzz.com.scrolldeleteview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    private List<String> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.list_view);
        list = new ArrayList<>();
        for (int i = 0; i <30 ; i++) {
            list.add("==="+i+"===");
        }
        Adapter a = new Adapter();
        mListView.setAdapter(a);
        a.notifyDataSetChanged();

    }


    class Adapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder ;
            if (convertView==null){
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item,null);
                holder = new Holder(convertView);
            }else {
                holder = (Holder) convertView.getTag();
            }
            holder.textTv.setText(list.get(position));
            final ScrollDeleteView s = (ScrollDeleteView) convertView;

            s.setOnAppendClickListener(new ScrollDeleteView.OnAppendClickListener() {
                @Override
                public void click(View v, int p) {
                    switch (p){
                        case 1:
                            list.remove(position);
                            notifyDataSetChanged();
                            break;
                        case 2:
                            String str = list.get(position);
                            list.remove(position);
                            list.add(0,str);
                            notifyDataSetChanged();
                            break;
                    }
                }

                @Override
                public void clickBg() {
                    Toast.makeText(MainActivity.this,"=="+position,Toast.LENGTH_LONG).show();
                }
            });


            return convertView;
        }

        class Holder{
            View v;
            TextView textTv;
            Button btnDelete;
            Button btnTop;

            public Holder(View v) {
                this.v = v;
                textTv = (TextView) v.findViewById(R.id.tv_text);
                btnDelete = (Button) v.findViewById(R.id.btn_delete);
                btnTop = (Button) v.findViewById(R.id.btn_top);
                v.setTag(this);
            }
        }
    }
}
