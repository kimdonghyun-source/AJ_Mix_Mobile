package kr.co.ajcc.wms.model;

import java.util.List;

public class UserInfoModel extends ResultModel{
    Items items;

    public class Items extends ResultModel{
        //부서코드
        String dpt_code;
        //부서명
        String dpt_name;
        //사원번호
        String emp_code;
        //사원명
        String emp_name;
        //앱버전
        String app_ver;

        public String getDpt_code() {
            return dpt_code;
        }

        public void setDpt_code(String dpt_code) {
            this.dpt_code = dpt_code;
        }

        public String getDpt_name() {
            return dpt_name;
        }

        public void setDpt_name(String dpt_name) {
            this.dpt_name = dpt_name;
        }

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

        public String getApp_ver() {
            return app_ver;
        }

        public void setApp_ver(String app_ver) {
            this.app_ver = app_ver;
        }
    }
}
