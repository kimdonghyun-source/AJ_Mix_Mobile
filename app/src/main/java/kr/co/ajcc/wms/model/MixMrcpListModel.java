package kr.co.ajcc.wms.model;

import java.util.List;

public class MixMrcpListModel extends ResultModel{
    List<Items> items;

    public List<Items> getItems(){
        return items;
    }

    public void setItems(List<Items> items){
        this.items = items;
    }

    public class Items extends ResultModel{
        //제조지시번호
        String mrcp_slip_code;
        //지시일자
        String mrcp_date;
        //설비코드
        String equ_code;
        //설비명
        String equ_name;
        //출고수량
        int mrcp_qty;


        public String getMrcp_slip_code() {
            return mrcp_slip_code;
        }

        public void setMrcp_slip_code(String mrcp_slip_code) {
            this.mrcp_slip_code = mrcp_slip_code;
        }

        public String getMrcp_date() {
            return mrcp_date;
        }

        public void setMrcp_date(String mrcp_date) {
            this.mrcp_date = mrcp_date;
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

        public int getMrcp_qty() {
            return mrcp_qty;
        }

        public void setMrcp_qty(int mrcp_qty) {
            this.mrcp_qty = mrcp_qty;
        }
    }

}
