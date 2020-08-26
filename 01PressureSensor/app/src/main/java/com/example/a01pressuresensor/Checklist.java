package com.example.a01pressuresensor;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Checklist extends AppCompatActivity {

    ArrayList<Float> valuelist = new ArrayList<>();//기울기값 리스트

    SQLiteDatabase db = null;
    int column_index;
    int column_max;
    String str_date;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checklist);

        //textview, button 변수 저장
        final TextView tv_measuresinfo = findViewById(R.id.measures_info);
        final TextView tv_slopeinfo = findViewById(R.id.slope_info);
        final TextView tv_IDprint = findViewById(R.id.ID_print);
        final TextView tv_dateprint = findViewById(R.id.date_print);
        final Button btn_back = findViewById(R.id.back);
        final Button btn_next = findViewById(R.id.next);

        //textview 안에 터치시 화면 스크롤 금지, textview 스크롤 가능
        tv_measuresinfo.setMovementMethod(new ScrollingMovementMethod());
        tv_slopeinfo.setMovementMethod(new ScrollingMovementMethod());

        //전달값 변수 저장
        Intent it = getIntent();
        final String str_ID = it.getStringExtra("it_ID");
        str_date = it.getStringExtra("it_date");

        db = openOrCreateDatabase("Measures.db", MODE_PRIVATE, null);

        //회원정보 및 날짜 출력
        tv_IDprint.setText(str_ID);
        tv_dateprint.setText(str_date);

        //측정값 db에서 추출, 추출한 컬럼 위치 추출
        final Cursor cursor = db.rawQuery(String.format("SELECT %s FROM %s", str_date, str_ID), null);
        column_index = cursor.getColumnIndex(str_date);
        column_max = cursor.getColumnCount();

        cursor.moveToFirst();

        //textview에 측정값 표시
        for(int i=0; i<cursor.getCount(); i++, cursor.moveToNext()){
            if(tv_measuresinfo.getText() == null)
                tv_measuresinfo.setText(i + ") " + cursor.getInt(cursor.getColumnIndex(str_date)));
            else
                tv_measuresinfo.setText(tv_measuresinfo.getText() + "\n" + i + ") " + cursor.getInt(cursor.getColumnIndex(str_date)));
        }

        for(int i=0; i<cursor.getCount(); i++, cursor.moveToNext()){
            //기울기값 계산 및 출력
//            for(int i = 0; i < ylist.size(); i++){
//                if(i == 0)
//                    valuelist1.add(ylist.get(i)/f_count);
//                else
//                    valuelist1.add((ylist.get(i) - ylist.get(i-1))/(f_count * i));
//
//                valuelist2.add(ylist.get(i)/f_count);
//            }
        }

        db.close();

        //이전 측정값 출력
//        btn_back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if((column_index-1) <0)
//                    Toast.makeText(view.getContext(), "이전 측정값이 없습니다", Toast.LENGTH_LONG).show();
//                else{
//                    column_index--;//현재 출력할 컬럼
//                    db = openOrCreateDatabase("Measures.db", MODE_PRIVATE, null);//db 오픈
//
//                    Cursor cursor1 = db.rawQuery("SELECT * FROM %s" + str_ID, null);
//                    cursor1.getColumnName(column_index);
//
//                    cursor1 = db.rawQuery(String.format("SELECT %s FROM %s", cursor1.getColumnName(column_index), str_ID), null);
//                    cursor1.moveToFirst();
//
//                    //textview에 측정값 표시
//                    for(int i=0; i<cursor1.getCount(); i++, cursor1.moveToNext()){
//                        if(tv_measuresinfo.getText() == null)
//                            tv_measuresinfo.setText(i + ") " + cursor.getInt(cursor.getColumnIndex(str_date)));
//                        else
//                            tv_measuresinfo.setText(tv_measuresinfo.getText() + "\n" + i + ") " + cursor.getInt(cursor.getColumnIndex(str_date)));
//                    }
//                }
//            }
//        });

        //다음 측정값 출력
//        btn_next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if((column_index+1) > column_max)
//                    Toast.makeText(view.getContext(), "다음 측정값이 없습니다", Toast.LENGTH_LONG).show();
//                else {
//                    column_index++;//현재 출력할 컬럼
//                    db = openOrCreateDatabase("Measures.db", MODE_PRIVATE, null);//db 오픈
//
//                    Cursor cursor2 = db.rawQuery("SELECT * FROM %s" + str_ID, null);
//                    cursor2.getColumnName(column_index);
//
//                    cursor2 = db.rawQuery(String.format("SELECT %s FROM %s", cursor2.getColumnName(column_index), str_ID), null);
//                    cursor2.moveToFirst();
//
//                    //textview에 측정값 표시
//                    for (int i = 0; i < cursor2.getCount(); i++, cursor2.moveToNext()) {
//                        if (tv_measuresinfo.getText() == null)
//                            tv_measuresinfo.setText(i + ") " + cursor.getInt(cursor.getColumnIndex(str_date)));
//                        else
//                            tv_measuresinfo.setText(tv_measuresinfo.getText() + "\n" + i + ") " + cursor.getInt(cursor.getColumnIndex(str_date)));
//                    }
//                }
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        Intent itBack = new Intent(this, Check.class);
        itBack.putExtra("itBack_date", str_date);
        startActivity(itBack);
        finish();
    }
}
