package kr.co.ajcc.wms.model;

import java.util.List;

public class EmpListModel extends ResultModel {
    List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item extends ResultModel{
        //사원코드
        String emp_code;
        //사원명
        String emp_name;
        //선택값
        String c_emp_code;

        public String getEmp_code() {
            return emp_code;
        }

        public void setEmp_code(String emp_code) {
            this.emp_code = emp_code;
        }

        public String getEmp_name() {
            return emp_name;
        }

        public void setEmp_name(String emp_name) {
            this.emp_name = emp_name;
        }

        public String getC_emp_code() {
            return c_emp_code;
        }

        public void setC_emp_code(String c_emp_code) {
            this.c_emp_code = c_emp_code;
        }
    }


}
