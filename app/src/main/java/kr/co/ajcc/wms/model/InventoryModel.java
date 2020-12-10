package kr.co.ajcc.wms.model;


import java.util.List;

public class InventoryModel extends ResultModel {
    List<Items> items;

    public List<Items> getItems(){
        return items;
    }

    public void setItems(List<Items>items){
        this.items = items;
    }

    public class Items extends ResultModel {
        //품목코드
        String itm_code;
        //품목명
        String itm_name;
        //규격
        String itm_size;
        //단위
        String itm_unit;
        //파레트수량
        int itm_pallet_qty;
        //창고코드
        String wh_code;
        //창고명
        String wh_name;
        //로케이션코드
        String location_code;
        //로케이션명
        String location_name;
        //재고수량
        int inv_qty;
        //시리얼번호
        String serial_no;

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

        public int getItm_pallet_qty() {
            return itm_pallet_qty;
        }

        public void setItm_pallet_qty(int itm_pallet_qty) {
            this.itm_pallet_qty = itm_pallet_qty;
        }

        public String getWh_code() {
            return wh_code;
        }

        public void setWh_code(String wh_code) {
            this.wh_code = wh_code;
        }

        public String getWh_name() {
            return wh_name;
        }

        public void setWh_name(String wh_name) {
            this.wh_name = wh_name;
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

        public int getInv_qty() {
            return inv_qty;
        }

        public void setInv_qty(int inv_qty) {
            this.inv_qty = inv_qty;
        }

        public String getSerial_no() {
            return serial_no;
        }

        public void setSerial_no(String serial_no) {
            this.serial_no = serial_no;
        }
    }
}

