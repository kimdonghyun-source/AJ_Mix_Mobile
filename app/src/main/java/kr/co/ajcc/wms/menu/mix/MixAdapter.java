package kr.co.ajcc.wms.menu.mix;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.model.MixDetailList;

public class MixAdapter extends RecyclerView.Adapter<MixAdapter.ViewHolder> {

    List<MixDetailList.Items> itemsList;
    Activity mActivity;
    Handler mHandler = null;

    public MixAdapter(Activity context) {
        mActivity = context;
    }

    public void setData(List<MixDetailList.Items> list){
        itemsList = list;
    }
    public void clearData(){
        if(itemsList!=null)itemsList.clear();
    }

    public void setRetHandler(Handler h){
        this.mHandler = h;
    }

    public List<MixDetailList.Items> getData(){
        return itemsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_mix_detail, viewGroup, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final MixDetailList.Items item = itemsList.get(position);

        holder.tv_code.setText(item.getItm_code());
        holder.tv_name.setText(item.getItm_name());
        holder.tv_size.setText(item.getItm_size());
        holder.tv_scan_cnt.setText(Utils.setComma(item.getScan_cnt()));
        holder.tv_all_cnt.setText(Utils.setComma(item.getAll_cnt()));
        //holder.tv_emp_name.setText(item.get()); 작업자 추가 되면 넣기

    }

    @Override
    public int getItemCount() {
        return (null == itemsList ? 0 : itemsList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_code;
        TextView tv_name;
        TextView tv_size;
        TextView tv_scan_cnt;
        TextView tv_all_cnt;
        TextView tv_emp_name;

        public ViewHolder(View view) {
            super(view);

            tv_code = view.findViewById(R.id.tv_code);
            tv_name = view.findViewById(R.id.tv_name);
            tv_size = view.findViewById(R.id.tv_size);
            tv_scan_cnt = view.findViewById(R.id.tv_scan_cnt);
            tv_all_cnt = view.findViewById(R.id.tv_all_cnt);
            tv_emp_name = view.findViewById(R.id.tv_emp_name);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //Utils.Toast(mActivity, itemsList.get(getAdapterPosition()).getItm_name()+" 피킹 페이지 이동");
                        Message msg = new Message();
                        msg.obj = itemsList.get(getAdapterPosition());
                        msg.what = getAdapterPosition();
                        mHandler.sendMessage(msg);
                    } catch (Exception e){
                        Utils.Log(e.getMessage());
                    }
                }
            });

        }
    }
}
