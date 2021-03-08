package kr.co.ajcc.wms.menu.mix;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.honeywell.aidc.BarcodeReadEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Define;
import kr.co.ajcc.wms.common.UtilDate;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.honeywell.AidcReader;
import kr.co.ajcc.wms.menu.main.BaseActivity;
import kr.co.ajcc.wms.menu.popup.LocationListPopup;
import kr.co.ajcc.wms.menu.popup.OutMeterialListPopup;
import kr.co.ajcc.wms.model.MixDetailList;
import kr.co.ajcc.wms.model.MixMrcpListModel;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.network.ApiClientService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MixFragment extends CommonFragment {

    OutMeterialListPopup mOutMeterialListPopup;
    MixDetailList.Items mMixDetailList;
    MixDetailList mDetail;
    MixMrcpListModel.Items mMixList;
    List<MixDetailList.Items>mItems;
    EditText et_location;
    RecyclerView recycleview;
    MixAdapter mAdapter;
    TextView text_lot, text_info, text_customer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }//Close onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_mix, container, false);
        et_location = v.findViewById(R.id.et_location);
        recycleview = v.findViewById(R.id.recycleview);
        text_lot = v.findViewById(R.id.text_lot);
        text_info = v.findViewById(R.id.text_info);
        text_customer = v.findViewById(R.id.text_customer);

        recycleview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MixAdapter(getActivity());
        recycleview.setAdapter(mAdapter);

        mItems = new ArrayList<>();

        v.findViewById(R.id.bt_search).setOnClickListener(onClickListener);

        mAdapter.setRetHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what >= 0) {
                    goMixManageDetail(msg.what);
                }
            }
        });
        return v;
    }//Close onCreateView

    private void goMixManageDetail(int position){
        Intent intent = new Intent(getActivity(), BaseActivity.class);
        intent.putExtra("menu", Define.MENU_MIX_MANAGE);

        Bundle extras = new Bundle();
        extras.putSerializable("model", mDetail);
        extras.putSerializable("position", position);
        intent.putExtra("args", extras);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onResume(){
        super.onResume();
        AidcReader.getInstance().claim(mContext);
        AidcReader.getInstance().setListenerHandler(new Handler() {
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 1){
                    mAdapter.clearData();
                    BarcodeReadEvent event = (BarcodeReadEvent)msg.obj;
                    String barcode = event.getBarcodeData();
                    String location = et_location.getText().toString();
                    et_location.setText(barcode);
                    requestOutOrderDetail(barcode);
                    /*if(Utils.isEmpty(location)){
                        requestOutOrderDetail(barcode);
                    } else {
                        *//*for(PalletSnanModel.Items item : mItems){
                            if(barcode.equals(item.getSerial_no())) {
                                Utils.Toast(mContext, "동일한 팔레트를 선택하셨습니다.");
                                return;
                            }
                        }*//*

                        requestPalletScan(barcode);
                    }*/
                }
            }
        });
    }



    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int view = v.getId();
            switch (view) {
                case R.id.bt_search:
                    requestWarehouse("");
                    //requestWarehouse("20210225", "");   //테스트 데이터 끝나면 위에걸로 변경

                    break;
            }
        }
    };//Close onClickListener

    /**
     * 제조지시 No 조회
     * @param1 date 제조지시일자
     * @param2 code 설비코드
     */
    private void requestWarehouse(String slip_no) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MixDetailList> call = service.mrcp_Detail_list("sp_pda_mix_mrcp_detail", slip_no);

        call.enqueue(new Callback<MixDetailList>() {
            @Override
            public void onResponse(Call<MixDetailList> call, Response<MixDetailList> response) {
                if(response.isSuccessful()){
                    MixDetailList model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {

                            mOutMeterialListPopup = new OutMeterialListPopup(getActivity(), model.getItems(), R.drawable.popup_title_searchloc, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        MixMrcpListModel.Items item = (MixMrcpListModel.Items)msg.obj;
                                        mMixList = item;
                                        et_location.setText(mMixList.getMrcp_slip_code());
                                        mOutMeterialListPopup.hideDialog();
                                        requestOutOrderDetail(et_location.getText().toString());
                                    }
                                }
                            });

                    }
                }else{
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code()+" : "+response.message());
                }
            }

            @Override
            public void onFailure(Call<MixDetailList> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }


    /**
     * 제조지시서상세
     * @param slip_no 제조지시번호
     */
    private void requestOutOrderDetail(String slip_no) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MixDetailList> call = service.mrcp_Detail_list("sp_pda_mix_mrcp_detail",  slip_no);

        call.enqueue(new Callback<MixDetailList>() {
            @Override
            public void onResponse(Call<MixDetailList> call, Response<MixDetailList> response) {
                if(response.isSuccessful()){
                    MixDetailList model = response.body();
                    mDetail = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            mAdapter.clearData();
                            mMixDetailList = model.getItems().get(0);
                            MixDetailList.Items o = mDetail.getItems().get(0);
                            text_lot.setText(mMixDetailList.getMrcp_lotno());
                            text_info.setText(String.valueOf(mMixDetailList.getMrcp_qty()));
                            text_customer.setText(mMixDetailList.getEqu_name());
                            mItems.add(model.getItems().get(0));

                            mAdapter.setData(mItems);
                            mAdapter.notifyDataSetChanged();

                        }else{
                            Utils.Toast(mContext, model.getMSG());
                        }
                    }
                }else{
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code()+" : "+response.message());
                }
            }

            @Override
            public void onFailure(Call<MixDetailList> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, mContext.getString(R.string.error_network));
            }
        });
    }










}//Close Activity
