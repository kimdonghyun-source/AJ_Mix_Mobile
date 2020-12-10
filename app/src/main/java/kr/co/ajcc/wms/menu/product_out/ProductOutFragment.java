package kr.co.ajcc.wms.menu.product_out;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import java.util.Date;
import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Define;
import kr.co.ajcc.wms.common.SharedData;
import kr.co.ajcc.wms.common.UtilDate;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.custom.MergeAdapter;
import kr.co.ajcc.wms.honeywell.AidcReader;
import kr.co.ajcc.wms.menu.main.BaseActivity;
import kr.co.ajcc.wms.menu.popup.OneBtnPopup;
import kr.co.ajcc.wms.menu.popup.OutProductListPopup;
import kr.co.ajcc.wms.menu.popup.TwoBtnPopup;
import kr.co.ajcc.wms.menu.production_in.ProductionInAdapter;
import kr.co.ajcc.wms.model.CustomerInfoModel;
import kr.co.ajcc.wms.model.DeliveryOrderModel;
import kr.co.ajcc.wms.model.PalletSnanModel;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductOutFragment extends CommonFragment {
    Context mContext;
    EditText et_location;
    OutProductListPopup mLocationListPopup;
    //ListView o_listView;
    MergeAdapter mMergeAdapter;
    TextView text_empty, text_customer, text_info;

    ProductOutAdapter mAdapter = null;
    DeliveryOrderModel mDeliveryOrderModel  =   null;
    TwoBtnPopup mPopup = null;
    OneBtnPopup mOneBtnPopup  = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_product_out, container, false);
        et_location = v.findViewById(R.id.et_location);
        //o_listView = v.findViewById(R.id.o_listView);
        text_empty = v.findViewById(R.id.text_empty);
        text_customer = v.findViewById(R.id.text_customer);
        text_info = v.findViewById(R.id.text_info);
        mMergeAdapter = new MergeAdapter();
        v.findViewById(R.id.bt_search).setOnClickListener(onClickListener);
        v.findViewById(R.id.btn_next).setOnClickListener(onClickListener);

        //정제영 테스트
        v.findViewById(R.id.bt_test_1).setOnClickListener(onClickListener);

        RecyclerView recycleview = v.findViewById(R.id.recycleview);
        recycleview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ProductOutAdapter(getActivity());
        recycleview.setAdapter(mAdapter);
        mAdapter.setRetHandler(new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what >= 0){
                    goProductPicking(msg.what);
                }
            }
        });
        return v;
    }

    private void goProductPicking(int position){
        Intent intent = new Intent(getActivity(), BaseActivity.class);
        intent.putExtra("menu", Define.MENU_PRODUCT_PICKING);

        Bundle extras = new Bundle();
        extras.putSerializable("model",mDeliveryOrderModel);
        extras.putSerializable("position",position);
        intent.putExtra("args",extras);
        startActivityForResult(intent,100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        Utils.Log("--------------onActivityResult---------------1");
        if(requestCode == 100){
            if(resultCode == Activity.RESULT_OK){
                mDeliveryOrderModel = (DeliveryOrderModel) data.getSerializableExtra("model");
                mAdapter.setData(mDeliveryOrderModel.getItems());
                mAdapter.notifyDataSetChanged();
            }
        }
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
                    text_customer.setText("");
                    text_info.setText("");
                    mAdapter.clearData();

                    requestDeliveryOrderDetail(barcode);
                }
            }
        });
        if(mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        AidcReader.getInstance().release();
        AidcReader.getInstance().setListenerHandler(null);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            int view = v.getId();

            switch (view){
                case R.id.btn_next:
                    try{
                        mPopup.hideDialog();
                    } catch (Exception e){

                    }
                    String order_no = et_location.getText().toString();
                    if(Utils.isEmpty(order_no) || mDeliveryOrderModel==null){
                        Toast.makeText(mContext,"출품지시서를 스캔및 검색 선택하세요.",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int qty = 0;
                    StringBuffer buffer = null;
                    for(DeliveryOrderModel.DeliveryOrder o : mDeliveryOrderModel.getItems()){
                        if(o.getReq_qty() > 0) {
                            if (buffer == null) {
                                buffer = new StringBuffer();
                                buffer.append("\"" + text_customer.getText().toString() + "\"").append("으로");
                                buffer.append("\"" + o.getItm_name() + "\"");
                            }
                            qty++;
                        }
                    }
                    if(qty == 1){
                        buffer.append("출고처리를 하시겠습니까?");
                    } else if(qty > 1){
                        buffer.append("외 ").append((qty-1)+"개품목").append("출고처리를 하시겠습니까?");
                    } else {
                        Toast.makeText(mContext,"출품 처리할 수량이 없습니다..",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mPopup = new TwoBtnPopup(getActivity(), buffer.toString(), R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestSendProductionOut();
                                mPopup.hideDialog();
                            }
                        }
                    });
                    break;
                case R.id.bt_search:
                    mLocationListPopup = new OutProductListPopup(getActivity(), R.drawable.popup_title_searchoutorder, new Handler(){
                        @Override
                        public void handleMessage(Message msg){
                            if (msg.what ==1 ){
                                mLocationListPopup.hideDialog();
                                CustomerInfoModel.CustomerInfo order = (CustomerInfoModel.CustomerInfo)msg.obj;
                                et_location.setText(order.getReq_car_no());
                                text_customer.setText(order.getCst_name());
                                text_info.setText(order.getPo_no());
                                text_customer.setSelected(true);

                                requestDeliveryOrderDetail(order.getReq_car_no());
                            }
                        }
                    });
                    break;
                case R.id.bt_test_1:
                    text_customer.setText("");
                    text_info.setText("");
                    requestDeliveryOrderDetail("100-1-20200310-0703");
                    break;
            }
        }
    };

    /**
     * 출고지시서 상세
     */
    private void requestDeliveryOrderDetail(final String param) {
        if(mAdapter!=null) {
            mAdapter.clearData();
            mAdapter.notifyDataSetChanged();
        }
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<DeliveryOrderModel> call = service.postShipReqDetail("sp_pda_ship_req_detail", param);

        call.enqueue(new Callback<DeliveryOrderModel>() {
            @Override
            public void onResponse(Call<DeliveryOrderModel> call, Response<DeliveryOrderModel> response) {
                if(response.isSuccessful()){
                    mDeliveryOrderModel = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (mDeliveryOrderModel != null) {
                        if(mDeliveryOrderModel.getFlag() == ResultModel.SUCCESS) {
                            et_location.setText(param);
                            if(mDeliveryOrderModel.getItems().size() > 0) {
                                DeliveryOrderModel.DeliveryOrder o = mDeliveryOrderModel.getItems().get(0);
                                text_customer.setText(o.getCst_name());
                                text_info.setText(o.getPo_no());
                                text_customer.setSelected(true);

                                mAdapter.setData(mDeliveryOrderModel.getItems());
                                mAdapter.notifyDataSetChanged();
                            }
                        }else{
                            Utils.Toast(mContext, mDeliveryOrderModel.getMSG());
                        }
                    }
                }else{
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code()+" : "+response.message());
                }
            }

            @Override
            public void onFailure(Call<DeliveryOrderModel> call, Throwable t) {
                Utils.Log(t.getMessage());
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    //제품입고 저장
    private void requestSendProductionOut() {
        String order_no = et_location.getText().toString();
        if(Utils.isEmpty(order_no) || mDeliveryOrderModel==null){
            Toast.makeText(mContext,"출품지시서를 스캔및 검색 선택하세요.",Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        JsonObject json = new JsonObject();
        //로그인ID
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");

        json.addProperty("p_req_car_no", et_location.getText().toString());
        json.addProperty("p_user_id", userID);

        List<DeliveryOrderModel.DeliveryOrder> items = (List<DeliveryOrderModel.DeliveryOrder>)mDeliveryOrderModel.getItems();
        JsonArray list = new JsonArray();
        int count = 0;
        if(items!=null && items.size()>0) {
            for (DeliveryOrderModel.DeliveryOrder item : items) {
                List<PalletSnanModel.Items> snList = item.getItems();
                if (snList != null && snList.size() > 0) {
                    for (PalletSnanModel.Items sn : snList) {
                        JsonObject obj = new JsonObject();
                        //스캔바코드
                        obj.addProperty("scan_psn", sn.getSerial_no());
                        //출고수량
                        obj.addProperty("out_qty", sn.getReq_qty());
                        //출하의뢰품목순번
                        obj.addProperty("req_no2", item.getReq_no2());
                        list.add(obj);
                        count += sn.getReq_qty();
                    }
                }
            }
        }
        if(count<=0){
            Toast.makeText(mContext,"전송할 제품출고수량이 없습니다.",Toast.LENGTH_SHORT).show();
            return;
        }
        json.add("detail", list);

        Utils.Log("new Gson().toJson(json) ==> : "+new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<ResultModel> call = service.postSendProductionOut(body);

        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                try{
                    mPopup.hideDialog();
                    mOneBtnPopup.hideDialog();
                }catch (Exception e){

                }
                if(response.isSuccessful()){
                    ResultModel model = response.body();
                    Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), text_customer.getText().toString()+"으로 출고처리 등록되었습니다.", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        getActivity().finish();
                                        mOneBtnPopup.hideDialog();
                                    }
                                }
                            });
                        }else{
                            Utils.Toast(mContext, model.getMSG());
                            mPopup = new TwoBtnPopup(getActivity(), "제품출고내역 전송이 실패하였습니다.재전송하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        requestSendProductionOut();
                                        mPopup.hideDialog();
                                    }
                                }
                            });
                        }
                    }
                }else{
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code()+" : "+response.message());
                    mPopup = new TwoBtnPopup(getActivity(), "제품출고내역 전송이 실패하였습니다.재전송하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestSendProductionOut();
                                mPopup.hideDialog();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                try{
                    mPopup.hideDialog();
                }catch (Exception e){

                }
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
                mPopup = new TwoBtnPopup(getActivity(), "제품출고내역 전송이 실패하였습니다.재전송하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            requestSendProductionOut();
                            mPopup.hideDialog();
                        }
                    }
                });
            }
        });
    }
}
