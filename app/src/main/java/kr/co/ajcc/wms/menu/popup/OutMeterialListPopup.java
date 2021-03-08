package kr.co.ajcc.wms.menu.popup;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.UtilDate;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.model.MaterialOutListModel;
import kr.co.ajcc.wms.model.MixDetailList;
import kr.co.ajcc.wms.model.MixMrcpListModel;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.model.WarehouseModel;
import kr.co.ajcc.wms.network.ApiClientService;
import kr.co.ajcc.wms.spinner.SpinnerPopupAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutMeterialListPopup {
    Activity mActivity;
    Dialog dialog;
    List<MixMrcpListModel.Items> mMixList;
    List<MixDetailList.Items> mMixDetailList;
    Handler mHandler;
    Spinner mSpinner;
    int mSpinnerSelect = 0;
    TextView tv_date;
    ListView mListView;
    ListAdapter mAdapter;

    public OutMeterialListPopup(Activity activity, List<MixDetailList.Items> list, int title, Handler handler) {
        mActivity = activity;
        mMixDetailList = list;
        mHandler = handler;
        showPopUpDialog(activity, title);
    }

    public void hideDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();

        }
    }

    public boolean isShowDialog() {
        if (dialog != null && dialog.isShowing()) {
            return true;
        } else {
            return false;
        }
    }

    private void showPopUpDialog(Activity activity, int title) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        dialog.setContentView(R.layout.popup_material_list);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        //팝업을 맨 위로 올려야 함.
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        ImageView iv_title = dialog.findViewById(R.id.iv_title);
        iv_title.setBackgroundResource(title);

        tv_date = dialog.findViewById(R.id.tv_date);
        final String date = UtilDate.getDateToString(new Date(System.currentTimeMillis()), "yyyy-MM-dd");
        tv_date.setText(date);
        tv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(mActivity, dateListener, Utils.stringToInt(date.replace("-", "").substring(0, 4)), Utils.stringToInt(date.replace("-", "").substring(4, 6))-1, Utils.stringToInt(date.replace("-", "").substring(6, 8)));
                dialog.show();
            }
        });

        List<String> list = new ArrayList<>();
      /*  for (MixMrcpListModel.Items item : mMixList)
            list.add(item.getEqu_name());*/
        
        mSpinner = dialog.findViewById(R.id.spinner);
        SpinnerPopupAdapter spinnerAdapter = new SpinnerPopupAdapter(activity, list, mSpinner);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(onItemSelectedListener);

        mListView = dialog.findViewById(R.id.list);
        mAdapter = new ListAdapter();
        mListView.setAdapter(mAdapter);

        dialog.findViewById(R.id.bt_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( tv_date.getText().toString() != null)
                    requestOutOrderList();
            }
        });

        dialog.findViewById(R.id.bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDialog();
            }
        });

        dialog.show();
    }

    DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String date = String.format("%04d", year)+"-"+String.format("%02d", monthOfYear+1)+"-"+String.format("%02d", dayOfMonth);
            tv_date.setText(date);
        }
    };

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSpinnerSelect = position;

            MixMrcpListModel.Items item = mMixList.get(position);
            //String item = (String) mSpinner.getSelectedItem();

            mActivity = null;
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };



    class ListAdapter extends BaseAdapter {
        LayoutInflater mInflater;
        List<MixMrcpListModel.Items> mList;

        public void setData(List<MixMrcpListModel.Items> list){
            mList = list;
        }

        public ListAdapter() {
            mInflater = LayoutInflater.from(mActivity);
        }

        @Override
        public int getCount() {
            if (mList == null) {
                return 0;
            }
            return mList.size();
        }


        @Override
        public MixMrcpListModel.Items getItem(int position){
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final ListAdapter.ViewHolder holder;
            if (v == null) {
                holder = new ListAdapter.ViewHolder();
                v = mInflater.inflate(R.layout.cell_pop_mix, null);
                v.setTag(holder);

                holder.tv_date = v.findViewById(R.id.tv_date);
                holder.tv_code = v.findViewById(R.id.tv_code);
                holder.tv_qty = v.findViewById(R.id.tv_qty);

            } else {
                holder = (ListAdapter.ViewHolder) v.getTag();
            }

            final MixMrcpListModel.Items data = mList.get(position);
            holder.tv_date.setText(data.getMrcp_date());
            holder.tv_code.setText(data.getEqu_name());
            holder.tv_qty.setText(String.valueOf(data.getMrcp_qty()));

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = mList.get(position);
                    mHandler.sendMessage(msg);
                }
            });

            return v;
        }

        class ViewHolder {
            TextView tv_date;
            TextView tv_code;
            TextView tv_qty;
        }
    }

    /**
     * 출고지시서 검색
     */
    private void requestOutOrderList() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MixMrcpListModel> call = service.mrcp_list("sp_pda_mix_mrcp_list", tv_date.getText().toString().replace("-", ""), "");

        call.enqueue(new Callback<MixMrcpListModel>() {
            @Override
            public void onResponse(Call<MixMrcpListModel> call, Response<MixMrcpListModel> response) {
                if(response.isSuccessful()){
                    MixMrcpListModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            ListView listView = dialog.findViewById(R.id.list);
                            ListAdapter adapter = new ListAdapter();
                            adapter.setData(model.getItems());
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }else{
                            Utils.Toast(mActivity, model.getMSG());
                        }
                    }
                }else{
                    Utils.LogLine(response.message());
                    Utils.Toast(mActivity, response.code()+" : "+response.message());
                }
            }

            @Override
            public void onFailure(Call<MixMrcpListModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mActivity, mActivity.getString(R.string.error_network));
            }
        });
    }   //Close 출고검색

}