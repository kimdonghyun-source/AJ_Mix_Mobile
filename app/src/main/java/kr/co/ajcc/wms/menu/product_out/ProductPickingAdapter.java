package kr.co.ajcc.wms.menu.product_out;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Define;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.menu.main.BaseActivity;
import kr.co.ajcc.wms.model.DeliveryOrderModel;
import kr.co.ajcc.wms.model.PalletSnanModel;

public class ProductPickingAdapter extends RecyclerView.Adapter<ProductPickingAdapter.ViewHolder> {

    List<PalletSnanModel.Items> itemsList;
    Activity mActivity;
    Handler mHandler = null;

    public ProductPickingAdapter(Activity context) {
        mActivity = context;
    }

    public void setData(List<PalletSnanModel.Items> list) {
        itemsList = list;
    }

    public void addData(PalletSnanModel.Items item) {
        if (itemsList == null) itemsList = new ArrayList<>();
        itemsList.add(item);
    }

    public void setSumHandler(Handler h){
        this.mHandler = h;
    }

    public void clearData() {
        if (itemsList != null) itemsList.clear();
    }

    public List<PalletSnanModel.Items> getData() {
        return itemsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_product_picking, viewGroup, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final PalletSnanModel.Items item = itemsList.get(position);

        holder.tv_no.setText(""+(position+1)+".");
        holder.tv_code.setText(item.getSerial_no());
        holder.et_count.setText(Utils.setComma(item.getReq_qty()));

        holder.et_count.addTextChangedListener(new TextWatcher() {
            String result = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0 && !s.toString().equals(String.valueOf(result))) {     // StackOverflow를 막기위해,
                    result = s.toString();   // 에딧텍스트의 값을 변환하여, result에 저장.

                    int cnt = Utils.stringToInt(result);

                    //아이템 수량 초과시
                    if(cnt > Utils.stringToInt(item.getItm_pallet_qty())){
                        cnt = Utils.stringToInt(item.getItm_pallet_qty());
                        result = item.getItm_pallet_qty();
                    }

                    holder.et_count.setText(result);    // 결과 텍스트 셋팅.
                    holder.et_count.setSelection(result.length());     // 커서를 제일 끝으로 보냄.

                    //입력된 수량을 list에 넣어줌
                    itemsList.get(holder.getAdapterPosition()).setReq_qty(cnt);
                }
                if(Utils.isEmpty(s.toString())){
                    itemsList.get(holder.getAdapterPosition()).setReq_qty(0);
                }
                mHandler.sendEmptyMessage(1);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null == itemsList ? 0 : itemsList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_no;
        TextView tv_code;
        EditText et_count;

        public ViewHolder(View view) {
            super(view);
            tv_no = view.findViewById(R.id.tv_no);
            tv_code = view.findViewById(R.id.tv_code);
            et_count = view.findViewById(R.id.et_count);
        }
    }
}
