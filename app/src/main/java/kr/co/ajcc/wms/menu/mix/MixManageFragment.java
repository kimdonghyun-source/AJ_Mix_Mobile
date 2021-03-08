package kr.co.ajcc.wms.menu.mix;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import kr.co.ajcc.wms.model.MixDetailList;
import kr.co.ajcc.wms.model.MixBarcodeScan;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.network.ApiClientService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MixManageFragment extends CommonFragment {

    Context mContext = null;
    TextView tv_itm_name, tv_size, tv_qty, tv_scan_cnt, tv_all_cnt, tv_equ_name, tv_gone;
    RecyclerView recycleview;
    MixDetailList mDetail;
    MixDetailList.Items mOredr;
    MixBarcodeScan.Items mBarcodeList;
    int mPosition = -1;
    MixManegeAdapter mAdapter;
    List<MixBarcodeScan.Items>mItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();


    }//Close onCreate


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_mix_manage, container, false);
        Bundle arguments = getArguments();

        mDetail = (MixDetailList) arguments.getSerializable("model");
        mPosition = arguments.getInt("position");
        mOredr = mDetail.getItems().get(mPosition);

        tv_itm_name = v.findViewById(R.id.tv_itm_name);
        tv_size = v.findViewById(R.id.tv_size);
        tv_qty = v.findViewById(R.id.tv_qty);
        tv_scan_cnt = v.findViewById(R.id.tv_scan_cnt);
        tv_all_cnt = v.findViewById(R.id.tv_all_cnt);
        tv_equ_name = v.findViewById(R.id.tv_equ_name);
        recycleview = v.findViewById(R.id.recycleview);
        tv_gone = v.findViewById(R.id.tv_gone);

        recycleview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MixManegeAdapter(getActivity());
        recycleview.setAdapter(mAdapter);

        mItems = new ArrayList<>();

        //v.findViewById(R.id.bt_search).setOnClickListener(onClickListener);

        tv_itm_name.setText(mOredr.getItm_name());
        tv_size.setText(mOredr.getItm_size());
        tv_qty.setText(Utils.setComma(mOredr.getMrcp_qty()));
        tv_scan_cnt.setText(Utils.setComma(mOredr.getScan_cnt()));
        tv_all_cnt.setText(Utils.setComma(mOredr.getAll_cnt()));
        tv_equ_name.setText(mOredr.getEqu_name());

        return v;

    }//Close onCreateView


    @Override
    public void onResume() {

        AidcReader.getInstance().claim(mContext);
        AidcReader.getInstance().setListenerHandler(new Handler() {
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 1){
                    BarcodeReadEvent event = (BarcodeReadEvent)msg.obj;
                    String barcode = event.getBarcodeData();
                   tv_gone.setVisibility(View.GONE);
                    requestMixEqu(mOredr.getMrcp_slip_code(), barcode);
                }
            }
        });

        super.onResume();
    }//Close onResume

    /**
     * 설비코드조회
     * @param barcode 설비코드
     */
    private void requestMixEqu(String slip_no, String barcode) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MixBarcodeScan> call = service.mix_equ("sp_pda_mix_qr_scan", slip_no, barcode);

        call.enqueue(new Callback<MixBarcodeScan>() {
            @Override
            public void onResponse(Call<MixBarcodeScan> call, Response<MixBarcodeScan> response) {
                if(response.isSuccessful()){
                    MixBarcodeScan model = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            mAdapter.clearData();
                            mBarcodeList = model.getItems().get(0);
                            mItems.add(model.getItems().get(0));
                            mAdapter.setData(mItems);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }else{
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code()+" : "+response.message());
                }
            }

            @Override
            public void onFailure(Call<MixBarcodeScan> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

















}//Close Activity
