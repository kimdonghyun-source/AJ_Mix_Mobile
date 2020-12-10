package kr.co.ajcc.wms.menu.location;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.model.LotItemsModel;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    List<LotItemsModel.Items> itemsList;
    Activity mActivity;

    public LocationAdapter(Activity context) {
        mActivity = context;
        itemsList = new ArrayList<>();
    }

    public void setData(LotItemsModel.Items item){ itemsList.add(item); }

    public void clearData(){ itemsList.clear(); }

    public List<LotItemsModel.Items> getData(){
        return itemsList;
    }

    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_lot_item, viewGroup, false);
        LocationAdapter.ViewHolder holder = new LocationAdapter.ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final LocationAdapter.ViewHolder holder, final int position) {

        final LotItemsModel.Items item = itemsList.get(position);

        holder.tv_product.setText(item.getItm_name());
        holder.tv_standard.setText(item.getItm_size());
        holder.tv_count.setText(Utils.setComma(item.getInv_qty()));

        //list에 입력된 수량을 보여주는데 0일 셋팅 되어있는 경우 사용자가 값을 입력하기 번거롭기 때문에 "" 처리
        if(item.getInput_qty() <= 0)
            holder.et_count.setText("");
        else
            holder.et_count.setText(Utils.setComma(item.getInput_qty()));

        //흐르는 text 구현을 위해 selected 처리
        holder.tv_product.setSelected(true);

        holder.et_count.addTextChangedListener(new TextWatcher(){
            String result="";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() > 0 && !s.toString().equals(result)) {     // StackOverflow를 막기위해,
                    result = s.toString();   // 에딧텍스트의 값을 변환하여, result에 저장.

                    float cnt = Utils.stringToInt(s.toString());

                    //수량 체크
                    if(cnt > Utils.stringToInt(holder.tv_count.getText().toString())){
                        Utils.Toast(mActivity, mActivity.getString(R.string.error_location_cnt));
                        holder.et_count.setText("");
                        return;
                    }

                    holder.et_count.setText(result);    // 결과 텍스트 셋팅.
                    holder.et_count.setSelection(result.length());     // 커서를 제일 끝으로 보냄.

                    //입력된 수량을 list에 넣어줌
                    itemsList.get(holder.getAdapterPosition()).setInput_qty(cnt);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null == itemsList ? 0 : itemsList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_product;
        TextView tv_standard;
        EditText et_count;
        TextView tv_count;

        public ViewHolder(View view) {
            super(view);

            tv_product = view.findViewById(R.id.tv_product);
            tv_standard = view.findViewById(R.id.tv_standard);
            et_count = view.findViewById(R.id.et_count);
            tv_count = view.findViewById(R.id.tv_count);
        }
    }
}
