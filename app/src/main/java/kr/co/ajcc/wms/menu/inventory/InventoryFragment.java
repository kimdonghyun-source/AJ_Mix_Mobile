package kr.co.ajcc.wms.menu.inventory;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.Date;
import java.util.List;

import kr.co.ajcc.wms.R;

import kr.co.ajcc.wms.common.SharedData;
import kr.co.ajcc.wms.common.UtilDate;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.honeywell.AidcReader;

import kr.co.ajcc.wms.menu.popup.OneBtnPopup;
import kr.co.ajcc.wms.menu.popup.TwoBtnPopup;
import kr.co.ajcc.wms.model.InventoryModel;
import kr.co.ajcc.wms.model.PalletSnanModel;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.model.SerialNumberModel;
import kr.co.ajcc.wms.network.ApiClientService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryFragment extends CommonFragment {
    Context mContext;
    EditText et_location, et_qty;
    TextView itm_code, itm_name, itm_size, itm_unit, pallet_qty, wh_name, location_name, inv_qty;
    InventoryModel minventoryModel;
    List<InventoryModel.Items> mItems;
    OneBtnPopup mOneBtnPopup;
    TwoBtnPopup mTwoBtnPopup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_inventory, container, false);
        v.findViewById(R.id.btn_next).setOnClickListener(onClickListener);
        v.findViewById(R.id.bt_search).setOnClickListener(onClickListener);
        et_location = v.findViewById(R.id.et_location);
        et_qty = v.findViewById(R.id.et_qty);
        itm_code = v.findViewById(R.id.itm_code);
        itm_name = v.findViewById(R.id.itm_name);
        itm_size = v.findViewById(R.id.itm_size);
        itm_unit = v.findViewById(R.id.itm_unit);
        pallet_qty = v.findViewById(R.id.pallet_qty);
        wh_name = v.findViewById(R.id.wh_name);
        location_name = v.findViewById(R.id.location_name);
        inv_qty = v.findViewById(R.id.inv_qty);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        AidcReader.getInstance().claim(mContext);
        AidcReader.getInstance().setListenerHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    BarcodeReadEvent event = (BarcodeReadEvent) msg.obj;
                    String barcode = event.getBarcodeData();
                    requestInventoryDetail(barcode);
                    et_location.setText("");
                    et_qty.setText("");
                    itm_code.setText("");
                    itm_name.setText("");
                    itm_size.setText("");
                    itm_unit.setText("");
                    pallet_qty.setText("");
                    wh_name.setText("");
                    location_name.setText("");
                    inv_qty.setText("");
                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        AidcReader.getInstance().release();
        AidcReader.getInstance().setListenerHandler(null);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            int view = v.getId();

            switch (view) {
                case R.id.btn_next:
                    String order_no = et_location.getText().toString();
                    String qty = et_qty.getText().toString();
                    if (Utils.isEmpty(order_no) || minventoryModel == null) {
                        Toast.makeText(mContext, "PALLET SN을 스캔및 검색 선택하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (Utils.isEmpty(qty)){
                        Toast.makeText(mContext, "재고수량을 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                        else {
                        mTwoBtnPopup = new TwoBtnPopup(getActivity(), et_location.getText().toString()+" 로케이션에 완제품 적치등록을 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg.what == 1) {
                                    requestSendInventory();
                                    mTwoBtnPopup.hideDialog();
                                }
                            }
                        });


                    }
                        break;

                case R.id.bt_search:
                    String pallet_no = et_location.getText().toString();
                    if (Utils.isEmpty(pallet_no) || minventoryModel == null) {
                        Toast.makeText(mContext, "PALLET SN을 스캔및 검색 선택하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        mTwoBtnPopup = new TwoBtnPopup(getActivity(), et_location.getText().toString()+" 로케이션에 완제품 적치등록을 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg.what == 1) {
                                    requestInventoryDetail(et_location.getText().toString());
                                    mTwoBtnPopup.hideDialog();
                                }
                            }
                        });

                    }
                    break;
            }
        }
    };

        /**
         * 재고실사 상세
         */
        private void requestInventoryDetail(final String param) {
            ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

            Call<InventoryModel> call = service.postInventoryScan("sp_pda_mod_itm_scan", param);

            call.enqueue(new Callback<InventoryModel>() {
                @Override
                public void onResponse(Call<InventoryModel> call, Response<InventoryModel> response) {
                    if (response.isSuccessful()) {
                        minventoryModel = response.body();
                        final InventoryModel model = response.body();
                        Utils.Log("model ==> : " + new Gson().toJson(model));
                        if (minventoryModel != null) {
                            if (minventoryModel.getFlag() == ResultModel.SUCCESS) {
                                et_location.setText(param);
                                if (minventoryModel.getItems().size() > 0) {
                                    InventoryModel.Items o = minventoryModel.getItems().get(0);
                                    itm_code.setText(o.getItm_code());
                                    itm_name.setText(o.getItm_name());
                                    itm_unit.setText(o.getItm_unit());
                                    itm_size.setText(o.getItm_size());
                                    pallet_qty.setText(String.valueOf(o.getItm_pallet_qty()));
                                    wh_name.setText(o.getWh_name());
                                    location_name.setText(o.getLocation_name());
                                    inv_qty.setText(String.valueOf(o.getInv_qty()));
                                }
                                et_qty.requestFocus();
                                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                            } else {
                                Utils.Toast(mContext, minventoryModel.getMSG());
                            }
                        }
                    } else {
                        Utils.LogLine(response.message());
                        Utils.Toast(mContext, response.code() + " : " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<InventoryModel> call, Throwable t) {
                    Utils.Log(t.getMessage());
                    Utils.LogLine(t.getMessage());
                    Utils.Toast(mContext, getString(R.string.error_network));
                }
            });
        }

        //재고실사 전표생성
        private void requestSendInventory() {
            ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

            JsonObject json = new JsonObject();
            //로그인ID
            String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
            json.addProperty("p_user_id", userID);

            JsonArray list = new JsonArray();
            InventoryModel.Items o = minventoryModel.getItems().get(0);
                JsonObject obj = new JsonObject();
                //시리얼번호
                obj.addProperty("scan_psn", o.getSerial_no());
                //재고실사일자
                obj.addProperty("mod_date", UtilDate.getDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd"));
                //품목코드
                obj.addProperty("itm_code", o.getItm_code());
                //창고코드
                obj.addProperty("wh_code", o.getWh_code());
                //로케이션코드
                obj.addProperty("location_code", o.getLocation_code());
                //재고수량
                obj.addProperty("inv_qty", o.getInv_qty());
                //재고실사수량
                obj.addProperty("mod_qty", String.valueOf(et_qty.getText().toString()));
                list.add(obj);

            json.add("detail", list);

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(json));

            Call<ResultModel> call = service.postSendInventory(body);

            call.enqueue(new Callback<ResultModel>() {
                @Override
                public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                    if (response.isSuccessful()) {
                        ResultModel model = response.body();
                        //Utils.Log("model ==> : "+new Gson().toJson(model));
                        if (model != null) {
                            if (model.getFlag() == ResultModel.SUCCESS) {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), et_location.getText().toString()+"재고실사 전송이 완료되었습니다.", R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        //getActivity().finish();
                                        mOneBtnPopup.hideDialog();
                                        et_location.setText("");
                                        et_qty.setText("");
                                        itm_code.setText("");
                                        itm_name.setText("");
                                        itm_size.setText("");
                                        itm_unit.setText("");
                                        pallet_qty.setText("");
                                        wh_name.setText("");
                                        location_name.setText("");
                                        inv_qty.setText("");
                                        et_qty.setText("");
                                    }
                                }
                            });
                                //getActivity().finish();
                            } else {
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
                    } else {
                        Utils.LogLine(response.message());
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "재고실사 전송이 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestSendInventory();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                    }
                }

                @Override
                public void onFailure(Call<ResultModel> call, Throwable t) {
                    Utils.LogLine(t.getMessage());
                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "재고실사 전송이 실패하였습니다.\n 재전송 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requestSendInventory();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });
                }
            });
        }

}
