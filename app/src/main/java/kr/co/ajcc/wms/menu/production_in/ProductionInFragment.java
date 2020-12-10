package kr.co.ajcc.wms.menu.production_in;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.ajcc.wms.GlobalApplication;
import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.SharedData;
import kr.co.ajcc.wms.common.UtilDate;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.honeywell.AidcReader;
import kr.co.ajcc.wms.menu.items.LotItemView;
import kr.co.ajcc.wms.menu.popup.LocationListPopup;
import kr.co.ajcc.wms.menu.popup.OneBtnPopup;
import kr.co.ajcc.wms.menu.popup.TwoBtnPopup;
import kr.co.ajcc.wms.model.LocationModel;
import kr.co.ajcc.wms.model.LotItemsModel;
import kr.co.ajcc.wms.model.PalletSnanModel;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.model.UserInfoModel;
import kr.co.ajcc.wms.model.WarehouseModel;
import kr.co.ajcc.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductionInFragment extends CommonFragment {
    Context mContext;

    EditText et_location;

    TextView tv_cnt;
    TextView tv_total_cnt;

    LocationModel.Items mLocationModel;

    LocationListPopup mLocationListPopup;
    OneBtnPopup mOneBtnPopup;
    TwoBtnPopup mTwoBtnPopup;

    ProductionInAdapter mAdapter;
    List<PalletSnanModel.Items> mItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_production_in, container, false);

        et_location = v.findViewById(R.id.et_location);
        tv_cnt = v.findViewById(R.id.tv_cnt);
        tv_total_cnt = v.findViewById(R.id.tv_total_cnt);

        v.findViewById(R.id.bt_search).setOnClickListener(onClickListener);
        v.findViewById(R.id.bt_next).setOnClickListener(onClickListener);

        //정제영 테스트
        v.findViewById(R.id.bt_scan_location).setOnClickListener(onClickListener);
        v.findViewById(R.id.bt_scan_pallet).setOnClickListener(onClickListener);

        RecyclerView recycleview = v.findViewById(R.id.recycleview);
        recycleview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ProductionInAdapter(getActivity());
        recycleview.setAdapter(mAdapter);

        mItems = new ArrayList<>();

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        AidcReader.getInstance().claim(mContext);
        AidcReader.getInstance().setListenerHandler(new Handler() {
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 1){
                    BarcodeReadEvent event = (BarcodeReadEvent)msg.obj;
                    String barcode = event.getBarcodeData();
                    String location = et_location.getText().toString();
                    if(Utils.isEmpty(location)){
                        requestLocation(barcode, "P");
                    } else {
                        for(PalletSnanModel.Items item : mItems){
                            if(barcode.equals(item.getSerial_no())) {
                                Utils.Toast(mContext, "동일한 팔레트를 선택하셨습니다.");
                                return;
                            }
                        }

                        requestPalletScan(barcode);
                    }
                }
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        AidcReader.getInstance().release();
        AidcReader.getInstance().setListenerHandler(null);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int view = v.getId();
            switch (view) {
                case R.id.bt_search:
                    requestWarehouse("P");
                    break;
                case R.id.bt_scan_location:
                    requestLocation("AA01-1", "P");
                    break;
                case R.id.bt_scan_pallet:
                    requestPalletScan("20200318-000001");
                    break;
                case R.id.bt_next:
                    if(mLocationModel == null) {
                        Utils.Toast(mContext, getString(R.string.error_location));
                        return;
                    }
                    if(mItems.size() <= 0) {
                        Utils.Toast(mContext, getString(R.string.error_in_items));
                        return;
                    }

                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), et_location.getText().toString()+" 로케이션에 완제품 적치등록을 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestSendProductionIn();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                    break;
            }
        }
    };

    /**
     * 창고 검색
     * @param type 창고타입(M : 원자재, P : 완제품)
     */
    private void requestWarehouse(String type) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<WarehouseModel> call = service.postWarehouse("sp_pda_mst_wh_list", type);

        call.enqueue(new Callback<WarehouseModel>() {
            @Override
            public void onResponse(Call<WarehouseModel> call, Response<WarehouseModel> response) {
                if(response.isSuccessful()){
                    WarehouseModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            mLocationListPopup = new LocationListPopup(getActivity(), model.getItems(), R.drawable.popup_title_searchloc, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        mItems.clear();
                                        mAdapter.setData(mItems);
                                        mAdapter.notifyDataSetChanged();

                                        mLocationModel = (LocationModel.Items)msg.obj;
                                        et_location.setText(mLocationModel.getLocation_code());
                                        mLocationListPopup.hideDialog();

                                        tv_cnt.setText(String.valueOf(mLocationModel.getLoc_stk_cnt()));
                                        tv_total_cnt.setText(String.valueOf(mLocationModel.getLoc_tot_cnt()));
                                    }
                                }
                            });
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
            public void onFailure(Call<WarehouseModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 스캔으로 로케이션 검색
     * @param code 창고코드
     * @param type 창고타입(M : 원자재, P : 완제품)
     */
    private void requestLocation(String code, String type) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<LocationModel> call = service.postScanLocation("sp_pda_sin_location_stk_info", code, type);

        call.enqueue(new Callback<LocationModel>() {
            @Override
            public void onResponse(Call<LocationModel> call, Response<LocationModel> response) {
                if(response.isSuccessful()){
                    LocationModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            mLocationModel = model.getItems().get(0);
                            et_location.setText(mLocationModel.getLocation_code());

                            tv_cnt.setText(String.valueOf(mLocationModel.getLoc_stk_cnt()));
                            tv_total_cnt.setText(String.valueOf(mLocationModel.getLoc_tot_cnt()));
                        }else{
                            Utils.Toast(mContext, model.getMSG());
                        }
                    }
                }else{
                    mOneBtnPopup = new OneBtnPopup(getActivity(), response.message(), R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                String result = (String)msg.obj;
                                Utils.Toast(mContext, result);
                                mOneBtnPopup.hideDialog();
                            }
                        }
                    });
                    //Utils.LogLine(response.message());
                    //Utils.Toast(mContext, response.code()+" : "+response.message());
                }
            }

            @Override
            public void onFailure(Call<LocationModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 팔레트 바코드 스캔(시리얼번호)
     * @param barcode 스캔 바코드
     */
    private void requestPalletScan(String barcode) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<PalletSnanModel> call = service.postScanPallet("sp_pda_sin_serial_scan", barcode);


        call.enqueue(new Callback<PalletSnanModel>() {
            @Override
            public void onResponse(Call<PalletSnanModel> call, Response<PalletSnanModel> response) {
                if(response.isSuccessful()){
                    PalletSnanModel model = response.body();
                    Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            if(model.getItems().size() > 0) {
                                mItems.add(model.getItems().get(0));
                                mAdapter.setData(mItems);
                                mAdapter.notifyDataSetChanged();
                            }
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
            public void onFailure(Call<PalletSnanModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    //제품입고 저장
    private void requestSendProductionIn() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        JsonObject json = new JsonObject();
        //로그인ID
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        json.addProperty("p_user_id", userID);

        JsonArray list = new JsonArray();
        for(PalletSnanModel.Items item : mItems){
            JsonObject obj = new JsonObject();
            //입고창고코드
            obj.addProperty("wh_code", mLocationModel.getWh_code());
            //스캔바코드
            obj.addProperty("scan_psn", item.getSerial_no());
            //입고일자
            obj.addProperty("sin_date", UtilDate.getDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd"));
            //입고창고코드
            obj.addProperty("wh_code", mLocationModel.getWh_code());
            //로케이션코드
            obj.addProperty("location_code", mLocationModel.getLocation_code());
            list.add(obj);
        }
        json.add("detail", list);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<ResultModel> call = service.postSendProductionIn(body);

        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                if(response.isSuccessful()){
                    ResultModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), et_location.getText().toString()+" 로케이션에 완제품 입고 및 적치가 등록되었습니다.", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        getActivity().finish();
                                        mOneBtnPopup.hideDialog();
                                    }
                                }
                            });
                            getActivity().finish();
                        }else{
                            mOneBtnPopup = new OneBtnPopup(getActivity(), model.getMSG(), R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        mOneBtnPopup.hideDialog();
                                    }
                                }
                            });
                        }
                    }
                }else{
                    Utils.LogLine(response.message());
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "완제품 입고 및 적치등록 전송이 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestSendProductionIn();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "완제품 입고 및 적치등록 전송이 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            requestSendProductionIn();
                            mTwoBtnPopup.hideDialog();
                        }
                    }
                });
            }
        });
    }
}
