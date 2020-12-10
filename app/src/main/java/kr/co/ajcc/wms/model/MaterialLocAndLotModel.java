package kr.co.ajcc.wms.model;

import java.util.List;

public class MaterialLocAndLotModel extends ResultModel {
    List<Items> items;

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public class Items extends ResultModel {
        //입력된 수량(서버에서 받는게 아님)
        int input_qty;

        //품목코드
        String itm_code;
        //품목명
        String itm_name;
        //규격
        String itm_size;
        //단위
        String itm_unit;
        //시스템로트번호
        String lot_no;
        //로케이션코드
        String location_code;
        //로케이션명
        String location_name;
        //불출처코드
        String wh_code_out;
        //불출처명
        String wh_name_out;
        //재고수량
        int inv_qty;
        String serial_no;

        public int getInput_qty() {
            return input_qty;
        }

        public void setInput_qty(int input_qty) {
            this.input_qty = input_qty;
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

        public String getItm_unit() {
            return itm_unit;
        }

        public void setItm_unit(String itm_unit) {
            this.itm_unit = itm_unit;
        }

        public String getLot_no() {
            return lot_no;
        }

        public void setLot_no(String lot_no) {
            this.lot_no = lot_no;
        }

        public String getLocation_code() {
            return location_code;
        }

        public void setLocation_code(String location_code) {
            this.location_code = location_code;
        }

        public String getLocation_name() {
            return location_name;
        }

        public void setLocation_name(String location_name) {
            this.location_name = location_name;
        }

        public String getWh_code_out() {
            return wh_code_out;
        }

        public void setWh_code_out(String wh_code_out) {
            this.wh_code_out = wh_code_out;
        }

        public String getWh_name_out() {
            return wh_name_out;
        }

        public void setWh_name_out(String wh_name_out) {
            this.wh_name_out = wh_name_out;
        }

        public int getInv_qty() {
            return inv_qty;
        }

        public void setInv_qty(int inv_qty) {
            this.inv_qty = inv_qty;
        }

        public String getSerial_no() { return serial_no; }

        public void setSerial_no(String serial_no) { this.serial_no = serial_no; }
    }
}
