package com.example.a01pressuresensor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

//블루투스연결
//추출값 g->N으로
//line chart
public class Check extends AppCompatActivity {

    private BluetoothSPP bt;
    private LineChart chart;

    int f_count = 10;//y값을 몇번만에 측정할 것인가
    int count = f_count;//측정값을 받은 횟수(0이되면 10으로 초기화)
    boolean y_falg = false;//y값이 0->0이상이 되었는가, 0이상->0이 되었는가

    //기울기값 ver.1, ver.2 저장 리스트
    ArrayList<Float> valuelist1 = new ArrayList<>();//1번째 방법 기울기값 리스트
    ArrayList<Float> valuelist2 = new ArrayList<>();//2번째 방법 기울기값 리스트
    ArrayList<Float> ylist = new ArrayList<>();//y값(측정값) 출력 저장 리스트(리스트내 객체 개수 = x의 증가량)

    SQLiteDatabase db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check);

        //textview, scrollview, button 변수에 저장
        final TextView tv_value1 = (TextView)findViewById(R.id.value1);
        final TextView tv_value2 = (TextView)findViewById(R.id.value2);
        final TextView tv_ID = (TextView)findViewById(R.id.ID);
        final TextView tv_date = (TextView)findViewById(R.id.date);
        final ScrollView scrview = (ScrollView)findViewById(R.id.scrview);
        final Button bt_connect = (Button)findViewById(R.id.connect);
        final Button bt_save = (Button)findViewById(R.id.savebtn);

        //textview 안에 터치시 화면 스크롤 금지, textview 스크롤 가능
        tv_value1.setMovementMethod(new ScrollingMovementMethod());
        tv_value2.setMovementMethod(new ScrollingMovementMethod());

       tv_value1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scrview.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        tv_value2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scrview.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        //tv_date.setText(db.getVersion());

        //line chart 틀 생성
        chart = (LineChart)findViewById(R.id.linechart);

        chart.setTouchEnabled(false);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setAutoScaleMinMaxEnabled(false);
        chart.setPinchZoom(false);

        chart.getXAxis().setDrawGridLines(true);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getXAxis().setEnabled(true);
        chart.getXAxis().setDrawGridLines(false);

        chart.enableScroll();

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.invalidate();

        //bluetooth 지원 여부
        bt = new BluetoothSPP(this);
        if(!bt.isBluetoothAvailable()){
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않습니다", Toast.LENGTH_SHORT).show();
        }

        //데이터 수신
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            TextView tv_measure = findViewById(R.id.measures);

            public void onDataReceived(byte[] data, String message){
                //String->float형 변환
                float measures = Float.valueOf(message);
                tv_measure.setText(message + "g");

                //y값(측정값) 추출
                if(y_falg == false && measures > 0)
                {
                    y_falg = true;
                    count--;
                }
                else if(y_falg == true){
                    if(count > 0){
                        count--;

                        if(measures == 0){
                            ylist.add(measures);
                            y_falg = false;
                            count = f_count;
                        }
                    }
                    else if(count == 0){
                        count = f_count;
                        ylist.add(measures);
                    }
                }

                //차트 객체 추가 함수
                addEntry(measures);
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext(), "Connected to " + name + "\n" + address, Toast.LENGTH_SHORT).show();
            }

            //연결해제
            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext(), "연결 해제", Toast.LENGTH_SHORT).show();
            }

            //연결실패
            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext(), "연결 실패", Toast.LENGTH_SHORT).show();
            }
        });

        bt_connect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                    bt_connect.setText("측정 재시작");

                    //기울기 측정
                    for(int i = 0; i < ylist.size(); i++){
                        if(i == 0)
                            valuelist1.add(ylist.get(i)/f_count);
                        else
                            valuelist1.add((ylist.get(i) - ylist.get(i-1))/(f_count * i));

                        valuelist2.add(ylist.get(i)/f_count);
                    }

                    //기울기 출력
                    TextView tv_value1 = findViewById(R.id.value1);
                    TextView tv_value2 = findViewById(R.id.value2);

                    //기울기값 textview에 설정
                    for (int i = 0; i<valuelist1.size(); i++)
                    {
                        if(i == 0)
                        {
                            tv_value1.setText("(" + (i+1) + ")" +  valuelist1.get(i).toString());
                            tv_value2.setText("(" + (i+1) + ")" +  valuelist2.get(i).toString());
                        }
                        else
                        {
                            tv_value1.setText(tv_value1.getText() + "\n(" + (i+1) + ")" +  valuelist1.get(i).toString());
                            tv_value2.setText(tv_value2.getText() + "\n(" + (i+1) + ")" +  valuelist2.get(i).toString());
                        }
                    }

                } else {

                    //기존에 작성된 차트 삭제 및 textview 텍스트 삭제
                    if(chart.getLineData() != null){
                        LineData data = chart.getLineData();
                        data.clearValues();
                        chart.invalidate();
                        chart.clear();

                        tv_value1.setText(null);
                        tv_value2.setText(null);
                    }
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                    bt_connect.setText("측정 중지");
                }
            }
        });

        //저장
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_ID = (String) tv_ID.getText();
                String str_date = "D" + (String)tv_date.getText();
                String DB_NAME = "Measures.db";

                db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
                db.setVersion(Integer.parseInt(tv_date.getText().toString())+1);

                //테이블 생성 및 컬럼 추가
                Cursor cursor = db.rawQuery(String.format("SELECT name FROM sqlite_master WHERE type='table' AND name='%s'", str_ID),
                        null);
                //최종 수정 후 주석부분으로 변경
//                if(cursor.moveToNext()){
//                    db.execSQL(String.format("ALTER TABLE %s ADD %s INTEGER", str_ID, str_date));
//                }
//                else{
//                    db.execSQL(String.format("CREATE TABLE %s (%s int)", str_ID, str_date));
//                }
                if(cursor.moveToNext())
                    db.execSQL(String.format("DROP TABLE %s", str_ID));
                    //db.execSQL(String.format("ALTER TABLE %s ADD %s INTEGER", str_ID, db.getVersion())); //next, back버튼 활성화 후 주석부분으로 변경
                db.execSQL(String.format("CREATE TABLE %s (%s int)", str_ID, str_date));

                //새로 추가된 컬럼(혹은 테이블)에 측정값 저장
                for(int i=0; i<ylist.size(); i++){
                    db.execSQL(String.format("INSERT INTO %s (%s) VALUES ('%s')", str_ID, str_date, ylist.get(i)));
                }

                db.close();

                //화면 전환 및 ID값 date값 전달
                Intent it = new Intent(view.getContext(), Checklist.class);
                it.putExtra("it_ID", str_ID);
                it.putExtra("it_date", str_date);
                //it.putExtra("it_date", str_date); //최종 수정 후 주석부분으로 변경

                startActivity(it);
                finish();
            }
        });
    }

    //블루투스 중지
    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth was not enabled.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void addEntry(float measures){
        LineData data = chart.getData();

        if(data == null){
            data = new LineData();
            chart.setData(data);
        }

        //0번째 데이턴셋
        ILineDataSet set = data.getDataSetByIndex(0);

        if(set == null){
            set = createSet();
            data.addDataSet(set);
        }

        data.addEntry(new Entry((float)set.getEntryCount(), measures), 0);
        data.notifyDataChanged();
        chart.notifyDataSetChanged();

        chart.setVisibleXRangeMaximum(200);
        chart.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);
    }

    //linechart 세팅
    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null, "압력값(g)");

        set.setLineWidth(1f);
        set.setDrawValues(false);
        set.setValueTextColor(Color.RED);
        set.setColor(Color.RED);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawCircles(false);

        return set;
    }
}
