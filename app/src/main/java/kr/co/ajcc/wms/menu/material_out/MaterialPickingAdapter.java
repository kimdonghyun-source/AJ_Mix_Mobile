package kr.co.ajcc.wms.menu.material_out;

import android.app.Activity;
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
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.menu.location.LocationAdapter;
import kr.co.ajcc.wms.model.MaterialLocAndLotModel;
import kr.co.ajcc.wms.model.MaterialLocAndLotModel;
import kr.co.ajcc.wms.model.PalletSnanModel;

public class MaterialPickingAdapter extends RecyclerView.Adapter<MaterialPickingAdapter.ViewHolder> {

    List<MaterialLocAndLotModel.Items> itemsList;
    Activity mActivity;
    Handler mHandler = null;

    public MaterialPickingAdapter(Activity context) {
        mActivity = context;
        itemsList = new ArrayList<>();
    }

    public void setData(List<MaterialLocAndLotModel.Items> item){
        itemsList = item;
    }

    public void addData(MaterialLocAndLotModel.Items item) {
        if (itemsList == null) itemsList = new ArrayList<>();
        itemsList.add(item);
    }

    public void clearData(){
        itemsList.clear();
    }

    public void setSumHandler(Handler h){
        this.mHandler = h;
    }

    public List<MaterialLocAndLotModel.Items> getData(){
        return itemsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
//        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_material_picking, viewGroup, false);
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_lot_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final MaterialLocAndLotModel.Items item = itemsList.get(position);

//        holder.tv_lot.setText("LOT NO. : "+item.getLot_no());
//        holder.tv_count.setText(Utils.setComma(item.getInv_qty()));
//        holder.tv_loc.setText("LOC : "+item.getLocation_code());
        Log.d("JeLib",""+position+":"+Utils.setComma(item.getInv_qty()));
        holder.tv_product.setText(item.getItm_name());
        holder.tv_standard.setText(item.getLocation_code());
        holder.tv_count.setText(Utils.setComma(item.getInv_qty()));
        holder.et_count.setText(""+item.getInput_qty());

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
                Log.d("JeLib","---------------------------");
                if(s.toString().length() > 0 && !s.toString().equals(result)) {     // StackOverflow를 막기위해,
                    result = s.toString();   // 에딧텍스트의 값을 변환하여, result에 저장.

                    int cnt = Utils.stringToInt(result);

                    //아이템 수량 초과시
                    if(cnt > item.getInv_qty()){
                        cnt = item.getInv_qty();
                        result = String.valueOf((int)item.getInv_qty());
                    }

                    holder.et_count.setText(result);    // 결과 텍스트 셋팅.
                    holder.et_count.setSelection(result.length());     // 커서를 제일 끝으로 보냄.

                    //입력된 수량을 list에 넣어줌
                    itemsList.get(holder.getAdapterPosition()).setInput_qty(cnt);
                }
                if(Utils.isEmpty(s.toString())){
                    itemsList.get(holder.getAdapterPosition()).setInput_qty(0);
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

//        TextView tv_lot;
//        TextView tv_count;
//        TextView tv_loc;
//        EditText et_count;

        TextView tv_product;
        TextView tv_standard;
        EditText et_count;
        TextView tv_count;

        public ViewHolder(View view) {
            super(view);

//            tv_lot = view.findViewById(R.id.tv_lot);
//            tv_count = view.findViewById(R.id.tv_count);
//            tv_loc = view.findViewById(R.id.tv_loc);
//            et_count = view.findViewById(R.id.et_count);

            tv_product = view.findViewById(R.id.tv_product);
            tv_standard = view.findViewById(R.id.tv_standard);
            et_count = view.findViewById(R.id.et_count);
            tv_count = view.findViewById(R.id.tv_count);
        }
    }
}
