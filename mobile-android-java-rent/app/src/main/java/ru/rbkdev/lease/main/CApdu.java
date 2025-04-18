package ru.rbkdev.lease.main;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.widget.Toast;

import ru.rbkdev.lease.key.CSliderFarmRecyclerAdapter;

public class CApdu extends HostApduService {

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Toast.makeText(getApplicationContext(), "processCommandApdu", Toast.LENGTH_SHORT).show();
        return CSliderFarmRecyclerAdapter.g_tag;
    }

    @Override
    public void onDeactivated(int reason) {
        Toast.makeText(getApplicationContext(), "onDeactivated", Toast.LENGTH_SHORT).show();
        System.out.println(reason);
    }
}
