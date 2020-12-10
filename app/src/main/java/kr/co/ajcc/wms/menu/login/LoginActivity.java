package kr.co.ajcc.wms.menu.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import kr.co.ajcc.wms.GlobalApplication;
import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.common.SharedData;
import kr.co.ajcc.wms.custom.CommonCompatActivity;
import kr.co.ajcc.wms.menu.main.MainActivity;
import kr.co.ajcc.wms.model.ResultModel;
import kr.co.ajcc.wms.model.UserInfoModel;
import kr.co.ajcc.wms.network.ApiClientService;
import kr.co.jesoft.jelib.tsc.printer.TSCPrinter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends CommonCompatActivity {
    Context mContext;

    EditText et_user_id;
    EditText et_pass;
    ImageButton bt_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_login);

        mContext = this;

        findViewById(R.id.bt_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isEmpty(et_user_id.getText().toString())) {
                    Utils.Toast(mContext, "아이디를 입력해주세요.");
                    et_user_id.requestFocus();
                    return;
                }
                if(Utils.isEmpty(et_pass.getText().toString())) {
                    Utils.Toast(mContext, "비밀번호를 입력해주세요.");
                    et_pass.requestFocus();
                    return;
                }
                requestLogin();
            }
        });

        findViewById(R.id.bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView tv_version = findViewById(R.id.tv_version);
        tv_version.setText(Utils.appVersionName(mContext));

        et_user_id = findViewById(R.id.et_user_id);
        et_pass = findViewById(R.id.et_pass);
        bt_check = findViewById(R.id.bt_check);
        bt_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt_check.setSelected(!bt_check.isSelected());
            }
        });

        boolean isSave = (boolean) SharedData.getSharedData(mContext, SharedData.UserValue.SAVE_ID.name(), false);
        bt_check.setSelected(isSave);
        if(isSave) {
            String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
            et_user_id.setText(userID);
        }

        //정제영 테스트
        //et_user_id.setText("axlrose");
        //et_pass.setText("1234!");
    }

    @Override
    public void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request == 1000){
            try {
                TSCPrinter.shared(mContext).closeSession();
            } catch (Exception e){

            }
            finish();
        }
    }
    @Override
    public void onDestroy(){
        try {
            TSCPrinter.shared(mContext).closeSession();
        } catch (Exception e){

        }
        super.onDestroy();
    }
    private void requestLogin() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<UserInfoModel> call = service.postLogin("sp_pda_login", et_user_id.getText().toString(), et_pass.getText().toString(), Utils.appVersionName(mContext));

        call.enqueue(new Callback<UserInfoModel>() {
            @Override
            public void onResponse(Call<UserInfoModel> call, Response<UserInfoModel> response) {
                if(response.isSuccessful()){
                    UserInfoModel model = response.body();
                    if (model != null) {
                        if(model.getFlag() == ResultModel.SUCCESS) {
                            SharedData.setSharedData(mContext, SharedData.UserValue.USER_ID.name(), et_user_id.getText().toString());
                            SharedData.setSharedData(mContext, SharedData.UserValue.IS_LOGIN.name(), true);
                            SharedData.setSharedData(mContext, SharedData.UserValue.SAVE_ID.name(), bt_check.isSelected());

                            GlobalApplication application = (GlobalApplication)getApplicationContext();
                            application.setUserInfoModel(model.getItems().get(0));

                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityForResult(intent,1000);
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
            public void onFailure(Call<UserInfoModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }
}
