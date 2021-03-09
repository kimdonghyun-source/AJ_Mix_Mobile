package kr.co.ajcc.wms.menu.mix;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.model.EmpListModel;
import kr.co.ajcc.wms.model.MixDetailList;
import kr.co.ajcc.wms.model.MixEquModel;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.network.ApiClientService;
import kr.co.ajcc.wms.spinner.SpinnerPopupAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MixAdapter extends RecyclerView.Adapter<MixAdapter.ViewHolder> {

    List<MixDetailList.Items> itemsList;
    List<EmpListModel.Item> empList;
    Activity mActivity;
    Handler mHandler = null;
    int mSpinnerSelect = 0;
    List<Map<String, Object>> spList;
    String sp_select;

    public MixAdapter(Activity context) {
        mActivity = context;
    }

    public void setData(List<MixDetailList.Items> list) {
        itemsList = list;
    }

    public void clearData() {
        if (itemsList != null) itemsList.clear();
    }

    public void setRetHandler(Handler h) {
        this.mHandler = h;
    }

    public List<MixDetailList.Items> getData() {
        return itemsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {
        spList = new ArrayList<>();
        requestEmpList("");
        List<String> list = new ArrayList<>();
        list.add("");

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

        holder.tv_emp_name.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                requestEmpList("");

                List<String> list = new ArrayList<>();
                for (EmpListModel.Item items : empList)
                    list.add(items.getEmp_code() + " " + items.getEmp_name());

                SpinnerPopupAdapter spinnerAdapter = new SpinnerPopupAdapter(mActivity, list, holder.tv_emp_name);
                holder.tv_emp_name.setAdapter(spinnerAdapter);
                holder.tv_emp_name.setOnItemSelectedListener(onItemSelectedListener);
                holder.tv_emp_name.setSelection(mSpinnerSelect);
                final String str = holder.tv_emp_name.getSelectedItem().toString();
                empList.get(holder.getAdapterPosition()).setC_emp_code(str);
                holder.tv_spinner.setText(str);
                return false;
            }
        });
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
        Spinner tv_emp_name;
        FrameLayout f_gone;
        TextView tv_spinner;

        public ViewHolder(View view) {
            super(view);

            tv_code = view.findViewById(R.id.tv_code);
            tv_name = view.findViewById(R.id.tv_name);
            tv_size = view.findViewById(R.id.tv_size);
            tv_scan_cnt = view.findViewById(R.id.tv_scan_cnt);
            tv_all_cnt = view.findViewById(R.id.tv_all_cnt);
            tv_emp_name = view.findViewById(R.id.tv_emp_name);
            f_gone = view.findViewById(R.id.f_gone);
            tv_spinner = view.findViewById(R.id.tv_spinner);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //Utils.Toast(mActivity, itemsList.get(getAdapterPosition()).getItm_name()+" 피킹 페이지 이동");
                        Message msg = new Message();
                        msg.obj = itemsList.get(getAdapterPosition());
                        msg.what = getAdapterPosition();
                        mHandler.sendMessage(msg);
                    } catch (Exception e) {
                        Utils.Log(e.getMessage());
                    }
                }
            });

        }
    }

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSpinnerSelect = position;
            sp_select = empList.get(mSpinnerSelect).getEmp_code().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    /**
     * 사원찾기
     */
    private void requestEmpList(String code) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<EmpListModel> call = service.postEmpList("sp_pda_mix_emp", "");

        call.enqueue(new Callback<EmpListModel>() {
            @Override
            public void onResponse(Call<EmpListModel> call, Response<EmpListModel> response) {
                if (response.isSuccessful()) {
                    EmpListModel model = response.body();

                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            //MixEquModel.Items o = mEquModel.getItems().get(0);
                            empList = model.getItems();

                            /*List<String> list = new ArrayList<>();
                            for (EmpListModel.Item items : empList)
                                list.add(items.getEmp_code());*/

                        } else {
                            Utils.Toast(mActivity, model.getMSG());
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mActivity, response.code() + " : " + response.message());
                }
            }

            @Override
            public void onFailure(Call<EmpListModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mActivity, mActivity.getString(R.string.error_network));
            }
        });
    }

}
