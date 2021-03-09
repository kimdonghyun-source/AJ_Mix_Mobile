package kr.co.ajcc.wms.menu.mix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.honeywell.aidc.BarcodeReadEvent;

import java.util.ArrayList;
import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Define;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.honeywell.AidcReader;
import kr.co.ajcc.wms.menu.main.BaseActivity;
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
    MixBarcodeScan mBarcodeDetail;
    int mPosition = -1;
    MixManegeAdapter mAdapter;
    List<MixBarcodeScan.Items>mItems;
    ImageButton btn_next;

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
        btn_next = v.findViewById(R.id.btn_next);

        recycleview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MixManegeAdapter(getActivity());
        recycleview.setAdapter(mAdapter);

        mItems = new ArrayList<>();

        v.findViewById(R.id.btn_next).setOnClickListener(onClickListener);

        tv_itm_name.setText(mOredr.getItm_name());
        tv_size.setText(mOredr.getItm_size());
        tv_qty.setText(Utils.setComma(mOredr.getMrcp_qty()));
        tv_scan_cnt.setText(Utils.setComma(mOredr.getScan_cnt()));
        tv_all_cnt.setText(Utils.setComma(mOredr.getAll_cnt()));
        tv_equ_name.setText(mOredr.getEqu_name());



        return v;

    }//Close onCreateView


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int view = v.getId();
            switch (view){
                case R.id.btn_next:

                    if (mAdapter.getData() == null){
                        Utils.Toast(mContext,"투입 품목의 바코드 SN을 스캔해주세요.");
                        return;
                    }


                    List<MixBarcodeScan.Items> itms = mAdapter.getData();
                    List<MixBarcodeScan.Items> datas = new ArrayList<>();

                    int count = 0;

                    for(int i = 0; i < itms.size(); i++){
                        MixBarcodeScan.Items itm = itms.get(i);


                    }

                    Intent intent = new Intent(getActivity(), BaseActivity.class);
                    intent.putExtra("menu", Define.MENU_MIX_EQU);

                    Bundle extras = new Bundle();
                    extras.putSerializable("model", mDetail);
                    intent.putExtra("args", extras);


                    extras.putSerializable("model1", mBarcodeDetail);
                    intent.putExtra("args1", extras);

                    startActivityForResult(intent, 100);
                    getActivity().finish();

                    break;
            }
        }
    };

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
                    requestBarcodeSearch(mOredr.getMrcp_slip_code(), barcode);
                    //requestMixEqu("100-20210225-5-1", "20210304000001");
                }
            }
        });

        super.onResume();
    }//Close onResume



    /**
     * 바코드조회
     * @param slip_no 제조지시번호
     * @param barcode 바코드번호
     */
    private void requestBarcodeSearch(String slip_no, String barcode) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MixBarcodeScan> call = service.barcode_list("sp_pda_mix_qr_scan",  slip_no, barcode);

        call.enqueue(new Callback<MixBarcodeScan>() {
            @Override
            public void onResponse(Call<MixBarcodeScan> call, Response<MixBarcodeScan> response) {
                if(response.isSuccessful()){
                    MixBarcodeScan model = response.body();
                    mBarcodeDetail = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            mAdapter.clearData();
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
            public void onFailure(Call<MixBarcodeScan> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, mContext.getString(R.string.error_network));
            }
        });
    }














}//Close Activity
