package nodomain.freeyourgadget.gadgetbridge.util;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class ChartUtils {
    private ChartUtils() {
        // utility class
    }

    /**
     * Find the gaps in a list of values with potentially varying sample rates, and create separate
     * datasets for each.
     */
    public static List<ILineDataSet> findGaps(final List<Entry> values,
                                              final Function<List<Entry>, ILineDataSet> datasetCreator) {
        if (values.isEmpty()) {
            return Collections.emptyList();
        } else if (values.size() == 1) {
            return Collections.singletonList(datasetCreator.apply(values));
        }

        final List<ILineDataSet> ret = new ArrayList<>();
        int lastStart = 0;
        float lastGap = -1;
        for (int i = 0; i < values.size() - 1; i++) {
            final float gapToNext = values.get(i + 1).getX() - values.get(i).getX();

            if (lastGap >= 0) {
                if (gapToNext >= lastGap * 5 && i > lastStart) {
                    // sample rate decreased - insert a gap
                    ret.add(datasetCreator.apply(values.subList(lastStart, i + 1)));
                    lastGap = -1;
                    lastStart = i + 1;
                    continue;
                } else if (gapToNext < lastGap / 5 && i - 1 > lastStart) {
                    // sample rate increased drastically (workout start?) - insert a gap
                    ret.add(datasetCreator.apply(values.subList(lastStart, i)));
                    lastStart = i;
                }
            }

            lastGap = gapToNext;
        }

        if (lastStart < values.size()) {
            ret.add(datasetCreator.apply(values.subList(lastStart, values.size())));
        }

        return ret;
    }
}
