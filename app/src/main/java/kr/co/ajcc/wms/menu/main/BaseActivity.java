package kr.co.ajcc.wms.menu.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

import kr.co.ajcc.wms.GlobalApplication;
import kr.co.ajcc.wms.R;
import kr.co.ajcc.wms.common.Define;
import kr.co.ajcc.wms.common.Utils;
import kr.co.ajcc.wms.custom.BusProvider;
import kr.co.ajcc.wms.custom.CommonCompatActivity;
import kr.co.ajcc.wms.custom.CommonFragment;
import kr.co.ajcc.wms.menu.config.ConfigFragment;


import kr.co.ajcc.wms.menu.mix.MixEquFragment;
import kr.co.ajcc.wms.menu.mix.MixFragment;


import kr.co.ajcc.wms.menu.mix.MixManageFragment;
import kr.co.ajcc.wms.menu.popup.TwoBtnPopup;




import kr.co.ajcc.wms.model.UserInfoModel;

public class BaseActivity extends CommonCompatActivity {
    Context mContext;

    //사이드 메뉴 레이아웃
    DrawerLayout drawer;
    View drawer_layout;
    //사이드 메뉴 리스트
    //DrawerMenu mDrawerMenu;
    //좌측 메뉴 리스트 아답터
    ListAdapter mAdapter;
    //다른 메뉴 이동시 묻는 팝업
    TwoBtnPopup mTwoBtnPopup;
    //메뉴 타이틀
    ImageView iv_title;
    //GNB 배경 이미지(피킹은 햄버거버튼 사용 안하기 때문)
    ImageView iv_gnb;
    ImageButton bt_drawer;
    ImageButton bt_print;
    //선택된 메뉴 postion
    int mSelectMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_base);

        mContext = BaseActivity.this;

        findViewById(R.id.bt_back).setOnClickListener(onClickListener);

        drawer = findViewById(R.id.drawer);
        drawer_layout = findViewById(R.id.drawer_layout);
        bt_drawer = findViewById(R.id.bt_drawer);
        bt_drawer.setOnClickListener(onClickListener);
        bt_print = findViewById(R.id.bt_print);
        bt_print.setOnClickListener(onClickListener);
        findViewById(R.id.bt_close).setOnClickListener(onClickListener);

        ArrayList<String> list = new ArrayList<>();
        list.add("배합관리");



        ListView listView = findViewById(R.id.list);
        mAdapter = new ListAdapter();
        mAdapter.setData(list);
        listView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        GlobalApplication application = (GlobalApplication)getApplicationContext();
        UserInfoModel.Items model = application.getUserInfoModel();

        TextView tv_name = findViewById(R.id.tv_name);
        //tv_name.setText(model.getDpt_name()+" "+model.getEmp_name());

        iv_title = findViewById(R.id.iv_title);
        iv_gnb = findViewById(R.id.iv_gnb);

        int menu = getIntent().getIntExtra("menu", 0);

        Bundle args = getIntent().getBundleExtra("args");

        Bundle args1 = getIntent().getBundleExtra("args1");

        mSelectMenu = menu-2;

        switch (menu){

                //배합관리
            case Define.MENU_MIX: {
                CommonFragment fragment = new MixFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_MIX, R.id.fl_content);
                break;
            }

            case Define.MENU_MIX_MANAGE: {
                CommonFragment fragment = new MixManageFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_MIX_MANAGE, R.id.fl_content);
                break;
            }

            case Define.MENU_MIX_EQU: {
                CommonFragment fragment = new MixEquFragment();
                CommonFragment fragment1 = new MixEquFragment();
                fragment.setArguments(args);
                fragment1.setArguments(args1);
                replaceContent(fragment, Define.TAG_MIX_EQU, R.id.fl_content);
                replaceContent(fragment1, Define.TAG_MIX_EQU, R.id.fl_content);
                break;
            }
            /*case Define.MENU_LOCATION: {
                CommonFragment fragment = new LocationFragment();
                replaceContent(fragment, Define.TAG_LOCATION, R.id.fl_content);
                break;
            }
            case Define.MENU_MATERIAL_OUT: {
                CommonFragment fragment = new MaterialOutFragment();
                replaceContent(fragment, Define.TAG_MATERIAL_OUT, R.id.fl_content);
                break;
            }
            case Define.MENU_PRODUCTION_IN: {
                CommonFragment fragment = new ProductionInFragment();
                replaceContent(fragment, Define.TAG_PRODUCTION_IN, R.id.fl_content);
                break;
            }
            case Define.MENU_PRODUCT_OUT: {
                CommonFragment fragment = new ProductOutFragment();
                replaceContent(fragment, Define.TAG_PRODUCT_OUT, R.id.fl_content);
                break;
            }
            case Define.MENU_PALLET: {
                CommonFragment fragment = new PalletFragment();
                replaceContent(fragment, Define.TAG_PALLET, R.id.fl_content);
                break;
            }
            case Define.MENU_CONFIG: {
                CommonFragment fragment = new ConfigFragment();
                replaceContent(fragment, Define.TAG_CONFIG, R.id.fl_content);
                break;
            }
            case Define.MENU_PRODUCT_PICKING: {
                CommonFragment fragment = new ProductPickingFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_PRODUCT_PICKING, R.id.fl_content);
                break;
            }
            case Define.MENU_MATERIAL_PICKING: {
                CommonFragment fragment = new MaterialPickingFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_MATERIAL_PICKING, R.id.fl_content);
                break;
            }
            case Define.MENU_INVENTORY: {
                CommonFragment fragment = new InventoryFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_INVENTORY, R.id.fl_content);
                break;
            }
            case Define.MENU_PALLET_PRINTER: {
                CommonFragment fragment = new PrinterFragment();
                fragment.setArguments(args);
                replaceContent(fragment, Define.TAG_PALLET_PRINTER, R.id.fl_content);
                break;
            }*/

        }

        setTitleImage(menu);
    }

    private void setTitleImage(int menu){
        //메뉴별 타이틀 이미지
        int image = 0;
        //자재불출과 제품출고에 있는 피킹 상단엔 좌측메뉴가 노출되지 않기 때문에 분기해야함
        int gnb = R.drawable.titilbar;
        int isDrawer = View.VISIBLE;
        int isLock = DrawerLayout.LOCK_MODE_UNLOCKED;

        //프린터 화면에서만 노출하면 되기 때문에 gone 처리 후 프린터 화면 진입 시 visible
        bt_print.setVisibility(View.GONE);

        switch (menu){
            case Define.MENU_MIX: {
                image = R.drawable.menu_mix_title;
                break;
            }

            case Define.MENU_MIX_MANAGE: {
                image = R.drawable.menu_mix_title2;
                break;
            }

            case Define.MENU_MIX_EQU: {
                image = R.drawable.menu_mix_title3;
                break;
            }

            /*case Define.MENU_LOCATION: {
                image = R.drawable.menu_moveloc_title;
                break;
            }
            case Define.MENU_MATERIAL_OUT: {
                image = R.drawable.menu_release_title;
                break;
            }
            case Define.MENU_PRODUCTION_IN: {
                image = R.drawable.menu_inproduct_title;
                break;
            }
            case Define.MENU_PRODUCT_OUT: {
                image = R.drawable.menu_outproduct_title;
                break;
            }
            case Define.MENU_PALLET: {
                image = R.drawable.pallet_title;
                break;
            }
            case Define.MENU_CONFIG: {
                image = R.drawable.menu_setting_title;
                break;
            }
            case Define.MENU_INVENTORY: {
                image = R.drawable.menu_inventory_title;
                break;
            }
            case Define.MENU_PRODUCT_PICKING: {
                image = R.drawable.prod_picking_title;
                gnb = R.drawable.titlebar_submenu;
                isDrawer = View.GONE;
                isLock = DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
                break;
            }
            case Define.MENU_MATERIAL_PICKING: {
                image = R.drawable.menu_release_title;
                gnb = R.drawable.titlebar_submenu;
                isDrawer = View.GONE;
                isLock = DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
                break;
            }
            case Define.MENU_PALLET_PRINTER: {
                image = R.drawable.print_title;
                gnb = R.drawable.titlebar_submenu;
                isDrawer = View.GONE;
                isLock = DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
                bt_print.setVisibility(View.VISIBLE);
                break;
            }
            case Define.MENU_INVENTORY_PICKING: {
                image = R.drawable.menu_inhouse_title;
                gnb = R.drawable.titlebar_submenu;
                isDrawer = View.GONE;
                isLock = DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
                break;
            }*/
        }
        iv_title.setBackgroundResource(image);
        iv_gnb.setBackgroundResource(gnb);
        bt_drawer.setVisibility(isDrawer);
        drawer.setDrawerLockMode(isLock);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int view = v.getId();
            switch (view) {
                case R.id.bt_back:
                    backPressed();
                    break;
                case R.id.bt_drawer:
                    drawer.openDrawer(drawer_layout);
                    break;
                case R.id.bt_close:
                    drawer.closeDrawers();
                    break;
                case R.id.bt_print:
                    BusProvider.getInstance().post(0);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        backPressed();
    }

    private void backPressed(){
        try {
            //사이드 메뉴가 열려있으면 닫아준다.
            if (drawer.isDrawerOpen(drawer_layout)) {
                drawer.closeDrawers();
                return;
            }

            FragmentManager mFragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();
            int count = mFragmentManager.getBackStackEntryCount();

            if (count >= 1) {
                mFragmentManager.popBackStack();
            } else {
                finish();
            }
        }catch (Exception e){
            finish();
        }
    }

    class ListAdapter extends BaseAdapter {
        LayoutInflater mInflater;
        ArrayList<String> mList;

        public void setData(ArrayList<String> list){
            mList = list;
        }

        public ListAdapter() {
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            if (mList == null) {
                return 0;
            }

            return mList.size();
        }

        @Override
        public String getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                holder = new ViewHolder();
                v = mInflater.inflate(R.layout.cell_drawer, null);
                v.setTag(holder);

                holder.tv_menu = v.findViewById(R.id.tv_menu);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            if(mSelectMenu == position){
                holder.tv_menu.setSelected(true);
            }else{
                holder.tv_menu.setSelected(false);
            }

            final String menu = mList.get(position);
            holder.tv_menu.setText((position+1)+". "+menu);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mSelectMenu == position){
                        drawer.closeDrawer(drawer_layout);
                        return;
                    }
                    mTwoBtnPopup = new TwoBtnPopup(BaseActivity.this, menu+" 메뉴로 이동하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                //팝업 닫기
                                mTwoBtnPopup.hideDialog();
                                //좌측메뉴 닫기
                                drawer.closeDrawer(drawer_layout);
                                //선택한 메뉴 기억
                                mSelectMenu = position;
                                //0이 없고 입고등록이 삭제되어 +2 해줘야함
                                switch (position+2){
                                    case Define.MENU_MIX: {
                                        CommonFragment fragment = new MixFragment();
                                        replaceContent(fragment, Define.TAG_MIX, R.id.fl_content);
                                        break;
                                    }
                                    /*case Define.MENU_MATERIAL_OUT: {
                                        CommonFragment fragment = new MaterialOutFragment();
                                        replaceContent(fragment, Define.TAG_MATERIAL_OUT, R.id.fl_content);
                                        break;
                                    }
                                    case Define.MENU_PRODUCTION_IN: {
                                        CommonFragment fragment = new ProductionInFragment();
                                        replaceContent(fragment, Define.TAG_MATERIAL_OUT, R.id.fl_content);
                                        break;
                                    }
                                    case Define.MENU_PRODUCT_OUT: {
                                        CommonFragment fragment = new ProductOutFragment();
                                        replaceContent(fragment, Define.TAG_PRODUCTION_IN, R.id.fl_content);
                                        break;
                                    }
                                    case Define.MENU_PALLET: {
                                        CommonFragment fragment = new PalletFragment();
                                        replaceContent(fragment, Define.TAG_PALLET, R.id.fl_content);
                                        break;
                                    }
                                    case Define.MENU_CONFIG: {
                                        CommonFragment fragment = new ConfigFragment();
                                        replaceContent(fragment, Define.TAG_CONFIG, R.id.fl_content);
                                        break;
                                    }

                                    case Define.MENU_INVENTORY: {
                                        CommonFragment fragment = new InventoryFragment();
                                        replaceContent(fragment, Define.TAG_INVENTORY, R.id.fl_content);
                                        break;
                                    }*/
                                }
                                setTitleImage(position+2);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            });

            return v;
        }

        class ViewHolder {
            TextView tv_menu;
        }
    }
}
