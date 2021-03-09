package kr.co.ajcc.wms.model;

import java.util.List;

public class MixDetailList extends ResultModel {
    List<Items> items;

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public class Items extends ResultModel{
        //사업장코드
        String corp_code;
        //지시일자
        String mrcp_date;
        //지시순번1
        int mrcp_no1;
        //지시순번2
        int mrcp_no2;
        //품목코드
        String itm_code;
        //품목명
        String itm_name;
        //규격
        String itm_size;
        //설비코드
        String equ_code;
        //설비명
        String equ_name;
        //제조량
        float mrcp_qty;
        //제조로트번호
        String mrcp_lotno;
        //스캔수량
        int scan_cnt;
        //총수량
        int all_cnt;
        //전표번호(내가 보낸값 리턴값)
        String mrcp_slip_code;

        public String getCorp_code() {
            return corp_code;
        }

        public void setCorp_code(String corp_code) {
            this.corp_code = corp_code;
        }

        public String getMrcp_date() {
            return mrcp_date;
        }

        public void setMrcp_date(String mrcp_date) {
            this.mrcp_date = mrcp_date;
        }

        public int getMrcp_no1() {
            return mrcp_no1;
        }

        public void setMrcp_no1(int mrcp_no1) {
            this.mrcp_no1 = mrcp_no1;
        }

        public int getMrcp_no2() {
            return mrcp_no2;
        }

        public void setMrcp_no2(int mrcp_no2) {
            this.mrcp_no2 = mrcp_no2;
        }

        public String getItm_code() {
            return itm_code;
        }

        public void setItm_code(String itm_code) {
            this.itm_code = itm_code;
        }

        public String getItm_name() {
            return itm_name;
        }

        public void setItm_name(String itm_name) {
            this.itm_name = itm_name;
        }

        public String getItm_size() {
            return itm_size;
        }

        public void setItm_size(String itm_size) {
            this.itm_size = itm_size;
        }

        public String getEqu_code() {
            return equ_code;
        }

        public void setEqu_code(String equ_code) {
            this.equ_code = equ_code;
        }

        public String getEqu_name() {
            return equ_name;
        }

        public void setEqu_name(String equ_name) {
            this.equ_name = equ_name;
        }

        public float getMrcp_qty() {
            return mrcp_qty;
        }

        public void setMrcp_qty(float mrcp_qty) {
            this.mrcp_qty = mrcp_qty;
        }

        public String getMrcp_lotno() {
            return mrcp_lotno;
        }

        public void setMrcp_lotno(String mrcp_lotno) {
            this.mrcp_lotno = mrcp_lotno;
        }

        public int getScan_cnt() {
            return scan_cnt;
        }

        public void setScan_cnt(int scan_cnt) {
            this.scan_cnt = scan_cnt;
        }

        public int getAll_cnt() {
            return all_cnt;
        }

        public void setAll_cnt(int all_cnt) {
            this.all_cnt = all_cnt;
        }

        public String getMrcp_slip_code() {
            return mrcp_slip_code;
        }

        public void setMrcp_slip_code(String mrcp_slip_code) {
            this.mrcp_slip_code = mrcp_slip_code;
        }
    }
}
