package kr.co.ajcc.wms.menu.mix;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.menu.popup.TwoBtnPopup;
import kr.co.ajcc.wms.model.MixBarcodeScan;

public class MixManegeAdapter extends RecyclerView.Adapter<MixManegeAdapter.ViewHolder> {

        List<MixBarcodeScan.Items> itemsList;
        Activity mActivity;
        Handler mHandler = null;
        TwoBtnPopup mPopup;

public MixManegeAdapter(Activity context) {
        mActivity = context;
        }

public void setData(List<MixBarcodeScan.Items> list) {
        itemsList = list;
        }

public void setRetHandler(Handler h){
        this.mHandler = h;
        }

public void addData(MixBarcodeScan.Items item) {
        if (itemsList == null) itemsList = new ArrayList<>();
        itemsList.add(item);
        }

public void setSumHandler(Handler h){
        this.mHandler = h;
        }

public void clearData() {
        if (itemsList != null) itemsList.clear();
        }

public List<MixBarcodeScan.Items> getData() {
        return itemsList;
        }

@Override
public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_mix_manage, viewGroup, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
        }

@Override
public void onBindViewHolder(final ViewHolder holder, final int position) {

final MixBarcodeScan.Items item = itemsList.get(position);

        //holder.tv_no.setText(""+(position+1)+".");
        holder.tv_code.setText(item.getItm_code());
        holder.tv_name.setText(item.getItm_name());
        holder.tv_size.setText(item.getItm_size());
        holder.tv_serial.setText(item.getMwe_serial());
        holder.tv_qty.setText(Utils.setComma(item.getMwe_qty()));

        /*holder.btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopup = new TwoBtnPopup(mActivity, item.getItm_code()+" 삭제하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            itemsList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, itemsList.size());
                            mPopup.hideDialog();
                        }
                    }
                });
            }
        });*/

        }

@Override
public int getItemCount() {
        return (null == itemsList ? 0 : itemsList.size());
        }

public class ViewHolder extends RecyclerView.ViewHolder {

    TextView tv_code;
    TextView tv_name;
    TextView tv_size;
    TextView tv_serial;
    TextView tv_qty;


    public ViewHolder(View view) {
        super(view);
        tv_code = view.findViewById(R.id.tv_code);
        tv_name = view.findViewById(R.id.tv_name);
        tv_size = view.findViewById(R.id.tv_size);
        tv_serial = view.findViewById(R.id.tv_serial);
        tv_qty = view.findViewById(R.id.tv_qty);

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