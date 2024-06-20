package org.supla.android.charts;

/*
Copyright (C) AC SOFTWARE SP. Z O.O.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.supla.android.R;
import org.supla.android.data.source.remote.channel.SuplaChannelFlag;
import org.supla.android.db.Channel;
import org.supla.android.db.DbHelper;
import org.supla.android.db.MeasurementsDbHelper;
import org.supla.android.db.SuplaContract;

public class ElectricityChartHelper extends IncrementalMeterChartHelper {

  private double totalActiveEnergy[];
  private boolean mProductionDataSource;
  private String colPhase1;
  private String colPhase2;
  private String colPhase3;
  private boolean singlePhase;

  public ElectricityChartHelper(Context context) {
    super(context);
    totalActiveEnergy = new double[] {0, 0, 0};
    singlePhase = false;
    setProductionDataSource(false);
  }

  @Override
  public void loadCombinedChart(int channelId) {
    DbHelper dbh = DbHelper.getInstance(context);
    Channel ch = dbh.getChannel(channelId);
    if (ch != null) {
      long flags = ch.getFlags();
      singlePhase =
          SuplaChannelFlag.PHASE2_UNSUPPORTED.inside(flags)
              && SuplaChannelFlag.PHASE3_UNSUPPORTED.inside(flags);
    } else {
      singlePhase = false;
    }

    super.loadCombinedChart(channelId);
  }

  @Override
  protected Cursor getCursor(MeasurementsDbHelper DBH, int channelId, String dateFormat) {
    return DBH.getElectricityMeasurements(channelId, dateFormat, dateFrom, dateTo);
  }

  @Override
  @SuppressLint("Range")
  protected void addBarEntries(int n, float time, Cursor c, ArrayList<BarEntry> entries) {
    if (isBalanceChartType(ctype)) {

      float cons = 0;
      float prod = 0;

      if (isVectorBalanceChartType(ctype)) {
        prod =
            (float)
                c.getDouble(
                    c.getColumnIndex(
                        SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_RAE_BALANCED));
        cons =
            (float)
                c.getDouble(
                    c.getColumnIndex(
                        SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_FAE_BALANCED));
      } else {
        double prod1 =
            c.getDouble(
                c.getColumnIndex(
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_RAE));
        double prod2 =
            c.getDouble(
                c.getColumnIndex(
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_RAE));
        double prod3 =
            c.getDouble(
                c.getColumnIndex(
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_RAE));

        double cons1 =
            c.getDouble(
                c.getColumnIndex(
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE));
        double cons2 =
            c.getDouble(
                c.getColumnIndex(
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE));
        double cons3 =
            c.getDouble(
                c.getColumnIndex(
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE));

        cons = (float) (cons1 + cons2 + cons3);
        prod = (float) (prod1 + prod2 + prod3);
      }

      float cons_diff = prod > cons ? cons : prod;
      float prod_diff = cons > prod ? prod : cons;

      float[] values = new float[4];
      values[0] = prod_diff * -1;
      values[1] = (prod - prod_diff) * -1;
      values[2] = cons - cons_diff;
      values[3] = cons_diff;

      entries.add(new BarEntry(n, values));

    } else {
      float[] phases = new float[3];

      phases[0] = (float) c.getDouble(c.getColumnIndex(colPhase1));
      phases[1] = (float) c.getDouble(c.getColumnIndex(colPhase2));
      phases[2] = (float) c.getDouble(c.getColumnIndex(colPhase3));
      entries.add(new BarEntry(n, phases));
    }
  }

  @Override
  protected void addLineEntries(int n, Cursor c, float time, ArrayList<Entry> entries) {}

  @Override
  @SuppressLint("Range")
  protected void addPieEntries(SimpleDateFormat spf, Cursor c, ArrayList<PieEntry> entries) {
    if (ctype.equals(ChartType.Pie_PhaseRank)) {
      Resources res = context.getResources();

      entries.add(
          new PieEntry((float) totalActiveEnergy[0], res.getText(R.string.em_phase1).toString()));
      entries.add(
          new PieEntry((float) totalActiveEnergy[1], res.getText(R.string.em_phase2).toString()));
      entries.add(
          new PieEntry((float) totalActiveEnergy[2], res.getText(R.string.em_phase3).toString()));

    } else {

      float phases;
      phases = (float) c.getDouble(c.getColumnIndex(colPhase1));
      phases += (float) c.getDouble(c.getColumnIndex(colPhase2));
      phases += (float) c.getDouble(c.getColumnIndex(colPhase3));

      long timestamp =
          c.getLong(
              c.getColumnIndex(SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP));

      entries.add(new PieEntry(phases, spf.format(new java.util.Date(timestamp * 1000))));
    }
  }

  @Override
  @SuppressLint("Range")
  protected long getTimestamp(Cursor c) {
    return c.getLong(
        c.getColumnIndex(SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP));
  }

  @Override
  protected SuplaBarDataSet newBarDataSetInstance(ArrayList<BarEntry> barEntries, String label) {
    SuplaBarDataSet result = super.newBarDataSetInstance(barEntries, label);

    Resources res = context.getResources();

    if (isBalanceChartType(ctype)) {
      result.setStackLabels(new String[] {"", "", ""});

      List<Integer> Colors = new ArrayList<Integer>(4);
      Colors.add(Color.GRAY);
      Colors.add(res.getColor(R.color.chart_color_value_negative));
      Colors.add(res.getColor(R.color.chart_color_value_positive));
      Colors.add(Color.GRAY);
      result.setColors(Colors);
    } else {
      result.setStackLabels(
          new String[] {
            res.getString(R.string.em_phase1),
            res.getString(R.string.em_phase2),
            res.getString(R.string.em_phase3)
          });

      List<Integer> Colors = new ArrayList<Integer>(3);
      Colors.add(res.getColor(R.color.phase1));
      Colors.add(res.getColor(R.color.phase2));
      Colors.add(res.getColor(R.color.phase3));
      result.setColors(Colors);
    }

    return result;
  }

  @Override
  protected void prepareBarDataSet(SuplaBarDataSet barDataSet) {
    if ((isProductionDataSource() && isComparisonChartType(ctype))) {
      barDataSet.setColorDependsOnTheValue(true);
      barDataSet.setColors(getBarChartComparsionColors(true));
    } else {
      super.prepareBarDataSet(barDataSet);
    }
  }

  public void setTotalActiveEnergy(double[] totalActiveEnergy) {
    if (totalActiveEnergy != null && totalActiveEnergy.length == 3) {
      this.totalActiveEnergy = totalActiveEnergy;
    }
  }

  @Override
  public String[] getMasterSpinnerItems(int limit) {
    String[] result = super.getMasterSpinnerItems(limit);
    if (mProductionDataSource && result != null && result.length >= 14) {
      Resources r = context.getResources();
      result[13] = r.getString(R.string.production_acording_to_phases);
    }
    return result;
  }

  public boolean isProductionDataSource() {
    return mProductionDataSource;
  }

  public void setProductionDataSource(boolean mProduction) {
    this.mProductionDataSource = mProduction;
    if (mProduction) {
      colPhase1 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_RAE;
      colPhase2 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_RAE;
      colPhase3 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_RAE;
    } else {
      colPhase1 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE;
      colPhase2 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE;
      colPhase3 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE;
    }
  }

  @Override
  public String getCurrency() {
    return mProductionDataSource ? null : currency;
  }

  @Override
  protected IMarker getMarker() {
    if (isBalanceChartType(ctype)
        || isComparisonChartType(ctype)
        || isPieChartType(ctype)
        || singlePhase) {
      return super.getMarker();
    } else {
      return new EMMarkerView(this, context);
    }
  }
}
