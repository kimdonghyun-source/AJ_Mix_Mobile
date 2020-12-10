package kr.co.ajcc.wms.spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import kr.co.ajcc.wms.R;

public class SpinnerAdapter extends BaseAdapter {
    Context mContext;
    List<String> mData;
    Spinner mSpinner;
    LayoutInflater inflater;
    int idx = 0;

    public SpinnerAdapter(Context context, List<String> data, Spinner spinner, int flag){
        mContext = context;
        mData = data;
        mSpinner = spinner;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        idx = flag;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            view = inflater.inflate(R.layout.view_spinner, parent, false);
        }

        if(idx == 0){
            view.findViewById(R.id.fr_spinner).setBackgroundResource(R.drawable.menu_release_inputinwh);
        }else{
            view.findViewById(R.id.fr_spinner).setBackgroundResource(R.drawable.menu_release_inputoutwh);
        }

        if(mData != null){
            String text = mData.get(position);
            TextView tv_title = view.findViewById(R.id.tv_title);
            tv_title.setText(text);
        }

        return view;
    }

    //스피너 눌렀을때 리스트 레이아웃
    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if(view == null){
            view = inflater.inflate(R.layout.cell_spinner, parent, false);
        }

        //데이터세팅
        String text = mData.get(position);
        TextView tv_title = view.findViewById(R.id.tv_title);
        tv_title.setText(text);

        return view;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}