package kr.co.ajcc.wms.model;

import java.util.List;

public class MixEquModel extends ResultModel {
    List<Items> items;

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public class Items extends ResultModel{
        //설비코드
        String equ_code;
        //설비명
        String equ_name;

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
    }
}
