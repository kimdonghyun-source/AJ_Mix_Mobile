package kr.co.ajcc.wms.custom;

import com.squareup.otto.Bus;

public class BusProvider extends Bus {
    private static Bus sBus = new Bus();

    public static Bus getInstance() {
        if (sBus == null)
            sBus = new Bus();
        return sBus;
    }
}
