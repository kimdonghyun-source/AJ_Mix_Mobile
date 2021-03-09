package kr.co.ajcc.wms.menu.mix;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.menu.popup.TwoBtnPopup;
import kr.co.ajcc.wms.model.MixBarcodeScan;
import kr.co.ajcc.wms.model.MixEquModel;

public class MixEquAdapter  extends RecyclerView.Adapter<MixEquAdapter.ViewHolder> {

    List<MixEquModel.Items> itemsList;
    Activity mActivity;
    Handler mHandler = null;
    TwoBtnPopup mPopup;

    public MixEquAdapter(Activity context) {
        mActivity = context;
    }

    public void setData(List<MixEquModel.Items> list) {
        itemsList = list;
    }

    public void setRetHandler(Handler h){
        this.mHandler = h;
    }

    public void addData(MixEquModel.Items item) {
        if (itemsList == null) itemsList = new ArrayList<>();
        itemsList.add(item);
    }

    public void setSumHandler(Handler h){
        this.mHandler = h;
    }

    public void clearData() {
        if (itemsList != null) itemsList.clear();
    }

    public List<MixEquModel.Items> getData() {
        return itemsList;
    }

    @Override
    public MixEquAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_mix_equ, viewGroup, false);
        MixEquAdapter.ViewHolder holder = new MixEquAdapter.ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MixEquAdapter.ViewHolder holder, final int position) {

        final MixEquModel.Items item = itemsList.get(position);

        //holder.tv_no.setText(""+(position+1)+".");
        holder.tv_code.setText(item.getEqu_code() + " " + item.getEqu_name());


    }

    @Override
    public int getItemCount() {
        return (null == itemsList ? 0 : itemsList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_code;

        public ViewHolder(View view) {
            super(view);
            tv_code = view.findViewById(R.id.tv_code);


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
