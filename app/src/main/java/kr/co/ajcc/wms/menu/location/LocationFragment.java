package kr.co.ajcc.wms.menu.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import kr.co.ajcc.wms.GlobalApplication;
import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.UtilDate;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.common.SharedData;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.custom.MergeAdapter;
import kr.co.ajcc.wms.honeywell.AidcReader;
import kr.co.ajcc.wms.menu.items.LotItemView;
import kr.co.ajcc.wms.menu.popup.LocationListPopup;
import kr.co.ajcc.wms.menu.popup.OneBtnPopup;
import kr.co.ajcc.wms.menu.popup.TwoBtnPopup;
import kr.co.ajcc.wms.menu.production_in.ProductionInAdapter;
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

public class LocationFragment extends CommonFragment {
    static final int FROM = 0;
    static final int TO = 1;

    Context mContext;

    LocationListPopup mLocationListPopup;
    OneBtnPopup mOneBtnPopup;

    EditText et_from;
    EditText et_to;

    RecyclerView recycleview;
    LocationAdapter mAdapter;

    //리스트가 없을때 보여지는 text
    TextView tv_empty;

    //로케이션 model
    LocationModel.Items fromLocation;
    LocationModel.Items toLocation;

    TwoBtnPopup mTwoBtnPopup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_location, container, false);

        v.findViewById(R.id.bt_from).setOnClickListener(onClickListener);
        v.findViewById(R.id.bt_to).setOnClickListener(onClickListener);
        v.findViewById(R.id.bt_next).setOnClickListener(onClickListener);

        //정제영 테스트
        v.findViewById(R.id.bt_from_location).setOnClickListener(onClickListener);
        v.findViewById(R.id.bt_to_location).setOnClickListener(onClickListener);
        v.findViewById(R.id.bt_scan_product).setOnClickListener(onClickListener);

        et_from = v.findViewById(R.id.et_from);
        et_to = v.findViewById(R.id.et_to);

        tv_empty = v.findViewById(R.id.tv_empty);

        recycleview = v.findViewById(R.id.recycleview);
        recycleview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new LocationAdapter(getActivity());
        recycleview.setAdapter(mAdapter);

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
                    String from_location = et_from.getText().toString();
                    String to_location = et_to.getText().toString();

                    if(Utils.nullString(from_location,"").length() <= 0){
                        requestLocation(FROM, barcode);
                    } else if(Utils.nullString(to_location,"").length() <= 0){
                        requestLocation(TO, barcode);
                    } else {
                        if(fromLocation == null) {
                            Utils.Toast(mContext, getString(R.string.error_location_from));
                            return;
                        }
                        if(toLocation == null) {
                            Utils.Toast(mContext, getString(R.string.error_location_to));
                            return;
                        }
                        List<LotItemsModel.Items> items = mAdapter.getData();
                        if(items!=null){
                            for(LotItemsModel.Items it : items){
                                if(it.getLot_no().trim().equals(barcode)){
                                    Toast.makeText(mContext,"중복 스캔 데이터가 있습니다.",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }

                        requestLotItems(fromLocation.getLocation_code(), barcode);
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
                case R.id.bt_from:
                    requestWarehouse(FROM, "M");
                    break;
                case R.id.bt_to:
                    //from이 반드시 선택 되어야 to를 선택할 수 있다.
                    if(fromLocation == null)
                        Utils.Toast(mContext, getString(R.string.error_location_from));
                    else
                        requestWarehouse(TO, "M");
                    break;
                case R.id.bt_from_location:
                    requestLocation(FROM, "AA01-1");
                    break;
                case R.id.bt_to_location:
                    requestLocation(TO, "AA01-2");
                    break;
                case R.id.bt_scan_product:
                    if(fromLocation == null) {
                        Utils.Toast(mContext, getString(R.string.error_location_from));
                        return;
                    }
                    if(toLocation == null) {
                        Utils.Toast(mContext, getString(R.string.error_location_to));
                        return;
                    }
                    requestLotItems(fromLocation.getLocation_code(), "*");
                    break;
                case R.id.bt_next:
                    if(fromLocation == null) {
                        Utils.Toast(mContext, getString(R.string.error_location_from));
                        return;
                    }
                    if(toLocation == null) {
                        Utils.Toast(mContext, getString(R.string.error_location_to));
                        return;
                    }
                    if(mAdapter.getData().size() <= 0) {
                        Utils.Toast(mContext, getString(R.string.error_location_items));
                        return;
                    }


                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), et_to.getText().toString()+" 로케이션으로 이동처리를 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestSendLocation();
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
     * @param cmd  0 : from, 1 : to
     * @param type 창고타입(M : 원자재, P : 완제품)
     */
    private void requestWarehouse(final int cmd, String type) {
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
                                        LocationModel.Items item = (LocationModel.Items)msg.obj;
                                        if(cmd == FROM){    //from인 경우에만 재고정보를 조회한다.
                                            mAdapter.clearData();
                                            mAdapter.notifyDataSetChanged();

                                            fromLocation = item;
                                            et_from.setText(fromLocation.getLocation_code());
                                        }else if(cmd == TO){    //to인 경우엔 from과 같은 창고인지 비교
                                            mAdapter.clearData();
                                            mAdapter.notifyDataSetChanged();


                                            if (fromLocation.getWh_code().equals(item.getWh_code())) {
                                                toLocation = item;

                                                if(fromLocation.getLocation_code().equals(toLocation.getLocation_code())) {
                                                    Utils.Toast(mContext, getString(R.string.error_location_check));
                                                    return;
                                                }

                                                et_to.setText(toLocation.getLocation_code());
                                            } else {      //같은 창고가 아니면
                                                mOneBtnPopup = new OneBtnPopup(getActivity(), "같은 창고내에서만 로케이션 이동이 가능합니다.\n로케이션을 확인하세요.", R.drawable.popup_title_alert, new Handler() {
                                                    @Override
                                                    public void handleMessage(Message msg) {
                                                        if (msg.what == 1) {
                                                            mOneBtnPopup.hideDialog();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                        mLocationListPopup.hideDialog();
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
     * 로케이션 조회
     * @param cmd  0 : from, 1 : to
     * @param code  로케이션 코드
     */
    private void requestLocation(final int cmd, String code) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<LocationModel> call = service.postWarehouseLocation("sp_pda_location_search", code);

        call.enqueue(new Callback<LocationModel>() {
            @Override
            public void onResponse(Call<LocationModel> call, Response<LocationModel> response) {
                if(response.isSuccessful()){
                    LocationModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            if(cmd == FROM){    //from인 경우에만 재고정보를 조회한다.
                                mAdapter.clearData();
                                mAdapter.notifyDataSetChanged();

                                fromLocation = model.getItems().get(0);
                                et_from.setText(fromLocation.getLocation_code());
                            }else if(cmd == TO){    //to인 경우엔 from과 같은 창고인지 비교
                                mAdapter.clearData();
                                mAdapter.notifyDataSetChanged();

                                if(fromLocation.getWh_code().equals(model.getItems().get(0).getWh_code())) {
                                    toLocation = model.getItems().get(0);

                                    if(fromLocation.getLocation_code().equals(toLocation.getLocation_code())) {
                                        Utils.Toast(mContext, getString(R.string.error_location_check));
                                        return;
                                    }

                                    et_to.setText(toLocation.getLocation_code());
                                }else{      //같은 창고가 아니면
                                    mOneBtnPopup = new OneBtnPopup(getActivity(), "같은 창고내에서만 로케이션 이동이 가능합니다.\n로케이션을 확인하세요.", R.drawable.popup_title_alert, new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            if (msg.what == 1) {
                                                mOneBtnPopup.hideDialog();
                                                et_to.setText("");
                                            }
                                        }
                                    });
                                }
                            }
                        }else{
                            if(cmd == FROM){    //from인 경우에만 재고정보를 조회한다.
                                et_from.setText("");
                            }else if(cmd == TO) {
                                et_to.setText("");
                            }
                            mAdapter.clearData();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }else{
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code()+" : "+response.message());
                    if(cmd == FROM){    //from인 경우에만 재고정보를 조회한다.
                        et_from.setText("");
                    }else if(cmd == TO) {
                        et_to.setText("");
                    }
                    mAdapter.clearData();
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<LocationModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
                if(cmd == FROM){    //from인 경우에만 재고정보를 조회한다.
                    et_from.setText("");
                }else if(cmd == TO) {
                    et_to.setText("");
                }
                mAdapter.clearData();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 로트번호 스캔
     * @param location 로케이션
     * @param lot 스캔한 로트번호
     */
    private void requestLotItems(String location, String lot) {

        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<LotItemsModel> call = service.postLotItems("sp_pda_location_move_itm_scan", location, lot);

        call.enqueue(new Callback<LotItemsModel>() {
            @Override
            public void onResponse(Call<LotItemsModel> call, Response<LotItemsModel> response) {
                if(response.isSuccessful()){
                    LotItemsModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            if(model.getItems().size() > 0) {
                                mAdapter.setData(model.getItems().get(0));
                                mAdapter.notifyDataSetChanged();

                                tv_empty.setVisibility(View.GONE);
                                recycleview.setVisibility(View.VISIBLE);
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
            public void onFailure(Call<LotItemsModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    //로케이션 이동 전송
    private void requestSendLocation() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        GlobalApplication application = (GlobalApplication)mContext.getApplicationContext();
        UserInfoModel.Items userModel = application.getUserInfoModel();

        JsonObject json = new JsonObject();
        //이동일자
        json.addProperty("p_move_date", UtilDate.getDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd"));
        //사원번호
        json.addProperty("p_emp_code", userModel.getEmp_code());
        //사용예정일(사용 안하기 때문에 공란)
        json.addProperty("p_use_date", "");
        //입고창고코드
        json.addProperty("p_wh_code_in", fromLocation.getWh_code());
        //출고창고코드
        json.addProperty("p_wh_code_out", toLocation.getWh_code());
        //로그인ID
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        json.addProperty("p_user_id", userID);

        List<LotItemsModel.Items> items = mAdapter.getData();
        float total = 0;
        JsonArray list = new JsonArray();
        for(LotItemsModel.Items item : items){

            JsonObject obj = new JsonObject();
            //품목코드
            obj.addProperty("itm_code", item.getItm_code());
            //출고로케이션
            obj.addProperty("location_from", fromLocation.getLocation_code());
            //입고로케이션
            obj.addProperty("location_to", toLocation.getLocation_code());
            //이동수량
            obj.addProperty("move_qty", String.valueOf(item.getInput_qty()));
            //로트 번호
            obj.addProperty("lot_no", item.getLot_no());
            //제조사 로트 번호
            obj.addProperty("lot_no2", item.getLot_no2());
            list.add(obj);

            total =+ item.getInput_qty();
        }
        //총 이동수량
        json.addProperty("p_itm_qty", total);
        json.add("detail", list);

        Utils.Log("new Gson().toJson(json) ==> : "+new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<ResultModel> call = service.postSendLocation(body);

        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                if(response.isSuccessful()){
                    ResultModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), et_to.getText().toString()+" 로케이션으로 이동처리 되었습니다.", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        getActivity().finish();
                                        mOneBtnPopup.hideDialog();
                                    }
                                }
                            });
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
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "이동 전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestSendLocation();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "이동 전송을 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            requestSendLocation();
                            mTwoBtnPopup.hideDialog();
                        }
                    }
                });
            }
        });
    }
}
