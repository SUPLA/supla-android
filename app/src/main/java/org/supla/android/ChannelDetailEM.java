package org.supla.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.lib.SuplaChannelElectricityMeter;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;

public class ChannelDetailEM extends DetailLayout implements View.OnClickListener {

    private Integer phase;

    private TextView tvTotalForwardActiveEnergy;
    private TextView tvTotalReverseActiveEnergy;
    private TextView tvTotalForwardReactiveEnergy;
    private TextView tvTotalReverseReactiveEnergy;

    private TextView tvFreq;
    private TextView tvVoltage;
    private TextView tvCurrent;
    private TextView tvPowerActive;
    private TextView tvPowerReactive;
    private TextView tvPowerApparent;
    private TextView tvPowerFactor;
    private TextView tvPhaseAngle;
    private TextView tvPhaseForwardActiveEnergy;
    private TextView tvPhaseReverseActiveEnergy;
    private TextView tvPhaseForwardReactiveEnergy;
    private TextView tvPhaseReverseReactiveEnergy;

    private TextView tvChannelTitle;

    private Button btnPhase1;
    private Button btnPhase2;
    private Button btnPhase3;

    public ChannelDetailEM(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailEM(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailEM(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailEM(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init() {
        super.init();

        phase = new Integer(1);

        tvTotalForwardActiveEnergy = (TextView)findViewById(R.id.emtv_TotalForwardActiveEnergy);
        tvTotalReverseActiveEnergy = (TextView)findViewById(R.id.emtv_TotalReverseActiveEnergy);
        tvTotalForwardReactiveEnergy = (TextView)findViewById(R.id.emtv_TotalForwardRectiveEnergy);
        tvTotalReverseReactiveEnergy = (TextView)findViewById(R.id.emtv_TotalReverseRectiveEnergy);

        tvFreq = (TextView)findViewById(R.id.emtv_Freq);
        tvVoltage = (TextView)findViewById(R.id.emtv_Voltage);
        tvCurrent = (TextView)findViewById(R.id.emtv_Current);
        tvPowerActive = (TextView)findViewById(R.id.emtv_PowerActive);
        tvPowerReactive = (TextView)findViewById(R.id.emtv_PowerReactive);
        tvPowerApparent = (TextView)findViewById(R.id.emtv_PowerApparent);
        tvPowerFactor = (TextView)findViewById(R.id.emtv_PowerFactor);
        tvPhaseAngle = (TextView)findViewById(R.id.emtv_PhaseAngle);
        tvPhaseForwardActiveEnergy = (TextView)findViewById(R.id.emtv_PhaseForwardActiveEnergy);
        tvPhaseReverseActiveEnergy = (TextView)findViewById(R.id.emtv_PhaseReverseActiveEnergy);
        tvPhaseForwardReactiveEnergy = (TextView)findViewById(R.id.emtv_PhaseForwardRectiveEnergy);
        tvPhaseReverseReactiveEnergy = (TextView)findViewById(R.id.emtv_PhaseReverseRectiveEnergy);

        tvChannelTitle = (TextView)findViewById(R.id.emtv_ChannelTitle);

        btnPhase1 = (Button) findViewById(R.id.embtn_Phase1);
        btnPhase2 = (Button) findViewById(R.id.embtn_Phase2);
        btnPhase3 = (Button) findViewById(R.id.embtn_Phase3);

        btnPhase1.setOnClickListener(this);
        btnPhase2.setOnClickListener(this);
        btnPhase3.setOnClickListener(this);

        btnPhase1.setTag(new Integer(1));
        btnPhase2.setTag(new Integer(2));
        btnPhase3.setTag(new Integer(3));

    }

    public void channelExtendedDataToViews() {

        Channel channel = (Channel) getChannelFromDatabase();
        tvChannelTitle.setText(channel.getNotEmptyCaption(getContext()));

        ChannelExtendedValue cev = DBH.getChannelExtendedValue(getRemoteId());

        setBtnBackground(btnPhase1, R.drawable.em_phase_btn_black);
        setBtnBackground(btnPhase2, R.drawable.em_phase_btn_black);
        setBtnBackground(btnPhase3, R.drawable.em_phase_btn_black);

        String empty = "----";
        tvTotalForwardActiveEnergy.setText(empty);
        tvTotalReverseActiveEnergy.setText(empty);
        tvTotalForwardReactiveEnergy.setText(empty);
        tvTotalReverseReactiveEnergy.setText(empty);

        tvFreq.setText(empty);
        tvVoltage.setText(empty);
        tvCurrent.setText(empty);
        tvPowerActive.setText(empty);
        tvPowerReactive.setText(empty);
        tvPowerApparent.setText(empty);
        tvPowerFactor.setText(empty);
        tvPhaseAngle.setText(empty);
        tvPhaseForwardActiveEnergy.setText(empty);
        tvPhaseReverseActiveEnergy.setText(empty);
        tvPhaseForwardReactiveEnergy.setText(empty);
        tvPhaseReverseReactiveEnergy.setText(empty);

        if (cev != null
                && cev.getExtendedValue() != null
                && cev.getExtendedValue().ElectricityMeterValue != null) {

            SuplaChannelElectricityMeter em = cev.getExtendedValue().ElectricityMeterValue;

            SuplaChannelElectricityMeter.Summary sum = em.getSummary();

            tvTotalForwardActiveEnergy.setText(String.format("%.5f kWh", sum.getTotalForwardActiveEnergy()));
            tvTotalReverseActiveEnergy.setText(String.format("%.5f kWh", sum.getTotalReverseActiveEnergy()));
            tvTotalForwardReactiveEnergy.setText(String.format("%.5f kvar", sum.getTotalForwardReactiveEnergy()));
            tvTotalReverseReactiveEnergy.setText(String.format("%.5f kvar", sum.getTotalReverseReactiveEnergy()));

            SuplaChannelElectricityMeter.Measurement m = em.getMeasurement(phase.intValue(), 0);
            if (m!= null) {

                Button btn = null;
                switch(phase.intValue()) {
                    case 1: btn = btnPhase1; break;
                    case 2: btn = btnPhase2; break;
                    case 3: btn = btnPhase3; break;
                }

                setBtnBackground(btn, m.getVoltage() > 0 ? R.drawable.em_phase_btn_green : R.drawable.em_phase_btn_red);

                tvFreq.setText(String.format("%.2f Hz", m.getFreq()));
                tvVoltage.setText(String.format("%.2f V", m.getVoltage()));
                tvCurrent.setText(String.format("%.3f A", m.getCurrent()));
                tvPowerActive.setText(String.format("%.5f W", m.getPowerActive()));
                tvPowerReactive.setText(String.format("%.5f var", m.getPowerReactive()));
                tvPowerApparent.setText(String.format("%.5f VA", m.getPowerApparent()));
                tvPowerFactor.setText(String.format("%.3f", m.getPowerFactor()));
                tvPhaseAngle.setText(String.format("%.5f", m.getPhaseAngle()));

                sum = em.getSummary(phase.intValue());
                tvPhaseForwardActiveEnergy.setText(String.format("%.5f kWh", sum.getTotalForwardActiveEnergy()));
                tvPhaseReverseActiveEnergy.setText(String.format("%.5f kWh", sum.getTotalReverseActiveEnergy()));
                tvPhaseForwardReactiveEnergy.setText(String.format("%.5f kvar", sum.getTotalForwardReactiveEnergy()));
                tvPhaseReverseReactiveEnergy.setText(String.format("%.5f kvar", sum.getTotalReverseReactiveEnergy()));
            }
        }
    }

    public void setData(ChannelBase channel) {

        super.setData(channel);
        channelExtendedDataToViews();
    }

    @Override
    public View getContentView() {
        return inflateLayout(R.layout.detail_em);
    }

    @Override
    public void OnChannelDataChanged() {
        channelExtendedDataToViews();
    }

    private void setBtnBackground(Button btn, int i) {

        Drawable d = getResources().getDrawable(i);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            btn.setBackgroundDrawable(d);
        } else {
            btn.setBackground(d);
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof Button && v.getTag() instanceof Integer) {
            phase = (Integer)v.getTag();
            channelExtendedDataToViews();
        }
    }
}

