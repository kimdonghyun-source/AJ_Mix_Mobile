package kr.co.ajcc.wms.menu.pallet;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.Date;

import kr.co.ajcc.wms.BuildConfig;
import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Define;
import kr.co.ajcc.wms.common.SharedData;
import kr.co.ajcc.wms.common.UtilDate;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.honeywell.AidcReader;
import kr.co.ajcc.wms.menu.main.BaseActivity;
import kr.co.ajcc.wms.menu.popup.OneBtnPopup;
import kr.co.ajcc.wms.menu.popup.TwoBtnPopup;
import kr.co.ajcc.wms.model.DeliveryOrderModel;
import kr.co.ajcc.wms.model.PalletSnanModel;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.model.SerialNumberModel;
import kr.co.ajcc.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PalletFragment extends CommonFragment {
    Context mContext;
    ImageButton ib_bunhal;
    ImageButton ib_merge;
    ImageView iv_gd;
    TwoBtnPopup mPopup = null;
    OneBtnPopup mOneBtnPopup;

    LinearLayout ll_pallet_bunhal = null;
    LinearLayout ll_pallet_merge = null;
    TextView tv_bunhal_product = null;
    TextView tv_bunhal_count = null;
    EditText et_bunhal  =   null;
    EditText et_bunhal_count  =   null;

    ImageButton btn_next = null;

    EditText et_merge_1  =   null;
    EditText et_merge_2  =   null;

    EditText et_merge_count_1  =   null;
    EditText et_merge_count_2  =   null;

    TextView tv_merge_product_1 = null;
    TextView tv_merge_count_1   = null;
    TextView tv_merge_product_2 = null;
    TextView tv_merge_count_2   = null;

    PalletSnanModel.Items bunhalItem = null;
    PalletSnanModel.Items mergeItem1 = null;
    PalletSnanModel.Items mergeItem2 = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_pallet, container, false);

        ib_bunhal       = v.findViewById(R.id.ib_bunhal);
        ib_merge        = v.findViewById(R.id.ib_merge);
        iv_gd           = v.findViewById(R.id.iv_gd);
        ll_pallet_bunhal= v.findViewById(R.id.ll_pallet_bunhal);
        ll_pallet_merge = v.findViewById(R.id.ll_pallet_merge);
        tv_bunhal_product= v.findViewById(R.id.tv_bunhal_product);
        tv_bunhal_count = v.findViewById(R.id.tv_bunhal_count);
        et_bunhal= v.findViewById(R.id.et_bunhal);
        et_bunhal_count= v.findViewById(R.id.et_bunhal_count);
        btn_next        = v.findViewById(R.id.btn_next);

        et_merge_1  =   v.findViewById(R.id.et_merge_1);
        et_merge_2  =   v.findViewById(R.id.et_merge_2);

        et_merge_count_1  =   v.findViewById(R.id.et_merge_count_1);
        et_merge_count_2  =   v.findViewById(R.id.et_merge_count_2);

        tv_merge_product_1  =   v.findViewById(R.id.tv_merge_product_1);
        tv_merge_count_1  =   v.findViewById(R.id.tv_merge_count_1);
        tv_merge_product_2  =   v.findViewById(R.id.tv_merge_product_2);
        tv_merge_count_2  =   v.findViewById(R.id.tv_merge_count_2);


        ib_bunhal.setOnClickListener(onClickListener);
        ib_merge.setOnClickListener(onClickListener);
        btn_next.setOnClickListener(onClickListener);

        v.findViewById(R.id.bt_scan_test).setOnClickListener(onClickListener);

        v.findViewById(R.id.ib_delete_1).setOnClickListener(onClickListener);
        v.findViewById(R.id.ib_delete_2).setOnClickListener(onClickListener);

        String printer = (String) SharedData.getSharedData(mContext, "printer_info","");
        String arr[] = printer.split(" ");
        if(arr!=null && arr.length>=2) {

        } else {
            mPopup = new TwoBtnPopup(getActivity(), "프린터가 설정되지 않았습니다. 확인을 누르시면 설정화면으로 이동합니다.", R.drawable.popup_title_alert, new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        goConfig();
                    } else {
                        getActivity().finish();
                    }
                }
            });
        }
        setTab(1);
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
                    requestBunhalSnScan(barcode);
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

    private void setTab(int idx){
        if(idx == 1){
            ib_bunhal.setSelected(true);
            ib_merge.setSelected(false);
            iv_gd.setBackgroundResource(R.drawable.pallet_separate_guidetext1);
            ll_pallet_bunhal.setVisibility(View.VISIBLE);
            ll_pallet_merge.setVisibility(View.GONE);
            btn_next.setImageResource(R.drawable.pallet_bt_separate);
        } else if(idx == 2){
            ib_bunhal.setSelected(false);
            ib_merge.setSelected(true);
            iv_gd.setBackgroundResource(R.drawable.pallet_merge_guidetext);
            ll_pallet_merge.setVisibility(View.VISIBLE);
            ll_pallet_bunhal.setVisibility(View.GONE);
            btn_next.setImageResource(R.drawable.pallet_bottom_bt_merge);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.ib_bunhal: {
                    setTab(1);
                    break;
                }
                case R.id.ib_merge: {
                    setTab(2);
                    break;
                }
                case R.id.bt_scan_test: {
                    //1,2,3,6,7
                    requestBunhalSnScan("20200325-000001");
                    break;
                }
                case R.id.btn_next: {
                    makeBarcode();
                    //goBarcode("20200325-000001");
                    break;
                }
                case R.id.ib_delete_1: {
                    mergeItem1 = null;
                    tv_merge_product_1.setText("");
                    tv_merge_product_1.setSelected(false);
                    tv_merge_count_1.setText("");
                    et_merge_count_1.setText("");
                    et_merge_1.setText("");
                    break;
                }
                case R.id.ib_delete_2: {
                    mergeItem2 = null;
                    tv_merge_product_2.setText("");
                    tv_merge_product_2.setSelected(false);
                    tv_merge_count_2.setText("");
                    et_merge_count_2.setText("");
                    et_merge_2.setText("");
                    break;
                }
            }
        }
    };

    private void makeBarcode(){
        if(ib_bunhal.isSelected()){
            if(bunhalItem!=null) {
                String bunhalCount = et_bunhal_count.getText().toString();
                int oriQty = bunhalItem.getWrk_inv_qty(); //재고수량(분할)

                if(bunhalCount==null || Float.parseFloat(bunhalCount) <= 0){
                    Toast.makeText(mContext,"분할수량을 입력하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Float.parseFloat(bunhalCount) > oriQty){
                    Toast.makeText(mContext,"재고수량보다 값이 큽니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                mPopup = new TwoBtnPopup(getActivity(), et_bunhal.getText().toString()+" PALLET를 분할 처리 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            requestBunhalMake();
                            mPopup.hideDialog();
                        }
                    }
                });
            } else {
                Toast.makeText(mContext,"PALLET SN을 스캔하세요.",Toast.LENGTH_SHORT).show();
            }
        } else if(ib_merge.isSelected()){
            if(mergeItem1!=null && mergeItem2!=null) {
                String mergeCount1 = et_merge_count_1.getText().toString();
                String mergeCount2 = et_merge_count_2.getText().toString();
                int oriQty1 = mergeItem1.getWrk_inv_qty(); //재고수량1(병합)
                int oriQty2 = mergeItem2.getWrk_inv_qty(); //재고수량2(병합)

                if(mergeCount1==null || mergeCount2==null || Float.parseFloat(mergeCount1)<=0 || Float.parseFloat(mergeCount2)<=0){
                    Toast.makeText(mContext,"병합수량을 입력하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Float.parseFloat(mergeCount1) > oriQty1 || Float.parseFloat(mergeCount2) > oriQty2){
                    Toast.makeText(mContext,"재고수량보다 값이 큽니다.",Toast.LENGTH_SHORT).show();
                    return;
                }

                String str1 = et_merge_1.getText().toString();
                String str2 = et_merge_2.getText().toString();
                mPopup = new TwoBtnPopup(getActivity(), str1+"과 "+str2 +" PALLET를 병합 처리하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            requestMergeMake();
                            mPopup.hideDialog();
                        }
                    }
                });
            } else {
                Toast.makeText(mContext,"PALLET SN을 스캔하세요.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goConfig(){
        Intent intent = new Intent(mContext, BaseActivity.class);
        intent.putExtra("menu", Define.MENU_CONFIG);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
    }

    private void goBarcode(String sn){
        Utils.Log("goBarcode");
        Intent intent = new Intent(mContext, BaseActivity.class);
        intent.putExtra("menu", Define.MENU_PALLET_PRINTER);
        Bundle args=new Bundle();
        args.putString("SN",sn);
        if(ib_bunhal.isSelected()) {
            int totalCnt = Integer.parseInt(Utils.nullString(tv_bunhal_count.getText().toString(),"0"));
            int buhalCnt = Integer.parseInt(Utils.nullString(et_bunhal_count.getText().toString(),"0"));
            if(totalCnt-buhalCnt <= 0){
                buhalCnt = 0;
            } else {
                buhalCnt = totalCnt-buhalCnt;
            }
            args.putString("B_SN", et_bunhal.getText().toString());
            args.putString("ITEMNM", bunhalItem.getItm_name());
            args.putString("ITEMCD", bunhalItem.getItm_code());
            args.putString("CNT", et_bunhal_count.getText().toString());
            args.putString("QTY", Integer.toString(buhalCnt));
        } else if(ib_merge.isSelected()){
            int totalCnt = Integer.parseInt(Utils.nullString(tv_merge_count_1.getText().toString(),"0"));
            int mergeCnt1 = Integer.parseInt(Utils.nullString(et_merge_count_1.getText().toString(),"0"));
            if(totalCnt-mergeCnt1 <= 0){
                mergeCnt1 = 0;
            } else {
                mergeCnt1 = totalCnt-mergeCnt1;
            }

            totalCnt = Integer.parseInt(Utils.nullString(tv_merge_count_2.getText().toString(),"0"));
            int mergeCnt2 = Integer.parseInt(Utils.nullString(et_merge_count_2.getText().toString(),"0"));
            if(totalCnt-mergeCnt2 <= 0){
                mergeCnt2 = 0;
            } else {
                mergeCnt2 = totalCnt-mergeCnt2;
            }

            int sum = Integer.parseInt(et_merge_count_1.getText().toString()) + Integer.parseInt(et_merge_count_2.getText().toString());
            args.putString("B_SN", et_merge_1.getText().toString());
            args.putString("B_SN_2", et_merge_2.getText().toString());

            args.putString("ITEMNM", mergeItem1.getItm_name());
            args.putString("ITEMCD", mergeItem1.getItm_code());
            args.putString("CNT", Integer.toString(sum));

            args.putString("QTY", Integer.toString(mergeCnt1));
            args.putString("QTY_2", Integer.toString(mergeCnt2));
        }
        intent.putExtra("args",args);
        startActivity(intent);
    }

    private void initMerge(){
        mergeItem1 = null;
        tv_merge_product_1.setText("");
        tv_merge_product_1.setSelected(false);
        tv_merge_count_1.setText("");
        et_merge_count_1.setText("");
        et_merge_1.setText("");
        mergeItem2 = null;
        tv_merge_product_2.setText("");
        tv_merge_product_2.setSelected(false);
        tv_merge_count_2.setText("");
        et_merge_count_2.setText("");
        et_merge_2.setText("");
    }

    private void initBunhal(){
        bunhalItem = null;
        tv_bunhal_product.setText("");
        tv_bunhal_product.setSelected(false);
        tv_bunhal_count.setText("");
        et_bunhal_count.setText("");
        et_bunhal.setText("");
    }
    /**
     * 시리얼정보 상세
     */
    private void requestBunhalSnScan(final String param) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<PalletSnanModel> call = service.postScanPallet("sp_pda_plt_serial_scan", param);

        call.enqueue(new Callback<PalletSnanModel>() {
            @Override
            public void onResponse(Call<PalletSnanModel> call, Response<PalletSnanModel> response) {
                if(response.isSuccessful()){

                    PalletSnanModel model = response.body();
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            if(model.getItems()!=null && model.getItems().size() > 0){
                                if(ib_bunhal.isSelected()) {
                                    bunhalItem = model.getItems().get(0);
                                    tv_bunhal_product.setText(bunhalItem.getItm_name());
                                    tv_bunhal_product.setSelected(true);
                                    tv_bunhal_count.setText(Integer.toString(bunhalItem.getWrk_inv_qty()));
                                    et_bunhal_count.setText("0");
                                    et_bunhal.setText(param);
                                } else if(ib_merge.isSelected()){
                                    //시리얼 중복 체크
                                    if(mergeItem1 != null){
                                        if(mergeItem1.getSerial_no().equals(model.getItems().get(0).getSerial_no())){
                                            Utils.Toast(mContext, getString(R.string.error_productOut_check));
                                            return;
                                        }
                                    }
                                    if(mergeItem2 != null){
                                        if(mergeItem2.getSerial_no().equals(model.getItems().get(0).getSerial_no())){
                                            Utils.Toast(mContext, getString(R.string.error_productOut_check));
                                            return;
                                        }
                                    }

                                    String merge = et_merge_1.getText().toString();
                                    if (Utils.isEmpty(merge)) {
                                        mergeItem1 = model.getItems().get(0);
                                        tv_merge_product_1.setText(mergeItem1.getItm_name());
                                        tv_merge_product_1.setSelected(true);
                                        tv_merge_count_1.setText(Integer.toString(mergeItem1.getWrk_inv_qty()));
                                        et_merge_count_1.setText("0");
                                        et_merge_1.setText(param);
                                        mergeItem2 = null;
                                    } else {
                                        mergeItem2 = model.getItems().get(0);
                                        tv_merge_product_2.setText(mergeItem2.getItm_name());
                                        tv_merge_product_2.setSelected(true);
                                        tv_merge_count_2.setText(Integer.toString(mergeItem2.getWrk_inv_qty()));
                                        et_merge_count_2.setText("0");
                                        et_merge_2.setText(param);
                                    }
                                }
                            } else {
                                Utils.Toast(mContext, "품목이 없습니다.");
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

    /**
     * 전표생성
     */
    private void requestBunhalMake() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        //로그인ID
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        /*
        시리얼번호
        품목코드
        창고코드
        로케이션코드
        원수량(재고수량)
        분할수량
        로그인ID
        */
        String bunhalCount = et_bunhal_count.getText().toString();

        String param1 = bunhalItem.getSerial_no();
        String param2 = bunhalItem.getItm_code();
        String param3 = bunhalItem.getWh_code();
        String param4 = bunhalItem.getLocation_code();
        String param5 = Float.toString(bunhalItem.getWrk_inv_qty());
        String param6 = bunhalCount;
        String param7 = userID;

        Call<SerialNumberModel> call = service.postMakeBunhalJunphyo("sp_pda_plt_div_make", param1,param2,param3,param4,param5,param6,param7);
        call.enqueue(new Callback<SerialNumberModel>() {
            @Override
            public void onResponse(Call<SerialNumberModel> call, Response<SerialNumberModel> response) {
                if(response.isSuccessful()){

                    final SerialNumberModel model = response.body();
                    Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            String msg = String.format("%s PALLET를 분할 처리되었습니다. 확인을 누르시면 바코드 출력 화면으로 이동합니다.",bunhalItem.getSerial_no());
                            mOneBtnPopup = new OneBtnPopup(getActivity(), msg, R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        SerialNumberModel.Items item = model.getItems().get(0);
                                        goBarcode(item.getNewSerialNo());
                                        initBunhal();
                                        mOneBtnPopup.hideDialog();
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
            public void onFailure(Call<SerialNumberModel> call, Throwable t) {
                Utils.Log(t.getMessage());
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }

    /**
     * 병합 전표생성
     */
    private void requestMergeMake() {
        String mergeCount1 = et_merge_count_1.getText().toString();
        String mergeCount2 = et_merge_count_2.getText().toString();

        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        JsonObject json = new JsonObject();
        //로그인ID
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        json.addProperty("p_user_id", userID);

        JsonArray list = new JsonArray();
        /*
        시리얼번호
        품목코드
        창고코드
        로케이션코드
        원수량(재고수량)
        병합수량
        */
        JsonObject merge1 = new JsonObject();
        //시리얼번호
        merge1.addProperty("serial_no", mergeItem1.getSerial_no());
        //품목코드
        merge1.addProperty("itm_code", mergeItem1.getItm_code());
        //창고코드
        merge1.addProperty("wh_code", mergeItem1.getWh_code());
        //로케이션코드
        merge1.addProperty("location_code",mergeItem1.getLocation_code());
        //원수량(재고수량)
        merge1.addProperty("ori_qty",Float.toString(mergeItem1.getWrk_inv_qty()));
        //병합수량
        merge1.addProperty("mrg_qty", mergeCount1);
        list.add(merge1);

        JsonObject merge2 = new JsonObject();
        //시리얼번호
        merge2.addProperty("serial_no", mergeItem2.getSerial_no());
        //품목코드
        merge2.addProperty("itm_code", mergeItem2.getItm_code());
        //창고코드
        merge2.addProperty("wh_code", mergeItem2.getWh_code());
        //로케이션코드
        merge2.addProperty("location_code", mergeItem2.getLocation_code());
        //원수량(재고수량)
        merge2.addProperty("ori_qty",Float.toString(mergeItem2.getWrk_inv_qty()));
        //병합수량
        merge2.addProperty("mrg_qty", mergeCount2);
        list.add(merge2);

        json.add("detail", list);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<SerialNumberModel> call = service.postMakeMergeJunphyo(body);
        call.enqueue(new Callback<SerialNumberModel>() {
            @Override
            public void onResponse(Call<SerialNumberModel> call, Response<SerialNumberModel> response) {
                if(response.isSuccessful()){

                    final SerialNumberModel model = response.body();
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            String str1 = et_merge_1.getText().toString();
                            String str2 = et_merge_2.getText().toString();

                            String msg = String.format("%s과 %s PALLET를 병합 처리 하였습니다. 확인을 누르시면 바코드 출력화면으로 이동합니다.",str1,str2);
                            mOneBtnPopup = new OneBtnPopup(getActivity(), msg, R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        goBarcode(model.getNewSerialNo());
                                        initMerge();
                                        mOneBtnPopup.hideDialog();
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
            public void onFailure(Call<SerialNumberModel> call, Throwable t) {
                Utils.Log(t.getMessage());
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }
}