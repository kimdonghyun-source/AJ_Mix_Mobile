package kr.co.ajcc.wms.menu.material_out;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Define;
import kr.co.ajcc.wms.common.SharedData;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.honeywell.AidcReader;
import kr.co.ajcc.wms.menu.main.BaseActivity;
import kr.co.ajcc.wms.menu.popup.OneBtnPopup;
import kr.co.ajcc.wms.menu.popup.OutMeterialListPopup;
import kr.co.ajcc.wms.menu.popup.TwoBtnPopup;
import kr.co.ajcc.wms.model.DeliveryOrderModel;
import kr.co.ajcc.wms.model.MaterialLocAndLotModel;
import kr.co.ajcc.wms.model.MaterialOutDetailModel;
import kr.co.ajcc.wms.model.MaterialOutListModel;
import kr.co.ajcc.wms.model.MaterialSaveModel;
import kr.co.ajcc.wms.model.PalletSnanModel;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.model.WarehouseModel;
import kr.co.ajcc.wms.network.ApiClientService;
import kr.co.ajcc.wms.spinner.SpinnerAdapter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaterialOutFragment extends CommonFragment {
    Context mContext;

    TextView tv_warehouse, tv_input;
    EditText et_order;
    OutMeterialListPopup mOutMeterialListPopup;

    RecyclerView recycleview;
//    MaterialOutAdapter mAdapter;
    MaterialPickingAdapter mAdapter;

    //리스트가 없을때 보여지는 text
    TextView tv_empty;

    //불출 창고 코드
    String mWarehouseCode;
    String mWarehouseTargetCode;
    //불출지시 상세
    MaterialOutDetailModel mMaterialOutDetailModel;
    MaterialLocAndLotModel mMaterialLocAndLotModel;

    TwoBtnPopup mTwoBtnPopup;
    OneBtnPopup mOneBtnPopup;

    List<Map<String, Object>> spList;
    Spinner mSpinner;
    Spinner mSpinner2;
    int mSpinnerSelect = 0;
    int mSpinnerSelect2 = 0;

    List<String> mBarcode;
    String mLocation;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        spList = new ArrayList<>();
        mBarcode = new ArrayList<>();
    }

    @Override
    public void onResume(){
        super.onResume();
        AidcReader.getInstance().claim(mContext);
        AidcReader.getInstance().setListenerHandler(new Handler() {
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 1){

//                    BarcodeReadEvent event = (BarcodeReadEvent)msg.obj;
//                    String barcode = event.getBarcodeData();

//                    mAdapter.clearData();
//                    mAdapter.notifyDataSetChanged();

//                    requestOrderDetail(barcode);


                    BarcodeReadEvent event = (BarcodeReadEvent)msg.obj;
                    String barcode = event.getBarcodeData();

                    if(mSpinnerSelect == mSpinnerSelect2){
                        Utils.Toast(mContext, "동일한 창고를 선택하셨습니다.");
                        return;
                    }

                    if(mBarcode.contains(barcode)){
                        Utils.Toast(mContext, "동일한 품목을 선택하셨습니다.");
                        return;
                    }

                    mLocation = barcode;
                    mWarehouseCode = spList.get(mSpinnerSelect).get("value").toString();
                    mWarehouseTargetCode = spList.get(mSpinnerSelect2).get("value").toString();
                    requestLocAndLot();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_material_out, container, false);

        tv_empty = v.findViewById(R.id.tv_empty);
        et_order = v.findViewById(R.id.et_order);
        tv_warehouse = v.findViewById(R.id.tv_warehouse);
        tv_input = v.findViewById(R.id.tv_input);
        v.findViewById(R.id.bt_search).setOnClickListener(onClickListener);
        v.findViewById(R.id.bt_next).setOnClickListener(onClickListener);

        //정제영 테스트
        v.findViewById(R.id.bt_order).setOnClickListener(onClickListener);

        mSpinner =  v.findViewById(R.id.spinner);
        mSpinner2 = v.findViewById(R.id.spinner2);
        requestWarehouse2();
        /*recycleview = v.findViewById(R.id.recycleview);
        recycleview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MaterialOutAdapter(getActivity());
        recycleview.setAdapter(mAdapter);

        mAdapter.setRetHandler(new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what >= 0){
                    goMaterialPicking(msg.what);
                }
            }
        });*/

        recycleview = v.findViewById(R.id.recycleview);
        recycleview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MaterialPickingAdapter(getActivity());
        recycleview.setAdapter(mAdapter);

        mAdapter.setSumHandler(new Handler(){
            @Override
            public void handleMessage(Message msg){
            if(msg.what == 1){
                List<MaterialLocAndLotModel.Items> itms = mAdapter.getData();
            }
            }
        });


        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == 100){
            if(resultCode == Activity.RESULT_OK){
//                mMaterialOutDetailModel = (MaterialOutDetailModel) data.getSerializableExtra("model");
//                mAdapter.setData(mMaterialOutDetailModel.getItems());
//                mAdapter.notifyDataSetChanged();

                mMaterialLocAndLotModel = (MaterialLocAndLotModel) data.getSerializableExtra("model");
                mAdapter.setData(mMaterialLocAndLotModel.getItems());
                mAdapter.notifyDataSetChanged();

            }
        }
    }

    private void goMaterialPicking(int position){
        Intent intent = new Intent(mContext, BaseActivity.class);
        intent.putExtra("menu", Define.MENU_MATERIAL_PICKING);
        Bundle extras = new Bundle();
        extras.putSerializable("model", mMaterialOutDetailModel);
        extras.putSerializable("position", position);
        extras.putSerializable("code", mWarehouseCode);
        intent.putExtra("args",extras);
        startActivityForResult(intent,100);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int view = v.getId();

            switch (view){
                case R.id.bt_search:
//                    requestWarehouse();
                    break;
                case R.id.bt_order:
                    et_order.setText("100-20200323-001-160");
//                    requestOrderDetail("100-20200323-001-160");
                    break;
                case R.id.bt_next:
                    /*if(Utils.isEmpty(et_order.getText().toString())){
                        Utils.Toast(mContext, "불출지시서를 입력해주세요.");
                        return;
                    }*/
                    if(mAdapter.getItemCount() <= 0){
                        Utils.Toast(mContext, "불출할 자재를 입력해주세요.");
                        return;
                    }

//                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), et_order.getText().toString()+"의 자재불출을 전송하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "자재불출을 전송하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestMaterialSend();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                    break;
            }
        }
    };

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSpinnerSelect = mSpinner.getSelectedItemPosition();
            mSpinnerSelect2 = mSpinner2.getSelectedItemPosition();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    /**
     * 창고 검색
     */
  /*  private void requestWarehouse() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<WarehouseModel> call = service.postWarehouse("sp_pda_mst_wh_list", "M");

        call.enqueue(new Callback<WarehouseModel>() {
            @Override
            public void onResponse(Call<WarehouseModel> call, Response<WarehouseModel> response) {
                if(response.isSuccessful()){
                    WarehouseModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            mOutMeterialListPopup = new OutMeterialListPopup(getActivity(), model.getItems(), R.drawable.popup_title_searchloc, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        MaterialOutListModel.Items item = (MaterialOutListModel.Items) msg.obj;
                                        et_order.setText(item.getOut_slip_no());
                                        tv_warehouse.setText(item.getWh_name_out());
                                        tv_input.setText(item.getWh_name_in());
                                        mOutMeterialListPopup.hideDialog();

                                        mWarehouseCode = item.getWh_code_out();
                                        requestOrderDetail(item.getOut_slip_no());
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
    }*/

    /**
     * 창고 검색2
     */
    private void requestWarehouse2() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<WarehouseModel> call = service.postWarehouse("sp_pda_mst_wh_list", "M");

        call.enqueue(new Callback<WarehouseModel>() {
            @Override
            public void onResponse(Call<WarehouseModel> call, Response<WarehouseModel> response) {
                if(response.isSuccessful()){
                    WarehouseModel model = response.body();
                    List<String> list = new ArrayList<>();

                    for(WarehouseModel.Items itm : model.getItems()){
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", itm.getWh_name());
                        map.put("value", itm.getWh_code());
                        spList.add(map);
                        list.add(itm.getWh_name());
                    }

                    SpinnerAdapter spinnerAdapter = new SpinnerAdapter(mContext, list, mSpinner, 0);
                    mSpinner.setAdapter(spinnerAdapter);
                    mSpinner.setOnItemSelectedListener(onItemSelectedListener);
                    mSpinner.setSelection(mSpinnerSelect);

                    SpinnerAdapter spinnerAdapter2 = new SpinnerAdapter(mContext, list, mSpinner2, 1);
                    mSpinner2.setAdapter(spinnerAdapter2);
                    mSpinner2.setOnItemSelectedListener(onItemSelectedListener);
                    mSpinner2.setSelection(mSpinnerSelect2);

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
     * 불출지시 상세
     */
    /*private void requestOrderDetail(String param) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

            Call<MaterialOutDetailModel> call = service.postOutOrderDetail("sp_pda_out_detail", param);

        call.enqueue(new Callback<MaterialOutDetailModel>() {
            @Override
            public void onResponse(Call<MaterialOutDetailModel> call, Response<MaterialOutDetailModel> response) {
                if(response.isSuccessful()){
                    mMaterialOutDetailModel = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (mMaterialOutDetailModel != null) {
                        if(mMaterialOutDetailModel.getFlag() == ResultModel.SUCCESS) {
                            if(mMaterialOutDetailModel.getItems().size() > 0) {
                                //장고코드 입력
                                mWarehouseCode = mMaterialOutDetailModel.getItems().get(0).getWh_code_out();
                                tv_warehouse.setText(mMaterialOutDetailModel.getItems().get(0).getWh_name_out());
                                tv_input.setText(mMaterialOutDetailModel.getItems().get(0).getWh_name_in());
                                mAdapter.setData(mMaterialOutDetailModel.getItems());
                                mAdapter.notifyDataSetChanged();

                                tv_empty.setVisibility(View.GONE);
                                recycleview.setVisibility(View.VISIBLE);
                            }
                        }else{
                            Utils.Toast(mContext, mMaterialOutDetailModel.getMSG());
                        }
                    }
                }else{
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code()+" : "+response.message());
                }
            }

            @Override
            public void onFailure(Call<MaterialOutDetailModel> call, Throwable t) {
                Utils.Log(t.getMessage());
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }*/

    /**
     * 로케이션 및 로트번호 스캔
     */
    private void requestLocAndLot() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        if(Utils.isEmpty(mWarehouseCode)){
            Utils.Toast(mContext, "출고창고코드가 없습니다.");
            return;
        }
        if(Utils.isEmpty(mLocation)){
            Utils.Toast(mContext, "입력된 로케이션코드가 없습니다.");
            return;
        }
        Call<MaterialLocAndLotModel> call = service.postOutLocAndLot("sp_pda_out_loc_lot_scan", mWarehouseCode, mLocation, null);

        call.enqueue(new Callback<MaterialLocAndLotModel>() {
            @Override
            public void onResponse(Call<MaterialLocAndLotModel> call, Response<MaterialLocAndLotModel> response) {
                if(response.isSuccessful()){
                    MaterialLocAndLotModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            if(model.getItems().size() > 0) {
                                for(int i = 0 ; i < model.getItems().size();i++) {
                                    MaterialLocAndLotModel.Items item= (MaterialLocAndLotModel.Items)model.getItems().get(i);
                                    mAdapter.addData(item);
                                }
                                mAdapter.notifyDataSetChanged();

                                tv_empty.setVisibility(View.GONE);
                                recycleview.setVisibility(View.VISIBLE);
                                mSpinner.setEnabled(false);
                                mSpinner2.setEnabled(false);
                                mBarcode.add(mLocation);
                            }
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
                    Utils.Toast(mContext, response.code()+" : "+response.message());
                }
            }

            @Override
            public void onFailure(Call<MaterialLocAndLotModel> call, Throwable t) {
                Utils.Log(t.getMessage());
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 자재불출 전송
     */
    private void requestMaterialSend() {
        if(mAdapter.getItemCount() <= 0){
            Toast.makeText(mContext,"불출할 자재수량이 없습니다.",Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        //로그인ID
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");

        Map params = new HashMap();
        params.put("p_wh_code",mWarehouseCode);
        params.put("p_wh_code_target",mWarehouseTargetCode);
        params.put("p_user_id",userID);
        List detail = new ArrayList();
        List<MaterialLocAndLotModel.Items> itemList = mAdapter.getData();
        for (MaterialLocAndLotModel.Items material : itemList) {
            Map param = new HashMap();
            param.put("scan_psn", material.getSerial_no());
            param.put("out_qty", material.getInput_qty());
            detail.add(param);
        }
        params.put("detail", detail);
        Gson gson = new Gson();
        String json = gson.toJson(params);
        /*
        JsonObject json = new JsonObject();
        //불출창고
        json.addProperty("p_wh_code", mWarehouseCode);
        //투입공정(타켓창고)
        json.addProperty("p_wh_code_target", mWarehouseTargetCode);
        //로그인ID
        json.addProperty("p_user_id", userID);

        JsonArray list = new JsonArray();
        int count = 0;
        List<MaterialLocAndLotModel.Items> itemList = mAdapter.getData();
        if (itemList != null && itemList.size() > 0) {
            for (MaterialLocAndLotModel.Items material : itemList) {
                //JsonObject obj = new JsonObject();
                //시스템로트번호
                //obj.addProperty("lot_no", material.getLot_no());
                //로케이션코드
                //obj.addProperty("location_code", material.getLocation_code());
                //불출수량
                //obj.addProperty("out_qty", material.getInput_qty());
                //자재불출품목순번
                //obj.addProperty("out_no2", item.getOut_no2());

                //시리얼번호
                obj.addProperty("scan_psn", material.getSerial_no());
                //불출수량
                obj.addProperty("out_qty", material.getInput_qty());

                list.add(obj);
                count += material.getInput_qty();
            }
        }
        json.add("detail", list);
        */
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);

        Call<ResultModel> call = service.postMaterialSend(body);

        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                if(response.isSuccessful()){
                    ResultModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
//                            mOneBtnPopup = new OneBtnPopup(getActivity(), et_order.getText().toString()+"의 자재불출 처리가 완료되었습니다.", R.drawable.popup_title_alert, new Handler() {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), "자재불출 처리가 완료되었습니다.", R.drawable.popup_title_alert, new Handler() {
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
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), et_order.getText().toString()+"의 자재불출 전송이 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestMaterialSend();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                mTwoBtnPopup = new TwoBtnPopup(getActivity(), et_order.getText().toString()+"의 자재불출 전송이 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            requestMaterialSend();
                            mTwoBtnPopup.hideDialog();
                        }
                    }
                });
            }
        });
    }
}
