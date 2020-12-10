package kr.co.ajcc.wms.menu.product_out;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.honeywell.aidc.BarcodeReadEvent;

import java.util.ArrayList;
import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.honeywell.AidcReader;
import kr.co.ajcc.wms.model.DeliveryOrderModel;
import kr.co.ajcc.wms.model.PalletSnanModel;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.network.ApiClientService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductPickingFragment extends CommonFragment {
    Context mContext    = null;
    TextView tv_name    = null;
    TextView tv_order_count = null;
    TextView tv_empty   = null;
    EditText et_count   = null;
    DeliveryOrderModel.DeliveryOrder mOrder =   null;
    DeliveryOrderModel mDeliveryOrderModel  =   null;
    int mPosition = -1;
    RecyclerView recycleview = null;
    ProductPickingAdapter mAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_product_picking, container, false);
        Bundle arguments = getArguments();
        mDeliveryOrderModel= (DeliveryOrderModel)arguments.getSerializable("model");
        mPosition = arguments.getInt("position");
        mOrder = mDeliveryOrderModel.getItems().get(mPosition);

        Utils.Log("arguments::"+arguments);
        tv_name = v.findViewById(R.id.tv_name);
        tv_order_count = v.findViewById(R.id.tv_order_count);
        et_count    = v.findViewById(R.id.et_count);
        tv_empty    = v.findViewById(R.id.tv_empty);
        recycleview = v.findViewById(R.id.recycleview);

        v.findViewById(R.id.bt_test_1).setOnClickListener(onClickListener);
        v.findViewById(R.id.btn_printAdd).setOnClickListener(onClickListener);

        tv_name.setText(mOrder.getItm_name());
        tv_order_count.setText(Utils.setComma(mOrder.getBox_qty())+" BOX");

        tv_name.setSelected(true);

        tv_empty.setVisibility(View.VISIBLE);
        recycleview.setVisibility(View.GONE);

        recycleview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ProductPickingAdapter(getActivity());
        recycleview.setAdapter(mAdapter);
        mAdapter.setSumHandler(new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 1){
                    List<PalletSnanModel.Items> itms = mAdapter.getData();
                    float count = 0;
                    for(int i = 0 ; i < itms.size() ; i++){
                        PalletSnanModel.Items itm = itms.get(i);
                        count += itm.getReq_qty();
                    }
                    et_count.setText(Utils.setComma(count));
                }
            }
        });
        List<PalletSnanModel.Items> items = mOrder.getItems();
        if(items!=null && items.size() > 0){
            tv_empty.setVisibility(View.GONE);
            recycleview.setVisibility(View.VISIBLE);
            for(int i = 0 ; i < items.size() ; i ++){
                mAdapter.addData(items.get(i));
            }
            mAdapter.notifyDataSetChanged();
            et_count.setText(Utils.setComma(mOrder.getReq_qty()));
        }
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
                    List<PalletSnanModel.Items> itms = mAdapter.getData();

                    //시리얼스캔 중복
                    if(itms != null) {
                        for (PalletSnanModel.Items itm : itms) {
                            if (barcode.equals(itm.getSerial_no())) {
                                Utils.Toast(mContext, getString(R.string.error_productOut_check));
                                return;
                            }
                        }
                    }

                    requestLotSn(barcode);
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
        public void onClick(final View v) {
            int view = v.getId();

            switch (view){
                case R.id.bt_test_1:
                    tv_empty.setVisibility(View.GONE);
                    recycleview.setVisibility(View.VISIBLE);
                    requestLotSn("20200325-000001");
                    break;
                case R.id.btn_printAdd:
                    List<PalletSnanModel.Items> datas = new ArrayList<>();
                    List<PalletSnanModel.Items> itms = mAdapter.getData();
                    int count = 0;
                    for(int i = 0 ; i < itms.size() ; i++){
                        PalletSnanModel.Items itm = itms.get(i);
                        if(itm.getReq_qty()>0){
                            datas.add(itm);
                            count+=itm.getReq_qty();
                        }
                    }

                    mDeliveryOrderModel.getItems().get(mPosition).setReq_qty(count);
                    mDeliveryOrderModel.getItems().get(mPosition).setItems(datas);
                    Intent i = new Intent();
                    i.putExtra("model",mDeliveryOrderModel);
                    getActivity().setResult(Activity.RESULT_OK,i);
                    getActivity().finish();
                    break;
            }
        }
    };
    /**
     * 시리얼번호스캔(팔레트 바코드)
     */
    private void requestLotSn(final String param) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<PalletSnanModel> call = service.postScanPallet("sp_pda_ship_serial_scan", param);

        call.enqueue(new Callback<PalletSnanModel>() {
            @Override
            public void onResponse(Call<PalletSnanModel> call, Response<PalletSnanModel> response) {
                if(response.isSuccessful()){
                    PalletSnanModel model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            if(model.getItems().size() > 0) {
                                for(int i = 0 ; i < model.getItems().size();i++) {
                                    PalletSnanModel.Items item= (PalletSnanModel.Items)model.getItems().get(i);
                                    mAdapter.addData(item);
                                }
                                mAdapter.notifyDataSetChanged();

                                //출고번호 선택 후 스캔시 품목내용이 올라오지 않음
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
            public void onFailure(Call<PalletSnanModel> call, Throwable t) {
                Utils.Log(t.getMessage());
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }
}
