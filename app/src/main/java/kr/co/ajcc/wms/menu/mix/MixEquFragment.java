package kr.co.ajcc.wms.menu.mix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.ArrayList;
import java.util.List;

import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Define;
import kr.co.ajcc.wms.common.SharedData;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.honeywell.AidcReader;
import kr.co.ajcc.wms.menu.main.BaseActivity;
import kr.co.ajcc.wms.menu.popup.OneBtnPopup;
import kr.co.ajcc.wms.menu.popup.TwoBtnPopup;
import kr.co.ajcc.wms.model.MixBarcodeScan;
import kr.co.ajcc.wms.model.MixDetailList;
import kr.co.ajcc.wms.model.MixEquModel;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MixEquFragment extends CommonFragment {
    MixDetailList mDetail;
    MixDetailList.Items mOredr;
    MixBarcodeScan mBarcodeDetail;
    MixBarcodeScan.Items mBarcodeOrder;
    int mPosition = -1;
    int mPosition1 = -1;
    Context mContext;
    TextView tv_itm_name, tv_size, tv_qty, tv_scan_cnt, tv_all_cnt, tv_equ_name, tv_gone;
    RecyclerView recycleview;
    MixEquAdapter mAdapter;
    List<MixEquModel.Items> mItems;
    MixEquModel.Items mMixEquList;
    MixEquModel mMixEqumodel;
    TwoBtnPopup mPopup = null;
    OneBtnPopup mOneBtnPopup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

    }//Close onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_mix_equ, container, false);

        Bundle arguments = getArguments();
        mDetail = (MixDetailList) arguments.getSerializable("model");
        mPosition = arguments.getInt("position");
        mOredr = mDetail.getItems().get(mPosition);

        mBarcodeDetail = (MixBarcodeScan) arguments.getSerializable("model1");
        mPosition1 = arguments.getInt("position1");
        mBarcodeOrder = mBarcodeDetail.getItems().get(mPosition1);

        Utils.Log("mOrder 값 :" + mOredr.getItm_name());
        Utils.Log("mBarcodeOrder 값 :" + mBarcodeOrder.getItm_name());

        tv_itm_name = v.findViewById(R.id.tv_itm_name);
        tv_size = v.findViewById(R.id.tv_size);
        tv_qty = v.findViewById(R.id.tv_qty);
        tv_scan_cnt = v.findViewById(R.id.tv_scan_cnt);
        tv_all_cnt = v.findViewById(R.id.tv_all_cnt);
        tv_equ_name = v.findViewById(R.id.tv_equ_name);
        tv_gone = v.findViewById(R.id.tv_gone);
        recycleview = v.findViewById(R.id.recycleview);

        tv_itm_name.setText(mOredr.getItm_name());
        tv_size.setText(mOredr.getItm_size());
        tv_qty.setText(Utils.setComma(mOredr.getMrcp_qty()));
        tv_scan_cnt.setText(Utils.setComma(mOredr.getScan_cnt()));
        tv_all_cnt.setText(Utils.setComma(mOredr.getAll_cnt()));
        tv_equ_name.setText(mOredr.getEqu_name());

        recycleview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MixEquAdapter(getActivity());
        recycleview.setAdapter(mAdapter);

        mItems = new ArrayList<>();

        return v;

    }//Close onCreateView

    @Override
    public void onResume() {

        AidcReader.getInstance().claim(mContext);
        AidcReader.getInstance().setListenerHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    BarcodeReadEvent event = (BarcodeReadEvent) msg.obj;
                    String barcode = event.getBarcodeData();
                    tv_gone.setVisibility(View.GONE);
                    requestEquSeacrch(barcode);
                    //requestMixEqu("100-20210225-5-1", "20210304000001");
                }
            }
        });

        super.onResume();
    }//Close onResume


    /**
     * 바코드조회
     *
     * @param barcode 바코드번호
     */
    private void requestEquSeacrch(String barcode) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<MixEquModel> call = service.mix_equ_list("sp_pda_mix_equ", barcode);

        call.enqueue(new Callback<MixEquModel>() {
            @Override
            public void onResponse(Call<MixEquModel> call, Response<MixEquModel> response) {
                if (response.isSuccessful()) {
                    MixEquModel model = response.body();
                    mMixEqumodel = response.body();
                    //Utils.Log("model ==> : "+new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {

                            MixEquModel.Items o = mMixEqumodel.getItems().get(0);

                            if (mOredr.getEqu_code().equals(o.getEqu_code())) {
                                requestMixSave();
                            } else {
                                Utils.Toast(mContext, "설비코드가 다릅니다.");
                                return;
                            }

                            mAdapter.clearData();
                            mItems.add(model.getItems().get(0));
                            mAdapter.setData(mItems);
                            mAdapter.notifyDataSetChanged();

                        } else {
                            Utils.Toast(mContext, model.getMSG());
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MixEquModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, mContext.getString(R.string.error_network));
            }
        });
    }

    private void requestMixSave() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        JsonObject json = new JsonObject();
        //로그인ID
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        String emp_code = (String) SharedData.getSharedData(mContext, SharedData.UserValue.EMP_CODE.name(), "");


        json.addProperty("p_corp_code", mOredr.getCorp_code());
        json.addProperty("p_mrcp_date", mOredr.getMrcp_date());
        json.addProperty("p_mrcp_no1", String.valueOf(mOredr.getMrcp_no1()));
        json.addProperty("p_mrcp_no2", String.valueOf(mOredr.getMrcp_no2()));
        json.addProperty("p_equ_code", mOredr.getEqu_code());
        json.addProperty("p_itm_code", mOredr.getItm_code());
        json.addProperty("p_mrcp_lotno", mOredr.getMrcp_lotno());
        json.addProperty("p_job_qty", mOredr.getMrcp_qty());
        json.addProperty("p_user_id", userID);

        JsonArray list = new JsonArray();


            JsonObject obj = new JsonObject();

            obj.addProperty("bitm_code", mBarcodeOrder.getItm_code());
            obj.addProperty("mwe_serial", mBarcodeOrder.getMwe_serial());
            obj.addProperty("bom_qty", mBarcodeOrder.getBom_qty());
            obj.addProperty("mwe_qty", mBarcodeOrder.getMwe_qty());

            list.add(obj);

        json.add("detail", list);

        Utils.Log("new Gson().toJson(json) ==> : " + new Gson().toJson(json));

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

        Call<ResultModel> call = service.Mix_postsend(body);

        call.enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                try {
                    mPopup.hideDialog();
                    mOneBtnPopup.hideDialog();
                } catch (Exception e) {

                }
                if (response.isSuccessful()) {
                    ResultModel model = response.body();
                    Utils.Log("model ==> : " + new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), "배합완료되었습니다.", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        getActivity().finish();
                                        Intent intent = new Intent(mContext, BaseActivity.class);
                                        intent.putExtra("menu", Define.MENU_MIX);
                                        Bundle args=new Bundle();
                                        args.putString("serial", mOredr.getMrcp_slip_code());
                                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("args",args);
                                        startActivity(intent);

                                        mContext.startActivity(intent);
                                        mOneBtnPopup.hideDialog();
                                    }
                                }
                            });
                        } else {
                            Utils.Toast(mContext, model.getMSG());
                            mPopup = new TwoBtnPopup(getActivity(), "전송이 실패하였습니다.재전송하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        requestMixSave();
                                        mPopup.hideDialog();
                                    }
                                }
                            });
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                    mPopup = new TwoBtnPopup(getActivity(), "전송이 실패하였습니다.재전송하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestMixSave();
                                mPopup.hideDialog();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                try {
                    mPopup.hideDialog();
                } catch (Exception e) {

                }
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
                mPopup = new TwoBtnPopup(getActivity(), "제품출고내역 전송이 실패하였습니다.재전송하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            requestMixSave();
                            mPopup.hideDialog();
                        }
                    }
                });
            }
        });
    }


}//Close Activity
